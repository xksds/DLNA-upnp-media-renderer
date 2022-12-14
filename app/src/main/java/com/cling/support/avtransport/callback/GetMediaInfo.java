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

package com.cling.support.avtransport.callback;

import com.cling.controlpoint.ActionCallback;
import com.cling.model.action.ActionInvocation;
import com.cling.model.meta.Service;
import com.cling.model.types.UnsignedIntegerFourBytes;
import com.cling.support.model.MediaInfo;

import java.util.logging.Logger;

/**
 * @author Christian Bauer
 */
public abstract class GetMediaInfo extends ActionCallback {

    private static Logger log = Logger.getLogger(GetMediaInfo.class.getName());

    public GetMediaInfo(Service service) {
        this(new UnsignedIntegerFourBytes(0), service);
    }

    public GetMediaInfo(UnsignedIntegerFourBytes instanceId, Service service) {
        super(new ActionInvocation(service.getAction("GetMediaInfo")));
        getActionInvocation().setInput("InstanceID", instanceId);
    }

    public void success(ActionInvocation invocation) {
        MediaInfo mediaInfo = new MediaInfo(invocation.getOutputMap());
        received(invocation, mediaInfo);
    }

    public abstract void received(ActionInvocation invocation, MediaInfo mediaInfo);

}