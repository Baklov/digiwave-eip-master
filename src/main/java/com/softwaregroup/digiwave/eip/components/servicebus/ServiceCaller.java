package com.softwaregroup.digiwave.eip.components.servicebus;

import com.softwaregroup.digiwave.eip.components.servicebus.entities.*;
import com.softwaregroup.digiwave.eip.utils.Logger;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * An interface defining the Service Caller behavior.
 * NOTE: This is implemented by other internal behaviors and isn't intended for outside usage.
 *
 * @author Boris Kostadinov
 * @version 1.0
 * @since 2019.1.0
 */
interface ServiceCaller {
    /**
     * Used to call a service in the EIP microservice ecosystem asynchronously.
     *
     * @param serviceAddress     The service address has to define a valid service domain name, service alias, and optionally a service version.
     * @param serviceParams      Set of parameters to provide to the called service.
     * @param serviceCallContext The context in which the service call is performed.
     * @return The future completes with the result of the service call.
     */
    default CompletableFuture<ServiceCallResult> callServiceAsync( ServiceAddress serviceAddress, ServiceParams serviceParams, ServiceCallContext serviceCallContext ) {
        return CompletableFuture
                .supplyAsync( () -> prepareServiceCall( serviceAddress, serviceParams, serviceCallContext ) )
                .thenComposeAsync( serviceCall -> ServiceBusDispatcher.getInstance().sendServiceRequest( serviceCall ) )
                .handleAsync( ( result, exception ) -> {
                    // TODO: improve the error handling here
                    if ( exception != null ) {
                        Logger.log( "Error during attempted service call: " + exception, Logger.Severity.ERROR, Logger.Threads.ESB, exception );
                        result.setException( ( Exception ) exception );
                    }
                    return result;
                } )
                .thenApplyAsync( result -> result );
    }

    /**
     * Used to assemble and prepare a new ServiceCall object.
     *
     * @param serviceAddress     The service address has to define a valid service domain name, service alias, and optionally a service version.
     * @param serviceParams      Set of parameters to provide to the called service.
     * @param serviceCallContext The context in which the service call is performed.
     * @return The new ServiceCall object ready to be sent.
     */
    private ServiceCall prepareServiceCall( ServiceAddress serviceAddress, ServiceParams serviceParams, ServiceCallContext serviceCallContext ) {
        // assemble the new service call:
        String transactionID = ( serviceCallContext.serviceCall != null ) ? serviceCallContext.serviceCall.getTransactionID() : ( "T-" + UUID.randomUUID().toString() );
        int level = ( serviceCallContext.serviceCall != null ) ? serviceCallContext.serviceCall.getLevel() + 1 : 0;
        ServiceCallSource source = new ServiceCallSource();
        source.instanceID = MicroserviceInstance.INSTANCE_ID;
        source.serviceDomainName = MicroserviceInstance.SERVICE_DOMAIN_NAME;
        ServiceCallDestination destination = new ServiceCallDestination();
        destination.serviceAlias = serviceAddress.serviceAlias;
        destination.serviceDomainName = serviceAddress.serviceDomainName;
        destination.serviceVersion = serviceAddress.serviceVersion;
        destination.serviceParams = serviceParams;

        ServiceCall serviceCall = ServiceBusDispatcher.getInstance().createServiceCall( transactionID, level, source, destination );

        // if there is a predecessor, link it to the new service call:
        if ( serviceCallContext.serviceCall != null ) {
            serviceCall.setPredecessor( serviceCallContext.serviceCall );
        }

        return serviceCall;
    }
}
