package com.softwaregroup.digiwave.eip.components.communication;

/**
 * A singleton class handling the connectors operation and behavior.
 *
 * @author Boris Kostadinov
 * @version 1.0
 * @since 2019.1.0
 */
final class ConnectorManager {
    private static ConnectorManager instance;

    private ConnectorManager() {
    }

    /**
     * Singleton instance getter.
     *
     * @return The ConnectorManager instance.
     */
    static synchronized ConnectorManager getInstance() {
        if ( instance == null ) {
            instance = new ConnectorManager();
        }
        return instance;
    }
}
