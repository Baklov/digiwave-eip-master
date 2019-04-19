package com.softwaregroup.digiwave.eip.components.servicebus.entities;

import java.io.Serializable;

/**
 * Serializable class defining a single service call destination.
 *
 * @author Boris Kostadinov
 * @version 1.0
 * @since 2019.1.0
 */
public final class ServiceCallDestination implements Serializable {
    /**
     * @serial The instance ID of the microservice worker by which the service call was accepted (available after acceptance).
     */
    public String instanceID;
    /**
     * @serial The service alias of the API service requested by the service call.
     */
    public String serviceAlias;
    /**
     * @serial The params to be provided to the API service.
     */
    public ServiceParams serviceParams;
    /**
     * @serial The service domain name of the microservice to which the service call is sent.
     */
    public String serviceDomainName;
    /**
     * @serial The version of the API service requested by the service call.
     */
    public int serviceVersion;
}
