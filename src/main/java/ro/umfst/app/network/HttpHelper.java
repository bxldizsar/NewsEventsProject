/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ro.umfst.app.network;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;

/**
 *
 * @author boldi
 */
// everything the application needs from the network: status codes and downloads
public class HttpHelper {
    //Wait at most 5 seconds for connecting and 5 seconds for reading 
    private static final int TIMEOUT_MS = 5000;
    
    //returns the http status code of the url, or -1 if the server could not reached at all
    public static int getStatusCode(String urlAdress)
    {
        try
        {
            HttpURLConnection connection = openConnection(urlAdress);
            int statusCode = connection.getResponseCode();
            connection.disconnect();
            return statusCode;
        }
        catch (Exception hiba)
        {
            //invalid url,no network,unknown host or timeout
            return -1;
        }
    }
    // a short, readable explanation of a status code 
    public static String describeStatus(int statusCode)
    {
        return switch (statusCode)
        {
            case 200 -> "OK,the content can be processed";
            case 301, 302 -> "Redirect, the address has moved";
            case 400 -> "Bad request";
            case 401 -> "Unauthorized, login is required";
            case 403 -> "Forbidden, access denied";
            case 404 -> "Not found";
            case 500 -> "Server error";
            case -1 -> "No connection (network error, timeout or invalid URL)";
            default -> "Unexpected status";  
        };
    }
    //downloads the content of the url; throws ioexception with a clear message if it fails 
    
    public static byte[] download(String urlAddress) throws IOException 
    {
        HttpURLConnection connection = openConnection(urlAddress);
        try 
        {
            int statusCode = connection.getResponseCode();
            if (statusCode != 200) 
            {
                throw new IOException("HTTP " + statusCode + ": " + describeStatus(statusCode));
            }
            try (InputStream input = connection.getInputStream()) 
            {
                return input.readAllBytes();
            }
        } finally 
        {
            // runs whether the download succeeded or not
            connection.disconnect();
        }
    }

    private static HttpURLConnection openConnection(String urlAddress) throws IOException 
    {
        HttpURLConnection connection =
                (HttpURLConnection) URI.create(urlAddress).toURL().openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(TIMEOUT_MS);
        connection.setReadTimeout(TIMEOUT_MS);
        // some news sites refuse requests that do not say which program is asking
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (NewsEventsManager project)");
        return connection;
    }
}
