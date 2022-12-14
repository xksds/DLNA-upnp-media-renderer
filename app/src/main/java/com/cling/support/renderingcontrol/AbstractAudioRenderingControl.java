/*
 * Copyright (C) 2013 4th Line GmbH, Switzerland
 *
 * The contents of this file are subject to the terms of either the GNU
 * Lesser General Public License Version 2 or later ("LGPL") or the
 * Common Development and Distribution License Version 1 or later
 * ("CDDL") (collectively, the "License"). You may not use this file
 * except in compliance with the License. See LICENSE.txt for more
 * information.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.cling.support.renderingcontrol;

import com.cling.binding.annotations.UpnpAction;
import com.cling.binding.annotations.UpnpInputArgument;
import com.cling.binding.annotations.UpnpOutputArgument;
import com.cling.binding.annotations.UpnpService;
import com.cling.binding.annotations.UpnpServiceId;
import com.cling.binding.annotations.UpnpServiceType;
import com.cling.binding.annotations.UpnpStateVariable;
import com.cling.binding.annotations.UpnpStateVariables;
import com.cling.model.types.ErrorCode;
import com.cling.model.types.UnsignedIntegerFourBytes;
import com.cling.model.types.UnsignedIntegerTwoBytes;
import com.cling.support.lastchange.LastChange;
import com.cling.support.lastchange.LastChangeDelegator;
import com.cling.support.model.Channel;
import com.cling.support.model.PresetName;
import com.cling.support.model.VolumeDBRange;
import com.cling.support.renderingcontrol.lastchange.ChannelLoudness;
import com.cling.support.renderingcontrol.lastchange.ChannelMute;
import com.cling.support.renderingcontrol.lastchange.ChannelVolume;
import com.cling.support.renderingcontrol.lastchange.ChannelVolumeDB;
import com.cling.support.renderingcontrol.lastchange.RenderingControlLastChangeParser;
import com.cling.support.renderingcontrol.lastchange.RenderingControlVariable;

import java.beans.PropertyChangeSupport;

/**
 *
 */
@UpnpService(
        serviceId = @UpnpServiceId("RenderingControl"),
        serviceType = @UpnpServiceType(value = "RenderingControl", version = 1),
        stringConvertibleTypes = LastChange.class
)
@UpnpStateVariables({
        @UpnpStateVariable(
                name = "PresetNameList",
                sendEvents = false,
                datatype = "string"),
        @UpnpStateVariable(
                name = "Mute",
                sendEvents = false,
                datatype = "boolean"),
        @UpnpStateVariable(
                name = "Volume",
                sendEvents = false,
                datatype = "ui2",
                allowedValueMinimum = 0,
                allowedValueMaximum = 100),
        @UpnpStateVariable(
                name = "VolumeDB",
                sendEvents = false,
                datatype = "i2",
                allowedValueMinimum = -36864,
                allowedValueMaximum = 32767),
        @UpnpStateVariable(
                name = "Loudness",
                sendEvents = false,
                datatype = "boolean"),
        @UpnpStateVariable(
                name = "A_ARG_TYPE_Channel",
                sendEvents = false,
                allowedValuesEnum = Channel.class),
        @UpnpStateVariable(
                name = "A_ARG_TYPE_PresetName",
                sendEvents = false,
                allowedValuesEnum = PresetName.class),
        @UpnpStateVariable(
                name = "A_ARG_TYPE_InstanceID",
                sendEvents = false,
                datatype = "ui4")

})
public abstract class AbstractAudioRenderingControl implements LastChangeDelegator {

    final protected PropertyChangeSupport propertyChangeSupport;
    @UpnpStateVariable(eventMaximumRateMilliseconds = 200)
    final private LastChange lastChange;

    protected AbstractAudioRenderingControl() {
        this.propertyChangeSupport = new PropertyChangeSupport(this);
        this.lastChange = new LastChange(new RenderingControlLastChangeParser());
    }

    protected AbstractAudioRenderingControl(LastChange lastChange) {
        this.propertyChangeSupport = new PropertyChangeSupport(this);
        this.lastChange = lastChange;
    }

    protected AbstractAudioRenderingControl(PropertyChangeSupport propertyChangeSupport) {
        this.propertyChangeSupport = propertyChangeSupport;
        this.lastChange = new LastChange(new RenderingControlLastChangeParser());
    }

    protected AbstractAudioRenderingControl(PropertyChangeSupport propertyChangeSupport, LastChange lastChange) {
        this.propertyChangeSupport = propertyChangeSupport;
        this.lastChange = lastChange;
    }

    public static UnsignedIntegerFourBytes getDefaultInstanceID() {
        return new UnsignedIntegerFourBytes(0);
    }

    @Override
    public LastChange getLastChange() {
        return lastChange;
    }

