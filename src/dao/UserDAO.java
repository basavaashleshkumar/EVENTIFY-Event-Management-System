package dao;

import db.DatabaseConnection;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for USERS table.
 * Demonstrates: PreparedStatement parameterized queries for secure authentication.
 */
public class UserDAO {

    /**
     * Authenticates a user with username and password.
     * Prevents SQL injection by using PreparedStatement.
     */
    public User authenticate(String username, String password) throws SQLException {
        String sql = "SELECT USER_ID, USERNAME, PASSWORD, ROLE, FULL_NAME, EMAIL " +
                     "FROM USERS " +
                     "WHERE LOWER(USERNAME) = LOWER(?) AND PASSWORD = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractUserFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT USER_ID, USERNAME, PASSWORD, ROLE, FULL_NAME, EMAIL " +
                     "FROM USERS WHERE LOWER(USERNAME) = LOWER(?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractUserFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public User findById(int userId) throws SQLException {
        String sql = "SELECT USER_ID, USERNAME, PASSWORD, ROLE, FULL_NAME, EMAIL " +
                     "FROM USERS WHERE USER_ID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractUserFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public List<User> findAll() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT USER_ID, USERNAME, PASSWORD, ROLE, FULL_NAME, EMAIL " +
                     "FROM USERS ORDER BY USER_ID";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(extractUserFromResultSet(rs));
            }
        }
        return list;
    }

    public int insert(User user) throws SQLException {
        String sql = "INSERT INTO USERS (USER_ID, USERNAME, PASSWORD, ROLE, FULL_NAME, EMAIL) " +
                     "VALUES (SEQ_USERS.NEXTVAL, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getRole());
            ps.setString(4, user.getFullName());
            ps.setString(5, user.getEmail());

            return ps.executeUpdate();
        }
    }

    public int update(User user) throws SQLException {
        String sql = "UPDATE USERS SET PASSWORD = ?, ROLE = ?, FULL_NAME = ?, EMAIL = ? " +
                     "WHERE USER_ID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getPassword());
            ps.setString(2, user.getRole());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getEmail());
            ps.setInt(5, user.getUserId());

            return ps.executeUpdate();
        }
    }

    public int delete(int userId) throws SQLException {
        String sql = "DELETE FROM USERS WHERE USER_ID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            return ps.executeUpdate();
        }
    }

    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("USER_ID"),
                rs.getString("USERNAME"),
                rs.getString("PASSWORD"),
                rs.getString("ROLE"),
                rs.getString("FULL_NAME"),
                rs.getString("EMAIL")
        );
    }
}
