/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ro.umfst.app.model;
import java.sql.Timestamp;

/**
 *
 * @author boldi
 */
public class Event extends ContentItem 
{
    //the day and the start time of the event 
    private final Timestamp eventDateTime;
    
    
    //where the event takes place
    private final String location;
    
    public Event(String title,String description,String author,String publsiher,
            String language,String coverImage,Timestamp eventDateTime,String location,
            Timestamp creationDate,Timestamp lastModifiedDate)
    {
        super(title,description,author,publsiher,language,coverImage,creationDate,lastModifiedDate);
        
        //an avent with/ a date makes no sense, so date is required
        
        if(eventDateTime == null)
        {
            throw new IllegalArgumentException("Event date and time is required");
        }
        this.eventDateTime = eventDateTime;
        
        //require text inherited from contentitem
        
        this.location = requireText(location,"Location");
    }
    public static Timestamp parseDateTime(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Event date and time is required");
        }
        String value = text.trim();
        
        
        if (value.length() == 16) {
            value = value + ":00";
        }
        try {
            return Timestamp.valueOf(value);
        } catch (IllegalArgumentException hiba) {
            throw new IllegalArgumentException("The event date must look like 2026-10-20 18:30");
        }
    }

    public Timestamp getEventDateTime() {
        return eventDateTime;
    }

    public String getLocation() {
        return location;
    }
}
