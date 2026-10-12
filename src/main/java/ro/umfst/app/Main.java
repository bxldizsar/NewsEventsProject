package ro.umfst.app;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import ro.umfst.app.database.Database;
import ro.umfst.app.database.NewsRepository;
import ro.umfst.app.database.SQLiteNewsRepository;
import ro.umfst.app.model.News;

public class Main {

    public static void main(String[] args) {
        // Temporary manual test for updating and deleting news
        try (Connection connection = Database.getConnection()) {
            NewsRepository repository = new SQLiteNewsRepository(connection);

            List<News> allNews = repository.findAll();
            if (allNews.isEmpty()) {
                System.out.println("The database is empty, run the previous test first.");
                return;
            }

            // 1. Edit the newest news item and save the change
            News newest = allNews.get(0);
            newest.edit("Edited title", newest.getDescription(), "Boldi",
                    newest.getPublisher(), newest.getLanguage(), newest.getCoverImage());
            System.out.println("Updated: " + repository.update(newest));

            // 2. Delete the first test news by its key, then try again
            System.out.println("Deleted: " + repository.delete("Manual", "https://example.com/test-1"));
            System.out.println("Deleted again: " + repository.delete("Manual", "https://example.com/test-1"));

            // 3. Load everything again to see what is really in the database
            for (News news : repository.findAll()) {
                System.out.println(" - " + news.getTitle() + " | author: " + news.getAuthor()
                        + " | created: " + news.getCreationDate()
                        + " | modified: " + news.getLastModifiedDate());
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
}