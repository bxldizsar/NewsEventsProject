package ro.umfst.app.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import ro.umfst.app.model.News;

// the form used by the add and edit buttons
public class NewsForm extends JPanel 
{

    private final JTextField titleField = new JTextField(35);
    private final JTextField sourceField = new JTextField(35);
    private final JTextField authorField = new JTextField(35);
    private final JTextField publisherField = new JTextField(35);
    private final JTextField publishDateField = new JTextField(35);
    private final JTextField urlField = new JTextField(35);
    private final JComboBox<String> languageBox = new JComboBox<>(new String[] {"HU", "RO"});
    private final JTextField coverImageField = new JTextField(35);
    private final JTextArea descriptionArea = new JTextArea(6, 35);

    // news == null: an empty form for a new item; otherwise the form shows that item for editing
    public NewsForm(News news) 
    {
        setLayout(new BorderLayout(0, 10));

        JPanel fields = new JPanel(new GridLayout(0, 2, 5, 5));
        addRow(fields, "Title *", titleField);
        addRow(fields, "Source *", sourceField);
        addRow(fields, "URL *", urlField);
        addRow(fields, "Author", authorField);
        addRow(fields, "Publisher", publisherField);
        addRow(fields, "Publish date (yyyy-MM-dd HH:mm)", publishDateField);
        addRow(fields, "Language", languageBox);
        addRow(fields, "Cover image path", coverImageField);

        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JPanel descriptionPanel = new JPanel(new BorderLayout(0, 5));
        descriptionPanel.add(new JLabel("Description"), BorderLayout.NORTH);
        descriptionPanel.add(new JScrollPane(descriptionArea), BorderLayout.CENTER);

        add(fields, BorderLayout.NORTH);
        add(descriptionPanel, BorderLayout.CENTER);

        if (news == null) 
        {
            sourceField.setText("Manual");
        } else 
        {
            fillFrom(news);
        }
    }

    private void fillFrom(News news) 
    {
        titleField.setText(news.getTitle());
        sourceField.setText(news.getSource());
        urlField.setText(news.getUrl());
        authorField.setText(news.getAuthor());
        publisherField.setText(news.getPublisher());
        if (news.getPublishDate() != null) 
        {
            publishDateField.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(news.getPublishDate()));
        }
        languageBox.setSelectedItem(news.getLanguage());
        coverImageField.setText(news.getCoverImage());
        descriptionArea.setText(news.getDescription());

        // The key (source + url) and the publish date cannot be changed
        sourceField.setEditable(false);
        urlField.setEditable(false);
        publishDateField.setEditable(false);
    }

    // Shows the form in a dialog; returns true if the user pressed OK
    public boolean showDialog(Component parent, String dialogTitle) 
    {
        int answer = JOptionPane.showConfirmDialog(parent, this, dialogTitle,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        return answer == JOptionPane.OK_OPTION;
    }

    public String getTitleText() 
    {
        return textOf(titleField);
    }

    public String getSource() 
    {
        return textOf(sourceField);
    }

    public String getUrl() 
    {
        return textOf(urlField);
    }

    public String getAuthor() 
    {
        return textOf(authorField);
    }

    public String getPublisher() 
    {
        return textOf(publisherField);
    }

    public String getLanguage() 
    {
        return (String) languageBox.getSelectedItem();
    }

    public String getCoverImage() 
    {
        return textOf(coverImageField);
    }

    public String getDescription() 
    {
        String text = descriptionArea.getText().trim();
        if (text.isEmpty()) 
        {
            return null;
        }
        return text;
    }

    // An empty date is allowed; a badly written one is reported to the user
    public Timestamp getPublishDate() 
    {
        String text = publishDateField.getText().trim();
        if (text.isEmpty()) 
        {
            return null;
        }
        try 
        {
            return Timestamp.valueOf(text + ":00");
        } catch (IllegalArgumentException e) 
        {
            throw new IllegalArgumentException("The publish date must look like 2026-10-20 18:30");
        }
    }

    private static void addRow(JPanel panel, String label, Component field) 
    {
        panel.add(new JLabel(label));
        panel.add(field);
    }

    // The text of a field, or null if it is empty
    private static String textOf(JTextField field) 
    {
        String text = field.getText().trim();
        if (text.isEmpty()) 
        {
            return null;
        }
        return text;
    }
}