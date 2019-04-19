package com.softwaregroup.digiwave.eip.test.services;

import com.softwaregroup.digiwave.eip.components.servicebus.Service;
import com.softwaregroup.digiwave.eip.components.servicebus.entities.*;

import java.util.concurrent.CompletableFuture;

/**
 * A com.softwaregroup.digiwave.eip.test implementation of a Service.
 *
 * @author Boris Kostadinov
 * @version 1.0
 * @since 2019.1.0
 */
public final class TestService1 implements Service {
    /**
     * It will be called automatically by the EIP framework when the service needs to be executed.
     *
     * @param serviceDefinition  The ServiceDefinition object as received during service registration.
     * @param serviceParams      The ServiceParams object provided by the caller.
     * @param serviceCallContext The ServiceCallContext that initiated this service call.
     * @return This method has to return a completable future in order to work properly.
     */
    @Override
    public CompletableFuture<ServiceCallResult> executeAsync( ServiceDefinition serviceDefinition, ServiceParams serviceParams, ServiceCallContext serviceCallContext ) {
        return CompletableFuture.supplyAsync( () -> {
            ServiceAddress serviceAddress = new ServiceAddress();
            serviceAddress.serviceAlias = "service2";
            serviceAddress.serviceDomainName = "testService2";
            serviceAddress.serviceVersion = 1;

            return callServiceAsync( serviceAddress, serviceParams, serviceCallContext ).join();
        } ).thenApplyAsync( result -> result );
    }
}
