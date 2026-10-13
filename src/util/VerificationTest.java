package util;

import dao.ReportDAO;
import dao.ReportDAO.ReportResult;
import db.DatabaseConnection;
import model.Event;
import model.Participant;
import model.User;
import service.AuthenticationService;
import service.EventService;
import service.ParticipantService;
import service.RegistrationService;
import service.ReportService;

import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Comprehensive Automated Verification and CRUD Audit Suite for EVENTIFY.
 * Executes live tests directly against the Oracle Database.
 */
public class VerificationTest {

    private static int passCount = 0;
    private static int failCount = 0;
    private static int errorCount = 0;

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("        EVENTIFY - LIVE ORACLE DATABASE AUDIT & TEST SUITE       ");
        System.out.println("=================================================================\n");

        AuthenticationService authService = new AuthenticationService();
        EventService eventService = new EventService();
        ParticipantService participantService = new ParticipantService();
        RegistrationService registrationService = new RegistrationService();
        ReportService reportService = new ReportService();

        // 1. Database Connectivity
        runTest("Database Connectivity (Oracle Driver & TNS)", () -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn == null || conn.isClosed()) {
                    throw new Exception("Connection returned null or closed");
                }
                String version = conn.getMetaData().getDatabaseProductVersion();
                return "Connected successfully: " + version;
            }
        });

        // 2. Login (Valid credentials)
        runTest("Login (Valid Credentials: admin / admin123)", () -> {
            User u = authService.login("admin", "admin123");
            if (u != null && "ADMIN".equalsIgnoreCase(u.getRole())) {
                return "Authenticated as " + u.getUsername() + " (" + u.getRole() + ")";
            }
            throw new Exception("Authentication failed or returned invalid role");
        });

        // 3. Invalid Login Rejection
        runTest("Invalid Login Rejection (admin / wrongpass999)", () -> {
            User u = authService.login("admin", "wrongpass999");
            if (u == null) {
                return "Rejected correctly";
            }
            throw new Exception("Security breach: Invalid credentials accepted");
        });

        // 4. Add Event
        int[] createdEventId = new int[1];
        runTest("Add Event (Insert via PreparedStatement)", () -> {
            Event e = new Event();
            e.setEventName("Audit Automated Test Hackathon");
            e.setEventType("Hackathon");
            e.setDescription("Automated test event for technical audit.");
            e.setEventDate(Date.valueOf("2026-11-30"));
            e.setStartTime("10:00 AM");
            e.setEndTime("06:00 PM");
            e.setOrganizerId(1); // Dr. Rajesh Sharma
            e.setVenueId(2);     // Turing Lab (Cap: 120)
            e.setMaxCapacity(60);
            e.setRegistrationFee(50.0);
            e.setStatus("UPCOMING");

            int rows = eventService.addEvent(e);
            if (rows != 1) {
                throw new Exception("Expected 1 row affected, got " + rows);
            }

            // Find inserted event ID
            List<Event> list = eventService.searchEvents("Audit Automated Test Hackathon", null, null, null);
            if (list.isEmpty()) {
                throw new Exception("Inserted event could not be located in database");
            }
            createdEventId[0] = list.get(0).getEventId();
            return "Event inserted successfully with ID: " + createdEventId[0];
        });

        // 5. Update Event
        runTest("Update Event (Modify Name, Fee & Status)", () -> {
            Event e = eventService.getEventById(createdEventId[0]);
            if (e == null) throw new Exception("Event not found for update");
            e.setEventName("Audit Automated Test Hackathon - Updated");
            e.setRegistrationFee(75.0);
            e.setStatus("UPCOMING");
            int rows = eventService.updateEvent(e);
            if (rows != 1) throw new Exception("Expected 1 row updated, got " + rows);

            Event updated = eventService.getEventById(createdEventId[0]);
            if (!"Audit Automated Test Hackathon - Updated".equals(updated.getEventName()) || updated.getRegistrationFee() != 75.0) {
                throw new Exception("Updated values not reflected in database");
            }
            return "Event updated successfully: Fee ₹" + updated.getRegistrationFee();
        });

        // 6. Search Event
        runTest("Search Event (Multi-Criteria Filter)", () -> {
            List<Event> results = eventService.searchEvents("Audit Automated", "UPCOMING", null, null);
            if (results.isEmpty()) {
                throw new Exception("Search query returned 0 results for existing event");
            }
            return "Found " + results.size() + " matching event(s)";
        });

        // 7. Venue Capacity Validation (Event Cap > Venue Cap)
        runTest("Venue Capacity Validation (Event Cap > Venue Cap Rejection)", () -> {
            Event oversize = new Event();
            oversize.setEventName("Oversized Audit Event");
            oversize.setEventType("Conference");
            oversize.setEventDate(Date.valueOf("2026-12-05"));
            oversize.setStartTime("09:00 AM");
            oversize.setEndTime("05:00 PM");
            oversize.setOrganizerId(1);
            oversize.setVenueId(2); // Turing Lab capacity is 120
            oversize.setMaxCapacity(500); // 500 > 120!
            oversize.setRegistrationFee(100.0);
            oversize.setStatus("UPCOMING");

            try {
                eventService.addEvent(oversize);
                throw new Exception("Failed: Oversized capacity was accepted");
            } catch (IllegalArgumentException ex) {
                return "Blocked correctly: " + ex.getMessage();
            }
        });

        // 8. Add Participant
        int[] createdParticipantId = new int[1];
        String auditEmail = "audit.tester" + System.currentTimeMillis() + "@college.edu";
        runTest("Add Participant (Validation & Insert)", () -> {
            Participant p = new Participant(
                "Audit Test Participant",
                auditEmail,
                "9876500000",
                "College of Engineering Guindy",
                "Computer Science"
            );
            participantService.addParticipant(p);
            List<Participant> found = participantService.searchParticipants(auditEmail);
            if (found.isEmpty()) throw new Exception("Participant not found after insert");
            createdParticipantId[0] = found.get(0).getParticipantId();
            return "Participant created with ID: " + createdParticipantId[0];
        });

        // 9. Update Participant
        runTest("Update Participant (Modify Phone & Department)", () -> {
            Participant p = participantService.getParticipantById(createdParticipantId[0]);
            p.setPhone("9876599999");
            p.setDepartment("Artificial Intelligence");
            participantService.updateParticipant(p);
            Participant updated = participantService.getParticipantById(createdParticipantId[0]);
            if (!"9876599999".equals(updated.getPhone()) || !"Artificial Intelligence".equals(updated.getDepartment())) {
                throw new Exception("Participant updates did not persist");
            }
            return "Participant updated: Phone " + updated.getPhone() + ", Dept " + updated.getDepartment();
        });

        // 10. Register Participant (ACID Transaction)
        runTest("Register Participant (ACID Transaction Commit)", () -> {
            boolean ok = registrationService.register(createdEventId[0], createdParticipantId[0]);
            if (!ok) throw new Exception("Registration returned false");
            return "Participant ID " + createdParticipantId[0] + " registered for Event ID " + createdEventId[0];
        });

        // 11. Duplicate Registration Prevention (Unique Constraint & Rollback)
        runTest("Duplicate Registration Prevention (Transaction Rollback)", () -> {
            try {
                registrationService.register(createdEventId[0], createdParticipantId[0]);
                throw new Exception("Failed: Duplicate registration was permitted");
            } catch (SQLException ex) {
                return "Duplicate blocked and rolled back: " + ex.getMessage();
            }
        });

        // 12. Full Event Registration Rejection (Capacity Check)
        runTest("Full Event Registration Rejection (Capacity Limit Guard)", () -> {
            // Create mini-event with capacity = 1
            Event mini = new Event();
            mini.setEventName("Mini Capacity Test Event " + System.currentTimeMillis());
            mini.setEventType("Workshop");
            mini.setEventDate(Date.valueOf("2026-12-10"));
            mini.setStartTime("10:00 AM");
            mini.setEndTime("12:00 PM");
            mini.setOrganizerId(1);
            mini.setVenueId(2);
            mini.setMaxCapacity(1); // Capacity 1
            mini.setRegistrationFee(0.0);
            mini.setStatus("UPCOMING");
            eventService.addEvent(mini);

            List<Event> evList = eventService.searchEvents(mini.getEventName(), null, null, null);
            int miniId = evList.get(0).getEventId();

            // Register first participant
            registrationService.register(miniId, createdParticipantId[0]);

            String rejectionMsg = "";
            try {
                registrationService.register(miniId, 1);
                throw new Exception("Failed: Overbooking allowed on full event");
            } catch (SQLException ex) {
                rejectionMsg = ex.getMessage();
            }

            // Clean up mini event test registrations and mini event
            List<model.Registration> miniRegs = registrationService.getRegistrationsForEvent(miniId);
            for (model.Registration mr : miniRegs) {
                registrationService.deleteRegistration(mr.getRegistrationId());
            }
            eventService.deleteEvent(miniId);

            return "Enforced: " + rejectionMsg;
        });

        // 13. Event Deletion Protection (Child Registrations Exist)
        runTest("Event Deletion Protection (Safe Referential Integrity Guard)", () -> {
            try {
                eventService.deleteEvent(createdEventId[0]);
                throw new Exception("Failed: Event with active registrations was deleted");

            } catch (SQLException ex) {
                return "Deletion prevented: " + ex.getMessage();
            }
        });

        // 14. Delete Event (After removing test registrations)
        runTest("Delete Event (Clean Deletion without Registrations)", () -> {
            // Cancel/remove registration for test event
            List<model.Registration> regs = registrationService.getRegistrationsForEvent(createdEventId[0]);
            for (model.Registration r : regs) {
                registrationService.deleteRegistration(r.getRegistrationId());
            }

            int rows = eventService.deleteEvent(createdEventId[0]);
            if (rows != 1) throw new Exception("Expected 1 row deleted, got " + rows);
            return "Event ID " + createdEventId[0] + " deleted cleanly";
        });

        // 15. Delete Participant (Clean Deletion without Registrations)
        runTest("Delete Participant (Clean Deletion)", () -> {
            participantService.deleteParticipant(createdParticipantId[0]);
            Participant p = participantService.getParticipantById(createdParticipantId[0]);
            if (p != null) throw new Exception("Participant still exists after delete");
            return "Participant ID " + createdParticipantId[0] + " deleted successfully";
        });

        // 16. Analytical Reports Execution & Live Dashboard
        runTest("Analytical Reports & Dashboard Statistics (Multi-Table JOINs)", () -> {
            Map<String, Object> stats = reportService.getDashboardStatistics();
            ReportResult rep8 = reportService.getReport(8, 0); // Ranked by registrations
            if (stats.isEmpty() || rep8.getData().isEmpty()) {
                throw new Exception("Report or stats returned empty data");
            }
            return "Stats: " + stats.get("totalEvents") + " events, " + stats.get("totalRegistrations") +
                   " registrations. Report 8 rows: " + rep8.getData().size();
        });

        // 17. CSV Export Verification
        runTest("CSV Report Export (File I/O Verification)", () -> {
            ReportResult rep1 = reportService.getReport(1, 0);
            File tempCsv = new File("test_export_audit.csv");
            reportService.exportToCsv(rep1, tempCsv);

            if (!tempCsv.exists() || tempCsv.length() == 0) {
                throw new Exception("CSV file was not created or is empty");
            }

            List<String> lines = Files.readAllLines(tempCsv.toPath());
            tempCsv.delete(); // cleanup
            if (lines.size() <= 1) {
                throw new Exception("CSV file missing data rows (only header found)");
            }
            return "CSV export verified: " + lines.size() + " lines written successfully";
        });

        System.out.println("\n=================================================================");
        System.out.println("                         AUDIT SUMMARY                           ");
        System.out.println("=================================================================");
        System.out.println("TOTAL TESTS : " + (passCount + failCount + errorCount));
        System.out.println("PASSED      : " + passCount);
        System.out.println("FAILED      : " + failCount);
        System.out.println("ERRORS      : " + errorCount);
        System.out.println("STATUS      : " + (failCount == 0 && errorCount == 0 ? "ALL AUDIT TESTS PASSED" : "TEST FAILURES DETECTED"));
        System.out.println("=================================================================\n");
    }

    private interface TestCase {
        String execute() throws Exception;
    }

    private static void runTest(String testName, TestCase test) {
        System.out.printf("[TEST %02d] %-60s : ", (passCount + failCount + errorCount + 1), testName);
        try {
            String detail = test.execute();
            System.out.println("PASS (" + detail + ")");
            passCount++;
        } catch (AssertionError | IllegalArgumentException e) {
            System.out.println("FAIL (" + e.getMessage() + ")");
            failCount++;
        } catch (Exception e) {
            System.out.println("ERROR (" + e.getMessage() + ")");
            errorCount++;
        }
    }
}
