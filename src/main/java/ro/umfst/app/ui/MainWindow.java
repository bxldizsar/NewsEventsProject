/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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

/**
 *
 * @author boldi
 */
public class MainWindow extends JFrame
{
    private static final String RSS_CONFIG = "res/rss.json";

    // services (the window doesnt touch the db directly)
    private final NewsService newsService;
    private final RssImporter rssImporter;

    // top + bottom bar stuff
    private final JTextField searchField = new JTextField(30);
    private final JButton searchButton = new JButton("Search");
    private final JButton refreshButton = new JButton("Refresh");
    private final JButton addButton = new JButton("Add");
    private final JButton editButton = new JButton("Edit");
    private final JButton deleteButton = new JButton("Delete");
    private final JButton clearButton = new JButton("Clear DB");

    // left side list + right side details
    private final DefaultListModel<News> listModel = new DefaultListModel<>();
    private final JList<News> newsList = new JList<>(listModel);
    private final NewsPanel newsPanel = new NewsPanel();

    // loading
    private final JLabel statusLabel = new JLabel(" ");
    private final JProgressBar progressBar = new JProgressBar();

    public MainWindow(NewsService newsService, RssImporter rssImporter)
    {
        super("News & Events Manager");
        this.newsService = newsService;
        this.rssImporter = rssImporter;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null); // center of the screen

        add(createTopBar(), BorderLayout.NORTH);
        add(createCenter(), BorderLayout.CENTER);
        add(createBottomBar(), BorderLayout.SOUTH);

        newsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // unread = bold, read = normal (see NewsListRenderer)
        newsList.setCellRenderer(new NewsListRenderer());

        // clicking on a news -> show it on the right side
        newsList.addListSelectionListener(event ->
        {
            if (!event.getValueIsAdjusting())
            {
                News selected = newsList.getSelectedValue();
                newsPanel.showNews(selected);
                if (selected != null)
                {
                    markAsRead(selected);
                }
                // edit/delete only if something is selected
                editButton.setEnabled(selected != null);
                deleteButton.setEnabled(selected != null);
            }
        });

        // search with enter or with the button
        searchField.addActionListener(event -> reloadNews());
        searchButton.addActionListener(event -> reloadNews());

        addButton.addActionListener(event -> addNews());
        editButton.addActionListener(event -> editNews());
        deleteButton.addActionListener(event -> deleteNews());
        refreshButton.addActionListener(event -> refreshNews());
        clearButton.addActionListener(event -> clearDatabase());

        // load whats already in the db
        reloadNews();

        // first start, db is empty -> download
        if (listModel.isEmpty())
        {
            refreshNews();
        }
    }

    // downloads the rss feeds in the background so the window doesnt freeze
    private void refreshNews()
    {
        setLoading(true);

        SwingWorker<Integer, Void> worker = new SwingWorker<>()
        {
            @Override
            protected Integer doInBackground()
            {
                // background thread, dont touch the gui here
                List<RssSource> sources = RssSourceLoader.load(RSS_CONFIG);
                return rssImporter.importAll(sources);
            }

            @Override
            protected void done()
            {
                // back on the swing thread
                setLoading(false);
                try
                {
                    int newCount = get();
                    reloadNews();
                    String report = String.join("\n", rssImporter.getMessages());
                    JOptionPane.showMessageDialog(MainWindow.this,
                            "New news saved: " + newCount + "\n\n" + report,
                            "Refresh finished", JOptionPane.INFORMATION_MESSAGE);
                }
                catch (Exception hiba)
                {
                    // get() wraps the error, the real one is the cause
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

    private void setLoading(boolean loading)
    {
        progressBar.setVisible(loading);
        if (loading)
        {
            statusLabel.setText("Loading news... Please wait...");
        }
        else
        {
            statusLabel.setText(" ");
        }
        // no clicking around while loading
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
        // if theres an error the form comes back, so the typed text isnt lost
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
            }
            catch (RuntimeException hiba)
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
            try
            {
                newsService.editNews(selected, form.getTitleText(), form.getDescription(),
                        form.getAuthor(), form.getPublisher(), form.getLanguage(), form.getCoverImage());
                reloadNews();
                selectNews(selected);
                return;
            }
            catch (RuntimeException hiba)
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
        try
        {
            newsService.deleteNews(selected);
            reloadNews();
        }
        catch (RuntimeException hiba)
        {
            showError(hiba.getMessage());
        }
    }

    private void clearDatabase()
    {
        int answer = JOptionPane.showConfirmDialog(this,
                "Delete ALL news from the database?\nThis cannot be undone.",
                "Clear database", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION)
        {
            return;
        }
        try
        {
            int deleted = newsService.clearAll();
            reloadNews();
            JOptionPane.showMessageDialog(this, deleted + " news items deleted.",
                    "Clear database", JOptionPane.INFORMATION_MESSAGE);
        }
        catch (RuntimeException hiba)
        {
            showError(hiba.getMessage());
        }
    }

    // empty search box = all news
    private void reloadNews()
    {
        try
        {
            showInList(newsService.search(searchField.getText()));
        }
        catch (RuntimeException hiba)
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

    // after reload the objects are new, so i search by source + url
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

    // save as read + repaint so the title gets thin
    private void markAsRead(News news)
    {
        try
        {
            newsService.markAsRead(news);
            newsList.repaint();
        }
        catch (RuntimeException hiba)
        {
            showError(hiba.getMessage());
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
        splitPane.setDividerLocation(330); // ~1/3 of 1000px
        splitPane.setResizeWeight(0.33);   // keeps 1/3 - 2/3 on resize
        return splitPane;
    }

    private JPanel createBottomBar()
    {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(addButton);
        panel.add(editButton);
        panel.add(deleteButton);
        panel.add(clearButton);

        // only visible while loading
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        panel.add(progressBar);
        panel.add(statusLabel);
        return panel;
    }
}