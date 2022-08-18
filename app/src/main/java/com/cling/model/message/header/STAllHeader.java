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

package com.cling.model.message.header;

import com.cling.model.types.NotificationSubtype;

/**
 * @author Christian Bauer
 */
public class STAllHeader extends UpnpHeader<NotificationSubtype> {

    public STAllHeader() {
        setValue(NotificationSubtype.ALL);
    }

    public String getString() {
        return getValue().getHeaderString();
    }

    public void setString(String s) throws InvalidHeaderException {
        if (!s.equals(NotificationSubtype.ALL.getHeaderString())) {
            throw new InvalidHeaderException("Invalid ST header value (not " + NotificationSubtype.ALL + "): " + s);
        }
    }

}
