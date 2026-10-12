/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ro.umfst.app.database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
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
                      
    }
    @Override
    public boolean add(News news)
    {
        //OR IGNORE: if the key (source + url) already exists, SQLite skips the row instead of failing
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
}
