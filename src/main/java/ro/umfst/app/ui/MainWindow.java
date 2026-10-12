/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ro.umfst.app.ui;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 *
 * @author boldi
 */
// the main window: search bar on top, list on the left, details on the right, buttons at the bottom
public class MainWindow extends JFrame {
    // The controls are fields, so later steps can react to them
    private final JTextField searchField = new JTextField(30);
    private final JButton refreshButton = new JButton("Refresh");
    private final JButton addButton = new JButton("Add");
    private final JButton editButton = new JButton("Edit");
    private final JButton deleteButton = new JButton("Delete");
    private final JButton clearButton = new JButton("Clear DB");

    public MainWindow() 
    {
        super("News & Events Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);   // open in the middle of the screen

        add(createTopBar(), BorderLayout.NORTH);
        add(createCenter(), BorderLayout.CENTER);
        add(createBottomBar(), BorderLayout.SOUTH);
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
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.add(new JLabel("News list", SwingConstants.CENTER), BorderLayout.CENTER);

        JPanel detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.add(new JLabel("Select a news item on the left", SwingConstants.CENTER),
                BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listPanel, detailsPanel);
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
