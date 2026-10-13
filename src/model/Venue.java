package model;

/**
 * Model class representing a Venue / Location for events.
 */
public class Venue {
    private int venueId;
    private String venueName;
    private String location;
    private int capacity;
    private String available; // "YES" or "NO"

    public Venue() {
    }

    public Venue(int venueId, String venueName, String location, int capacity, String available) {
        this.venueId = venueId;
        this.venueName = venueName;
        this.location = location;
        this.capacity = capacity;
        this.available = available;
    }

    public Venue(String venueName, String location, int capacity, String available) {
        this.venueName = venueName;
        this.location = location;
        this.capacity = capacity;
        this.available = available;
    }

    public int getVenueId() {
        return venueId;
    }

    public void setVenueId(int venueId) {
        this.venueId = venueId;
    }

    public String getVenueName() {
        return venueName;
    }

    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getAvailable() {
        return available;
    }

    public void setAvailable(String available) {
        this.available = available;
    }

    public boolean isAvailable() {
        return "YES".equalsIgnoreCase(this.available);
    }

    @Override
    public String toString() {
        return venueName + " (Cap: " + capacity + ", " + (isAvailable() ? "Available" : "Unavailable") + ")";
    }
}
