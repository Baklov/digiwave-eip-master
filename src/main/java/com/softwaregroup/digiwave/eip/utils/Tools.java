package com.softwaregroup.digiwave.eip.utils;

import java.time.Instant;

public final class Tools {
    private Tools() {
    }

    public static long getUnixTimestamp() {
        return Instant.now().getEpochSecond();
    }
}
