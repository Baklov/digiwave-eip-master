package com.softwaregroup.digiwave.eip.components.servicebus;

import com.rabbitmq.client.*;
import com.rabbitmq.client.Channel;
import com.softwaregroup.digiwave.eip.components.servicebus.entities.ServiceCall;
import com.softwaregroup.digiwave.eip.components.servicebus.entities.ServiceCallDestination;
import com.softwaregroup.digiwave.eip.components.servicebus.entities.ServiceCallResult;
import com.softwaregroup.digiwave.eip.components.servicebus.entities.ServiceCallSource;
import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.rabbitmq.RabbitMQComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.spi.DataFormat;
import org.apache.commons.lang3.SerializationUtils;
import com.softwaregroup.digiwave.eip.utils.Config;
import com.softwaregroup.digiwave.eip.utils.Logger;
import com.softwaregroup.digiwave.eip.utils.Tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/**
 * A singleton class handling the service call internal dispatching between the microservices.
 *
 * @author Boris Kostadinov
 * @version 1.0
 * @since 2019.1.0
 */
final class ServiceBusDispatcher {
    // TODO: properties to be taken from config or ENV
    private static final String MSG_BROKER_HOST = "localhost";
    private static final int MSG_BROKER_PORT = 5672;
    private static final String MSG_BROKER_USER = "guest";
    private static final String MSG_BROKER_PASS = "guest";

    private static final String MSG_BROKER_QUEUE_REQUESTS_IN = MicroserviceInstance.SERVICE_DOMAIN_NAME;
    private static final String MSG_BROKER_QUEUE_RESPONSES_IN = MicroserviceInstance.SERVICE_DOMAIN_NAME + "-" + MicroserviceInstance.INSTANCE_ID;
    private static final String MSG_BROKER_QUEUE_RESPONSES_OUT = MicroserviceInstance.SERVICE_DOMAIN_NAME + "-" + MicroserviceInstance.INSTANCE_ID + "-completed";
    private static final String DESTINATION_QUEUE_HEADER = "dynamic-destination";
    private static ServiceBusDispatcher instance;

    private CamelContext camelContext;
    private final int serviceCallCapacity = Config.getSetting( "serviceCallCapacity", 0 );

    private Map<String, CompletableFuture<ServiceCallResult>> taskHandlers;
    private int serviceCallsInProcessing = 0;

    private ServiceBusDispatcher() {
        taskHandlers = new HashMap<>();
    }

    /**
     * Singleton instance getter.
     *
     * @return The ServiceBusDispatcher instance.
     */
    static synchronized ServiceBusDispatcher getInstance() {
        if ( instance == null ) {
            instance = new ServiceBusDispatcher();
            instance.initialize();
        }
        return instance;
    }

    //region Communication Exchange

    /**
     * Data formatter used to serialize and deserialize the ServiceCall object when it's sent and received from the message broker.
     */
    private class ServiceCallFormat implements DataFormat {
        /**
         * This method should be used when a ServiceCall object is about to be sent to the message broker. It will
         * serialize the ServiceCall and make it ready for dispatch.
         *
         * @param exchange The Camel exchange object.
         * @param graph    The ServiceCall object itself.
         * @param stream   The output stream where to send the serialized object.
         * @throws Exception Will be handled by Camel flow.
         */
        public void marshal( Exchange exchange, Object graph, OutputStream stream ) throws Exception {
            stream.write( SerializationUtils.serialize( ( ServiceCall ) graph ) );
        }

        /**
         * This method should be used when a ServiceCall object is received from the message broker. It will
         * deserialize the ServiceCall and make it ready for usage by the application.
         *
         * @param exchange The Camel exchange object.
         * @param stream   The input stream where to receive the serialized object.
         * @return The deserialized ServiceCall object.
         * @throws Exception Will be handled by Camel flow.
         */
        public ServiceCall unmarshal( Exchange exchange, InputStream stream ) throws Exception {
            return SerializationUtils.deserialize( stream.readAllBytes() );
        }
    }

    /**
     * A specialized RouteBuilder for sending service requests.
     * NOTE: By default this will be automatically enabled by the ServiceConsumer class on startup.
     */
    private class ServiceRequestSender extends RouteBuilder {
        /**
         * Base configuration method.
         * It will implement all Camel routes that are necessary for sending service requests.
         */
        @Override
        public void configure() {
            // nothing here for now...
        }
    }

    /**
     * A specialized RouteBuilder for receiving service requests.
     * NOTE: By default this will be automatically enabled by the ServiceProvider class on startup.
     */
    private class ServiceRequestReceiver extends RouteBuilder {
        private Processor processor;

