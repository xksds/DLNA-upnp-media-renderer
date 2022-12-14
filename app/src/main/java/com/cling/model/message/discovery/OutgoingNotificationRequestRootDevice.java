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

package com.cling.model.message.discovery;

import com.cling.model.Location;
import com.cling.model.message.header.InterfaceMacHeader;
import com.cling.model.message.header.RootDeviceHeader;
import com.cling.model.message.header.USNRootDeviceHeader;
import com.cling.model.message.header.UpnpHeader;
import com.cling.model.meta.LocalDevice;
import com.cling.model.types.NotificationSubtype;

/**
 * @author Christian Bauer
 */
public class OutgoingNotificationRequestRootDevice extends OutgoingNotificationRequest {

    public OutgoingNotificationRequestRootDevice(Location location, LocalDevice device, NotificationSubtype type) {
        super(location, device, type);

        getHeaders().add(UpnpHeader.Type.NT, new RootDeviceHeader());
        getHeaders().add(UpnpHeader.Type.USN, new USNRootDeviceHeader(device.getIdentity().getUdn()));

        if (location.getNetworkAddress().getHardwareAddress() != null) {
            getHeaders().add(
                    UpnpHeader.Type.EXT_IFACE_MAC,
                    new InterfaceMacHeader(location.getNetworkAddress().getHardwareAddress())
            );
        }
    }

}
