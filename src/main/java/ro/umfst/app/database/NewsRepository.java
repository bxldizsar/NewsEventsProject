/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ro.umfst.app.database;
import java.util.List;
import ro.umfst.app.model.News;

/**
 *
 * @author boldi
 */
// The contract: what any news storage must be able to do,without saying how
public interface NewsRepository {
    //save a new news item, returns false if a news item with the same source and url
    boolean add(News news);
    
    // Returns every stored news item, newest first
    List<News> findAll();
    
    // Saves the changes of an existing news item; returns false if it is not in the storage
    boolean update(News news);

    // Deletes the news item with the given key; returns false if there was nothing to delete
    boolean delete(String source, String url);
    
    // Returns the news items whose title or description contains the given text
    List<News> search(String text);
    // Deletes every news item; returns how many were deleted
    int deleteAll();
}
