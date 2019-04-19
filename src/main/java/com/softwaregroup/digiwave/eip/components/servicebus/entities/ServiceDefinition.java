package com.softwaregroup.digiwave.eip.components.servicebus.entities;

import java.io.Serializable;

/**
 * Serializable class defining service definition payload.
 * TODO: still work in progress
 *
 * @author Boris Kostadinov
 * @version 1.0
 * @since 2019.1.0
 */
public final class ServiceDefinition implements Serializable {
    public String serviceAlias;
    public String serviceClassName;
    public int serviceVersion;

    /**
     * Standard stringify method.
     *
     * @return The string representation of the object.
     */
    @Override
    public String toString() {
        return "SERVICE DEF { " + serviceAlias + " : v" + serviceVersion + " }";
    }
}
