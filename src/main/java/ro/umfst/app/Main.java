package ro.umfst.app;

import java.sql.Connection;
import java.sql.SQLException;
import ro.umfst.app.database.Database;
import ro.umfst.app.database.SQLiteNewsRepository;
import ro.umfst.app.model.News;
import ro.umfst.app.service.NewsService;

public class Main {

    public static void main(String[] args) {
        // Temporary manual test for the service layer
        try (Connection connection = Database.getConnection()) {
            NewsService service = new NewsService(new SQLiteNewsRepository(connection));

            // 1. Reading: an empty search must give the same result as "all news"
            System.out.println("All news: " + service.getAllNews().size());
            System.out.println("Empty search: " + service.search("   ").size());

            // 2. Adding: the second attempt must be rejected with a clear message
            News news = new News("Service test", "Added through the service.", "Manual",
                    null, null, null, "https://example.com/service", "HU", null, null, null);
            for (int attempt = 1; attempt <= 2; attempt++) {
                try {
                    service.addNews(news);
                    System.out.println("Added: " + news);
                } catch (IllegalArgumentException e) {
                    System.out.println("Not added: " + e.getMessage());
                }
            }

            // 3. Editing: an invalid edit is rejected, a valid one is saved
            try {
                service.editNews(news, "", null, null, null, "HU", null);
            } catch (IllegalArgumentException e) {
                System.out.println("Rejected edit: " + e.getMessage());
            }
            service.editNews(news, "Service test (edited)", "Added through the service.",
                    "Boldi", null, "HU", null);
            System.out.println("After edit: " + service.search("service").get(0));

            // 4. Deleting
            service.deleteNews(news);
            System.out.println("After delete: " + service.search("service").size()
                    + " result(s) for 'service'");
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
}