package com.softwaregroup.digiwave.eip;

import com.softwaregroup.digiwave.eip.components.servicebus.MicroserviceInstance;
import com.softwaregroup.digiwave.eip.utils.Logger;

import java.lang.reflect.Constructor;
import java.util.concurrent.*;

/**
 * Executable class used to start up a new microservice instance.
 *
 * @author Boris Kostadinov
 * @version 1.0
 * @since 2019.1.0
 */
public final class StartMicroserviceInstance {
    private static final int POOL_SIZE = 3;
    private static ExecutorService pool;

    /**
     * Entry point for the microservice instance.
     *
     * @param argv Not used at the moment.
     */
    public static void main( String[] argv ) {
        try {
            Logger.log( "Starting new microservice instance...", Logger.Severity.INFO, Logger.Threads.ESB );

            pool = Executors.newFixedThreadPool( POOL_SIZE );

            // initialize the microservice based on the provided child class:
            Class<?> microserviceClass = Class.forName( /*"com.softwaregroup.digiwave." + */System.getenv( "DIGIWAVE_SERVICE_CLASS" ) );
            Constructor<?> constructor = microserviceClass.getConstructor();
            MicroserviceInstance microservice = ( MicroserviceInstance ) constructor.newInstance();

            // once the microservice class is initialized, perform startup sequence:
            microservice.start().thenRunAsync( () -> {
                Logger.log( "Microservice instance " + MicroserviceInstance.INSTANCE_ID + " successfully started.", Logger.Severity.INFO, Logger.Threads.ESB );
            }, pool ).join();
        } catch ( Exception exception ) {
            Logger.log( "Failed to start microservice instance!", Logger.Severity.CRITICAL, Logger.Threads.ESB, exception );
            shutdown();
        }
    }

    /**
     * Used to gracefully shutdown the microservice instance.
     */
    private static void shutdown() {
        // disable new tasks from being submitted:
        pool.shutdown();
        try {
            // wait a while for existing tasks to terminate:
            if ( !pool.awaitTermination( 60, TimeUnit.SECONDS ) ) {
                // cancel currently executing tasks:
                pool.shutdownNow();
                // wait a while for tasks to respond to being cancelled:
                if ( !pool.awaitTermination( 60, TimeUnit.SECONDS ) ) {
                    Logger.log( "Microservice shutdown could not terminate the running thread pool.", Logger.Severity.WARNING, Logger.Threads.ESB );
                }
            }
        } catch ( InterruptedException exception ) {
            // (re-)cancel if current thread also interrupted:
            pool.shutdownNow();
            // preserve interrupt status:
            Thread.currentThread().interrupt();
        }
    }
}
