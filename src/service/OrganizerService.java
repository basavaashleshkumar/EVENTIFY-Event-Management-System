package service;

import dao.OrganizerDAO;
import model.Organizer;
import util.ValidationUtil;

import java.sql.SQLException;
import java.util.List;

/**
 * Service managing Organizers and contact information.
 */
public class OrganizerService {

    private final OrganizerDAO organizerDAO;

    public OrganizerService() {
        this.organizerDAO = new OrganizerDAO();
    }

    public List<Organizer> getAllOrganizers() throws SQLException {
        return organizerDAO.findAll();
    }

    public Organizer getOrganizerById(int id) throws SQLException {
        return organizerDAO.findById(id);
    }

    public Organizer getOrganizerByUsername(String username) throws SQLException {
        return organizerDAO.findByUsername(username);
    }

    public List<Organizer> searchOrganizers(String keyword) throws SQLException {
        return organizerDAO.search(keyword);
    }

    public void addOrganizer(Organizer org) throws SQLException {
        validateOrganizer(org);
        organizerDAO.insert(org);
    }

    public void updateOrganizer(Organizer org) throws SQLException {
        if (org.getOrganizerId() <= 0) {
            throw new IllegalArgumentException("Invalid Organizer ID.");
        }
        validateOrganizer(org);
        int rows = organizerDAO.update(org);
        if (rows == 0) {
            throw new SQLException("Organizer not found or no changes made.");
        }
    }

    public void deleteOrganizer(int organizerId) throws SQLException {
        if (organizerDAO.hasEvents(organizerId)) {
            throw new SQLException("Cannot delete this organizer because events are assigned to them. Please reassign the events first.");
        }
        int rows = organizerDAO.delete(organizerId);
        if (rows == 0) {
            throw new SQLException("Organizer not found.");
        }
    }

    private void validateOrganizer(Organizer org) {
        if (org == null) {
            throw new IllegalArgumentException("Organizer cannot be null.");
        }
        if (!ValidationUtil.isNotEmpty(org.getName())) {
            throw new IllegalArgumentException("Organizer name is required.");
        }
        if (!ValidationUtil.isValidEmail(org.getEmail())) {
            throw new IllegalArgumentException("Invalid email format for organizer.");
        }
        if (!ValidationUtil.isValidPhone(org.getPhone())) {
            throw new IllegalArgumentException("Invalid phone number format.");
        }
    }
}
