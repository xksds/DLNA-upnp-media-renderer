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

package com.cling.support.renderingcontrol.callback;

import com.cling.controlpoint.ActionCallback;
import com.cling.model.action.ActionInvocation;
import com.cling.model.meta.Service;
import com.cling.model.types.UnsignedIntegerFourBytes;
import com.cling.model.types.UnsignedIntegerTwoBytes;
import com.cling.support.model.Channel;

import java.util.logging.Logger;

/**
 * @author Christian Bauer
 */
public abstract class SetVolume extends ActionCallback {

    private static Logger log = Logger.getLogger(SetVolume.class.getName());

    public SetVolume(Service service, long newVolume) {
        this(new UnsignedIntegerFourBytes(0), service, newVolume);
    }

    public SetVolume(UnsignedIntegerFourBytes instanceId, Service service, long newVolume) {
        super(new ActionInvocation(service.getAction("SetVolume")));
        getActionInvocation().setInput("InstanceID", instanceId);
        getActionInvocation().setInput("Channel", Channel.Master.toString());
        getActionInvocation().setInput("DesiredVolume", new UnsignedIntegerTwoBytes(newVolume));
    }

    @Override
    public void success(ActionInvocation invocation) {
        log.fine("Executed successfully");

    }
}