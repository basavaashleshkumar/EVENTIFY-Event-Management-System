package service;

import dao.UserDAO;
import model.User;

import java.sql.SQLException;

/**
 * Service managing user authentication, sessions, and role checks.
 */
public class AuthenticationService {

    private static User currentUser = null;
    private final UserDAO userDAO;

    public AuthenticationService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Authenticates the user against Oracle USERS table using PreparedStatement.
     */
    public User login(String username, String password) throws SQLException {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Username and password cannot be empty.");
        }

        User user = userDAO.authenticate(username.trim(), password.trim());
        if (user != null) {
            currentUser = user;
            System.out.println("[AuthenticationService] User logged in: " + user.getUsername() + " (" + user.getRole() + ")");
        }
        return user;
    }

    public static void logout() {
        if (currentUser != null) {
            System.out.println("[AuthenticationService] Logging out user: " + currentUser.getUsername());
        }
        currentUser = null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    public static boolean isOrganizer() {
        return currentUser != null && currentUser.isOrganizer();
    }
}
