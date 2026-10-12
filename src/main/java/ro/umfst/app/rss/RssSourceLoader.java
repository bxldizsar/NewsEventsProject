/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ro.umfst.app.rss;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author boldi
 */
// Reads the list of RSS sources from the JSON configuration file
public class RssSourceLoader 
{
    
    public static List<RssSource> load(String filePath) 
    {
        List<RssSource> sources = new ArrayList<>();

        try (Reader reader = Files.newBufferedReader(Path.of(filePath), StandardCharsets.UTF_8)) 
        {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray list = root.getAsJsonArray("rssSources");
            if (list == null) 
            {
                throw new IllegalArgumentException(filePath + " has no \"rssSources\" list");
            }

            for (JsonElement element : list) 
            {
                JsonObject entry = element.getAsJsonObject();
                try {
                    sources.add(new RssSource(readText(entry, "name"), readText(entry, "url")));
                } catch (IllegalArgumentException hiba) {
                    // One broken entry should not stop the others from loading
                    System.out.println("Skipped an RSS source in " + filePath + ": " + hiba.getMessage());
                }
            }
        } catch (NoSuchFileException hiba) 
        {
            throw new RuntimeException("File not found: " + filePath, hiba);
        } catch (IOException hiba) 
        {
            throw new RuntimeException("Could not read " + filePath + ": " + hiba.getMessage(), hiba);
        } catch (JsonParseException | IllegalStateException hiba) 
        {
            throw new RuntimeException(filePath + " is not valid JSON: " + hiba.getMessage(), hiba);
        }
        return sources;
    }

    // Returns the text value of a field, or null if the field is missing
    private static String readText(JsonObject entry, String fieldName) 
    {
        JsonElement value = entry.get(fieldName);
        if (value == null || value.isJsonNull()) 
        {
            return null;
        }
        return value.getAsString();
    }
}
