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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import ro.umfst.app.model.News;
import ro.umfst.app.network.HttpHelper;
import ro.umfst.app.network.ImageDownloader;

/**
 *
 * @author boldi
 */
public class RssReader
{
    // finds the first <img ... src="..."> in a piece of html
    private static final Pattern IMG_SRC = Pattern.compile("<img[^>]*\\ssrc=[\"']([^\"']+)[\"']");

    // downloads the feed and makes News objects from the <item> tags
    public static List<News> read(RssSource source) throws Exception
    {
        // 1. download the xml and build a tree from it
        byte[] xml = HttpHelper.download(source.getUrl());
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = builder.parse(new ByteArrayInputStream(xml));

        // 2. language of the whole feed, "hu" or "ro-RO" -> "HU" / "RO"
        String languageText = getText(document.getDocumentElement(), "language");
        if (languageText == null)
        {
            throw new Exception("The feed has no <language>");
        }
        String language = languageText.substring(0, 2).toUpperCase();

        // 3. every <item> -> one News
        List<News> newsList = new ArrayList<>();
        NodeList items = document.getElementsByTagName("item");

        for (int i = 0; i < items.getLength(); i++)
        {
            Element item = (Element) items.item(i);
            try
            {
                // the picture of the article, downloaded to res/images/news (null -> default image)
                String coverImage = ImageDownloader.saveImage(findImageUrl(item));

                News news = new News(
                        getText(item, "title"),
                        getText(item, "description"),
                        source.getName(),
                        getText(item, "dc:creator"),
                        null,
                        parseDate(getText(item, "pubDate")),
                        getText(item, "link"),
                        language,
                        coverImage,
                        null, null);
                newsList.add(news);
            }
            catch (IllegalArgumentException hiba)
            {
                // no title or no link -> skip this one, keep the rest
                System.out.println("Skipped an item from " + source.getName() + ": " + hiba.getMessage());
            }
        }
        return newsList;
    }

    // feeds put the picture in different places, so we try them one after the other
    private static String findImageUrl(Element item)
    {
        // 1. <media:content url="..."> or <media:thumbnail url="...">
        String url = getAttribute(item, "media:content", "url");
        if (url == null)
        {
            url = getAttribute(item, "media:thumbnail", "url");
        }

        // 2. <enclosure url="..." type="image/jpeg">
        if (url == null)
        {
            NodeList enclosures = item.getElementsByTagName("enclosure");
            if (enclosures.getLength() > 0)
            {
                Element enclosure = (Element) enclosures.item(0);
                if (enclosure.getAttribute("type").startsWith("image"))
                {
                    url = enclosure.getAttribute("url");
                }
            }
        }

        // 3. the first <img src="..."> inside the article html
        if (url == null)
        {
            url = findImgInHtml(getText(item, "content:encoded"));
        }
        if (url == null)
        {
            url = findImgInHtml(getText(item, "description"));
        }
        return url;
    }

    // first real <img src="..."> in the html, or null
    private static String findImgInHtml(String html)
    {
        if (html == null)
        {
            return null;
        }
        Matcher matcher = IMG_SRC.matcher(html);
        while (matcher.find())
        {
            String url = matcher.group(1).replace("&amp;", "&");
            // lazy loading sites put a tiny placeholder first (data:image/...), skip those
            if (url.startsWith("http"))
            {
                return url;
            }
        }
        return null;
    }

    // value of an attribute of the first <tagName> inside parent, or null
    private static String getAttribute(Element parent, String tagName, String attributeName)
    {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() == 0)
        {
            return null;
        }
        String value = ((Element) nodes.item(0)).getAttribute(attributeName);
        if (value.isBlank())
        {
            return null;
        }
        return value.trim();
    }

    // text inside the first <tagName>, or null if there is none
    private static String getText(Element parent, String tagName)
    {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() == 0)
        {
            return null;
        }
        return nodes.item(0).getTextContent().trim();
    }

    // "Mon, 24 Aug 2026 09:25:47 +0000" -> Timestamp, bad date -> null
    private static Timestamp parseDate(String text)
    {
        try
        {
            ZonedDateTime date = ZonedDateTime.parse(text, DateTimeFormatter.RFC_1123_DATE_TIME);
            return Timestamp.from(date.toInstant());
        }
        catch (Exception hiba)
        {
            return null;
        }
    }
}