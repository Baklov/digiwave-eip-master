package com.softwaregroup.digiwave.eip.components.servicebus;

import com.softwaregroup.digiwave.eip.components.servicebus.entities.ServiceCall;
import com.softwaregroup.digiwave.eip.components.servicebus.entities.ServiceCallContext;
import com.softwaregroup.digiwave.eip.components.servicebus.entities.ServiceCallResult;
import com.softwaregroup.digiwave.eip.components.servicebus.entities.ServiceDefinition;
import com.softwaregroup.digiwave.eip.exceptions.ServiceBusException;
import com.softwaregroup.digiwave.eip.utils.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * An abstract class defining a ServiceProvider behavior.
 * NOTE: Extend this in order to create a microservice that both consumes other services and provides services on its own.
 *
 * @author Boris Kostadinov
 * @version 1.0
 * @since 2019.1.0
 */
public abstract class ServiceProvider extends ServiceConsumer {
    private Map<String, Map<Integer, ServiceDefinition>> serviceInterface;

    public ServiceProvider() {
        serviceInterface = new HashMap<>();
    }

    /**
     * Used to initialize the microservice as a ServiceProvider.
     * NOTE: Override this to implement custom startup behavior, but make sure to call the base method first.
     *
     * @return The default startup method returns nothing.
     */
    @Override
    public CompletableFuture start() {
        return super.start().thenRunAsync( () -> {
            try {
                ServiceBusDispatcher.getInstance().configureServiceProvider( this::processServiceRequest );
                Logger.log( "Service provider start-up sequence completed.", Logger.Severity.INFO, Logger.Threads.ESB );
            } catch ( Exception exception ) {
                Logger.log( "Service provider failed to start due to an exception.", Logger.Severity.ERROR, Logger.Threads.ESB, exception );
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
            Logger.log( "Service provider shut-down sequence completed.", Logger.Severity.INFO, Logger.Threads.ESB );
        } );
    }

    /**
     * Used to register a service into the service interface.
     *
     * @param serviceDefinition The ServiceDefinition object defining the service.
     */
    protected void registerService( ServiceDefinition serviceDefinition ) {
        Logger.log( "Registering service definition for service: " + serviceDefinition.serviceAlias, Logger.Severity.DEBUG, Logger.Threads.ESB, serviceDefinition );

        if ( !serviceInterface.containsKey( serviceDefinition.serviceAlias ) ) {
            serviceInterface.put( serviceDefinition.serviceAlias, new HashMap<>() );
        }
        Map<Integer, ServiceDefinition> serviceVersions = serviceInterface.get( serviceDefinition.serviceAlias );
        int serviceVersion = ( serviceDefinition.serviceVersion > 0 ) ? serviceDefinition.serviceVersion : 1;

        /*
         * NOTE: If the same version of the service already exists, it will be overridden!
         */
        if ( serviceVersions.containsKey( serviceVersion ) ) {
            Logger.log( "Service definition for '" + serviceDefinition.serviceAlias + "' version '" + serviceVersion + "' already existed and will be overridden.", Logger.Severity.WARNING, Logger.Threads.ESB );
            serviceVersions.replace( serviceVersion, serviceDefinition );
        } else {
            serviceVersions.put( serviceVersion, serviceDefinition );
        }
        serviceInterface.replace( serviceDefinition.serviceAlias, serviceVersions );
    }

    /**
     * Used to verify is the current caller has access to the service.
     * NOTE: Override this method to invoke custom functionality.
     * NOTE: Make sure to do an actual security check when overriding!
     *
     * @param authToken         The authorization token accessing the service.
     * @param serviceDefinition The ServiceDefinition object.
     * @return The future needs to complete with 'true' only if the service can be accessed by the authorization token.
     */
    CompletableFuture<Boolean> verifyAccess( String authToken, ServiceDefinition serviceDefinition ) {
        return CompletableFuture.completedFuture( false );
    }

    /**
     * Used to process any received service requests.
     *
     * @param serviceCall The ServiceCall object received from the message broker.
     */
    private void processServiceRequest( ServiceCall serviceCall ) {
        executeOwnService( serviceCall )
                .thenAcceptAsync( serviceCallResponse -> ServiceBusDispatcher.getInstance().sendServiceResponse( serviceCallResponse ) );
    }

    /**
     * Used to execute a service provided by this microservice instance.
     *
     * @param serviceCall The ServiceCall object received from the message broker.
     * @return The future completes with an updated ServiceCall object ready to be returned to the requester.
     */
    private CompletableFuture<ServiceCall> executeOwnService( ServiceCall serviceCall ) {
        return CompletableFuture.supplyAsync( () -> {
            try {
                ServiceDefinition serviceDefinition = identifyService( serviceCall );
                if ( serviceDefinition != null ) {
                    Service service = initializeService( serviceDefinition );
                    ServiceCallContext serviceCallContext = new ServiceCallContext();
                    serviceCallContext.serviceCall = serviceCall;
                    return service.executeAsync( serviceDefinition, serviceCall.getDestination().serviceParams, serviceCallContext ).join();
                } else {
                    throw new ServiceBusException( "Service definition not found!" );
                }
            } catch ( Exception exception ) {
                CompletableFuture<ServiceCallResult> future = new CompletableFuture<>();
                future.completeExceptionally( exception );
                return future.join();
            }
        } ).handleAsync( ( result, exception ) -> {
            if ( exception != null ) {
                serviceCall.setSuccessful( false );
                serviceCall.setResult( new ServiceCallResult() );
                serviceCall.getResult().setException( ( Exception ) exception );
            } else {
                serviceCall.setSuccessful( true );
                serviceCall.setResult( result );
            }
            return serviceCall;
        } ).thenApplyAsync( result -> result );
    }

    /**
     * Used to identify a service in the service interface by its alias and version (optionally).
     *
     * @param serviceCall The ServiceCall object as received from the message broker.
     * @return If the service is found this will return the ServiceDefinition object; otherwise it will return 'null'.
     */
    private ServiceDefinition identifyService( ServiceCall serviceCall ) {
        ServiceDefinition serviceDefinition = null;
        Map<Integer, ServiceDefinition> serviceVersions = serviceInterface.get( serviceCall.getDestination().serviceAlias );
        if ( serviceVersions != null ) {
            if ( serviceCall.getDestination().serviceVersion != 0 ) {
                serviceDefinition = serviceVersions.get( serviceCall.getDestination().serviceVersion );
            } else {
                Object[] versionKeys = serviceVersions.keySet().toArray();
                Arrays.sort( versionKeys );
                if ( versionKeys.length > 0 ) {
                    int newestServiceVersion = ( int ) versionKeys[ versionKeys.length - 1 ];
                    serviceDefinition = serviceVersions.get( newestServiceVersion );
                }
            }
        }
        return serviceDefinition;
    }

    /**
     * Used to initialize a Service class based on the provided ServiceDefinition.
     *
     * @param serviceDefinition The ServiceDefinition object defining the service.
     * @return The new instance of the Service class.
     * @throws ClassNotFoundException    To be handled by the caller.
     * @throws NoSuchMethodException     To be handled by the caller.
     * @throws IllegalAccessException    To be handled by the caller.
     * @throws InvocationTargetException To be handled by the caller.
     * @throws InstantiationException    To be handled by the caller.
     */
    private Service initializeService( ServiceDefinition serviceDefinition ) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<?> microserviceClass = Class.forName( serviceDefinition.serviceClassName );
        Constructor<?> constructor = microserviceClass.getConstructor();
        return ( Service ) constructor.newInstance();
    }
}
