package com.softwaregroup.digiwave.eip.test;

import com.softwaregroup.digiwave.eip.components.servicebus.entities.ServiceAddress;
import com.softwaregroup.digiwave.eip.components.servicebus.entities.ServiceCallContext;
import com.softwaregroup.digiwave.eip.components.servicebus.ServiceConsumer;
import com.softwaregroup.digiwave.eip.components.servicebus.entities.ServiceParams;
import com.softwaregroup.digiwave.eip.utils.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Executable class used to start up a new com.softwaregroup.digiwave.eip.test executor instance.
 * Test executor is a service consumer and can be configured to call various services in the EIP microservice ecosystem.
 * NOTE: To run this MS you need to supply the following ENV variables:
 * DIGIWAVE_SERVICE_DOMAIN_NAME=testExecutor
 *
 * @author Boris Kostadinov
 * @version 1.0
 * @since 2019.1.0
 */
public final class TestExecutor extends ServiceConsumer {
    /**
     * Entry point for the microservice instance.
     *
     * @param argv Not used at the moment.
     */
    public static void main( String[] argv ) {
        TestExecutor testExecutor = new TestExecutor();
        testExecutor.start().thenRunAsync( () -> {
            // NOTE: This is a testing run for the MQ communication. To be (re)moved after testing.
            final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool( 1 );
            scheduler.scheduleAtFixedRate( () -> {
                ServiceAddress serviceAddress = new ServiceAddress();
                serviceAddress.serviceAlias = "service1";
                serviceAddress.serviceDomainName = "testService1";
                serviceAddress.serviceVersion = 1;
                ServiceCallContext serviceCallContext = new ServiceCallContext();
                ServiceParams serviceParams = new ServiceParams();

                for ( int idx = 0; idx < 5; idx++ ) {
                    testExecutor.callServiceAsync( serviceAddress, serviceParams, serviceCallContext ).thenAccept( result -> {
                        if ( result.getException() != null ) {
                            Logger.log( "Scheduled service call result error: " + result.getException(), Logger.Severity.DEBUG, Logger.Threads.ESB );
                        } else {
                            Logger.log( "Scheduled service call result success: " + result.getPayload().get( "testParam" ), Logger.Severity.DEBUG, Logger.Threads.ESB );
                        }
                    } );
                }
            }, 2, 10, TimeUnit.SECONDS );

            Logger.log( "TestExecutor instance successfully started.", Logger.Severity.INFO, Logger.Threads.ESB );
        } ).join();
    }
}
