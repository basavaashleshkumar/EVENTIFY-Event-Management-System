package model;

/**
 * Model class representing a Participant.
 */
public class Participant {
    private int participantId;
    private String name;
    private String email;
    private String phone;
    private String college;
    private String department;

    public Participant() {
    }

    public Participant(int participantId, String name, String email, String phone, String college, String department) {
        this.participantId = participantId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.college = college;
        this.department = department;
    }

    public Participant(String name, String email, String phone, String college, String department) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.college = college;
        this.department = department;
    }

    public int getParticipantId() {
        return participantId;
    }

    public void setParticipantId(int participantId) {
        this.participantId = participantId;
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

    public String getCollege() {
        return college;
    }

    public void setCollege(String college) {
        this.college = college;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public String toString() {
        return name + " (" + email + ")";
    }
}
