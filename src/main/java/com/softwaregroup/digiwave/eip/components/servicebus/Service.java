package com.softwaregroup.digiwave.eip.components.servicebus;

import com.softwaregroup.digiwave.eip.components.servicebus.entities.ServiceCallContext;
import com.softwaregroup.digiwave.eip.components.servicebus.entities.ServiceCallResult;
import com.softwaregroup.digiwave.eip.components.servicebus.entities.ServiceDefinition;
import com.softwaregroup.digiwave.eip.components.servicebus.entities.ServiceParams;
import com.softwaregroup.digiwave.eip.exceptions.ServiceBusException;

import java.util.concurrent.CompletableFuture;

/**
 * An interface defining the frame for a Service behavior.
 * NOTE: Implement this in order to create a service encapsulating business logic.
 *
 * @author Boris Kostadinov
 * @version 1.0
 * @since 2019.1.0
 */
public interface Service extends ServiceCaller {
    /**
     * It will be called automatically by the EIP framework when the service needs to be executed asynchronously.
     *
     * @param serviceDefinition  The ServiceDefinition object as received during service registration.
     * @param serviceParams      The ServiceParams object provided by the caller.
     * @param serviceCallContext The ServiceCallContext that initiated this service call.
     * @return This method has to return a completable future in order to work properly.
     */
    default CompletableFuture<ServiceCallResult> executeAsync( ServiceDefinition serviceDefinition, ServiceParams serviceParams, ServiceCallContext serviceCallContext ) {
        ServiceCallResult result = new ServiceCallResult();
        result.setException( new ServiceBusException( "Service execution method not implemented!" ) );
        return CompletableFuture.completedFuture( result );
    }
}
