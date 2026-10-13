package dao;

import db.DatabaseConnection;
import model.Registration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for REGISTRATION table.
 * Demonstrates:
 * - Proper JDBC Transaction Management (setAutoCommit(false), commit(), rollback())
 * - Row-level locking / concurrency check (FOR UPDATE)
 * - Unique constraint handling and business capacity enforcement
 */
public class RegistrationDAO {

    private static final String BASE_SELECT =
        "SELECT r.REGISTRATION_ID, r.EVENT_ID, r.PARTICIPANT_ID, r.REGISTRATION_DATE, r.STATUS, " +
        "       e.EVENT_NAME, e.EVENT_DATE, v.VENUE_NAME, " +
        "       p.NAME AS PARTICIPANT_NAME, p.EMAIL AS PARTICIPANT_EMAIL, " +
        "       p.COLLEGE AS PARTICIPANT_COLLEGE, p.PHONE AS PARTICIPANT_PHONE " +
        "FROM REGISTRATION r " +
        "JOIN EVENT e ON r.EVENT_ID = e.EVENT_ID " +
        "JOIN VENUE v ON e.VENUE_ID = v.VENUE_ID " +
        "JOIN PARTICIPANT p ON r.PARTICIPANT_ID = p.PARTICIPANT_ID ";

    public List<Registration> findAll() throws SQLException {
        List<Registration> list = new ArrayList<>();
        String sql = BASE_SELECT + " ORDER BY r.REGISTRATION_DATE DESC, r.REGISTRATION_ID DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(extractFromResultSet(rs));
            }
        }
        return list;
    }

    public List<Registration> findByEvent(int eventId) throws SQLException {
        List<Registration> list = new ArrayList<>();
        String sql = BASE_SELECT + " WHERE r.EVENT_ID = ? ORDER BY r.REGISTRATION_DATE DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractFromResultSet(rs));
                }
            }
        }
        return list;
    }

    public List<Registration> findByParticipant(int participantId) throws SQLException {
        List<Registration> list = new ArrayList<>();
        String sql = BASE_SELECT + " WHERE r.PARTICIPANT_ID = ? ORDER BY r.REGISTRATION_DATE DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, participantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractFromResultSet(rs));
                }
            }
        }
        return list;
    }

    public List<Registration> search(String keyword, Integer eventId, String statusFilter) throws SQLException {
        List<Registration> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT);
        sql.append(" WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        if (eventId != null && eventId > 0) {
            sql.append(" AND r.EVENT_ID = ? ");
            params.add(eventId);
        }

        if (statusFilter != null && !statusFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(statusFilter)) {
            sql.append(" AND r.STATUS = ? ");
            params.add(statusFilter.toUpperCase().trim());
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            String pattern = "%" + keyword.toLowerCase().trim() + "%";
            sql.append(" AND (LOWER(p.NAME) LIKE ? OR LOWER(p.EMAIL) LIKE ? OR LOWER(e.EVENT_NAME) LIKE ? OR LOWER(p.COLLEGE) LIKE ?) ");
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }

        sql.append(" ORDER BY r.REGISTRATION_DATE DESC, r.REGISTRATION_ID DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof Integer) {
                    ps.setInt(i + 1, (Integer) p);
                } else {
                    ps.setString(i + 1, p.toString());
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractFromResultSet(rs));
                }
            }
        }
        return list;
    }

    /**
     * Registers a participant for an event using a strict ACID Transaction.
     * 
     * Workflow:
     * 1. con.setAutoCommit(false)
     * 2. Lock event row to read capacity and status
     * 3. Validate participant existence
     * 4. Check for duplicate registration
     * 5. Verify available capacity (Current Active Count < Max Capacity)
     * 6. Insert registration record with SEQ_REGISTRATION.NEXTVAL
     * 7. con.commit()
     * 8. On any error: con.rollback()
     */
    public boolean registerParticipantTransactional(int eventId, int participantId, String status) throws SQLException {
        Connection conn = null;
        PreparedStatement psLockEvent = null;
        PreparedStatement psCheckParticipant = null;
        PreparedStatement psCheckDup = null;
        PreparedStatement psCountActive = null;
        PreparedStatement psInsert = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // START TRANSACTION

            // Step 1: Check and Lock Event row
            String sqlLockEvent = "SELECT EVENT_NAME, MAX_CAPACITY, STATUS FROM EVENT WHERE EVENT_ID = ? FOR UPDATE";
            psLockEvent = conn.prepareStatement(sqlLockEvent);
            psLockEvent.setInt(1, eventId);
            rs = psLockEvent.executeQuery();

            if (!rs.next()) {
                throw new SQLException("Selected Event ID " + eventId + " does not exist.");
            }

            String eventName = rs.getString("EVENT_NAME");
            int maxCapacity = rs.getInt("MAX_CAPACITY");
            String eventStatus = rs.getString("STATUS");
            rs.close();

            if ("CANCELLED".equalsIgnoreCase(eventStatus)) {
                throw new SQLException("Cannot register: Event '" + eventName + "' is CANCELLED.");
            }
            if ("COMPLETED".equalsIgnoreCase(eventStatus)) {
                throw new SQLException("Cannot register: Event '" + eventName + "' has already COMPLETED.");
            }

            // Step 2: Validate Participant exists
            String sqlCheckPart = "SELECT PARTICIPANT_ID, NAME FROM PARTICIPANT WHERE PARTICIPANT_ID = ?";
            psCheckParticipant = conn.prepareStatement(sqlCheckPart);
            psCheckParticipant.setInt(1, participantId);
            rs = psCheckParticipant.executeQuery();
            if (!rs.next()) {
                throw new SQLException("Participant ID " + participantId + " does not exist.");
            }
            String participantName = rs.getString("NAME");
            rs.close();

            // Step 3: Check for duplicate registration
            String sqlCheckDup = "SELECT REGISTRATION_ID, STATUS FROM REGISTRATION WHERE EVENT_ID = ? AND PARTICIPANT_ID = ?";
            psCheckDup = conn.prepareStatement(sqlCheckDup);
            psCheckDup.setInt(1, eventId);
            psCheckDup.setInt(2, participantId);
            rs = psCheckDup.executeQuery();

            if (rs.next()) {
                int existingRegId = rs.getInt("REGISTRATION_ID");
                String existingStatus = rs.getString("STATUS");
                rs.close();

                if ("REGISTERED".equalsIgnoreCase(existingStatus) || "ATTENDED".equalsIgnoreCase(existingStatus)) {
                    throw new SQLException("Participant '" + participantName + "' is already registered for '" + eventName + "' (Registration ID: " + existingRegId + ").");
                } else if ("CANCELLED".equalsIgnoreCase(existingStatus)) {
                    // Re-activate previously cancelled registration
                    String sqlReactivate = "UPDATE REGISTRATION SET STATUS = 'REGISTERED', REGISTRATION_DATE = SYSDATE WHERE REGISTRATION_ID = ?";
                    try (PreparedStatement psRe = conn.prepareStatement(sqlReactivate)) {
                        psRe.setInt(1, existingRegId);
                        psRe.executeUpdate();
                    }
                    conn.commit(); // COMMIT TRANSACTION
                    return true;
                }
            }
            if (rs != null && !rs.isClosed()) {
                rs.close();
            }

            // Step 4: Check capacity against current active registrations
            String sqlCountActive = "SELECT COUNT(*) FROM REGISTRATION WHERE EVENT_ID = ? AND STATUS != 'CANCELLED'";
            psCountActive = conn.prepareStatement(sqlCountActive);
            psCountActive.setInt(1, eventId);
            rs = psCountActive.executeQuery();

            int currentCount = 0;
            if (rs.next()) {
                currentCount = rs.getInt(1);
            }
            rs.close();

            if (currentCount >= maxCapacity) {
                throw new SQLException("Registration failed: Event capacity reached (" + currentCount + "/" + maxCapacity + ").");
            }

            // Step 5: Insert new registration
            String sqlInsert = "INSERT INTO REGISTRATION (REGISTRATION_ID, EVENT_ID, PARTICIPANT_ID, REGISTRATION_DATE, STATUS) " +
                               "VALUES (SEQ_REGISTRATION.NEXTVAL, ?, ?, SYSDATE, ?)";
            psInsert = conn.prepareStatement(sqlInsert);
            psInsert.setInt(1, eventId);
            psInsert.setInt(2, participantId);
            psInsert.setString(3, (status != null && !status.isEmpty()) ? status : "REGISTERED");

            int rows = psInsert.executeUpdate();
            if (rows != 1) {
                throw new SQLException("Failed to record registration row.");
            }

            // COMMIT TRANSACTION
            conn.commit();
            return true;

        } catch (SQLException ex) {
            // ROLLBACK ON ANY ERROR
            if (conn != null) {
                try {
                    System.err.println("[RegistrationDAO] Rolling back transaction due to error: " + ex.getMessage());
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("[RegistrationDAO] Rollback error: " + rollbackEx.getMessage());
                }
            }
            throw ex; // Re-throw to inform caller/UI
        } finally {
            DatabaseConnection.closeQuietly(rs, psLockEvent, psCheckParticipant, psCheckDup, psCountActive, psInsert);
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    public int updateStatus(int registrationId, String status) throws SQLException {
        String sql = "UPDATE REGISTRATION SET STATUS = ? WHERE REGISTRATION_ID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, registrationId);
            return ps.executeUpdate();
        }
    }

    public int delete(int registrationId) throws SQLException {
        String sql = "DELETE FROM REGISTRATION WHERE REGISTRATION_ID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, registrationId);
            return ps.executeUpdate();
        }
    }

    private Registration extractFromResultSet(ResultSet rs) throws SQLException {
        Registration r = new Registration();
        r.setRegistrationId(rs.getInt("REGISTRATION_ID"));
        r.setEventId(rs.getInt("EVENT_ID"));
        r.setParticipantId(rs.getInt("PARTICIPANT_ID"));
        r.setRegistrationDate(rs.getDate("REGISTRATION_DATE"));
        r.setStatus(rs.getString("STATUS"));

        r.setEventName(rs.getString("EVENT_NAME"));
        r.setEventDate(rs.getDate("EVENT_DATE"));
        r.setVenueName(rs.getString("VENUE_NAME"));
        r.setParticipantName(rs.getString("PARTICIPANT_NAME"));
        r.setParticipantEmail(rs.getString("PARTICIPANT_EMAIL"));
        r.setParticipantCollege(rs.getString("PARTICIPANT_COLLEGE"));
        r.setParticipantPhone(rs.getString("PARTICIPANT_PHONE"));
        return r;
    }
}
