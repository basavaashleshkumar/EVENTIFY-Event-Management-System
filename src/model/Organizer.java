package model;

/**
 * Model class representing an Event Organizer.
 */
public class Organizer {
    private int organizerId;
    private String name;
    private String email;
    private String phone;
    private String department;
    private String username;

    public Organizer() {
    }

    public Organizer(int organizerId, String name, String email, String phone, String department, String username) {
        this.organizerId = organizerId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.department = department;
        this.username = username;
    }

    public Organizer(String name, String email, String phone, String department, String username) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.department = department;
        this.username = username;
    }

    public int getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(int organizerId) {
        this.organizerId = organizerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return name + " (" + department + ")";
    }
}
