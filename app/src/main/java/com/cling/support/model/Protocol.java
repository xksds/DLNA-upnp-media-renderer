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

package com.cling.support.model;

/**
 *
 */
public enum Protocol {

    ALL(ProtocolInfo.WILDCARD),
    HTTP_GET("http-get"),
    RTSP_RTP_UDP("rtsp-rtp-udp"),
    INTERNAL("internal"),
    IEC61883("iec61883");

    private String protocolString;

    Protocol(String protocolString) {
        this.protocolString = protocolString;
    }

    public static Protocol valueOrNullOf(String s) {
        for (Protocol protocol : values()) {
            if (protocol.toString().equals(s)) {
                return protocol;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return protocolString;
    }

}
