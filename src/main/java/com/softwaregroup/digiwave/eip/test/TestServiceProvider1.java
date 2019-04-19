package com.softwaregroup.digiwave.eip.test;

import com.softwaregroup.digiwave.eip.components.servicebus.ServiceProvider;
import com.softwaregroup.digiwave.eip.components.servicebus.entities.ServiceDefinition;

import java.util.concurrent.CompletableFuture;

/**
 * An com.softwaregroup.digiwave.eip.test class implementing the ServiceProvider behavior.
 * NOTE: To run this MS you need to supply the following ENV variables:
 *    DIGIWAVE_SERVICE_DOMAIN_NAME=testService1
 *    DIGIWAVE_SERVICE_CLASS=com.softwaregroup.digiwave.eip.test.TestServiceProvider1
 *
 * @author Boris Kostadinov
 * @version 1.0
 * @since 2019.1.0
 */
public class TestServiceProvider1 extends ServiceProvider {
    /**
     * Used to initialize the microservice as a ServiceProvider.
     * NOTE: Override this to implement custom startup behavior, but make sure to call the base method first.
     *
     * @return The default startup method returns nothing.
     */
    @Override
    public CompletableFuture start() {
        return super.start().thenRunAsync( () -> {
            ServiceDefinition serviceDefinition = new ServiceDefinition();
            serviceDefinition.serviceAlias = "service1";
            serviceDefinition.serviceClassName = "com.softwaregroup.digiwave.eip.test.services.TestService1";
            serviceDefinition.serviceVersion = 1;

            registerService( serviceDefinition );
        } );
    }
}
