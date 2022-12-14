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

package com.cling.transport.spi;


import com.cling.model.UnsupportedDataException;
import com.cling.model.message.IncomingDatagramMessage;
import com.cling.model.message.OutgoingDatagramMessage;

import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * Reads and creates UDP datagrams from and into UPnP messages.
 * <p>
 * An implementation of this interface has to be thread-safe.
 * </p>
 *
 * @author Christian Bauer
 */
public interface DatagramProcessor {

    /**
     * Reads the datagram and instantiates a message.
     * <p>
     * The message is either a {@link com.cling.model.message.UpnpRequest} or
     * a {@link com.cling.model.message.UpnpResponse} operation type.
     * </p>
     *
     * @param receivedOnAddress The address of the socket on which this datagram was received.
     * @param datagram          The received UDP datagram.
     * @return The populated instance.
     * @throws com.cling.model.UnsupportedDataException If the datagram could not be read, or didn't contain required data.
     */
    public IncomingDatagramMessage read(InetAddress receivedOnAddress, DatagramPacket datagram) throws UnsupportedDataException;

    /**
     * Creates a UDP datagram with the content of a message.
     * <p>
     * The outgoing message might be a {@link com.cling.model.message.UpnpRequest} or a
     * {@link com.cling.model.message.UpnpResponse}.
     * </p>
     *
     * @param message The outgoing datagram message.
     * @return An actual UDP datagram.
     * @throws UnsupportedDataException If the datagram could not be created.
     */
    public DatagramPacket write(OutgoingDatagramMessage message) throws UnsupportedDataException;

}


