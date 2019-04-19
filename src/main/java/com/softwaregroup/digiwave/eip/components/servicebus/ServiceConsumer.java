package com.softwaregroup.digiwave.eip.components.servicebus;

import com.softwaregroup.digiwave.eip.utils.Logger;

import java.util.concurrent.CompletableFuture;

/**
 * An abstract class defining a ServiceConsumer behavior.
 * NOTE: Extend this in order to create a microservice that only consumes other services.
 *
 * @author Boris Kostadinov
 * @version 1.0
 * @since 2019.1.0
 */
public abstract class ServiceConsumer extends MicroserviceInstance implements ServiceCaller {
    /**
     * Used to initialize the microservice as a ServiceConsumer.
     * NOTE: Override this to implement custom startup behavior, but make sure to call the base method first.
     *
     * @return The default startup method returns nothing.
     */
    @Override
    public CompletableFuture start() {
        return super.start().thenRunAsync( () -> {
            try {
                ServiceBusDispatcher.getInstance().configureServiceConsumer();
                Logger.log( "Service consumer start-up sequence completed.", Logger.Severity.INFO, Logger.Threads.ESB );
            } catch ( Exception exception ) {
                Logger.log( "Service consumer failed to start due to an exception.", Logger.Severity.ERROR, Logger.Threads.ESB, exception );
            }
        } );
    }

    /**
     * Used to shutdown the microservice.
     * NOTE: Override this to implement custom startup behavior, but make sure to call the base method first.
     *
     * @return The default shutdown method returns nothing.
     */
    @Override
    public CompletableFuture stop() {
        return super.stop().thenRunAsync( () -> {
            ServiceBusDispatcher.getInstance().shutDown();
            Logger.log( "Service consumer shut-down sequence completed.", Logger.Severity.INFO, Logger.Threads.ESB );
        } );
    }
}
