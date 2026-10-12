package ro.umfst.app;

import javax.swing.SwingUtilities;
import ro.umfst.app.ui.MainWindow;

public class Main {

    public static void main(String[] args) {
        // Swing windows must be created on Swing's own thread
        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }
}