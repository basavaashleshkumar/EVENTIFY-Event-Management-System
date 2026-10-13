package dao;

import db.DatabaseConnection;
import model.Organizer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for ORGANIZER table.
 */
public class OrganizerDAO {

    public List<Organizer> findAll() throws SQLException {
        List<Organizer> list = new ArrayList<>();
        String sql = "SELECT ORGANIZER_ID, NAME, EMAIL, PHONE, DEPARTMENT, USERNAME " +
                     "FROM ORGANIZER ORDER BY NAME";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(extractFromResultSet(rs));
            }
        }
        return list;
    }

    public Organizer findById(int organizerId) throws SQLException {
        String sql = "SELECT ORGANIZER_ID, NAME, EMAIL, PHONE, DEPARTMENT, USERNAME " +
                     "FROM ORGANIZER WHERE ORGANIZER_ID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, organizerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public Organizer findByUsername(String username) throws SQLException {
        String sql = "SELECT ORGANIZER_ID, NAME, EMAIL, PHONE, DEPARTMENT, USERNAME " +
                     "FROM ORGANIZER WHERE LOWER(USERNAME) = LOWER(?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public List<Organizer> search(String keyword) throws SQLException {
        List<Organizer> list = new ArrayList<>();
        String sql = "SELECT ORGANIZER_ID, NAME, EMAIL, PHONE, DEPARTMENT, USERNAME " +
                     "FROM ORGANIZER " +
                     "WHERE LOWER(NAME) LIKE ? OR LOWER(EMAIL) LIKE ? OR LOWER(DEPARTMENT) LIKE ? OR PHONE LIKE ? " +
                     "ORDER BY NAME";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String pattern = "%" + (keyword == null ? "" : keyword.toLowerCase().trim()) + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractFromResultSet(rs));
                }
            }
        }
        return list;
    }

    public int insert(Organizer org) throws SQLException {
        String sql = "INSERT INTO ORGANIZER (ORGANIZER_ID, NAME, EMAIL, PHONE, DEPARTMENT, USERNAME) " +
                     "VALUES (SEQ_ORGANIZER.NEXTVAL, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, org.getName());
            ps.setString(2, org.getEmail());
            ps.setString(3, org.getPhone());
            ps.setString(4, org.getDepartment());
            ps.setString(5, org.getUsername());

            return ps.executeUpdate();
        }
    }

    public int update(Organizer org) throws SQLException {
        String sql = "UPDATE ORGANIZER SET NAME = ?, EMAIL = ?, PHONE = ?, DEPARTMENT = ?, USERNAME = ? " +
                     "WHERE ORGANIZER_ID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, org.getName());
            ps.setString(2, org.getEmail());
            ps.setString(3, org.getPhone());
            ps.setString(4, org.getDepartment());
            ps.setString(5, org.getUsername());
            ps.setInt(6, org.getOrganizerId());

            return ps.executeUpdate();
        }
    }

    public int delete(int organizerId) throws SQLException {
        String sql = "DELETE FROM ORGANIZER WHERE ORGANIZER_ID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, organizerId);
            return ps.executeUpdate();
        }
    }

    public boolean hasEvents(int organizerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM EVENT WHERE ORGANIZER_ID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, organizerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private Organizer extractFromResultSet(ResultSet rs) throws SQLException {
        return new Organizer(
                rs.getInt("ORGANIZER_ID"),
                rs.getString("NAME"),
                rs.getString("EMAIL"),
                rs.getString("PHONE"),
                rs.getString("DEPARTMENT"),
                rs.getString("USERNAME")
        );
    }
}
