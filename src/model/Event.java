package model;

import java.sql.Date;

/**
 * Model class representing an Event with relational metadata and capacity statistics.
 */
public class Event {
    private int eventId;
    private String eventName;
    private String eventType;
    private String description;
    private Date eventDate;
    private String startTime;
    private String endTime;
    private int organizerId;
    private int venueId;
    private int maxCapacity;
    private double registrationFee;
    private String status; // UPCOMING, ONGOING, COMPLETED, CANCELLED

    // Joined / Calculated fields for GUI and Reporting
    private String organizerName;
    private String venueName;
    private int venueCapacity;
    private int currentRegistrations;

    public Event() {
    }

    public Event(int eventId, String eventName, String eventType, String description,
                 Date eventDate, String startTime, String endTime, int organizerId,
                 int venueId, int maxCapacity, double registrationFee, String status) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.eventType = eventType;
        this.description = description;
        this.eventDate = eventDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.organizerId = organizerId;
        this.venueId = venueId;
        this.maxCapacity = maxCapacity;
        this.registrationFee = registrationFee;
        this.status = status;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(int organizerId) {
        this.organizerId = organizerId;
    }

    public int getVenueId() {
        return venueId;
    }

    public void setVenueId(int venueId) {
        this.venueId = venueId;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public double getRegistrationFee() {
        return registrationFee;
    }

    public void setRegistrationFee(double registrationFee) {
        this.registrationFee = registrationFee;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOrganizerName() {
        return organizerName;
    }

    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
    }

    public String getVenueName() {
        return venueName;
    }

    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }

    public int getVenueCapacity() {
        return venueCapacity;
    }

    public void setVenueCapacity(int venueCapacity) {
        this.venueCapacity = venueCapacity;
    }

    public int getCurrentRegistrations() {
        return currentRegistrations;
    }

    public void setCurrentRegistrations(int currentRegistrations) {
        this.currentRegistrations = currentRegistrations;
    }

    public int getSeatsRemaining() {
        return Math.max(0, maxCapacity - currentRegistrations);
    }

    public boolean isFull() {
        return currentRegistrations >= maxCapacity;
    }

    @Override
    public String toString() {
        return eventName + " (" + (eventDate != null ? eventDate.toString() : "") + " - " + status + ")";
    }
}
