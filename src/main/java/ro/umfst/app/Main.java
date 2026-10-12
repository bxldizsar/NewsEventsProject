package ro.umfst.app;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import ro.umfst.app.database.Database;
import ro.umfst.app.database.NewsRepository;
import ro.umfst.app.database.SQLiteNewsRepository;
import ro.umfst.app.model.News;

public class Main {

    public static void main(String[] args) {
        // Temporary manual test for searching news
        try (Connection connection = Database.getConnection()) {
            NewsRepository repository = new SQLiteNewsRepository(connection);

            // Two more news items, so there is something to search in
            repository.add(new News("Dog show in the city", "More than a hundred dogs took part.",
                    "Manual", null, null, Timestamp.valueOf("2026-10-11 09:00:00"),
                    "https://example.com/dogs", "HU", null, null, null));
            repository.add(new News("New bus timetable", "The buses run more often from Monday.",
                    "Manual", null, null, Timestamp.valueOf("2026-10-10 08:00:00"),
                    "https://example.com/bus", "RO", null, null, null));

            printSearch(repository, "dog");
            printSearch(repository, "BUS");
            printSearch(repository, "xyz");
            printSearch(repository, "");
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // Runs one search and prints what it found
    private static void printSearch(NewsRepository repository, String text) {
        List<News> found = repository.search(text);
        System.out.println("Search '" + text + "': " + found.size() + " result(s)");
        for (News news : found) {
            System.out.println("   - " + news.getTitle());
        }
    }
}