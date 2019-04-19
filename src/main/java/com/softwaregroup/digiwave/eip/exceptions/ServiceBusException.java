package com.softwaregroup.digiwave.eip.exceptions;

/**
 * A custom exception defining a problem detected in the service bus of the EIP.
 *
 * @author Boris Kostadinov
 * @version 1.0
 * @since 2019.1.0
 */
public class ServiceBusException extends Exception {
    public ServiceBusException( String message, Throwable cause ) {
        super( message, cause );
    }

    public ServiceBusException( String message ) {
        super( message );
    }
}
