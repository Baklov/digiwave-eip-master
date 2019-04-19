package com.softwaregroup.digiwave.eip.components.servicebus.entities;

import java.io.Serializable;

/**
 * Serializable class defining a service bus transaction.
 * TODO: verify if and how this will be used...
 *
 * @author Boris Kostadinov
 * @version 1.0
 * @since 2019.1.0
 */
public final class ServiceBusTransaction implements Serializable {
    /**
     * @serial The auth token associated with this transaction.
     */
    public String authToken;
    /**
     * @serial A unix timestamp taken at creation time of the transaction.
     */
    public long createdOn;
    /**
     * @serial Indicates whether the transaction is still open (i.e. being processed).
     */
    public boolean isOpen;
    /**
     * @serial Indicates whether the transaction is considered to have been successfully executed.
     */
    public boolean isSuccessful;
    /**
     * @serial Will contain the results of the transaction's execution regardless of the outcome.
     * TODO: Should use a different type for this one.
     */
    public Object result;
    /**
     * @serial Container for all ServiceCalls within the transaction.
     */
    public ServiceCall[] serviceCalls;
    /**
     * @serial Unique transaction identifier.
     */
    public String transactionID;
    /**
     * @serial Used to store the anti-tampering hash, if enabled.
     */
    public String transactionHash;
}
