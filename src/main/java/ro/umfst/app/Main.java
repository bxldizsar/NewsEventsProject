package ro.umfst.app;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import ro.umfst.app.database.Database;
import ro.umfst.app.database.NewsRepository;
import ro.umfst.app.database.SQLiteNewsRepository;
import ro.umfst.app.model.News;

public class Main {

    public static void main(String[] args) {
        // Temporary manual test for saving news
        try (Connection connection = Database.getConnection()) {
            NewsRepository repository = new SQLiteNewsRepository(connection);

            News news = new News("Test news", "This is a test.", "Manual", null, null,
                    Timestamp.valueOf("2026-10-12 10:00:00"), "https://example.com/test-1",
                    "HU", null, null, null);

            System.out.println("First add: " + repository.add(news));
            System.out.println("Second add: " + repository.add(news));
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
}