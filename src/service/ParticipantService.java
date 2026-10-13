package service;

import dao.ParticipantDAO;
import model.Participant;
import util.ValidationUtil;

import java.sql.SQLException;
import java.util.List;

/**
 * Service managing Participants with email/phone validation.
 */
public class ParticipantService {

    private final ParticipantDAO participantDAO;

    public ParticipantService() {
        this.participantDAO = new ParticipantDAO();
    }

    public List<Participant> getAllParticipants() throws SQLException {
        return participantDAO.findAll();
    }

    public Participant getParticipantById(int id) throws SQLException {
        return participantDAO.findById(id);
    }

    public List<Participant> searchParticipants(String keyword) throws SQLException {
        return participantDAO.search(keyword);
    }

    public void addParticipant(Participant p) throws SQLException {
        validateParticipant(p);
        Participant existing = participantDAO.findByEmail(p.getEmail());
        if (existing != null) {
            throw new SQLException("A participant with email '" + p.getEmail() + "' is already registered in the system.");
        }
        participantDAO.insert(p);
    }

    public void updateParticipant(Participant p) throws SQLException {
        if (p.getParticipantId() <= 0) {
            throw new IllegalArgumentException("Invalid Participant ID.");
        }
        validateParticipant(p);
        Participant existing = participantDAO.findByEmail(p.getEmail());
        if (existing != null && existing.getParticipantId() != p.getParticipantId()) {
            throw new SQLException("Email '" + p.getEmail() + "' is already used by another participant.");
        }
        int rows = participantDAO.update(p);
        if (rows == 0) {
            throw new SQLException("Participant not found or no changes made.");
        }
    }

    public void deleteParticipant(int participantId) throws SQLException {
        if (participantDAO.hasRegistrations(participantId)) {
            throw new SQLException("Cannot delete participant because they have active event registrations. Cancel their registrations first.");
        }
        int rows = participantDAO.delete(participantId);
        if (rows == 0) {
            throw new SQLException("Participant not found.");
        }
    }

    private void validateParticipant(Participant p) {
        if (p == null) {
            throw new IllegalArgumentException("Participant cannot be null.");
        }
        if (!ValidationUtil.isNotEmpty(p.getName())) {
            throw new IllegalArgumentException("Participant name is required.");
        }
        if (!ValidationUtil.isValidEmail(p.getEmail())) {
            throw new IllegalArgumentException("Valid participant email is required.");
        }
        if (!ValidationUtil.isValidPhone(p.getPhone())) {
            throw new IllegalArgumentException("Invalid phone number format.");
        }
        if (!ValidationUtil.isNotEmpty(p.getCollege())) {
            throw new IllegalArgumentException("College / Institution name is required.");
        }
    }
}
