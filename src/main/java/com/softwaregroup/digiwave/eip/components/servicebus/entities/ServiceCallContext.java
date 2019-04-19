package com.softwaregroup.digiwave.eip.components.servicebus.entities;

import java.io.Serializable;

/**
 * Serializable class defining a service call context.
 * TODO: still work in progress
 *
 * @author Boris Kostadinov
 * @version 1.0
 * @since 2019.1.0
 */
public final class ServiceCallContext implements Serializable {
    /**
     * @serial A valid authentication token that initialized the service call.
     */
    public String authToken;
    /**
     * @serial The ServiceCall object itself.
     */
    public ServiceCall serviceCall;
}
