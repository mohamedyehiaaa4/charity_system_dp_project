package com.fooddonation.persistence;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class TestDatabase {

    private TestDatabase(){};

    public static DatabaseConnection open(Path file) throws SQLException, IOException {
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + file);
        runScript(conn, "/schema.sql");
        runScript(conn, "/seed.sql");

        DatabaseConnection db = mock(DatabaseConnection.class);
        when(db.getConnection()).thenReturn(conn);
        return db;
    }

    private static void runScript(Connection conn, String resource) throws SQLException, IOException {
        try (InputStream input = TestDatabase.class.getResourceAsStream(resource);
            Statement statement = conn.createStatement()){

                String script = new String(input.readAllBytes(), StandardCharsets.UTF_8);
                for (String sql : script.split(";")) {
                    if (!sql.isBlank()) {
                        statement.execute(sql);
                }
            }
        }
    }
}
