package com.softwaregroup.digiwave.eip.components.servicebus.entities;

import org.json.simple.JSONObject;

import java.io.Serializable;

/**
 * Serializable class defining a dynamic result of a service call.
 * TODO: still work in progress
 *
 * @author Boris Kostadinov
 * @version 1.0
 * @since 2019.1.0
 */
public class ServiceCallResult implements Serializable {
    /**
     * @serial A flag indicating if this result is a success or not.
     */
    private boolean isSuccessful;
    /**
     * @serial If there was exception during the service call processing, it will be set here. Otherwise it will be 'null'.
     */
    private Exception exception;
    /**
     * @serial The payload containing the results from the service call processing.
     */
    private JSONObject payload;

    /**
     * Standard getter.
     *
     * @return The success status of this service call result.
     */
    public boolean isSuccessful() {
        return isSuccessful;
    }

    /**
     * Standard setter.
     *
     * @param successful The new status of the service call result.
     */
    public void setSuccessful( boolean successful ) {
        isSuccessful = successful;
    }

    /**
     * Standard getter.
     *
     * @return The exception, if there is one. Will return 'null' otherwise.
     */
    public Exception getException() {
        return exception;
    }

    /**
     * Standard setter.
     *
     * @param exception An exception to be set for this result.
     */
    public void setException( Exception exception ) {
        this.exception = exception;
    }

    /**
     * Standard getter.
     *
     * @return The payload of this result as JSON object.
     */
    public JSONObject getPayload() {
        return payload;
    }

    /**
     * Standard setter.
     *
     * @param payload A payload to be set for this result.
     */
    public void setPayload( JSONObject payload ) {
        this.payload = payload;
    }
}
