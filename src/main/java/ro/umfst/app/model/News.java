/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ro.umfst.app.model;
import java.sql.Timestamp;
/**
 *
 * @author boldi
 */
public class News {
    private String title;   //The title of the news
    private String descripition; //The description/content of the news
    private String source; //The source of the news (the name of the RSS source)
    private String author; // The author of the news
    private String publisher; //The publisher of the news 
    private Timestamp publishDate; //The time of publication
    private String url; //The original URL of the news
    private Timestamp creationDate; //The creation date
    private Timestamp lastModifiedDate; //The last time when the database was modified
    private String language; // RO or HU
    private String coverImage; // The path of the local image
    
    public String getTitle()
    {
        return title;
    }
    public void setTitle(String title)
    {
        this.title = title;
    }
    
    public String getDescription()
    {
        return descripition;
    }
    public void setDescription(String descripition)
    {
        this.descripition = descripition;
    }
    
    public String getSource()
    {
        return source;
    }
    public void setSource(String source)
    {
        this.source = source;
    }
    
    public String getAuthor()
    {
        return author;
    }
    public void setAuthor(String author)
    {
        this.author = author;
    }
    
    public String getPublisher()
    {
        return publisher;
    }
    public void setPublisher(String publisher)
    {
        this.publisher = publisher;
    }
    
    public Timestamp getPublishDate()
    {
        return publishDate;
    }
    public void setPublishDate(Timestamp publishDate)
    {
        this.publishDate = publishDate;
    }
    
    public String getUrl()
    {
        return url;
    }
    public void setUrl(String url)
    {
        this.url = url;
    }
    
    public Timestamp getCreationDate()
    {
        return creationDate;
    }
    public void setCreationDate(Timestamp creationDate)
    {
        this.creationDate = creationDate;
    }
    
    public Timestamp getLastModifiedDate()
    {
        return lastModifiedDate;
    }
    public void setLastModifiedDate(Timestamp lastModifiedDate)
    {
        this.lastModifiedDate = lastModifiedDate;
    }
    
    public String getLanguage()
    {
        return language;
    }
    public void setLanguage(String language)
    {
        this.language = language;
    }
    
    public String getCoverImage()
    {
        return coverImage;
    }
    public void setCoverImage(String coverImage)
    {
        this.coverImage = coverImage;
    }
    
    //Calls tiString() to decide what text to show for each item 
    @Override
    public String toString()
    {
        return title;
    }
    
    
}
