package model;

import java.sql.Date;

/**
 * Model class representing a Registration record tying an Event to a Participant.
 */
public class Registration {
    private int registrationId;
    private int eventId;
    private int participantId;
    private Date registrationDate;
    private String status; // REGISTERED, CANCELLED, ATTENDED

    // Joined fields for display
    private String eventName;
    private String participantName;
    private String participantEmail;
    private String participantCollege;
    private String participantPhone;
    private Date eventDate;
    private String venueName;

    public Registration() {
    }

    public Registration(int registrationId, int eventId, int participantId, Date registrationDate, String status) {
        this.registrationId = registrationId;
        this.eventId = eventId;
        this.participantId = participantId;
        this.registrationDate = registrationDate;
        this.status = status;
    }

    public int getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(int registrationId) {
        this.registrationId = registrationId;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public int getParticipantId() {
        return participantId;
    }

    public void setParticipantId(int participantId) {
        this.participantId = participantId;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getParticipantName() {
        return participantName;
    }

    public void setParticipantName(String participantName) {
        this.participantName = participantName;
    }

    public String getParticipantEmail() {
        return participantEmail;
    }

    public void setParticipantEmail(String participantEmail) {
        this.participantEmail = participantEmail;
    }

    public String getParticipantCollege() {
        return participantCollege;
    }

    public void setParticipantCollege(String participantCollege) {
        this.participantCollege = participantCollege;
    }

    public String getParticipantPhone() {
        return participantPhone;
    }

    public void setParticipantPhone(String participantPhone) {
        this.participantPhone = participantPhone;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public String getVenueName() {
        return venueName;
    }

    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }

    @Override
    public String toString() {
        return "Registration #" + registrationId + " - " + participantName + " -> " + eventName;
    }
}
