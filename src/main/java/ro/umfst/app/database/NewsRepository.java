/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ro.umfst.app.database;
import ro.umfst.app.model.News;

/**
 *
 * @author boldi
 */
// The contract: what any news storage must be able to do,without saying how
public interface NewsRepository {
    //save a new news item, returns false if a news item with the same source and url
    boolean add(News news);
}
