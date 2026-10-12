/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ro.umfst.app.service;
import java.util.List;
import ro.umfst.app.database.NewsRepository;
import ro.umfst.app.model.News;
/**
 *
 * @author boldi
 */

//the layer between the user interface and the storage 
public class NewsService 
{
 // the service only knows the contract,not which database is behind it 
    
    private final NewsRepository repository;
    
    public NewsService(NewsRepository repository)
    {
        this.repository = repository;
    }
    
    public List<News> getAllNews()
    {
        return repository.findAll();
    }
    
    // an empty search box shows every news item
    public List<News> search(String text) 
    {
        if (text == null || text.isBlank()) 
        {
            return repository.findAll();
        }
        return repository.search(text);
    }
    public void addNews(News news) 
    {
        boolean added = repository.add(news);
        if (!added) {
            throw new IllegalArgumentException("A news item with this source and URL already exists");
        }
    }
     public void editNews(News news, String title, String description, String author,
                         String publisher, String language, String coverImage) {
        // The news item checks the new values itself and throws if they are invalid
        news.edit(title, description, author, publisher, language, coverImage);

        boolean updated = repository.update(news);
        if (!updated) {
            throw new IllegalStateException("This news item is no longer in the database");
        }
    }

    public void deleteNews(News news) {
        // If it was already deleted, the goal is reached anyway, so the result is not checked
        repository.delete(news.getSource(), news.getUrl());
    }
}
