package ro.umfst.app;

import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import ro.umfst.app.database.Database;
import ro.umfst.app.database.SQLiteNewsRepository;
import ro.umfst.app.service.NewsService;
import ro.umfst.app.ui.MainWindow;

public class Main {

    public static void main(String[] args) {
        // Swing windows must be created on Swing's own thread
        SwingUtilities.invokeLater(() -> {
            try {
                // The connection stays open while the program runs
                Connection connection = Database.getConnection();
                NewsService newsService = new NewsService(new SQLiteNewsRepository(connection));

                MainWindow window = new MainWindow(newsService);
                window.setVisible(true);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Could not open the database: " + e.getMessage(),
                        "Database error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}