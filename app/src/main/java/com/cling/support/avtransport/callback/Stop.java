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

import java.util.logging.Logger;

/**
 * @author Christian Bauer
 */
public abstract class Stop extends ActionCallback {

    private static Logger log = Logger.getLogger(Stop.class.getName());

    public Stop(Service service) {
        this(new UnsignedIntegerFourBytes(0), service);
    }

    public Stop(UnsignedIntegerFourBytes instanceId, Service service) {
        super(new ActionInvocation(service.getAction("Stop")));
        getActionInvocation().setInput("InstanceID", instanceId);
    }

    @Override
    public void success(ActionInvocation invocation) {
        log.fine("Execution successful");
    }
}