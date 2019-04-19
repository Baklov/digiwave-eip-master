package com.softwaregroup.digiwave.eip.components.servicebus;

import com.softwaregroup.digiwave.eip.utils.Tools;

import java.util.UUID;
import java.util.concurrent.*;

/**
 * An abstract class defining a basic microservice behavior.
 * NOTE: Extend this in order to create a custom microservice that does not implement any default behavior.
 *
 * @author Boris Kostadinov
 * @version 1.0
 * @since 2019.1.0
 */
public abstract class MicroserviceInstance {
    public static final String INSTANCE_ID = ( System.getenv( "DIGIWAVE_INSTANCE_ID" ) != null ) ? System.getenv( "DIGIWAVE_INSTANCE_ID" ) : "INST-" + UUID.randomUUID().toString();
    public static final long INSTANCE_START = Tools.getUnixTimestamp();
    public static final String SERVICE_DOMAIN_NAME = System.getenv( "DIGIWAVE_SERVICE_DOMAIN_NAME" );

    /**
     * Used to initialize the microservice.
     * NOTE: Override this to implement custom startup behavior.
     *
     * @return The default startup method returns nothing.
     */
    public CompletableFuture start() {
        return CompletableFuture.completedFuture( null );
    }

    /**
     * Used to shutdown the microservice.
     * NOTE: Override this to implement custom shutdown behavior.
     *
     * @return The default shutdown method returns nothing.
     */
    public CompletableFuture stop() {
        return CompletableFuture.completedFuture( null );
    }
}
