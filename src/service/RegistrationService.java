package service;

import dao.RegistrationDAO;
import model.Registration;

import java.sql.SQLException;
import java.util.List;

/**
 * Service managing Event Registrations, transactional reservations, and status transitions.
 */
public class RegistrationService {

    private final RegistrationDAO registrationDAO;

    public RegistrationService() {
        this.registrationDAO = new RegistrationDAO();
    }

    public List<Registration> getAllRegistrations() throws SQLException {
        return registrationDAO.findAll();
    }

    public List<Registration> getRegistrationsForEvent(int eventId) throws SQLException {
        return registrationDAO.findByEvent(eventId);
    }

    public List<Registration> searchRegistrations(String keyword, Integer eventId, String status) throws SQLException {
        return registrationDAO.search(keyword, eventId, status);
    }

    /**
     * Executes atomic participant registration transaction.
     */
    public boolean register(int eventId, int participantId) throws SQLException {
        if (eventId <= 0) {
            throw new IllegalArgumentException("Please select a valid event.");
        }
        if (participantId <= 0) {
            throw new IllegalArgumentException("Please select a valid participant.");
        }

        return registrationDAO.registerParticipantTransactional(eventId, participantId, "REGISTERED");
    }

    public void markAttended(int registrationId) throws SQLException {
        int rows = registrationDAO.updateStatus(registrationId, "ATTENDED");
        if (rows == 0) {
            throw new SQLException("Registration record not found.");
        }
    }

    public void cancelRegistration(int registrationId) throws SQLException {
        int rows = registrationDAO.updateStatus(registrationId, "CANCELLED");
        if (rows == 0) {
            throw new SQLException("Registration record not found.");
        }
    }

    public void deleteRegistration(int registrationId) throws SQLException {
        int rows = registrationDAO.delete(registrationId);
        if (rows == 0) {
            throw new SQLException("Registration record not found.");
        }
    }
}
