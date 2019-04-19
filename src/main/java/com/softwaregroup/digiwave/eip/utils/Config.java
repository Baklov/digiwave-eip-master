package com.softwaregroup.digiwave.eip.utils;

public final class Config {
    private Config() {
    }

    public static <T> T getSetting( String name ) {
        return getSetting( name, null );
    }

    public static <T> T getSetting( String name, T defaultValue ) {
        return defaultValue;
    }
}
