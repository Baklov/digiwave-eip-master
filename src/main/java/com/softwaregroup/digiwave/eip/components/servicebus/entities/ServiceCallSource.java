package com.softwaregroup.digiwave.eip.components.servicebus.entities;

import java.io.Serializable;

/**
 * Serializable class defining a single service call source.
 *
 * @author Boris Kostadinov
 * @version 1.0
 * @since 2019.1.0
 */
public final class ServiceCallSource implements Serializable {
    /**
     * @serial The instance ID of the microservice worker from which the service call originated.
     */
    public String instanceID;
    /**
     * @serial The service domain name of the microservice from which the service call originated.
     */
    public String serviceDomainName;
}