        ServiceRequestReceiver( Processor processor ) {
            this.processor = processor;
        }

        /**
         * Base configuration method.
         * It will implement all Camel routes that are necessary for receiving service requests.
         */
        @Override
        public void configure() {
            // route from inbound requests queue to internal processing:
            from( ServiceBusDispatcher.assembleMsgBrokerAddress( MSG_BROKER_QUEUE_REQUESTS_IN ) )
                    .unmarshal( new ServiceCallFormat() )
                    .choice()
                    .when( exchange -> {
                        ServiceCall serviceCall = exchange.getIn().getBody( ServiceCall.class );
                        return ( serviceCallCapacity == 0 || getServiceCallsCount() < serviceCallCapacity || serviceCall.getPredecessor() != null );
                    } )
                    .to( "direct:process-service-request" )
                    .otherwise()
                    .to( "direct:reject-service-request" );

            // internal route for initiating the service request processing:
            from( "direct:process-service-request" )
                    .process( processor );

            // internal route for rejecting the service request due to heavy load:
            from( "direct:reject-service-request" )
                    .process( exchange -> {
                        ServiceCall serviceCall = exchange.getIn().getBody( ServiceCall.class );
                        Logger.log( "Service call processing capacity of " + serviceCallCapacity + " reached! Current load is: " + getServiceCallsCount(), Logger.Severity.NOTICE, Logger.Threads.ESB );
                        exchange.getIn().setHeader( DESTINATION_QUEUE_HEADER, ServiceBusDispatcher.assembleMsgBrokerAddress( serviceCall.getDestination().serviceDomainName ) );
                    } )
                    .marshal( new ServiceCallFormat() )
                    .toD( "${header." + DESTINATION_QUEUE_HEADER + "}" );
        }
    }

    /**
     * A specialized RouteBuilder for sending service responses.
     * NOTE: By default this will be automatically enabled by the ServiceProvider class on startup.
     */
    private class ServiceResponseSender extends RouteBuilder {
        /**
         * Base configuration method.
         * It will implement all Camel routes that are necessary for sending service responses.
         */
        @Override
        public void configure() {
            // route from private response queue to the dynamic source queue:
            from( ServiceBusDispatcher.assembleMsgBrokerAddress( MSG_BROKER_QUEUE_RESPONSES_OUT ) )
                    .removeHeaders( "*" )
                    .unmarshal( new ServiceCallFormat() )
                    .process( exchange -> {
                        ServiceCall serviceCall = exchange.getIn().getBody( ServiceCall.class );
                        exchange.getIn().setHeader( DESTINATION_QUEUE_HEADER, ServiceBusDispatcher.assembleMsgBrokerAddress( serviceCall.getSource().serviceDomainName + "-" + serviceCall.getSource().instanceID ) );
                    } )
                    .marshal( new ServiceCallFormat() )
                    .toD( "${header." + DESTINATION_QUEUE_HEADER + "}" );
        }
    }

    /**
     * A specialized RouteBuilder for receiving service responses.
     * NOTE: By default this will be automatically enabled by the ServiceConsumer class on startup.
     */
    private class ServiceResponseReceiver extends RouteBuilder {
        private Processor processor;

        ServiceResponseReceiver( Processor processor ) {
            this.processor = processor;
        }

        /**
         * Base configuration method.
         * It will implement all Camel routes that are necessary for receiving service responses.
         */
        @Override
        public void configure() {
            // route from private queue for received responses to internal processing:
            from( ServiceBusDispatcher.assembleMsgBrokerAddress( MSG_BROKER_QUEUE_RESPONSES_IN ) )
                    .unmarshal( new ServiceCallFormat() )
                    .process( processor );
        }
    }

    /**
     * Used to assemble a uniform Camel address for the message broker. The only variable here is the name of the queue.
     *
     * @param queue The destination queue.
     * @return The destination address as expected by Camel.
     */
    private static String assembleMsgBrokerAddress( String queue ) {
        String address = "rabbitmq:" + queue + "?queue=" + queue;
        address += "&durable=true";
        address += "&autoAck=false";
        address += "&autoDelete=true";
        address += "&guaranteedDeliveries=true";
        return address;
    }

