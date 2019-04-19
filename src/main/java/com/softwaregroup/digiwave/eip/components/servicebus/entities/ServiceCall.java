package com.softwaregroup.digiwave.eip.components.servicebus.entities;

import java.io.Serializable;

/**
 * Serializable class defining a single service call.
 *
 * @author Boris Kostadinov
 * @version 1.0
 * @since 2019.1.0
 */
public final class ServiceCall implements Serializable {
    /**
     * @serial A unix timestamp taken at creation time of the service call.
     */
    private long createdOn;
    /**
     * @serial The destination of the service call.
     */
    private ServiceCallDestination destination;
    /**
     * @serial The total execution time of this service call in milliseconds.
     */
    private int executionTime;
    /**
     * @serial A unix timestamp taken at finish time of the service call.
     */
    private long finishedOn;
    /**
     * @serial Flag to indicate if this service call has been completed.
     */
    private boolean isCompleted;
    /**
     * @serial Indicates whether the service call is considered to have been successfully executed.
     */
    private boolean isSuccessful;
    /**
     * @serial A system property used to count the number of tasks used to complete the service call.
     */
    private int lastTaskSeq;
    /**
     * @serial The node level of this service call in the service call tree.
     */
    private int level;
    /**
     * @serial The service call ID of the predecessor in the service call tree.
     */
    private ServiceCall predecessor;
    /**
     * @serial Will contain the results of the service call's execution regardless of the outcome.
     */
    private ServiceCallResult result;
    /**
     * @serial Unique service call identifier.
     */
    private String serviceCallID;
    /**
     * @serial The source of the service call.
     */
    private ServiceCallSource source;
    /**
     * @serial The service call IDs of the successors in the service call tree.
     */
    private String[] successors;
    /**
     * @serial The ID of the parent transaction.
     */
    private String transactionID;

    /**
     * Standard getter.
     *
     * @return The corresponding encapsulated property.
     */
    public long getCreatedOn() {
        return createdOn;
    }

    /**
     * Standard setter.
     *
     * @param createdOn The new value for the encapsulated property.
     */
    public void setCreatedOn( long createdOn ) {
        this.createdOn = createdOn;
    }

    /**
     * Standard getter.
     *
     * @return The corresponding encapsulated property.
     */
    public ServiceCallDestination getDestination() {
        return destination;
    }

    /**
     * Standard setter.
     *
     * @param destination The new value for the encapsulated property.
     */
    public void setDestination( ServiceCallDestination destination ) {
        this.destination = destination;
    }

    /**
     * Standard getter.
     *
     * @return The corresponding encapsulated property.
     */
    public int getExecutionTime() {
        return executionTime;
    }

    /**
     * Standard setter.
     *
     * @param executionTime The new value for the encapsulated property.
     */
    public void setExecutionTime( int executionTime ) {
        this.executionTime = executionTime;
    }

    /**
     * Standard getter.
     *
     * @return The corresponding encapsulated property.
     */
    public long getFinishedOn() {
        return finishedOn;
    }

    /**
     * Standard setter.
     *
     * @param finishedOn The new value for the encapsulated property.
     */
    public void setFinishedOn( long finishedOn ) {
        this.finishedOn = finishedOn;
    }

    /**
     * Standard getter.
     *
     * @return The corresponding encapsulated property.
     */
    public boolean isCompleted() {
        return isCompleted;
    }

    /**
     * Standard setter.
     *
     * @param completed The new value for the encapsulated property.
     */
    public void setCompleted( boolean completed ) {
        isCompleted = completed;
    }

    /**
     * Standard getter.
     *
     * @return The corresponding encapsulated property.
     */
    public boolean isSuccessful() {
        return isSuccessful;
    }

    /**
     * Standard setter.
     *
     * @param successful The new value for the encapsulated property.
     */
    public void setSuccessful( boolean successful ) {
        isSuccessful = successful;
    }

    /**
     * Standard getter.
     *
     * @return The corresponding encapsulated property.
     */
    public int getLastTaskSeq() {
        return lastTaskSeq;
    }

    /**
     * Standard setter.
     *
     * @param lastTaskSeq The new value for the encapsulated property.
     */
    public void setLastTaskSeq( int lastTaskSeq ) {
        this.lastTaskSeq = lastTaskSeq;
    }

    /**
     * Standard getter.
     *
     * @return The corresponding encapsulated property.
     */
    public int getLevel() {
        return level;
    }

    /**
     * Standard setter.
     *
     * @param level The new value for the encapsulated property.
     */
    public void setLevel( int level ) {
        this.level = level;
    }

    /**
     * Standard getter.
     *
     * @return The corresponding encapsulated property.
     */
    public ServiceCall getPredecessor() {
        return predecessor;
    }

    /**
     * Standard setter.
     *
     * @param predecessor The new value for the encapsulated property.
     */
    public void setPredecessor( ServiceCall predecessor ) {
        this.predecessor = predecessor;
    }

    /**
     * Standard getter.
     *
     * @return The corresponding encapsulated property.
     */
    public ServiceCallResult getResult() {
        return result;
    }

    /**
     * Standard setter.
     *
     * @param result The new value for the encapsulated property.
     */
    public void setResult( ServiceCallResult result ) {
        this.result = result;
    }

    /**
     * Standard getter.
     *
     * @return The corresponding encapsulated property.
     */
    public String getServiceCallID() {
        return serviceCallID;
    }

    /**
     * Standard setter.
     *
     * @param serviceCallID The new value for the encapsulated property.
     */
    public void setServiceCallID( String serviceCallID ) {
        this.serviceCallID = serviceCallID;
    }

    /**
     * Standard getter.
     *
     * @return The corresponding encapsulated property.
     */
    public ServiceCallSource getSource() {
        return source;
    }

    /**
     * Standard setter.
     *
     * @param source The new value for the encapsulated property.
     */
    public void setSource( ServiceCallSource source ) {
        this.source = source;
    }

    /**
     * Standard getter.
     *
     * @return The corresponding encapsulated property.
     */
    public String[] getSuccessors() {
        return successors;
    }

    /**
     * Standard setter.
     *
     * @param successors The new value for the encapsulated property.
     */
    public void setSuccessors( String[] successors ) {
        this.successors = successors;
    }

    /**
     * Standard getter.
     *
     * @return The corresponding encapsulated property.
     */
    public String getTransactionID() {
        return transactionID;
    }

    /**
     * Standard setter.
     *
     * @param transactionID The new value for the encapsulated property.
     */
    public void setTransactionID( String transactionID ) {
        this.transactionID = transactionID;
    }

    /**
     * Standard stringify method.
     *
     * @return The string representation of the object.
     */
    @Override
    public String toString() {
        String string = "SERVICE CALL { " + getServiceCallID() + " > from: " + getSource().serviceDomainName + " to: " + getDestination().serviceDomainName + "." + getDestination().serviceAlias + " }";
        if ( getPredecessor() != null ) {
            string += "\n\r       ↳ " + getPredecessor().toString();
        }
        return string;
    }
}
