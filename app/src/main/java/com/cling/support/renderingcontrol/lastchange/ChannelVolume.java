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

package com.cling.support.renderingcontrol.lastchange;

import com.cling.support.model.Channel;

/**
 * @author Christian Bauer
 */
public class ChannelVolume {

    protected Channel channel;
    protected Integer volume;

    public ChannelVolume(Channel channel, Integer volume) {
        this.channel = channel;
        this.volume = volume;
    }

    public Channel getChannel() {
        return channel;
    }

    public Integer getVolume() {
        return volume;
    }

    @Override
    public String toString() {
        return "Volume: " + getVolume() + " (" + getChannel() + ")";
    }
}
