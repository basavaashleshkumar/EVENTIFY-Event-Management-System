package service;

import dao.EventDAO;
import dao.VenueDAO;
import model.Event;
import model.Venue;
import util.ValidationUtil;

import java.sql.SQLException;
import java.util.List;

/**
 * Service managing Event lifecycle and business validation rules.
 */
public class EventService {

    private final EventDAO eventDAO;
    private final VenueDAO venueDAO;

    public EventService() {
        this.eventDAO = new EventDAO();
        this.venueDAO = new VenueDAO();
    }

    public List<Event> getAllEvents() throws SQLException {
        return eventDAO.findAll();
    }

    public List<Event> getEventsForOrganizer(int organizerId) throws SQLException {
        return eventDAO.findByOrganizer(organizerId);
    }

    public List<Event> getEventsForOrganizerUsername(String username) throws SQLException {
        return eventDAO.findByOrganizerUsername(username);
    }

    public Event getEventById(int eventId) throws SQLException {
        return eventDAO.findById(eventId);
    }

    public List<Event> searchEvents(String keyword, String status, String type, Integer organizerId) throws SQLException {
        return eventDAO.search(keyword, status, type, organizerId);
    }

    public List<String> getEventTypes() throws SQLException {
        return eventDAO.getDistinctEventTypes();
    }

    /**
     * Adds an event enforcing capacity rules and venue availability.
     */
    public int addEvent(Event event) throws SQLException {
        validateEvent(event, true);
        return eventDAO.insert(event);
    }

    /**
     * Updates an existing event.
     */
    public int updateEvent(Event event) throws SQLException {
        if (event.getEventId() <= 0) {
            throw new IllegalArgumentException("Invalid Event ID for update.");
        }
        validateEvent(event, false);

        // Check if new max capacity is less than existing registered participants
        int currentRegistered = eventDAO.getActiveRegistrationCount(event.getEventId());
        if (event.getMaxCapacity() < currentRegistered) {
            throw new IllegalArgumentException("Event capacity cannot be reduced below the current registration count (" + currentRegistered + ").");
        }

        int rows = eventDAO.update(event);
        if (rows == 0) {
            throw new SQLException("Event not found.");
        }
        return rows;
    }

    /**
     * Deletes an event. Prevents deletion if registrations exist.
     */
    public int deleteEvent(int eventId) throws SQLException {
        if (eventDAO.hasRegistrations(eventId)) {
            throw new SQLException("This event has registered participants. Cancel the event instead of deleting it.");
        }
        int rows = eventDAO.delete(eventId);
        if (rows == 0) {
            throw new SQLException("Event not found.");
        }
        return rows;
    }

    /**
     * Cancels an event cleanly.
     */
    public void cancelEvent(int eventId) throws SQLException {
        int rows = eventDAO.updateStatus(eventId, "CANCELLED");
        if (rows == 0) {
            throw new SQLException("Event not found.");
        }
    }

    private void validateEvent(Event event, boolean isNew) throws SQLException {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null.");
        }
        if (!ValidationUtil.isNotEmpty(event.getEventName())) {
            throw new IllegalArgumentException("Event name is required.");
        }
        if (!ValidationUtil.isNotEmpty(event.getEventType())) {
            throw new IllegalArgumentException("Event type is required.");
        }
        if (event.getEventDate() == null) {
            throw new IllegalArgumentException("Event date is required.");
        }
        if (!ValidationUtil.isNotEmpty(event.getStartTime())) {
            throw new IllegalArgumentException("Start time is required.");
        }
        if (!ValidationUtil.isNotEmpty(event.getEndTime())) {
            throw new IllegalArgumentException("End time is required.");
        }
        if (event.getOrganizerId() <= 0) {
            throw new IllegalArgumentException("Valid organizer must be selected.");
        }
        if (event.getVenueId() <= 0) {
            throw new IllegalArgumentException("Valid venue must be selected.");
        }
        if (event.getMaxCapacity() <= 0) {
            throw new IllegalArgumentException("Maximum capacity must be greater than zero.");
        }
        if (event.getRegistrationFee() < 0) {
            throw new IllegalArgumentException("Registration fee cannot be negative.");
        }

        // Validate Venue Capacity and Availability
        Venue venue = venueDAO.findById(event.getVenueId());
        if (venue == null) {
            throw new SQLException("Selected venue does not exist in the database.");
        }

        if (isNew && !venue.isAvailable()) {
            throw new IllegalArgumentException("The selected venue '" + venue.getVenueName() + "' is currently marked as UNAVAILABLE.");
        }

        if (event.getMaxCapacity() > venue.getCapacity()) {
            throw new IllegalArgumentException("Event capacity (" + event.getMaxCapacity() +
                                              ") cannot exceed venue capacity (" + venue.getCapacity() + ").");
        }
    }
}