    /**
     * Used to send a service call request.
     *
     * @param serviceCall A ServiceCall object ready for dispatching.
     * @return After completing the future will provide the service call result.
     */
    CompletableFuture<ServiceCallResult> sendServiceRequest( ServiceCall serviceCall ) {
        try {
            serviceCall.setLastTaskSeq( serviceCall.getLastTaskSeq() + 1 );
            enqueueServiceCall( serviceCall, serviceCall.getDestination().serviceDomainName );
            Logger.log( "SendServiceRequest: enqueued pending service call for transaction '" + serviceCall.getTransactionID() + "'", Logger.Severity.DEBUG, Logger.Threads.TRACE, serviceCall );

            String taskID = serviceCall.getServiceCallID() + "." + serviceCall.getLastTaskSeq();
            addTaskHandler( taskID, new CompletableFuture<>() );

            return getTaskHandler( taskID );
        } catch ( Exception exception ) {
            ServiceCallResult result = new ServiceCallResult();
            result.setException( exception );
            return CompletableFuture.completedFuture( result );
        }
    }

    /**
     * Used to send a service call response.
     *
     * @param serviceCall A ServiceCall object ready for dispatching.
     */
    void sendServiceResponse( ServiceCall serviceCall ) {
        try {
            // update the instance ID so we can track which instance processed this request:
            serviceCall.getDestination().instanceID = MicroserviceInstance.INSTANCE_ID;
            enqueueServiceCall( serviceCall, MSG_BROKER_QUEUE_RESPONSES_OUT );
            Logger.log( "SendServiceResponse: enqueued processed service call for transaction '" + serviceCall.getTransactionID() + "'", Logger.Severity.DEBUG, Logger.Threads.TRACE, serviceCall );
            decreaseServiceCallCount();
        } catch ( Exception exception ) {
            Logger.log( "Error while trying to enqueue a service call response for transaction '" + serviceCall.getTransactionID() + "'!", Logger.Severity.ERROR, Logger.Threads.ESB, exception );
        }
    }

    //endregion

    //region Initialization

    /**
     * Used to initialize the Camel context and any related items. Will be executed once when the instance is first created.
     */
    private void initialize() {
        try {
            camelContext = new DefaultCamelContext();

            ConnectionFactory messageBrokerConnection = new ConnectionFactory();
            messageBrokerConnection.setHost( MSG_BROKER_HOST );
            messageBrokerConnection.setPort( MSG_BROKER_PORT );
            messageBrokerConnection.setUsername( MSG_BROKER_USER );
            messageBrokerConnection.setPassword( MSG_BROKER_PASS );

            ( ( RabbitMQComponent ) camelContext.getComponent( "rabbitmq" ) ).setConnectionFactory( messageBrokerConnection );

            camelContext.start();
        } catch ( Exception exception ) {
            Logger.log( "Failed to start Apache Camel context.", Logger.Severity.ERROR, Logger.Threads.ESB, exception );
        }
    }

    /**
     * Used to initialize the dispatcher for ServiceProvider behavior.
     * NOTE: By default this is called by the ServiceProvider class upon starting.
     *
     * @param processServiceRequest A method that will be used to process incoming service requests.
     * @throws Exception Should be handled by the caller.
     */
    void configureServiceProvider( Consumer<ServiceCall> processServiceRequest ) throws Exception {
        camelContext.addRoutes( new ServiceRequestReceiver( exchange -> {
            ServiceCall serviceCall = exchange.getIn().getBody( ServiceCall.class );
            Logger.log( "ServiceRequestReceiver: received pending service call for transaction '" + serviceCall.getTransactionID() + "'", Logger.Severity.DEBUG, Logger.Threads.TRACE, serviceCall );
            increaseServiceCallsCount();
            processServiceRequest.accept( serviceCall );
        } ) );
        camelContext.addRoutes( new ServiceResponseSender() );
    }

    /**
     * Used to initialize the dispatcher for ServiceConsumer behavior.
     * NOTE: By default this is called by the ServiceConsumer class upon starting.
     *
     * @throws Exception Should be handled by the caller.
     */
    void configureServiceConsumer() throws Exception {
        camelContext.addRoutes( new ServiceResponseReceiver( exchange -> {
            ServiceCall serviceCall = exchange.getIn().getBody( ServiceCall.class );
            Logger.log( "ServiceResponseReceiver: received processed service call for transaction '" + serviceCall.getTransactionID() + "'", Logger.Severity.DEBUG, Logger.Threads.TRACE, serviceCall );
            completeServiceCall( serviceCall );
        } ) );
        camelContext.addRoutes( new ServiceRequestSender() );
    }

