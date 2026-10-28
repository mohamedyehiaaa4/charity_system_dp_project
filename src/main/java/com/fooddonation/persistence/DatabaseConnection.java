package com.fooddonation.persistence;

import java.sql.Connection;

public class DatabaseConnection {
    private static DatabaseConnection instance;

    private Connection connection;

    private DatabaseConnection() {
    }

    public static synchronized DatabaseConnection getInstance() {
        throw new UnsupportedOperationException("TODO");
    }

    public Connection getConnection() {
        throw new UnsupportedOperationException("TODO");
    }

    /** Runs schema.sql and seed.sql. */
    public void initialize() {
        throw new UnsupportedOperationException("TODO");
    }

    public void reset() {
        throw new UnsupportedOperationException("TODO");
    }

    public void close() {
        throw new UnsupportedOperationException("TODO");
    }
}
