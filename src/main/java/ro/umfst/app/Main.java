package ro.umfst.app;

import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import ro.umfst.app.database.Database;
import ro.umfst.app.database.NewsRepository;
import ro.umfst.app.database.SQLiteNewsRepository;
import ro.umfst.app.service.NewsService;
import ro.umfst.app.service.RssImporter;
import ro.umfst.app.ui.MainWindow;

public class Main {

    public static void main(String[] args) 
    {
        // Swing windows must be created on Swing's own thread
        SwingUtilities.invokeLater(() -> 
        {
            try {
                // The connection stays open while the program runs
                Connection connection = Database.getConnection();
                NewsRepository repository = new SQLiteNewsRepository(connection);

                NewsService newsService = new NewsService(repository);
                RssImporter rssImporter = new RssImporter(repository);

                MainWindow window = new MainWindow(newsService, rssImporter);
                window.setVisible(true);
            } catch (SQLException hiba) {
                JOptionPane.showMessageDialog(null, "Could not open the database: " + hiba.getMessage(),
                        "Database error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}