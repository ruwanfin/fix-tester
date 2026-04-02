package com.finexa.fixtester.constant;

/**
 * Routing target constants for message filtering.
 */
public final class RoutingTarget {

    private RoutingTarget() {
        // Prevent instantiation
    }

    /**
     * Message should be processed by ALL consumers (DB + Exchange).
     */
    public static final String ALL = "ALL";

    /**
     * Message should be processed by DB consumer only.
     */
    public static final String DB_ONLY = "DB_ONLY";
}
