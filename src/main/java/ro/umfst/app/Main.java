package ro.umfst.app;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import ro.umfst.app.database.Database;
import ro.umfst.app.database.NewsRepository;
import ro.umfst.app.database.SQLiteNewsRepository;
import ro.umfst.app.model.News;
import ro.umfst.app.rss.RssSource;
import ro.umfst.app.rss.RssSourceLoader;
import ro.umfst.app.service.RssImporter;

public class Main {

    public static void main(String[] args) {
        // Temporary manual test: import every RSS source into the database
        try (Connection connection = Database.getConnection()) {
            NewsRepository repository = new SQLiteNewsRepository(connection);
            RssImporter importer = new RssImporter(repository);

            List<RssSource> sources = RssSourceLoader.load("res/rss.json");
            int newCount = importer.importAll(sources);

            for (String message : importer.getMessages()) {
                System.out.println(message);
            }
            System.out.println("New news saved: " + newCount);

            // What is in the database now
            List<News> allNews = repository.findAll();
            System.out.println("News in the database: " + allNews.size());
            for (int i = 0; i < 3 && i < allNews.size(); i++) {
                News news = allNews.get(i);
                System.out.println("   " + news.getTitle() + " | " + news.getSource()
                        + " | " + news.getPublishDate());
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
}