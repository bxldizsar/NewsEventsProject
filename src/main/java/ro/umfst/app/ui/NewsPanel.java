package ro.umfst.app.ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import ro.umfst.app.model.News;

// The right side of the window: every detail of the selected news item
public class NewsPanel extends JPanel 
{

    private static final int IMAGE_WIDTH = 320;

    private final JLabel imageLabel = new JLabel();
    private final JTextArea titleArea = createTextArea(18, Font.BOLD);
    private final JLabel sourceLabel = new JLabel();
    private final JLabel dateLabel = new JLabel();
    private final JLabel urlLabel = new JLabel();
    private final JLabel historyLabel = new JLabel();
    private final JTextArea descriptionArea = createTextArea(14, Font.PLAIN);

    public NewsPanel() 
    {
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Four lines of small details under each other
        JPanel infoPanel = new JPanel(new GridLayout(0, 1));
        infoPanel.add(sourceLabel);
        infoPanel.add(dateLabel);
        infoPanel.add(urlLabel);
        infoPanel.add(historyLabel);

        JPanel titleAndInfo = new JPanel(new BorderLayout(0, 10));
        titleAndInfo.add(titleArea, BorderLayout.NORTH);
        titleAndInfo.add(infoPanel, BorderLayout.CENTER);

        // Cover image on top, then the title and the details
        JPanel header = new JPanel(new BorderLayout(0, 10));
        header.add(imageLabel, BorderLayout.NORTH);
        header.add(titleAndInfo, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
        add(new JScrollPane(descriptionArea), BorderLayout.CENTER);

        showNews(null);
    }

    // Fills the panel with the given news item; null means "nothing is selected"
    public void showNews(News news) 
    {
        if (news == null) 
        {
            imageLabel.setIcon(null);
            imageLabel.setText("");
            titleArea.setText("Select a news item on the left");
            sourceLabel.setText("");
            dateLabel.setText("");
            urlLabel.setText("");
            historyLabel.setText("");
            descriptionArea.setText("");
            return;
        }

        showImage(CoverImageResolver.resolve(news.getCoverImage()));
        titleArea.setText(news.getTitle());
        sourceLabel.setText("Source: " + news.getSource()
                + "   |   Author: " + orDash(news.getAuthor())
                + "   |   Publisher: " + orDash(news.getPublisher()));
        dateLabel.setText("Published: " + formatDate(news.getPublishDate())
                + "   |   Language: " + news.getLanguage());
        urlLabel.setText("URL: " + news.getUrl());
        historyLabel.setText("Created: " + formatDate(news.getCreationDate())
                + "   |   Last modified: " + formatDate(news.getLastModifiedDate()));

        String description = orDash(news.getDescription());
        // RSS descriptions may contain HTML tags; show only the text
        description = description.replaceAll("<[^>]*>", "").replace("&#8230;", "...");
        descriptionArea.setText(description);
        descriptionArea.setCaretPosition(0);   // scroll back to the top
    }

    // Loads the picture from the given file and shrinks it to IMAGE_WIDTH pixels wide
    private void showImage(String path) 
    {
        ImageIcon icon = new ImageIcon(path);
        // A missing or broken file gives a width of -1 instead of an error
        if (icon.getIconWidth() <= 0) 
        {
            // broken picture -> try the default one instead
            String defaultImage = CoverImageResolver.resolve(null);
            if (!path.equals(defaultImage))
            {
                showImage(defaultImage);
                return;
            }
            imageLabel.setIcon(null);
            imageLabel.setText("(no image)");
            return;
        }
        int height = icon.getIconHeight() * IMAGE_WIDTH / icon.getIconWidth();
        Image scaled = icon.getImage().getScaledInstance(IMAGE_WIDTH, height, Image.SCALE_SMOOTH);
        imageLabel.setIcon(new ImageIcon(scaled));
        imageLabel.setText("");
    }

    // A read-only text area that wraps long lines at word boundaries
    private static JTextArea createTextArea(int fontSize, int fontStyle)
    {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setOpaque(false);
        area.setFont(new Font(Font.SANS_SERIF, fontStyle, fontSize));
        return area;
    }

    private static String orDash(String text) 
    {
        if (text == null || text.isBlank()) 
        {
            return "-";
        }
        return text;
    }

    private static String formatDate(Timestamp date)
    {
        if (date == null) 
        {
            return "-";
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date);
    }
}