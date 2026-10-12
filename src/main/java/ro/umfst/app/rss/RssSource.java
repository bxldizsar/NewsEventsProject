/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ro.umfst.app.rss;

/**
 *
 * @author boldi
 */
//one rss feed from rss.json: a name and the adress of the feed 

public class RssSource {
    private final String name;
    private final String url;
    
    public RssSource(String name,String url)
    {
        if (name == null || name.isBlank())
        {
            throw new IllegalArgumentException("RSS source name is required");
        }
        if (url == null || url.isBlank())
        {
            throw new IllegalArgumentException("RSS source url is required");
        }
        this.name = name.trim();
        this.url = url.trim();
    }
    public String getName()
    {
        return name;
    }
    public String getUrl()
    {
        return url;
    }
    @Override
    public String toString()
    {
        return name;
    }
    
}
