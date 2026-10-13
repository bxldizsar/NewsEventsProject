/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ro.umfst.app.service;
import java.util.ArrayList;
import java.util.List;
import ro.umfst.app.database.NewsRepository;
import ro.umfst.app.model.News;
import ro.umfst.app.network.HttpHelper;
import ro.umfst.app.network.ImageDownloader;
import ro.umfst.app.rss.RssReader;
import ro.umfst.app.rss.RssSource;

/**
 *
 * @author boldi
 */
//goes through every rss source and saves the news that are not in the database yet

public class RssImporter 
{
    private final NewsRepository repository;
    
    //what happened during the last import,one line per source
    private final List<String> messages = new ArrayList<>();
    
    public RssImporter(NewsRepository repository)
    {
        this.repository = repository;
    }
    //return how many news items were new
    public int importAll(List<RssSource> sources)
    {
        messages.clear();
        int newCount = 0;
        
        for (RssSource source : sources)
        {
            //check the http status first,skip the source if not available
            int status = HttpHelper.getStatusCode(source.getUrl());
            if (status != 200)
            {
                messages.add(source.getName() + ": " + status + " - " + HttpHelper.describeStatus(status));
                continue;
            }
            try
            {
                //download and read the feed 
                List<News> newsList = RssReader.read(source);
                //save each item, add() return false if it is already stored
                int savedFromSource = 0;
                for (News news : newsList) 
                {
                    if (repository.add(news)) 
                    {
                        savedFromSource++;
                        // the feed had no picture -> look for it on the article page (only for new items)
                        if (news.getCoverImage() == null) {
                            addImageFromArticle(news);
                        }
                    }
                }
                newCount += savedFromSource;
                messages.add(source.getName() + ": " + newsList.size() + " news, "
                        + savedFromSource + " new");
            }
            catch (Exception hiba) 
            {
                // One broken source must not stop the others
                messages.add(source.getName() + ": failed - " + hiba.getMessage());
            }
        }
        return newCount;
    }
    // opens the article, finds its og:image, downloads it and saves the path to the db
    private void addImageFromArticle(News news) {
        String imageUrl = ImageDownloader.findArticleImage(news.getUrl());
        String localPath = ImageDownloader.saveImage(imageUrl);
        if (localPath != null) {
            repository.updateCoverImage(news.getSource(), news.getUrl(), localPath);
        }
    }
    public List<String> getMessages() 
    {
        return messages;
    }
}
