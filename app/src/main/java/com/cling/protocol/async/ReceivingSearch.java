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

package com.cling.protocol.async;

import com.cling.UpnpService;
import com.cling.model.DiscoveryOptions;
import com.cling.model.Location;
import com.cling.model.NetworkAddress;
import com.cling.model.message.IncomingDatagramMessage;
import com.cling.model.message.UpnpRequest;
import com.cling.model.message.discovery.IncomingSearchRequest;
import com.cling.model.message.discovery.OutgoingSearchResponse;
import com.cling.model.message.discovery.OutgoingSearchResponseDeviceType;
import com.cling.model.message.discovery.OutgoingSearchResponseRootDevice;
import com.cling.model.message.discovery.OutgoingSearchResponseServiceType;
import com.cling.model.message.discovery.OutgoingSearchResponseUDN;
import com.cling.model.message.header.DeviceTypeHeader;
import com.cling.model.message.header.MXHeader;
import com.cling.model.message.header.RootDeviceHeader;
import com.cling.model.message.header.STAllHeader;
import com.cling.model.message.header.ServiceTypeHeader;
import com.cling.model.message.header.UDNHeader;
import com.cling.model.message.header.UpnpHeader;
import com.cling.model.meta.Device;
import com.cling.model.meta.LocalDevice;
import com.cling.model.types.DeviceType;
import com.cling.model.types.ServiceType;
import com.cling.model.types.UDN;
import com.cling.protocol.ReceivingAsync;
import com.cling.transport.RouterException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Handles reception of search requests, responds for local registered devices.
 * <p>
 * Waits a random time between 0 and the requested <em>MX</em> (maximum 120 seconds)
 * before executing. Only waits if there are actually any registered local devices.
 * </p>
 * <p>
 * Extracts the <em>search target</em>, builds and sends the dozens of messages
 * required by the UPnP specification, depending on the search target and what
 * local devices and services are found in the {@link com.cling.registry.Registry}.
 * </p>
 *
 * @author Christian Bauer
 */
public class ReceivingSearch extends ReceivingAsync<IncomingSearchRequest> {

    final private static Logger log = Logger.getLogger(ReceivingSearch.class.getName());

    final protected Random randomGenerator = new Random();

    public ReceivingSearch(UpnpService upnpService, IncomingDatagramMessage<UpnpRequest> inputMessage) {
        super(upnpService, new IncomingSearchRequest(inputMessage));
    }

    protected void execute() throws RouterException {
        if (getUpnpService().getRouter() == null) {
            // TODO: http://mailinglists.945824.n3.nabble.com/rare-NPE-on-start-tp3078213p3142767.html
            log.fine("Router hasn't completed initialization, ignoring received search message");
            return;
        }

        if (!getInputMessage().isMANSSDPDiscover()) {
            log.fine("Invalid search request, no or invalid MAN ssdp:discover header: " + getInputMessage());
            return;
        }

        UpnpHeader searchTarget = getInputMessage().getSearchTarget();

        if (searchTarget == null) {
            log.fine("Invalid search request, did not contain ST header: " + getInputMessage());
            return;
        }

        List<NetworkAddress> activeStreamServers =
                getUpnpService().getRouter().getActiveStreamServers(getInputMessage().getLocalAddress());
        if (activeStreamServers.size() == 0) {
            log.fine("Aborting search response, no active stream servers found (network disabled?)");
            return;
        }

        for (NetworkAddress activeStreamServer : activeStreamServers) {
            sendResponses(searchTarget, activeStreamServer);
        }
    }

    @Override
    protected boolean waitBeforeExecution() throws InterruptedException {

        Integer mx = getInputMessage().getMX();

        if (mx == null) {
            log.fine("Invalid search request, did not contain MX header: " + getInputMessage());
            return false;
        }

        // Spec says we should assume "less" if it's 120 or more
        // From the spec, MX should be "greater than or equal to 1"
        // Prevent negative MX to make nextInt() throw IllegalArgumentException below
        if (mx > 120 || mx <= 0) mx = MXHeader.DEFAULT_VALUE;

        // Only wait if there is something to wait for
        if (getUpnpService().getRegistry().getLocalDevices().size() > 0) {
            int sleepTime = randomGenerator.nextInt(mx * 1000);
            log.fine("Sleeping " + sleepTime + " milliseconds to avoid flooding with search responses");
            Thread.sleep(sleepTime);
        }

        return true;
    }

