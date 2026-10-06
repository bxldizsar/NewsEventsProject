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
public class Event extends News {
    private Timestamp eventDateTime;
    private String location;
    
    public Timestamp getEventDateTime()
    {
        return eventDateTime;
    }
    public void setEventDateTime(Timestamp eventDateTime)
    {
        this.eventDateTime = eventDateTime;
    }
    
    public String getLocation()
    {
        return location;
    }
    public void setLocation(String location)
    {
        this.location = location;
    }
}
