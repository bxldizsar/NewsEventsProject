package ro.umfst.app;

import java.util.List;
import ro.umfst.app.rss.RssSource;
import ro.umfst.app.rss.RssSourceLoader;

public class Main {

    public static void main(String[] args) {
        // Temporary manual test for reading rss.json

        // 1. The real configuration file
        List<RssSource> sources = RssSourceLoader.load("res/rss.json");
        System.out.println("RSS sources loaded: " + sources.size());
        for (RssSource source : sources) {
            System.out.println(" - " + source.getName() + " -> " + source.getUrl());
        }

        // 2. A file that does not exist must give a clear error, not a crash
        try {
            RssSourceLoader.load("res/missing.json");
        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}