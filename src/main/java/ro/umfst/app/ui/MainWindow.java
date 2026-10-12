package ro.umfst.app.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import ro.umfst.app.model.News;
import ro.umfst.app.rss.RssSource;
import ro.umfst.app.rss.RssSourceLoader;
import ro.umfst.app.service.NewsService;
import ro.umfst.app.service.RssImporter;

// The main window: search bar on top, list on the left, details on the right, buttons at the bottom
public class MainWindow extends JFrame 
{

    private static final String RSS_CONFIG = "res/rss.json";

    // The window only talks to the services, never to the database directly
    private final NewsService newsService;
    private final RssImporter rssImporter;

    // The controls are fields, so the methods below can react to them
    private final JTextField searchField = new JTextField(30);
    private final JButton searchButton = new JButton("Search");
    private final JButton refreshButton = new JButton("Refresh");
    private final JButton addButton = new JButton("Add");
    private final JButton editButton = new JButton("Edit");
    private final JButton deleteButton = new JButton("Delete");
    private final JButton clearButton = new JButton("Clear DB");

    // The list on the left shows whatever is in the model
    private final DefaultListModel<News> listModel = new DefaultListModel<>();
    private final JList<News> newsList = new JList<>(listModel);
    private final NewsPanel newsPanel = new NewsPanel();

    // Shown at the bottom while the RSS feeds are being downloaded
    private final JLabel statusLabel = new JLabel(" ");
    private final JProgressBar progressBar = new JProgressBar();

    public MainWindow(NewsService newsService, RssImporter rssImporter) 
    {
        super("News & Events Manager");
        this.newsService = newsService;
        this.rssImporter = rssImporter;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);   // open in the middle of the screen

        add(createTopBar(), BorderLayout.NORTH);
        add(createCenter(), BorderLayout.CENTER);
        add(createBottomBar(), BorderLayout.SOUTH);

        // When the user selects a news item, show its details on the right
        newsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        newsList.addListSelectionListener(event -> 
        {
            if (!event.getValueIsAdjusting()) 
            {
                News selected = newsList.getSelectedValue();
                newsPanel.showNews(selected);
                // Edit and Delete only make sense when something is selected
                editButton.setEnabled(selected != null);
                deleteButton.setEnabled(selected != null);
            }
        });

        // Searching: pressing Enter in the field or clicking the button
        searchField.addActionListener(event -> reloadNews());
        searchButton.addActionListener(event -> reloadNews());

        // What the buttons do
        addButton.addActionListener(event -> addNews());
        editButton.addActionListener(event -> editNews());
        deleteButton.addActionListener(event -> deleteNews());
        refreshButton.addActionListener(event -> refreshNews());
        clearButton.addActionListener(event -> clearDatabase());

        // On start, show what is already saved in the database
        reloadNews();

