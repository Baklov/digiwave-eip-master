package com.softwaregroup.digiwave.eip.utils;

import java.util.Date;

public final class Logger {
    public enum Severity {
        DEFAULT,
        DEBUG,
        INFO,
        NOTICE,
        WARNING,
        ERROR,
        CRITICAL,
        ALERT,
        EMERGENCY
    }

    public enum Threads {
        ESB,
        TRACE
    }

    public static void log( String message, Severity severity, Threads thread ) {
        log( message, severity, thread, null );
    }

    public static void log( String message, Severity severity, Threads thread, Object data ) {
        Date currentDate = new Date();
        System.out.println( currentDate.toString() + " - " + severity + " - " + message );
        if ( data != null ) {
            System.out.println( "      " + data.toString() );
        }
    }
}
