package dao;

import db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for Reports and Dashboard Analytical Queries.
 * Demonstrates:
 * - Aggregation functions (COUNT, MAX, GROUP BY, NVL)
 * - Multi-table SQL JOIN queries
 * - Dynamic metadata extraction for report generation and CSV export
 */
public class ReportDAO {

    public static class ReportResult {
        private final String[] columns;
        private final List<Object[]> data;

        public ReportResult(String[] columns, List<Object[]> data) {
            this.columns = columns;
            this.data = data;
        }

        public String[] getColumns() {
            return columns;
        }

        public List<Object[]> getData() {
            return data;
        }
    }

    /**
     * Executes any analytical report SQL and converts the ResultSet into ReportResult.
     */
    private ReportResult executeQueryToReport(String sql, Object... params) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();
                String[] colNames = new String[colCount];
                for (int i = 1; i <= colCount; i++) {
                    colNames[i - 1] = meta.getColumnLabel(i);
                }

                List<Object[]> rows = new ArrayList<>();
                while (rs.next()) {
                    Object[] row = new Object[colCount];
                    for (int i = 1; i <= colCount; i++) {
                        row[i - 1] = rs.getObject(i);
                    }
                    rows.add(row);
                }
                return new ReportResult(colNames, rows);
            }
        }
    }

    // 1. All Events
    public ReportResult reportAllEvents() throws SQLException {
        String sql =
            "SELECT e.EVENT_ID AS \"Event ID\", e.EVENT_NAME AS \"Event Name\", e.EVENT_TYPE AS \"Type\", " +
            "       TO_CHAR(e.EVENT_DATE, 'YYYY-MM-DD') AS \"Date\", e.START_TIME AS \"Start\", e.END_TIME AS \"End\", " +
            "       o.NAME AS \"Organizer\", v.VENUE_NAME AS \"Venue\", " +
            "       e.MAX_CAPACITY AS \"Capacity\", NVL(r.REG_COUNT, 0) AS \"Registrations\", " +
            "       e.REGISTRATION_FEE AS \"Fee (INR)\", e.STATUS AS \"Status\" " +
            "FROM EVENT e " +
            "JOIN ORGANIZER o ON e.ORGANIZER_ID = o.ORGANIZER_ID " +
            "JOIN VENUE v ON e.VENUE_ID = v.VENUE_ID " +
            "LEFT JOIN (SELECT EVENT_ID, COUNT(*) AS REG_COUNT FROM REGISTRATION WHERE STATUS != 'CANCELLED' GROUP BY EVENT_ID) r " +
            "       ON e.EVENT_ID = r.EVENT_ID " +
            "ORDER BY e.EVENT_DATE ASC";
        return executeQueryToReport(sql);
    }

    // 2. Upcoming Events
    public ReportResult reportUpcomingEvents() throws SQLException {
        String sql =
            "SELECT e.EVENT_ID AS \"Event ID\", e.EVENT_NAME AS \"Event Name\", e.EVENT_TYPE AS \"Type\", " +
            "       TO_CHAR(e.EVENT_DATE, 'YYYY-MM-DD') AS \"Date\", e.START_TIME AS \"Start Time\", " +
            "       o.NAME AS \"Organizer\", v.VENUE_NAME AS \"Venue\", " +
            "       e.MAX_CAPACITY AS \"Max Capacity\", NVL(r.REG_COUNT, 0) AS \"Booked\", " +
            "       (e.MAX_CAPACITY - NVL(r.REG_COUNT, 0)) AS \"Seats Left\" " +
            "FROM EVENT e " +
            "JOIN ORGANIZER o ON e.ORGANIZER_ID = o.ORGANIZER_ID " +
            "JOIN VENUE v ON e.VENUE_ID = v.VENUE_ID " +
            "LEFT JOIN (SELECT EVENT_ID, COUNT(*) AS REG_COUNT FROM REGISTRATION WHERE STATUS != 'CANCELLED' GROUP BY EVENT_ID) r " +
            "       ON e.EVENT_ID = r.EVENT_ID " +
            "WHERE e.STATUS = 'UPCOMING' " +
            "ORDER BY e.EVENT_DATE ASC";
        return executeQueryToReport(sql);
    }

    // 3. Completed Events
    public ReportResult reportCompletedEvents() throws SQLException {
        String sql =
            "SELECT e.EVENT_ID AS \"Event ID\", e.EVENT_NAME AS \"Event Name\", e.EVENT_TYPE AS \"Type\", " +
            "       TO_CHAR(e.EVENT_DATE, 'YYYY-MM-DD') AS \"Date Completed\", " +
            "       o.NAME AS \"Organizer\", v.VENUE_NAME AS \"Venue\", " +
            "       NVL(r.ATTENDED_COUNT, 0) AS \"Attended Count\" " +
            "FROM EVENT e " +
            "JOIN ORGANIZER o ON e.ORGANIZER_ID = o.ORGANIZER_ID " +
            "JOIN VENUE v ON e.VENUE_ID = v.VENUE_ID " +
            "LEFT JOIN (SELECT EVENT_ID, COUNT(*) AS ATTENDED_COUNT FROM REGISTRATION WHERE STATUS = 'ATTENDED' GROUP BY EVENT_ID) r " +
            "       ON e.EVENT_ID = r.EVENT_ID " +
            "WHERE e.STATUS = 'COMPLETED' " +
            "ORDER BY e.EVENT_DATE DESC";
        return executeQueryToReport(sql);
    }

    // 4. Events by Organizer
    public ReportResult reportEventsByOrganizer() throws SQLException {
        String sql =
            "SELECT o.ORGANIZER_ID AS \"Organizer ID\", o.NAME AS \"Organizer Name\", o.DEPARTMENT AS \"Department\", " +
            "       e.EVENT_NAME AS \"Event Name\", e.EVENT_TYPE AS \"Event Type\", " +
            "       TO_CHAR(e.EVENT_DATE, 'YYYY-MM-DD') AS \"Date\", e.STATUS AS \"Event Status\" " +
            "FROM ORGANIZER o " +
            "LEFT JOIN EVENT e ON o.ORGANIZER_ID = e.ORGANIZER_ID " +
            "ORDER BY o.NAME ASC, e.EVENT_DATE ASC";
        return executeQueryToReport(sql);
    }

    // 5. Events by Venue
    public ReportResult reportEventsByVenue() throws SQLException {
        String sql =
            "SELECT v.VENUE_ID AS \"Venue ID\", v.VENUE_NAME AS \"Venue Name\", v.LOCATION AS \"Location\", " +
            "       v.CAPACITY AS \"Venue Capacity\", v.AVAILABLE AS \"Available\", " +
            "       e.EVENT_NAME AS \"Scheduled Event\", TO_CHAR(e.EVENT_DATE, 'YYYY-MM-DD') AS \"Event Date\", " +
            "       e.STATUS AS \"Status\" " +
            "FROM VENUE v " +
            "LEFT JOIN EVENT e ON v.VENUE_ID = e.VENUE_ID " +
            "ORDER BY v.VENUE_NAME ASC, e.EVENT_DATE ASC";
        return executeQueryToReport(sql);
    }

    // 6. Participants in an Event
    public ReportResult reportParticipantsInEvent(int eventId) throws SQLException {
        String sql =
            "SELECT r.REGISTRATION_ID AS \"Reg ID\", e.EVENT_NAME AS \"Event Name\", " +
            "       p.PARTICIPANT_ID AS \"Participant ID\", p.NAME AS \"Participant Name\", " +
            "       p.EMAIL AS \"Email\", p.PHONE AS \"Phone\", p.COLLEGE AS \"College\", " +
            "       TO_CHAR(r.REGISTRATION_DATE, 'YYYY-MM-DD') AS \"Registered Date\", " +
            "       r.STATUS AS \"Reg Status\" " +
            "FROM REGISTRATION r " +
            "JOIN EVENT e ON r.EVENT_ID = e.EVENT_ID " +
            "JOIN PARTICIPANT p ON r.PARTICIPANT_ID = p.PARTICIPANT_ID " +
            (eventId > 0 ? "WHERE e.EVENT_ID = ? " : "") +
            "ORDER BY e.EVENT_NAME ASC, p.NAME ASC";

        if (eventId > 0) {
            return executeQueryToReport(sql, eventId);
        } else {
            return executeQueryToReport(sql);
        }
    }

    // 7. Registration Count per Event
    public ReportResult reportRegistrationCountPerEvent() throws SQLException {
        String sql =
            "SELECT e.EVENT_ID AS \"Event ID\", e.EVENT_NAME AS \"Event Name\", e.STATUS AS \"Status\", " +
            "       e.MAX_CAPACITY AS \"Capacity\", " +
            "       COUNT(CASE WHEN r.STATUS = 'REGISTERED' THEN 1 END) AS \"Registered\", " +
            "       COUNT(CASE WHEN r.STATUS = 'ATTENDED' THEN 1 END) AS \"Attended\", " +
            "       COUNT(CASE WHEN r.STATUS = 'CANCELLED' THEN 1 END) AS \"Cancelled\", " +
            "       COUNT(CASE WHEN r.STATUS != 'CANCELLED' THEN 1 END) AS \"Active Total\", " +
            "       (e.MAX_CAPACITY - COUNT(CASE WHEN r.STATUS != 'CANCELLED' THEN 1 END)) AS \"Remaining Seats\" " +
            "FROM EVENT e " +
            "LEFT JOIN REGISTRATION r ON e.EVENT_ID = r.EVENT_ID " +
            "GROUP BY e.EVENT_ID, e.EVENT_NAME, e.STATUS, e.MAX_CAPACITY " +
            "ORDER BY \"Active Total\" DESC";
        return executeQueryToReport(sql);
    }

    // 8. Events sorted by number of registrations
    public ReportResult reportEventsSortedByRegistrations() throws SQLException {
        String sql =
            "SELECT e.EVENT_ID AS \"Event ID\", e.EVENT_NAME AS \"Event Name\", e.EVENT_TYPE AS \"Type\", " +
            "       o.NAME AS \"Organizer\", v.VENUE_NAME AS \"Venue\", " +
            "       COUNT(r.REGISTRATION_ID) AS \"Total Registrations\", e.MAX_CAPACITY AS \"Max Capacity\", " +
            "       ROUND((COUNT(r.REGISTRATION_ID) / e.MAX_CAPACITY) * 100, 1) || '%' AS \"Occupancy Rate\" " +
            "FROM EVENT e " +
            "JOIN ORGANIZER o ON e.ORGANIZER_ID = o.ORGANIZER_ID " +
            "JOIN VENUE v ON e.VENUE_ID = v.VENUE_ID " +
            "LEFT JOIN REGISTRATION r ON e.EVENT_ID = r.EVENT_ID AND r.STATUS != 'CANCELLED' " +
            "GROUP BY e.EVENT_ID, e.EVENT_NAME, e.EVENT_TYPE, o.NAME, v.VENUE_NAME, e.MAX_CAPACITY " +
            "ORDER BY \"Total Registrations\" DESC";
        return executeQueryToReport(sql);
    }

    // 9. Available Venues
    public ReportResult reportAvailableVenues() throws SQLException {
        String sql =
            "SELECT VENUE_ID AS \"Venue ID\", VENUE_NAME AS \"Venue Name\", " +
            "       LOCATION AS \"Location\", CAPACITY AS \"Capacity\", AVAILABLE AS \"Status\" " +
            "FROM VENUE " +
            "WHERE AVAILABLE = 'YES' " +
            "ORDER BY CAPACITY DESC";
        return executeQueryToReport(sql);
    }

    // 10. Cancelled Events
    public ReportResult reportCancelledEvents() throws SQLException {
        String sql =
            "SELECT e.EVENT_ID AS \"Event ID\", e.EVENT_NAME AS \"Event Name\", e.EVENT_TYPE AS \"Type\", " +
            "       TO_CHAR(e.EVENT_DATE, 'YYYY-MM-DD') AS \"Event Date\", " +
            "       o.NAME AS \"Organizer\", v.VENUE_NAME AS \"Venue\", " +
            "       e.DESCRIPTION AS \"Remarks\" " +
            "FROM EVENT e " +
            "JOIN ORGANIZER o ON e.ORGANIZER_ID = o.ORGANIZER_ID " +
            "JOIN VENUE v ON e.VENUE_ID = v.VENUE_ID " +
            "WHERE e.STATUS = 'CANCELLED' " +
            "ORDER BY e.EVENT_DATE DESC";
        return executeQueryToReport(sql);
    }

    // =================================================================
    // DASHBOARD STATISTICS METHODS
    // =================================================================

    public Map<String, Object> getDashboardStats() throws SQLException {
        Map<String, Object> stats = new HashMap<>();

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Total Events
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM EVENT");
                 ResultSet rs = ps.executeQuery()) {
                stats.put("totalEvents", rs.next() ? rs.getInt(1) : 0);
            }

            // Upcoming Events
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM EVENT WHERE STATUS = 'UPCOMING'");
                 ResultSet rs = ps.executeQuery()) {
                stats.put("upcomingEvents", rs.next() ? rs.getInt(1) : 0);
            }

            // Completed Events
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM EVENT WHERE STATUS = 'COMPLETED'");
                 ResultSet rs = ps.executeQuery()) {
                stats.put("completedEvents", rs.next() ? rs.getInt(1) : 0);
            }

            // Cancelled Events
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM EVENT WHERE STATUS = 'CANCELLED'");
                 ResultSet rs = ps.executeQuery()) {
                stats.put("cancelledEvents", rs.next() ? rs.getInt(1) : 0);
            }

            // Total Participants
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM PARTICIPANT");
                 ResultSet rs = ps.executeQuery()) {
                stats.put("totalParticipants", rs.next() ? rs.getInt(1) : 0);
            }

            // Total Registrations
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM REGISTRATION WHERE STATUS != 'CANCELLED'");
                 ResultSet rs = ps.executeQuery()) {
                stats.put("totalRegistrations", rs.next() ? rs.getInt(1) : 0);
            }

            // Available Venues
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM VENUE WHERE AVAILABLE = 'YES'");
                 ResultSet rs = ps.executeQuery()) {
                stats.put("availableVenues", rs.next() ? rs.getInt(1) : 0);
            }

            // Event with Maximum Registrations
            String topEventSql =
                "SELECT e.EVENT_NAME, COUNT(r.REGISTRATION_ID) AS CNT " +
                "FROM EVENT e " +
                "JOIN REGISTRATION r ON e.EVENT_ID = r.EVENT_ID " +
                "WHERE r.STATUS != 'CANCELLED' " +
                "GROUP BY e.EVENT_ID, e.EVENT_NAME " +
                "ORDER BY CNT DESC";
            try (PreparedStatement ps = conn.prepareStatement(topEventSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stats.put("topEvent", rs.getString(1) + " (" + rs.getInt(2) + ")");
                } else {
                    stats.put("topEvent", "None yet");
                }
            }

            // Most Popular Event Type
            String popularTypeSql =
                "SELECT EVENT_TYPE, COUNT(*) AS CNT " +
                "FROM EVENT " +
                "GROUP BY EVENT_TYPE " +
                "ORDER BY CNT DESC";
            try (PreparedStatement ps = conn.prepareStatement(popularTypeSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stats.put("popularType", rs.getString(1) + " (" + rs.getInt(2) + " events)");
                } else {
                    stats.put("popularType", "N/A");
                }
            }
        }

        return stats;
    }
}
