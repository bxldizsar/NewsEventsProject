package ro.umfst.app.ui;

import java.io.File;

// Decides which picture to show for a news item; the only place that knows about default.jpg
public final class CoverImageResolver 
{

    private static final String DEFAULT_IMAGE = "res/images/default.jpg";

    // Returns the given path if that file exists, otherwise the default image
    public static String resolve(String imagePath) 
    {
        if (imagePath == null || imagePath.isBlank()) 
        {
            return DEFAULT_IMAGE;
        }
        if (!new File(imagePath).isFile()) 
        {
            return DEFAULT_IMAGE;
        }
        return imagePath;
    }
}