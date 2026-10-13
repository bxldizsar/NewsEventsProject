
package ro.umfst.app.model;
import java.sql.Timestamp;

public class News extends ContentItem {

    // source and url form the primary key of a news item, they can't change
    private final String source;
    private final String url;
    private final Timestamp publishDate;
    private boolean read = false;

    public News(String title, String description, String source, String author,
                String publisher, Timestamp publishDate, String url, String language,
                String coverImage, Timestamp creationDate, Timestamp lastModifiedDate) {
        super(title, description, author, publisher, language, coverImage,
              creationDate, lastModifiedDate);

        this.source = requireText(source, "Source");
        this.url = requireText(url, "URL");
        this.publishDate = publishDate;
    }

    public String getSource() {
        return source;
    }

    public String getUrl() {
        return url;
    }

    public Timestamp getPublishDate() {
        return publishDate;
    }
    public boolean isRead()
    {
        return read;
    }
    public void markAsRead()
    {
        this.read = true;
    }
}