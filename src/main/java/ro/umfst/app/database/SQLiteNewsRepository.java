/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ro.umfst.app.database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import ro.umfst.app.model.News;

/**
 *
 * @author boldi
 */
public class SQLiteNewsRepository implements NewsRepository {
    // every query in this class runs on this connection
    
    private final Connection connection;
    
    public SQLiteNewsRepository(Connection connection)
    {
        this.connection = connection;
        createTable();
    }
    
    private void createTable()
    {
        String query = """
                CREATE TABLE IF NOT EXISTS news (
                    title TEXT NOT NULL,
                    description TEXT,
                    source TEXT NOT NULL,
                    author TEXT,
                    publisher TEXT,
                    publish_date TIMESTAMP,
                    url TEXT NOT NULL,
                    creation_date TIMESTAMP NOT NULL,
                    last_modified_date TIMESTAMP NOT NULL,
                    language TEXT NOT NULL,
                    cover_image TEXT,
                    is_read INTEGER NOT NULL DEFAULT 0,
                    PRIMARY KEY (source, url)
                )
                """;
        
        try (Statement statement = connection.createStatement())
        {
            statement.executeUpdate(query);
        }
        catch (SQLException hiba)
        {
            throw new RuntimeException("Could not create the news table: " + hiba.getMessage(), hiba);
        }
        addReadColumnIfMissing();
                      
    }
     private void addReadColumnIfMissing()
    {
        // PRAGMA table_info lists the columns of the table, one row per column
        try (Statement statement = connection.createStatement();
             ResultSet columns = statement.executeQuery("PRAGMA table_info(news)"))
        {
            while (columns.next())
            {
                if (columns.getString("name").equals("is_read"))
                {
                    return; // already there, nothing to do
                }
            }
        }
        catch (SQLException hiba)
        {
            throw new RuntimeException("Could not check the news table: " + hiba.getMessage(), hiba);
        }

        // not found -> add it, every existing row gets 0 (unread)
        try (Statement statement = connection.createStatement())
        {
            statement.executeUpdate("ALTER TABLE news ADD COLUMN is_read INTEGER NOT NULL DEFAULT 0");
        }
        catch (SQLException hiba)
        {
            throw new RuntimeException("Could not add the is_read column: " + hiba.getMessage(), hiba);
        }
    }
    @Override
    public boolean add(News news)
    {
        //or ignore: if the key source + urlalready exists, SQLite skips the row instead of failing
        String query = """
                INSERT OR IGNORE INTO news (title, description, source, author, publisher,
                                            publish_date, url, creation_date, last_modified_date,
                                            language, cover_image)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(query))
        {
             statement.setString(1, news.getTitle());
            statement.setString(2, news.getDescription());
            statement.setString(3, news.getSource());
            statement.setString(4, news.getAuthor());
            statement.setString(5, news.getPublisher());
            statement.setTimestamp(6, news.getPublishDate());
            statement.setString(7, news.getUrl());
            statement.setTimestamp(8, news.getCreationDate());
            statement.setTimestamp(9, news.getLastModifiedDate());
            statement.setString(10, news.getLanguage());
            statement.setString(11, news.getCoverImage());

            int insertedRows = statement.executeUpdate();
            return insertedRows == 1;
        }
        catch (SQLException hiba)
        {
            throw new RuntimeException("Could not save news: " + hiba.getMessage(), hiba);
        }
    }
    @Override 
    public List<News> findAll()
    {
        String query = "SELECT * FROM news ORDER BY publish_date DESC";
        List<News> result = new ArrayList();
        
        try (Statement statement = connection.createStatement();
            ResultSet rows = statement.executeQuery(query))
            {
                // move through the result one row at a time 
                while (rows.next())
                {
                    result.add(readNews(rows));
                }
            }
                catch (SQLException hiba)
                        {
                            throw new RuntimeException("Could not load news: " + hiba.getMessage(), hiba);
                        }
                return result;
            }
             // builds a news object from the row, resultset is on
    private News readNews(ResultSet rows) throws SQLException 
    {
       News news = new News(
                rows.getString("title"),
                rows.getString("description"),
                rows.getString("source"),
                rows.getString("author"),
                rows.getString("publisher"),
                rows.getTimestamp("publish_date"),
                rows.getString("url"),
                rows.getString("language"),
                rows.getString("cover_image"),
                rows.getTimestamp("creation_date"),
                rows.getTimestamp("last_modified_date"));

        // getBoolean turns the stored 1 into true and 0 into false
        if (rows.getBoolean("is_read")) {
            news.markAsRead();
        }
        return news;
    }
    
    @Override
    public boolean update(News news) {
        // the key (source + url) finds the row, the other columns get the new values
        String query = """
                UPDATE news
                SET title = ?, description = ?, author = ?, publisher = ?,
                    language = ?, cover_image = ?, last_modified_date = ?
                WHERE source = ? AND url = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(query)) 
        {
            statement.setString(1, news.getTitle());
            statement.setString(2, news.getDescription());
            statement.setString(3, news.getAuthor());
            statement.setString(4, news.getPublisher());
            statement.setString(5, news.getLanguage());
            statement.setString(6, news.getCoverImage());
            statement.setTimestamp(7, news.getLastModifiedDate());
            statement.setString(8, news.getSource());
            statement.setString(9, news.getUrl());

            int changedRows = statement.executeUpdate();
            return changedRows == 1;
        } catch (SQLException hiba) 
        {
            throw new RuntimeException("Could not update news: " + hiba.getMessage(), hiba);
        }
    }

    @Override
    public boolean delete(String source, String url) 
    {
        String query = "DELETE FROM news WHERE source = ? AND url = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) 
        {
            statement.setString(1, source);
            statement.setString(2, url);

            int deletedRows = statement.executeUpdate();
            return deletedRows == 1;
        } catch (SQLException hiba) 
        {
            throw new RuntimeException("Could not delete news: " + hiba.getMessage(), hiba);
        }
    }
    //the save of a new "news" 
    @Override
    public List<News> search(String text) 
    {
        String query = """
                SELECT * FROM news
                WHERE title LIKE ? OR description LIKE ?
                ORDER BY publish_date DESC
                """;

        String searchText = "";
        if (text != null) 
        {
            searchText = text.trim();
        }
       
        String pattern = "%" + searchText + "%";

        List<News> result = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(query)) 
        {
            statement.setString(1, pattern);
            statement.setString(2, pattern);

            try (ResultSet rows = statement.executeQuery()) 
            {
                while (rows.next()) 
                {
                    result.add(readNews(rows));
                }
            }
        } catch (SQLException hiba) 
        {
            throw new RuntimeException("Could not search news: " + hiba.getMessage(), hiba);
        }
        return result;
    }
    
    @Override
    public int deleteAll() 
    {
        try (Statement statement = connection.createStatement()) {
            return statement.executeUpdate("DELETE FROM news");
        } catch (SQLException hiba) {
            throw new RuntimeException("Could not clear the database: " + hiba.getMessage(), hiba);
        }
    }
    @Override
    public boolean markAsRead(String source, String url) {
        String query = "UPDATE news SET is_read = 1 WHERE source = ? AND url = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, source);
            statement.setString(2, url);

            int changedRows = statement.executeUpdate();
            return changedRows == 1;
        } catch (SQLException hiba) {
            throw new RuntimeException("Could not mark news as read: " + hiba.getMessage(), hiba);
        }
    }
     @Override
    public boolean updateCoverImage(String source, String url, String coverImage)
    {
        String query = "UPDATE news SET cover_image = ? WHERE source = ? AND url = ?";

        try (PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setString(1, coverImage);
            statement.setString(2, source);
            statement.setString(3, url);

            int changedRows = statement.executeUpdate();
            return changedRows == 1;
        }
        catch (SQLException hiba)
        {
            throw new RuntimeException("Could not save the cover image: " + hiba.getMessage(), hiba);
        }
    }
    }

