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
// the contract: what any news storage must be able to do,without saying how
public interface NewsRepository {
    //save a new news item, returns false if a news item with the same source and url
    boolean add(News news);
    
    // returns every stored news item
    List<News> findAll();
    
    // saves the changes of an existing news item,returns false if it's not in the storage
    boolean update(News news);

    // deletes the news item with the given key, returns false if there was nothing to delete
    boolean delete(String source, String url);
    
    // returns the news items whose title or description has the given text
    List<News> search(String text);
    // deletes every news item,returns how many were deleted
    int deleteAll();
    
    //remembers that the user opened the news item
    boolean markAsRead(String source,String url);
    
    // sets the cover image path of a news item (used when the picture is found later)
    boolean updateCoverImage(String source, String url, String coverImage);
}
