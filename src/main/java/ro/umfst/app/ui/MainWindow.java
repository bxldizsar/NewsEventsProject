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
                newsPanel.showNews(newsList.getSelectedValue());
            }
        });

        // On start, show what is already saved in the database
        reloadNews();
    }

    // Loads every news item from the database into the list
    private void reloadNews() 
    {
        try
        {
            showInList(newsService.getAllNews());
        } catch (RuntimeException hiba) 
        {
            
            JOptionPane.showMessageDialog(this, hiba.getMessage(), "Database error",
                    JOptionPane.ERROR_MESSAGE);
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
        splitPane.setDividerLocation(330);   
        splitPane.setResizeWeight(0.33);     
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