    /**
     * Used to gracefully shutdown the dispatcher.
     * NOTE: By default this is called by the ServiceConsumer class upon stopping.
     */
    void shutDown() {
        try {
            camelContext.stop();
        } catch ( Exception exception ) {
            Logger.log( "Failed to stop Apache Camel context.", Logger.Severity.ERROR, Logger.Threads.ESB, exception );
        }
    }

    //endregion

    //region Utility Methods

    /**
     * Utility method used to create a new ServiceCall object with proper initialization.
     *
     * @param transactionID The ID of the parent service bus transaction.
     * @param level         The level of the service call in terms of its position in the call tree.
     * @param source        The source of the service call.
     * @param destination   The destination of the service call.
     * @return The new ServiceCall object.
     */
    ServiceCall createServiceCall( String transactionID, int level, ServiceCallSource source, ServiceCallDestination destination ) {
        ServiceCall serviceCall = new ServiceCall();
        serviceCall.setServiceCallID( "TSC-" + UUID.randomUUID().toString() );
        serviceCall.setTransactionID( transactionID );
        serviceCall.setCreatedOn( Tools.getUnixTimestamp() );
        serviceCall.setLevel( level );
        serviceCall.setCompleted( false );
        serviceCall.setPredecessor( null );
        serviceCall.setSuccessors( null );
        serviceCall.setLastTaskSeq( 0 );
        serviceCall.setSource( source );
        serviceCall.setDestination( destination );
        serviceCall.setExecutionTime( 0 );
        return serviceCall;
    }

    /**
     * Increment the current count of service calls in processing.
     */
    private synchronized void increaseServiceCallsCount() {
        serviceCallsInProcessing++;
    }

    /**
     * Decrement the current count of service calls in processing.
     */
    private synchronized void decreaseServiceCallCount() {
        serviceCallsInProcessing--;
        if ( serviceCallsInProcessing < 0 ) {
            serviceCallsInProcessing = 0;
        }
    }

    /**
     * Get the current count of service calls in processing.
     */
    private synchronized int getServiceCallsCount() {
        return serviceCallsInProcessing;
    }

    /**
     * Used to add a new internal task handler.
     *
     * @param taskID      The unique ID of the task.
     * @param taskHandler The completable future object corresponding to the task.
     */
    private synchronized void addTaskHandler( String taskID, CompletableFuture<ServiceCallResult> taskHandler ) {
        taskHandlers.put( taskID, taskHandler );
    }

    /**
     * Used to get an existing task handler.
     *
     * @param taskID The unique ID of the task.
     * @return The completable future object corresponding to the task.
     */
    private synchronized CompletableFuture<ServiceCallResult> getTaskHandler( String taskID ) {
        return taskHandlers.get( taskID );
    }

    /**
     * Used to remove an existing task handler - usually after it was once completed.
     *
     * @param taskID The unique ID of the task.
     */
    private synchronized void removeTaskHandler( String taskID ) {
        taskHandlers.remove( taskID );
    }

    /**
     * Used to enqueue a ServiceCall to the message broker for processing.
     *
     * @param serviceCall The ServiceCall object to enqueue (will be serialized).
     * @param destination The destination queue to which to send the ServiceCall.
     * @throws IOException      To be handled by caller.
     * @throws TimeoutException To be handled by caller.
     */
    private void enqueueServiceCall( ServiceCall serviceCall, String destination ) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost( MSG_BROKER_HOST );
        try ( Connection connection = factory.newConnection(); Channel channel = connection.createChannel() ) {
            channel.queueDeclare( destination, true, false, true, null );
            channel.basicPublish( "", destination, MessageProperties.PERSISTENT_BASIC, SerializationUtils.serialize( serviceCall ) );
        }
    }

    /**
     * Used to complete a ServiceCall and executeAsync its handler.
     *
     * @param serviceCall The ServiceCall object to complete.
     */
    private void completeServiceCall( ServiceCall serviceCall ) {
        String taskID = serviceCall.getServiceCallID() + "." + serviceCall.getLastTaskSeq();
        serviceCall.setLastTaskSeq( serviceCall.getLastTaskSeq() - 1 );
        serviceCall.setFinishedOn( Tools.getUnixTimestamp() );
        serviceCall.setCompleted( true );
        CompletableFuture<ServiceCallResult> taskHandler = getTaskHandler( taskID );
        if ( taskHandler != null ) {
            taskHandler.complete( serviceCall.getResult() );
            removeTaskHandler( taskID );
        } else {
            Logger.log( "No handler found for a service call task with ID '" + taskID + "'.", Logger.Severity.ERROR, Logger.Threads.ESB, serviceCall );
        }
    }

    //endregion
}
