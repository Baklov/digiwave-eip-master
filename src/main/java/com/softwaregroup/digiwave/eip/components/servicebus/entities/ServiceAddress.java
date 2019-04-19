package com.softwaregroup.digiwave.eip.components.servicebus.entities;

import java.io.Serializable;

/**
 * Serializable class defining a service address in the EIP microservice ecosystem.
 *
 * @author Boris Kostadinov
 * @version 1.0
 * @since 2019.1.0
 */
public final class ServiceAddress implements Serializable {
    /**
     * @serial A valid service alias.
     */
    public String serviceAlias;
    /**
     * @serial A valid service domain name.
     */
    public String serviceDomainName;
    /**
     * @serial Optional service version. If not provided, the latest version will be assumed as a target.
     */
    public int serviceVersion;
}
