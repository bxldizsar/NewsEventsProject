/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ro.umfst.app.network;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author boldi
 */
public class ImageDownloader
{
    // downloaded pictures go here (not into git, see .gitignore)
    private static final String FOLDER = "res/images/news";

    // <meta property="og:image" content="...">  and  <meta content="..." property="og:image">
    private static final Pattern OG_IMAGE = Pattern.compile(
            "<meta[^>]*property=[\"']og:image[\"'][^>]*content=[\"']([^\"']+)[\"']");
    private static final Pattern OG_IMAGE_REVERSED = Pattern.compile(
            "<meta[^>]*content=[\"']([^\"']+)[\"'][^>]*property=[\"']og:image[\"']");

    // downloads the picture once and returns the local path, or null if it didnt work
    public static String saveImage(String imageUrl)
    {
        if (imageUrl == null || imageUrl.isBlank())
        {
            return null;
        }

        String extension = extensionOf(imageUrl);
        if (extension == null)
        {
            return null; // swing cant show it (webp etc.) -> default image
        }

        try
        {
            // same url -> same file name, so a picture is only downloaded once
            String fileName = Integer.toHexString(imageUrl.hashCode()) + extension;
            Path file = Path.of(FOLDER, fileName);

            if (Files.exists(file))
            {
                return file.toString(); // already downloaded at an earlier refresh
            }

            Files.createDirectories(file.getParent());
            byte[] bytes = HttpHelper.download(imageUrl);
            Files.write(file, bytes);
            return file.toString();
        }
        catch (Exception hiba)
        {
            // no picture is not a big deal, the news still gets saved with the default image
            System.out.println("Could not download image " + imageUrl + ": " + hiba.getMessage());
            return null;
        }
    }

    // opens the article page and reads <meta property="og:image" content="...">, or null
    // (this is the picture facebook shows when you share the link)
    public static String findArticleImage(String pageUrl)
    {
        try
        {
            String html = new String(HttpHelper.download(pageUrl), StandardCharsets.UTF_8);

            // sites write the two attributes in different order, so we check both
            Matcher matcher = OG_IMAGE.matcher(html);
            if (matcher.find())
            {
                return matcher.group(1).replace("&amp;", "&");
            }
            matcher = OG_IMAGE_REVERSED.matcher(html);
            if (matcher.find())
            {
                return matcher.group(1).replace("&amp;", "&");
            }
        }
        catch (Exception hiba)
        {
            System.out.println("Could not open article " + pageUrl + ": " + hiba.getMessage());
        }
        return null;
    }

    // .jpg / .png / .gif, or null if its a format swing cant read
    private static String extensionOf(String imageUrl)
    {
        String path = imageUrl.toLowerCase();

        // cut off things like ?resize=300 at the end
        int questionMark = path.indexOf('?');
        if (questionMark != -1)
        {
            path = path.substring(0, questionMark);
        }

        if (path.endsWith(".png"))
        {
            return ".png";
        }
        if (path.endsWith(".gif"))
        {
            return ".gif";
        }
        if (path.endsWith(".webp") || path.endsWith(".svg") || path.endsWith(".avif"))
        {
            return null;
        }
        return ".jpg";
    }
}
