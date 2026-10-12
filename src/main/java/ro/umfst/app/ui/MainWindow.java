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
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import ro.umfst.app.model.News;
import ro.umfst.app.service.NewsService;

// The main window: search bar on top, list on the left, details on the right, buttons at the bottom
public class MainWindow extends JFrame 
{

    // The window only talks to the service, never to the database directly
    private final NewsService newsService;

    // The controls are fields, so later steps can react to them
    private final JTextField searchField = new JTextField(30);
    private final JButton refreshButton = new JButton("Refresh");
    private final JButton addButton = new JButton("Add");
    private final JButton editButton = new JButton("Edit");
    private final JButton deleteButton = new JButton("Delete");
    private final JButton clearButton = new JButton("Clear DB");

    // The list on the left shows whatever is in the model
    private final DefaultListModel<News> listModel = new DefaultListModel<>();
    private final JList<News> newsList = new JList<>(listModel);
    private final NewsPanel newsPanel = new NewsPanel();

    public MainWindow(NewsService newsService) 
    {
        super("News & Events Manager");
        this.newsService = newsService;

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

        // What the buttons do
        addButton.addActionListener(event -> addNews());
        editButton.addActionListener(event -> editNews());
        deleteButton.addActionListener(event -> deleteNews());

        // On start, show what is already saved in the database
        reloadNews();
    }

    private void addNews() 
    {
        NewsForm form = new NewsForm(null);
        // Show the form again after an error, so the user does not lose what they typed
        while (form.showDialog(this, "Add news")) {
            try {
                News news = new News(form.getTitleText(), form.getDescription(), form.getSource(),
                        form.getAuthor(), form.getPublisher(), form.getPublishDate(), form.getUrl(),
                        form.getLanguage(), form.getCoverImage(), null, null);
                newsService.addNews(news);
                reloadNews();
                selectNews(news);
                return;
            } catch (RuntimeException e) {
                showError(e.getMessage());
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
            } catch (RuntimeException e) {
                showError(e.getMessage());
            }
        }
    }

    private void deleteNews() 
    {
        News selected = newsList.getSelectedValue();
        int answer = JOptionPane.showConfirmDialog(this,
                "Delete this news item?\n" + selected.getTitle(),
                "Delete news", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        try 
        {
            newsService.deleteNews(selected);
            reloadNews();
        } catch (RuntimeException hiba) {
            showError(hiba.getMessage());
        }
    }

    // Loads every news item from the database into the list
    private void reloadNews() 
    {
        try {
            showInList(newsService.getAllNews());
        } catch (RuntimeException hiba) {
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
        for (int i = 0; i < listModel.size(); i++) {
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
        return panel;
    }
}