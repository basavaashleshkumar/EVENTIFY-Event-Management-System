package service;

import dao.VenueDAO;
import model.Venue;
import util.ValidationUtil;

import java.sql.SQLException;
import java.util.List;

/**
 * Service managing Venues and room capacity rules.
 */
public class VenueService {

    private final VenueDAO venueDAO;

    public VenueService() {
        this.venueDAO = new VenueDAO();
    }

    public List<Venue> getAllVenues() throws SQLException {
        return venueDAO.findAll();
    }

    public List<Venue> getAvailableVenues() throws SQLException {
        return venueDAO.findAvailable();
    }

    public Venue getVenueById(int id) throws SQLException {
        return venueDAO.findById(id);
    }

    public List<Venue> searchVenues(String keyword) throws SQLException {
        return venueDAO.search(keyword);
    }

    public void addVenue(Venue venue) throws SQLException {
        validateVenue(venue);
        venueDAO.insert(venue);
    }

    public void updateVenue(Venue venue) throws SQLException {
        if (venue.getVenueId() <= 0) {
            throw new IllegalArgumentException("Invalid Venue ID for update.");
        }
        validateVenue(venue);
        int rows = venueDAO.update(venue);
        if (rows == 0) {
            throw new SQLException("Venue not found or no changes made.");
        }
    }

    public void deleteVenue(int venueId) throws SQLException {
        if (venueDAO.hasEvents(venueId)) {
            throw new SQLException("Cannot delete this venue because events are scheduled in it. Reassign or cancel the events first.");
        }
        int rows = venueDAO.delete(venueId);
        if (rows == 0) {
            throw new SQLException("Venue not found.");
        }
    }

    private void validateVenue(Venue venue) {
        if (venue == null) {
            throw new IllegalArgumentException("Venue cannot be null.");
        }
        if (!ValidationUtil.isNotEmpty(venue.getVenueName())) {
            throw new IllegalArgumentException("Venue name is required.");
        }
        if (venue.getCapacity() <= 0) {
            throw new IllegalArgumentException("Venue capacity must be greater than zero.");
        }
        if (!"YES".equalsIgnoreCase(venue.getAvailable()) && !"NO".equalsIgnoreCase(venue.getAvailable())) {
            throw new IllegalArgumentException("Availability must be either 'YES' or 'NO'.");
        }
    }
}
