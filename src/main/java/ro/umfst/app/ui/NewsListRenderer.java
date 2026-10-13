/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ro.umfst.app.ui;
import java.awt.Component;
import java.awt.Font;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import ro.umfst.app.model.News;
/**
 *
 * @author boldi
 */
public class NewsListRenderer extends DefaultListCellRenderer 
{
 
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        // let the parent do the normal work first: text (the title), colors, selection
        JLabel label = (JLabel) super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus);

        // in our list every item is a News object
        News news = (News) value;

        // deriveFont = the same font, only with a different style
        if (news.isRead()) 
        {
            label.setFont(label.getFont().deriveFont(Font.PLAIN));
        } else 
        {
            label.setFont(label.getFont().deriveFont(Font.BOLD));
        }
        return label;
    }
}
