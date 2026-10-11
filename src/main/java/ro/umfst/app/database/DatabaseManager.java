/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ro.umfst.app.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    // One shared connection, used by every query in this class
    private static Connection connection;

    // Opens the connection and makes sure the tables exist
    public static void init() throws SQLException {
        connection = Database.getConnection();
        createTables();
    }

    private static void createTables() throws SQLException {
        String newsQuery = """
                CREATE TABLE IF NOT EXISTS news (
                    title TEXT NOT NULL,
                    description TEXT,
                    source TEXT NOT NULL,
                    author TEXT,
                    publisher TEXT,
                    publish_date TIMESTAMP,
                    url TEXT NOT NULL,
                    creation_date TIMESTAMP NOT NULL,
                    last_modified_date TIMESTAMP NOT NULL,
                    language TEXT NOT NULL,
                    cover_image TEXT,
                    PRIMARY KEY (source, url)
                )
                """;

        String eventsQuery = """
                CREATE TABLE IF NOT EXISTS events (
                    title TEXT NOT NULL,
                    description TEXT,
                    source TEXT,
                    author TEXT,
                    publisher TEXT,
                    publish_date TIMESTAMP,
                    url TEXT,
                    creation_date TIMESTAMP NOT NULL,
                    last_modified_date TIMESTAMP NOT NULL,
                    language TEXT NOT NULL,
                    cover_image TEXT,
                    event_datetime TIMESTAMP NOT NULL,
                    location TEXT NOT NULL,
                    PRIMARY KEY (title, event_datetime, location)
                )
                """;

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(newsQuery);
            statement.executeUpdate(eventsQuery);
        }
    }
}