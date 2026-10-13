package model;

/**
 * Model class representing a system user with authentication and role authorization.
 */
public class User {
    private int userId;
    private String username;
    private String password;
    private String role; // "ADMIN" or "ORGANIZER"
    private String fullName;
    private String email;

    public User() {
    }

    public User(int userId, String username, String password, String role, String fullName, String email) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.email = email;
    }

    public User(String username, String password, String role, String fullName, String email) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.email = email;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(this.role);
    }

    public boolean isOrganizer() {
        return "ORGANIZER".equalsIgnoreCase(this.role);
    }

    @Override
    public String toString() {
        return fullName != null && !fullName.isEmpty() ? fullName + " (" + role + ")" : username;
    }
}