    protected void sendResponses(UpnpHeader searchTarget, NetworkAddress activeStreamServer) throws RouterException {
        if (searchTarget instanceof STAllHeader) {

            sendSearchResponseAll(activeStreamServer);

        } else if (searchTarget instanceof RootDeviceHeader) {

            sendSearchResponseRootDevices(activeStreamServer);

        } else if (searchTarget instanceof UDNHeader) {

            sendSearchResponseUDN((UDN) searchTarget.getValue(), activeStreamServer);

        } else if (searchTarget instanceof DeviceTypeHeader) {

            sendSearchResponseDeviceType((DeviceType) searchTarget.getValue(), activeStreamServer);

        } else if (searchTarget instanceof ServiceTypeHeader) {

            sendSearchResponseServiceType((ServiceType) searchTarget.getValue(), activeStreamServer);

        } else {
            log.warning("Non-implemented search request target: " + searchTarget.getClass());
        }
    }

    protected void sendSearchResponseAll(NetworkAddress activeStreamServer) throws RouterException {
        log.fine("Responding to 'all' search with advertisement messages for all local devices");
        for (LocalDevice localDevice : getUpnpService().getRegistry().getLocalDevices()) {

            if (isAdvertisementDisabled(localDevice))
                continue;

            // We are re-using the regular notification messages here but override the NT with the ST header
            log.finer("Sending root device messages: " + localDevice);
            List<OutgoingSearchResponse> rootDeviceMsgs =
                    createDeviceMessages(localDevice, activeStreamServer);
            for (OutgoingSearchResponse upnpMessage : rootDeviceMsgs) {
                getUpnpService().getRouter().send(upnpMessage);
            }

            if (localDevice.hasEmbeddedDevices()) {
                for (LocalDevice embeddedDevice : localDevice.findEmbeddedDevices()) {
                    log.finer("Sending embedded device messages: " + embeddedDevice);
                    List<OutgoingSearchResponse> embeddedDeviceMsgs =
                            createDeviceMessages(embeddedDevice, activeStreamServer);
                    for (OutgoingSearchResponse upnpMessage : embeddedDeviceMsgs) {
                        getUpnpService().getRouter().send(upnpMessage);
                    }
                }
            }

            List<OutgoingSearchResponse> serviceTypeMsgs =
                    createServiceTypeMessages(localDevice, activeStreamServer);
            if (serviceTypeMsgs.size() > 0) {
                log.finer("Sending service type messages");
                for (OutgoingSearchResponse upnpMessage : serviceTypeMsgs) {
                    getUpnpService().getRouter().send(upnpMessage);
                }
            }

        }
    }

    protected List<OutgoingSearchResponse> createDeviceMessages(LocalDevice device,
                                                                NetworkAddress activeStreamServer) {
        List<OutgoingSearchResponse> msgs = new ArrayList<OutgoingSearchResponse>();

        // See the tables in UDA 1.0 section 1.1.2

        if (device.isRoot()) {
            msgs.add(
                    new OutgoingSearchResponseRootDevice(
                            getInputMessage(),
                            getDescriptorLocation(activeStreamServer, device),
                            device
                    )
            );
        }

        msgs.add(
                new OutgoingSearchResponseUDN(
                        getInputMessage(),
                        getDescriptorLocation(activeStreamServer, device),
                        device
                )
        );

        msgs.add(
                new OutgoingSearchResponseDeviceType(
                        getInputMessage(),
                        getDescriptorLocation(activeStreamServer, device),
                        device
                )
        );

        for (OutgoingSearchResponse msg : msgs) {
            prepareOutgoingSearchResponse(msg);
        }

        return msgs;
    }

