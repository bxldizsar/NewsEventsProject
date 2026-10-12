package ro.umfst.app.model;
import java.sql.Timestamp;

public abstract class ContentItem {
    private String title; 
    private String description; 
    private String author; 
    private String publisher;
    private String language; 
    private final Timestamp creationDate;
    private Timestamp lastModifiedDate;
    private String coverImage;
     
    
    protected ContentItem(String title, String description, String author, String publisher,
                          String language, String coverImage,
                          Timestamp creationDate, Timestamp lastModifiedDate)
    {
        this.title = requireText(title,"Title");
        this.language = requireLanguage(language);
        this.description = description; 
        this.author = author; 
        this.publisher = publisher; 
        this.coverImage = coverImage; 
        
        // A new item gets the current time; an item loaded from the database keeps its stored dates
        
        Timestamp now = new Timestamp(System.currentTimeMillis());
        if (creationDate == null) 
        {
            this.creationDate = now; 
        }
        else 
        {
            this.creationDate = creationDate;
        }
        if (lastModifiedDate == null)
        {
            this.lastModifiedDate = now;
        }
        else 
        {
            this.lastModifiedDate = lastModifiedDate;
        }
    }
    
    // Changes the editable fields and records when the change happened 
    
    public void edit(String newTitle,String newDescription,String newAuthor,String newPublisher,String newLanguage, String newCoverImage)
    {
        //Validate everything first, so a failed edeti leaves the object unchanged
        String checkedTitle = requireText(newTitle, "Title");
        String checkedLanguage = requireLanguage(newLanguage);
        
        this.title = checkedTitle;
        this.language = checkedLanguage;
        this.description = newDescription; 
        this.author = newAuthor;
        this.publisher = newPublisher;
        this.coverImage = newCoverImage;
        this.lastModifiedDate = new Timestamp(System.currentTimeMillis());
    }
    
    // Shared check for required text fields, subclasses use it too.
    
    protected static String requireText(String value, String fieldName)
    {
        if (value == null || value.isBlank() )
        {
            throw new IllegalArgumentException(fieldName + "is required");
        }
        return value.trim();
    }
    
    private static String requireLanguage(String language)
    {
        String code = requireText(language,"Language").toUpperCase();
        if (!code.equals("RO") && !code.equals("HU"))
        {
            throw new IllegalArgumentException("Language must be RO or HU");
        }
        return code;
    }
        public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getAuthor() {
        return author;
    }

    public String getPublisher() {
        return publisher;
    }

    public String getLanguage() {
        return language;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public Timestamp getCreationDate() {
        return creationDate;
    }

    public Timestamp getLastModifiedDate() {
        return lastModifiedDate;
    }

    // JList calls toString() to decide what text to show for each item
    @Override
    public String toString() {
        return title;
    }
      
    
}