    @Override
    public void appendCurrentState(LastChange lc, UnsignedIntegerFourBytes instanceId) throws Exception {
        for (Channel channel : getCurrentChannels()) {
            String channelString = channel.name();
            lc.setEventedValue(
                    instanceId,
                    new RenderingControlVariable.Mute(new ChannelMute(channel, getMute(instanceId, channelString))),
                    new RenderingControlVariable.Loudness(new ChannelLoudness(channel, getLoudness(instanceId, channelString))),
                    new RenderingControlVariable.Volume(new ChannelVolume(channel, getVolume(instanceId, channelString).getValue().intValue())),
                    new RenderingControlVariable.VolumeDB(new ChannelVolumeDB(channel, getVolumeDB(instanceId, channelString))),
                    new RenderingControlVariable.PresetNameList(PresetName.FactoryDefaults.name())
            );
        }
    }

    public PropertyChangeSupport getPropertyChangeSupport() {
        return propertyChangeSupport;
    }

    @UpnpAction(out = @UpnpOutputArgument(name = "CurrentPresetNameList", stateVariable = "PresetNameList"))
    public String listPresets(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes instanceId) throws RenderingControlException {
        return PresetName.FactoryDefaults.toString();
    }

    @UpnpAction
    public void selectPreset(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes instanceId,
                             @UpnpInputArgument(name = "PresetName") String presetName) throws RenderingControlException {
    }

    @UpnpAction(out = @UpnpOutputArgument(name = "CurrentMute", stateVariable = "Mute"))
    public abstract boolean getMute(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes instanceId,
                                    @UpnpInputArgument(name = "Channel") String channelName) throws RenderingControlException;

    @UpnpAction
    public abstract void setMute(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes instanceId,
                                 @UpnpInputArgument(name = "Channel") String channelName,
                                 @UpnpInputArgument(name = "DesiredMute", stateVariable = "Mute") boolean desiredMute) throws RenderingControlException;

    @UpnpAction(out = @UpnpOutputArgument(name = "CurrentVolume", stateVariable = "Volume"))
    public abstract UnsignedIntegerTwoBytes getVolume(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes instanceId,
                                                      @UpnpInputArgument(name = "Channel") String channelName) throws RenderingControlException;

    @UpnpAction
    public abstract void setVolume(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes instanceId,
                                   @UpnpInputArgument(name = "Channel") String channelName,
                                   @UpnpInputArgument(name = "DesiredVolume", stateVariable = "Volume") UnsignedIntegerTwoBytes desiredVolume) throws RenderingControlException;

    @UpnpAction(out = @UpnpOutputArgument(name = "CurrentVolume", stateVariable = "VolumeDB"))
    public Integer getVolumeDB(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes instanceId,
                               @UpnpInputArgument(name = "Channel") String channelName) throws RenderingControlException {
        return 0;
    }

    @UpnpAction
    public void setVolumeDB(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes instanceId,
                            @UpnpInputArgument(name = "Channel") String channelName,
                            @UpnpInputArgument(name = "DesiredVolume", stateVariable = "VolumeDB") Integer desiredVolumeDB) throws RenderingControlException {
        /*
        VolumeDB volumeDB = new VolumeDB();
        volumeDB.setChannel(channelName);
        volumeDB.setVal(new BigInteger(desiredVolumeDB.toString()));
        */
    }

    @UpnpAction(out = {
            @UpnpOutputArgument(name = "MinValue", stateVariable = "VolumeDB", getterName = "getMinValue"),
            @UpnpOutputArgument(name = "MaxValue", stateVariable = "VolumeDB", getterName = "getMaxValue")
    })
    public VolumeDBRange getVolumeDBRange(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes instanceId,
                                          @UpnpInputArgument(name = "Channel") String channelName) throws RenderingControlException {
        return new VolumeDBRange(0, 0);
    }

    @UpnpAction(out = @UpnpOutputArgument(name = "CurrentLoudness", stateVariable = "Loudness"))
    public boolean getLoudness(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes instanceId,
                               @UpnpInputArgument(name = "Channel") String channelName) throws RenderingControlException {
        return false;
    }

    @UpnpAction
    public void setLoudness(@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes instanceId,
                            @UpnpInputArgument(name = "Channel") String channelName,
                            @UpnpInputArgument(name = "DesiredLoudness", stateVariable = "Loudness") boolean desiredLoudness) throws RenderingControlException {
/*
        Loudness loudness = new Loudness();
        loudness.setChannel(channelName);
        loudness.setVal(desiredLoudness);
*/
    }

    protected abstract Channel[] getCurrentChannels();

    protected Channel getChannel(String channelName) throws RenderingControlException {
        try {
            return Channel.valueOf(channelName);
        } catch (IllegalArgumentException ex) {
            throw new RenderingControlException(ErrorCode.ARGUMENT_VALUE_INVALID, "Unsupported audio channel: " + channelName);
        }
    }

}
