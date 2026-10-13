package dao;

import db.DatabaseConnection;
import model.Participant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for PARTICIPANT table.
 */
public class ParticipantDAO {

    public List<Participant> findAll() throws SQLException {
        List<Participant> list = new ArrayList<>();
        String sql = "SELECT PARTICIPANT_ID, NAME, EMAIL, PHONE, COLLEGE, DEPARTMENT " +
                     "FROM PARTICIPANT ORDER BY NAME";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(extractFromResultSet(rs));
            }
        }
        return list;
    }

    public Participant findById(int participantId) throws SQLException {
        String sql = "SELECT PARTICIPANT_ID, NAME, EMAIL, PHONE, COLLEGE, DEPARTMENT " +
                     "FROM PARTICIPANT WHERE PARTICIPANT_ID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, participantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public Participant findByEmail(String email) throws SQLException {
        String sql = "SELECT PARTICIPANT_ID, NAME, EMAIL, PHONE, COLLEGE, DEPARTMENT " +
                     "FROM PARTICIPANT WHERE LOWER(EMAIL) = LOWER(?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public List<Participant> search(String keyword) throws SQLException {
        List<Participant> list = new ArrayList<>();
        String sql = "SELECT PARTICIPANT_ID, NAME, EMAIL, PHONE, COLLEGE, DEPARTMENT " +
                     "FROM PARTICIPANT " +
                     "WHERE LOWER(NAME) LIKE ? OR LOWER(EMAIL) LIKE ? OR LOWER(COLLEGE) LIKE ? " +
                     "OR LOWER(DEPARTMENT) LIKE ? OR PHONE LIKE ? " +
                     "ORDER BY NAME";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String pattern = "%" + (keyword == null ? "" : keyword.toLowerCase().trim()) + "%";
            for (int i = 1; i <= 5; i++) {
                ps.setString(i, pattern);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractFromResultSet(rs));
                }
            }
        }
        return list;
    }

    public int insert(Participant p) throws SQLException {
        String sql = "INSERT INTO PARTICIPANT (PARTICIPANT_ID, NAME, EMAIL, PHONE, COLLEGE, DEPARTMENT) " +
                     "VALUES (SEQ_PARTICIPANT.NEXTVAL, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getName());
            ps.setString(2, p.getEmail());
            ps.setString(3, p.getPhone());
            ps.setString(4, p.getCollege());
            ps.setString(5, p.getDepartment());

            return ps.executeUpdate();
        }
    }

    public int update(Participant p) throws SQLException {
        String sql = "UPDATE PARTICIPANT SET NAME = ?, EMAIL = ?, PHONE = ?, COLLEGE = ?, DEPARTMENT = ? " +
                     "WHERE PARTICIPANT_ID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getName());
            ps.setString(2, p.getEmail());
            ps.setString(3, p.getPhone());
            ps.setString(4, p.getCollege());
            ps.setString(5, p.getDepartment());
            ps.setInt(6, p.getParticipantId());

            return ps.executeUpdate();
        }
    }

    public int delete(int participantId) throws SQLException {
        String sql = "DELETE FROM PARTICIPANT WHERE PARTICIPANT_ID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, participantId);
            return ps.executeUpdate();
        }
    }

    public boolean hasRegistrations(int participantId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM REGISTRATION WHERE PARTICIPANT_ID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, participantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private Participant extractFromResultSet(ResultSet rs) throws SQLException {
        return new Participant(
                rs.getInt("PARTICIPANT_ID"),
                rs.getString("NAME"),
                rs.getString("EMAIL"),
                rs.getString("PHONE"),
                rs.getString("COLLEGE"),
                rs.getString("DEPARTMENT")
        );
    }
}
