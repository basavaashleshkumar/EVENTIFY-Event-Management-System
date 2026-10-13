package dao;

import db.DatabaseConnection;
import model.Venue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for VENUE table.
 */
public class VenueDAO {

    public List<Venue> findAll() throws SQLException {
        List<Venue> list = new ArrayList<>();
        String sql = "SELECT VENUE_ID, VENUE_NAME, LOCATION, CAPACITY, AVAILABLE " +
                     "FROM VENUE ORDER BY VENUE_NAME";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(extractFromResultSet(rs));
            }
        }
        return list;
    }

    public List<Venue> findAvailable() throws SQLException {
        List<Venue> list = new ArrayList<>();
        String sql = "SELECT VENUE_ID, VENUE_NAME, LOCATION, CAPACITY, AVAILABLE " +
                     "FROM VENUE WHERE AVAILABLE = 'YES' ORDER BY VENUE_NAME";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(extractFromResultSet(rs));
            }
        }
        return list;
    }

    public Venue findById(int venueId) throws SQLException {
        String sql = "SELECT VENUE_ID, VENUE_NAME, LOCATION, CAPACITY, AVAILABLE " +
                     "FROM VENUE WHERE VENUE_ID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, venueId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public List<Venue> search(String keyword) throws SQLException {
        List<Venue> list = new ArrayList<>();
        String sql = "SELECT VENUE_ID, VENUE_NAME, LOCATION, CAPACITY, AVAILABLE " +
                     "FROM VENUE " +
                     "WHERE LOWER(VENUE_NAME) LIKE ? OR LOWER(LOCATION) LIKE ? " +
                     "ORDER BY VENUE_NAME";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String pattern = "%" + (keyword == null ? "" : keyword.toLowerCase().trim()) + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractFromResultSet(rs));
                }
            }
        }
        return list;
    }

    public int insert(Venue v) throws SQLException {
        String sql = "INSERT INTO VENUE (VENUE_ID, VENUE_NAME, LOCATION, CAPACITY, AVAILABLE) " +
                     "VALUES (SEQ_VENUE.NEXTVAL, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, v.getVenueName());
            ps.setString(2, v.getLocation());
            ps.setInt(3, v.getCapacity());
            ps.setString(4, v.getAvailable());

            return ps.executeUpdate();
        }
    }

    public int update(Venue v) throws SQLException {
        String sql = "UPDATE VENUE SET VENUE_NAME = ?, LOCATION = ?, CAPACITY = ?, AVAILABLE = ? " +
                     "WHERE VENUE_ID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, v.getVenueName());
            ps.setString(2, v.getLocation());
            ps.setInt(3, v.getCapacity());
            ps.setString(4, v.getAvailable());
            ps.setInt(5, v.getVenueId());

            return ps.executeUpdate();
        }
    }

    public int delete(int venueId) throws SQLException {
        String sql = "DELETE FROM VENUE WHERE VENUE_ID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, venueId);
            return ps.executeUpdate();
        }
    }

    public boolean hasEvents(int venueId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM EVENT WHERE VENUE_ID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, venueId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private Venue extractFromResultSet(ResultSet rs) throws SQLException {
        return new Venue(
                rs.getInt("VENUE_ID"),
                rs.getString("VENUE_NAME"),
                rs.getString("LOCATION"),
                rs.getInt("CAPACITY"),
                rs.getString("AVAILABLE")
        );
    }
}