        // First start: the database is still empty, so download the news right away
        if (listModel.isEmpty()) 
        {
            refreshNews();
        }
    }

    // Downloads every RSS feed in the background and then reloads the list
    private void refreshNews() 
    {
        setLoading(true);

        SwingWorker<Integer, Void> worker = new SwingWorker<>() 
        {
            @Override
            protected Integer doInBackground() 
            {
                // Runs on a background thread: the slow network work happens here
                List<RssSource> sources = RssSourceLoader.load(RSS_CONFIG);
                return rssImporter.importAll(sources);
            }

            @Override
            protected void done() 
            {
                // Runs on the Swing thread again: only here may the window be updated
                setLoading(false);
                try 
                {
                    int newCount = get();
                    reloadNews();
                    String report = String.join("\n", rssImporter.getMessages());
                    JOptionPane.showMessageDialog(MainWindow.this,
                            "New news saved: " + newCount + "\n\n" + report,
                            "Refresh finished", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception hiba) 
                {
                    // get() wraps the real error, so show the original message
                    Throwable cause = hiba;
                    if (hiba.getCause() != null) 
                    {
                        cause = hiba.getCause();
                    }
                    showError("Refresh failed: " + cause.getMessage());
                }
            }
        };
        worker.execute();
    }

    // Shows or hides the loading state and blocks the buttons while loading
    private void setLoading(boolean loading)
    {
        progressBar.setVisible(loading);
        if (loading) 
        {
            statusLabel.setText("Loading news... Please wait...");
        } else
        {
            statusLabel.setText(" ");
        }
        refreshButton.setEnabled(!loading);
        addButton.setEnabled(!loading);
        clearButton.setEnabled(!loading);
        searchField.setEnabled(!loading);
        searchButton.setEnabled(!loading);
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }

    private void addNews() 
    {
        NewsForm form = new NewsForm(null);
        // Show the form again after an error, so the user does not lose what they typed
        while (form.showDialog(this, "Add news")) 
        {
            try 
            {
                News news = new News(form.getTitleText(), form.getDescription(), form.getSource(),
                        form.getAuthor(), form.getPublisher(), form.getPublishDate(), form.getUrl(),
                        form.getLanguage(), form.getCoverImage(), null, null);
                newsService.addNews(news);
                reloadNews();
                selectNews(news);
                return;
            } catch (RuntimeException hiba) 
            {
                showError(hiba.getMessage());
            }
        }
    }

    private void editNews() 
    {
        News selected = newsList.getSelectedValue();
        NewsForm form = new NewsForm(selected);
        while (form.showDialog(this, "Edit news"))
        {
            try {
                newsService.editNews(selected, form.getTitleText(), form.getDescription(),
                        form.getAuthor(), form.getPublisher(), form.getLanguage(), form.getCoverImage());
                reloadNews();
                selectNews(selected);
                return;
            } catch (RuntimeException hiba) 
            {
                showError(hiba.getMessage());
            }
        }
    }

    private void deleteNews() 
    {
        News selected = newsList.getSelectedValue();
        int answer = JOptionPane.showConfirmDialog(this,
                "Delete this news item?\n" + selected.getTitle(),
                "Delete news", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) 
        {
            return;
        }
        try {
            newsService.deleteNews(selected);
            reloadNews();
        } catch (RuntimeException hiba) {
            showError(hiba.getMessage());
        }
    }

    private void clearDatabase() {
        int answer = JOptionPane.showConfirmDialog(this,
                "Delete ALL news from the database?\nThis cannot be undone.",
                "Clear database", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            int deleted = newsService.clearAll();
            reloadNews();
            JOptionPane.showMessageDialog(this, deleted + " news items deleted.",
                    "Clear database", JOptionPane.INFORMATION_MESSAGE);
        } catch (RuntimeException hiba) 
        {
            showError(hiba.getMessage());
        }
    }

    // Loads the news that match the search box (an empty box means every news item)
    private void reloadNews() 
    {
        try {
            showInList(newsService.search(searchField.getText()));
        } catch (RuntimeException hiba) 
        {
            showError(hiba.getMessage());
        }
    }

    private void showInList(List<News> newsItems) 
    {
        listModel.clear();
        for (News news : newsItems) 
        {
            listModel.addElement(news);
        }
        newsPanel.showNews(null);
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }

    // After reloading, the list contains new objects, so the item is found by its key
    private void selectNews(News news) 
    {
        for (int i = 0; i < listModel.size(); i++) 
        {
            News item = listModel.get(i);
            if (item.getSource().equals(news.getSource()) && item.getUrl().equals(news.getUrl())) 
            {
                newsList.setSelectedIndex(i);
                newsList.ensureIndexIsVisible(i);
                return;
            }
        }
    }

    private void showError(String message) 
    {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private JPanel createTopBar() 
    {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Search:"));
        panel.add(searchField);
        panel.add(searchButton);
        panel.add(refreshButton);
        return panel;
    }

    private JSplitPane createCenter() 
    {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(newsList), newsPanel);
        splitPane.setDividerLocation(330);   // about 1/3 of the 1000 pixel wide window
        splitPane.setResizeWeight(0.33);     // keep the 1/3 - 2/3 ratio when the window is resized
        return splitPane;
    }

    private JPanel createBottomBar() 
    {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(addButton);
        panel.add(editButton);
        panel.add(deleteButton);
        panel.add(clearButton);

        // The animated bar only appears while loading
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        panel.add(progressBar);
        panel.add(statusLabel);
        return panel;
    }
}