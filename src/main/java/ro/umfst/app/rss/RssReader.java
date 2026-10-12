/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ro.umfst.app.rss;
import java.io.ByteArrayInputStream;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import ro.umfst.app.model.News;
import ro.umfst.app.network.HttpHelper;
/**
 *
 * @author boldi
 */
// downloads an rss feed and turns its items into news objects
public class RssReader 
{
    public static List<News> read(RssSource source) throws Exception 
    {
        // download the xml and build a tree from it
        byte[] xml = HttpHelper.download(source.getUrl());
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = builder.parse(new ByteArrayInputStream(xml));

        // the language of the whole feed, e.g. "hu" or "ro-RO" -> "HU" or "RO"
        String languageText = getText(document.getDocumentElement(), "language");
        if (languageText == null) 
        {
            throw new Exception("The feed has no <language>");
        }
        String language = languageText.substring(0, 2).toUpperCase();

        // every <item> becomes a news object
        List<News> newsList = new ArrayList<>();
        NodeList items = document.getElementsByTagName("item");

        for (int i = 0; i < items.getLength(); i++) 
        {
            Element item = (Element) items.item(i);
            try 
            {
                News news = new News(
                        getText(item, "title"),
                        getText(item, "description"),
                        source.getName(),
                        getText(item, "dc:creator"),
                        null,
                        parseDate(getText(item, "pubDate")),
                        getText(item, "link"),
                        language,
                        null, null, null);
                newsList.add(news);
            } catch (IllegalArgumentException hiba) 
            {
                // an item without a title or link is skipped, the others are kept
                System.out.println("Skipped an item from " + source.getName() + ": " + hiba.getMessage());
            }
        }
        return newsList;
    }

    // returns the text inside the first <tagName> element, or null if there is none
    private static String getText(Element parent, String tagName) 
    {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) 
        {
            return null;
        }
        return nodes.item(0).getTextContent().trim();
    }

    //an invalid date becomes null
    private static Timestamp parseDate(String text) 
    {
        try 
        {
            ZonedDateTime date = ZonedDateTime.parse(text, DateTimeFormatter.RFC_1123_DATE_TIME);
            return Timestamp.from(date.toInstant());
        } catch (Exception hiba) 
        {
            return null;
        }
    }
    
}
