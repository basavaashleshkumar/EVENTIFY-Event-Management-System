package dao;

import db.DatabaseConnection;
import model.Event;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for EVENT table.
 * Demonstrates:
 * - Complex SQL JOINs (EVENT + ORGANIZER + VENUE + REGISTRATION count subquery)
 * - Parameterized PreparedStatement queries
 * - Capacity verification
 */
public class EventDAO {

    private static final String BASE_SELECT =
        "SELECT e.EVENT_ID, e.EVENT_NAME, e.EVENT_TYPE, e.DESCRIPTION, e.EVENT_DATE, " +
        "       e.START_TIME, e.END_TIME, e.ORGANIZER_ID, e.VENUE_ID, e.MAX_CAPACITY, " +
        "       e.REGISTRATION_FEE, e.STATUS, " +
        "       o.NAME AS ORGANIZER_NAME, " +
        "       v.VENUE_NAME, v.CAPACITY AS VENUE_CAPACITY, " +
        "       NVL(r.REG_COUNT, 0) AS REG_COUNT " +
        "FROM EVENT e " +
        "JOIN ORGANIZER o ON e.ORGANIZER_ID = o.ORGANIZER_ID " +
        "JOIN VENUE v ON e.VENUE_ID = v.VENUE_ID " +
        "LEFT JOIN ( " +
        "    SELECT EVENT_ID, COUNT(*) AS REG_COUNT " +
        "    FROM REGISTRATION " +
        "    WHERE STATUS != 'CANCELLED' " +
        "    GROUP BY EVENT_ID " +
        ") r ON e.EVENT_ID = r.EVENT_ID ";

