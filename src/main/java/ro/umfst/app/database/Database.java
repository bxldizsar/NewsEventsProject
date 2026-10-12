/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ro.umfst.app.database;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
/**
 *
 * @author boldi
 */
public class Database {
    //The database file is created in the project folder on first use 
    private static final String URL = "jdbc:sqlite:news_events.db";
    
    public static Connection getConnection() throws SQLException
    {
        return DriverManager.getConnection(URL);
    }
    
}