    protected List<OutgoingSearchResponse> createServiceTypeMessages(LocalDevice device,
                                                                     NetworkAddress activeStreamServer) {
        List<OutgoingSearchResponse> msgs = new ArrayList<OutgoingSearchResponse>();
        for (ServiceType serviceType : device.findServiceTypes()) {
            OutgoingSearchResponse message =
                    new OutgoingSearchResponseServiceType(
                            getInputMessage(),
                            getDescriptorLocation(activeStreamServer, device),
                            device,
                            serviceType
                    );
            prepareOutgoingSearchResponse(message);
            msgs.add(message);
        }
        return msgs;
    }

    protected void sendSearchResponseRootDevices(NetworkAddress activeStreamServer) throws RouterException {
        log.fine("Responding to root device search with advertisement messages for all local root devices");
        for (LocalDevice device : getUpnpService().getRegistry().getLocalDevices()) {

            if (isAdvertisementDisabled(device))
                continue;

            OutgoingSearchResponse message =
                    new OutgoingSearchResponseRootDevice(
                            getInputMessage(),
                            getDescriptorLocation(activeStreamServer, device),
                            device
                    );
            prepareOutgoingSearchResponse(message);
            getUpnpService().getRouter().send(message);
        }
    }

    protected void sendSearchResponseUDN(UDN udn, NetworkAddress activeStreamServer) throws RouterException {
        Device device = getUpnpService().getRegistry().getDevice(udn, false);
        if (device != null && device instanceof LocalDevice) {

            if (isAdvertisementDisabled((LocalDevice) device))
                return;

            log.fine("Responding to UDN device search: " + udn);
            OutgoingSearchResponse message =
                    new OutgoingSearchResponseUDN(
                            getInputMessage(),
                            getDescriptorLocation(activeStreamServer, (LocalDevice) device),
                            (LocalDevice) device
                    );
            prepareOutgoingSearchResponse(message);
            getUpnpService().getRouter().send(message);
        }
    }

    protected void sendSearchResponseDeviceType(DeviceType deviceType, NetworkAddress activeStreamServer) throws RouterException {
        log.fine("Responding to device type search: " + deviceType);
        Collection<Device> devices = getUpnpService().getRegistry().getDevices(deviceType);
        for (Device device : devices) {
            if (device instanceof LocalDevice) {

                if (isAdvertisementDisabled((LocalDevice) device))
                    continue;

                log.finer("Sending matching device type search result for: " + device);
                OutgoingSearchResponse message =
                        new OutgoingSearchResponseDeviceType(
                                getInputMessage(),
                                getDescriptorLocation(activeStreamServer, (LocalDevice) device),
                                (LocalDevice) device
                        );
                prepareOutgoingSearchResponse(message);
                getUpnpService().getRouter().send(message);
            }
        }
    }

    protected void sendSearchResponseServiceType(ServiceType serviceType, NetworkAddress activeStreamServer) throws RouterException {
        log.fine("Responding to service type search: " + serviceType);
        Collection<Device> devices = getUpnpService().getRegistry().getDevices(serviceType);
        for (Device device : devices) {
            if (device instanceof LocalDevice) {

                if (isAdvertisementDisabled((LocalDevice) device))
                    continue;

                log.finer("Sending matching service type search result: " + device);
                OutgoingSearchResponse message =
                        new OutgoingSearchResponseServiceType(
                                getInputMessage(),
                                getDescriptorLocation(activeStreamServer, (LocalDevice) device),
                                (LocalDevice) device,
                                serviceType
                        );
                prepareOutgoingSearchResponse(message);
                getUpnpService().getRouter().send(message);
            }
        }
    }

    protected Location getDescriptorLocation(NetworkAddress activeStreamServer, LocalDevice device) {
        return new Location(
                activeStreamServer,
                getUpnpService().getConfiguration().getNamespace().getDescriptorPath(device)
        );
    }

    protected boolean isAdvertisementDisabled(LocalDevice device) {
        DiscoveryOptions options =
                getUpnpService().getRegistry().getDiscoveryOptions(device.getIdentity().getUdn());
        return options != null && !options.isAdvertised();
    }

    /**
     * Override this to edit the outgoing message, e.g. by adding headers.
     */
    protected void prepareOutgoingSearchResponse(OutgoingSearchResponse message) {
    }

}