    public List<Event> findAll() throws SQLException {
        List<Event> list = new ArrayList<>();
        String sql = BASE_SELECT + " ORDER BY e.EVENT_DATE ASC, e.EVENT_ID ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(extractFromResultSet(rs));
            }
        }
        return list;
    }

    public Event findById(int eventId) throws SQLException {
        String sql = BASE_SELECT + " WHERE e.EVENT_ID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public List<Event> findByOrganizer(int organizerId) throws SQLException {
        List<Event> list = new ArrayList<>();
        String sql = BASE_SELECT + " WHERE e.ORGANIZER_ID = ? ORDER BY e.EVENT_DATE ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, organizerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractFromResultSet(rs));
                }
            }
        }
        return list;
    }

    public List<Event> findByOrganizerUsername(String username) throws SQLException {
        List<Event> list = new ArrayList<>();
        String sql = BASE_SELECT + " WHERE LOWER(o.USERNAME) = LOWER(?) ORDER BY e.EVENT_DATE ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractFromResultSet(rs));
                }
            }
        }
        return list;
    }

    public List<Event> findUpcoming() throws SQLException {
        List<Event> list = new ArrayList<>();
        String sql = BASE_SELECT + " WHERE e.STATUS = 'UPCOMING' ORDER BY e.EVENT_DATE ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(extractFromResultSet(rs));
            }
        }
        return list;
    }

    /**
     * Flexible multi-criteria search and filtering.
     */
    public List<Event> search(String keyword, String statusFilter, String typeFilter, Integer organizerId) throws SQLException {
        List<Event> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT);
        sql.append(" WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            String trimmed = keyword.trim();
            String pattern = "%" + trimmed.toLowerCase() + "%";
            boolean isNumeric = false;
            int idVal = -1;
            try {
                idVal = Integer.parseInt(trimmed);
                isNumeric = true;
            } catch (NumberFormatException ignored) {
            }

            if (isNumeric) {
                sql.append(" AND (LOWER(e.EVENT_NAME) LIKE ? OR LOWER(e.DESCRIPTION) LIKE ? ")
                   .append(" OR LOWER(v.VENUE_NAME) LIKE ? OR LOWER(o.NAME) LIKE ? OR e.EVENT_ID = ?) ");
                params.add(pattern);
                params.add(pattern);
                params.add(pattern);
                params.add(pattern);
                params.add(idVal);
            } else {
                sql.append(" AND (LOWER(e.EVENT_NAME) LIKE ? OR LOWER(e.DESCRIPTION) LIKE ? ")
                   .append(" OR LOWER(v.VENUE_NAME) LIKE ? OR LOWER(o.NAME) LIKE ?) ");
                params.add(pattern);
                params.add(pattern);
                params.add(pattern);
                params.add(pattern);
            }
        }


        if (statusFilter != null && !statusFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(statusFilter)) {
            sql.append(" AND e.STATUS = ? ");
            params.add(statusFilter.toUpperCase().trim());
        }

        if (typeFilter != null && !typeFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(typeFilter)) {
            sql.append(" AND LOWER(e.EVENT_TYPE) = LOWER(?) ");
            params.add(typeFilter.trim());
        }

        if (organizerId != null && organizerId > 0) {
            sql.append(" AND e.ORGANIZER_ID = ? ");
            params.add(organizerId);
        }

        sql.append(" ORDER BY e.EVENT_DATE ASC, e.EVENT_ID ASC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof Integer) {
                    ps.setInt(i + 1, (Integer) p);
                } else if (p instanceof Double) {
                    ps.setDouble(i + 1, (Double) p);
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

    public int insert(Event e) throws SQLException {
        String sql = "INSERT INTO EVENT (EVENT_ID, EVENT_NAME, EVENT_TYPE, DESCRIPTION, " +
                     "EVENT_DATE, START_TIME, END_TIME, ORGANIZER_ID, VENUE_ID, " +
                     "MAX_CAPACITY, REGISTRATION_FEE, STATUS) " +
                     "VALUES (SEQ_EVENT.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, e.getEventName());
            ps.setString(2, e.getEventType());
            ps.setString(3, e.getDescription());
            ps.setDate(4, e.getEventDate());
            ps.setString(5, e.getStartTime());
            ps.setString(6, e.getEndTime());
            ps.setInt(7, e.getOrganizerId());
            ps.setInt(8, e.getVenueId());
            ps.setInt(9, e.getMaxCapacity());
            ps.setDouble(10, e.getRegistrationFee());
            ps.setString(11, e.getStatus());

            return ps.executeUpdate();
        }
    }

    public int update(Event e) throws SQLException {
        String sql = "UPDATE EVENT SET EVENT_NAME = ?, EVENT_TYPE = ?, DESCRIPTION = ?, " +
                     "EVENT_DATE = ?, START_TIME = ?, END_TIME = ?, ORGANIZER_ID = ?, " +
                     "VENUE_ID = ?, MAX_CAPACITY = ?, REGISTRATION_FEE = ?, STATUS = ? " +
                     "WHERE EVENT_ID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, e.getEventName());
            ps.setString(2, e.getEventType());
            ps.setString(3, e.getDescription());
            ps.setDate(4, e.getEventDate());
            ps.setString(5, e.getStartTime());
            ps.setString(6, e.getEndTime());
            ps.setInt(7, e.getOrganizerId());
            ps.setInt(8, e.getVenueId());
            ps.setInt(9, e.getMaxCapacity());
            ps.setDouble(10, e.getRegistrationFee());
            ps.setString(11, e.getStatus());
            ps.setInt(12, e.getEventId());

            return ps.executeUpdate();
        }
    }

    public int updateStatus(int eventId, String status) throws SQLException {
        String sql = "UPDATE EVENT SET STATUS = ? WHERE EVENT_ID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, eventId);
            return ps.executeUpdate();
        }
    }

    public int delete(int eventId) throws SQLException {
        String sql = "DELETE FROM EVENT WHERE EVENT_ID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, eventId);
            return ps.executeUpdate();
        }
    }

    public boolean hasRegistrations(int eventId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM REGISTRATION WHERE EVENT_ID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public int getActiveRegistrationCount(int eventId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM REGISTRATION WHERE EVENT_ID = ? AND STATUS != 'CANCELLED'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public List<String> getDistinctEventTypes() throws SQLException {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT EVENT_TYPE FROM EVENT WHERE EVENT_TYPE IS NOT NULL ORDER BY EVENT_TYPE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(rs.getString("EVENT_TYPE"));
            }
        }
        return list;
    }

    private Event extractFromResultSet(ResultSet rs) throws SQLException {
        Event e = new Event();
        e.setEventId(rs.getInt("EVENT_ID"));
        e.setEventName(rs.getString("EVENT_NAME"));
        e.setEventType(rs.getString("EVENT_TYPE"));
        e.setDescription(rs.getString("DESCRIPTION"));
        e.setEventDate(rs.getDate("EVENT_DATE"));
        e.setStartTime(rs.getString("START_TIME"));
        e.setEndTime(rs.getString("END_TIME"));
        e.setOrganizerId(rs.getInt("ORGANIZER_ID"));
        e.setVenueId(rs.getInt("VENUE_ID"));
        e.setMaxCapacity(rs.getInt("MAX_CAPACITY"));
        e.setRegistrationFee(rs.getDouble("REGISTRATION_FEE"));
        e.setStatus(rs.getString("STATUS"));

        e.setOrganizerName(rs.getString("ORGANIZER_NAME"));
        e.setVenueName(rs.getString("VENUE_NAME"));
        e.setVenueCapacity(rs.getInt("VENUE_CAPACITY"));
        e.setCurrentRegistrations(rs.getInt("REG_COUNT"));
        return e;
    }
}
