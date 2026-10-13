# EVENTIFY - Comprehensive Test Suite & Verification Cases

This document records the official test cases and validation results for **EVENTIFY: College Event Management System** built with **Java Swing, JDBC, and Oracle Relational Database**.

---

## Test Execution Summary

| Total Test Cases | Passed | Failed | Status |
| :---: | :---: | :---: | :---: |
| **15** | **15** | **0** | **PASSED (100%)** |

---

## Detailed Test Cases

### TC01: Successful Login (Authentication)
* **Objective:** Verify that an administrator or organizer can authenticate successfully using credentials stored in Oracle `USERS` table.
* **Input:**
  * Username: `admin`
  * Password: `admin123`
* **Expected Result:**
  * Query `SELECT USER_ID, USERNAME, PASSWORD, ROLE FROM USERS WHERE LOWER(USERNAME)=? AND PASSWORD=?` succeeds.
  * System welcomes user, displays `ADMIN` role badge, closes Login window, and opens Main Dashboard.
* **Actual Result:** Dashboard opened with admin privileges and full management sidebar.
* **Status:** **PASS**

---

### TC02: Invalid Login (Authentication Failure)
* **Objective:** Verify that invalid or forged credentials are rejected and do not grant access to the system.
* **Input:**
  * Username: `admin`
  * Password: `wrongpassword999`
* **Expected Result:**
  * PreparedStatement returns 0 rows.
  * Error dialog or label: `"Invalid username or password."`
  * Dashboard is NOT loaded.
* **Actual Result:** Displayed error message `"Invalid username or password."` Access denied.
* **Status:** **PASS**

---

### TC03: Add Event (Validation & Database Insertion)
* **Objective:** Verify that a new valid event is inserted into the Oracle `EVENT` table using `PreparedStatement.executeUpdate()`.
* **Input:**
  * Event Name: `National Robotics Challenge 2026`
  * Event Type: `Competition`
  * Date: `2026-11-28`
  * Start Time: `09:00 AM`, End Time: `05:00 PM`
  * Organizer: `Dr. Rajesh Sharma` (ID: 1)
  * Venue: `Dr. APJ Abdul Kalam Auditorium` (ID: 1, Capacity: 500)
  * Max Capacity: `300`
  * Registration Fee: `100.00`
  * Status: `UPCOMING`
* **Expected Result:**
  * Sequence `SEQ_EVENT.NEXTVAL` assigns next primary key.
  * `executeUpdate()` returns `1`.
  * Dialog: `"Event successfully added to Oracle database! Rows affected: 1"`.
  * Event immediately appears in the directory table.
* **Actual Result:** Record inserted, sequence incremented, table refreshed with new event.
* **Status:** **PASS**

---

### TC04: Update Event
* **Objective:** Verify that modifying an existing event updates its Oracle record and reflects updated fields.
* **Input:**
  * Select Event ID: `1`
  * Change Status from `UPCOMING` to `ONGOING`
  * Change Fee to `180.00`
  * Click **UPDATE EVENT**
* **Expected Result:**
  * `UPDATE EVENT SET ... WHERE EVENT_ID = ?` executes.
  * Message: `"Event updated successfully! Rows updated: 1"`.
  * Table row displays updated fee and status.
* **Actual Result:** Row updated in Oracle, table dynamically reflected new values.
* **Status:** **PASS**

---

### TC05: Delete Event (Integrity Constraint Safeguard)
* **Objective:** Verify that attempting to delete an event with active participant registrations is safely blocked.
* **Input:**
  * Select Event ID: `1` (`Tech Innovation Summit 2026`, which has 4 registered participants).
  * Click **DELETE EVENT**, confirm prompt.
* **Expected Result:**
  * Integrity check triggers: `SELECT COUNT(*) FROM REGISTRATION WHERE EVENT_ID = ?`.
  * Operation halted before SQL delete executes.
  * User-friendly dialog: `"This event has registered participants. Cancel the event instead of deleting it."`
* **Actual Result:** Deletion blocked, warning dialog shown, data preserved intact.
* **Status:** **PASS**

---

### TC06: Search & Filter Events
* **Objective:** Verify that multi-criteria search and filter queries correctly retrieve filtered records from Oracle.
* **Input:**
  * Search Keyword: `Workshop`
  * Status Filter: `UPCOMING`
* **Expected Result:**
  * PreparedStatement executes dynamic `WHERE LOWER(EVENT_NAME) LIKE ? AND STATUS = ?`.
  * JTable updates to display only matching Workshop events.
* **Actual Result:** Correctly filtered to display `Hands-on AI & Deep Learning Workshop`.
* **Status:** **PASS**

---

### TC07: Add Participant (Input Validation & Duplicate Prevention)
* **Objective:** Verify participant creation and validation against duplicate email address.
* **Input:**
  * Name: `Sameer Kulkarni`
  * Email: `sameer.k@nitk.edu.in`
  * Phone: `9876543299`
  * College: `NIT Karnataka`
  * Department: `Civil Engineering`
* **Expected Result:**
  * Email syntax validated.
  * `SEQ_PARTICIPANT.NEXTVAL` generates primary key.
  * Record inserted into `PARTICIPANT` table.
* **Actual Result:** Inserted successfully, verified in database.
* **Status:** **PASS**

---

### TC08: Successful Event Registration (ACID Transaction)
* **Objective:** Verify participant registration under JDBC transaction control (`setAutoCommit(false)`, `commit()`).
* **Input:**
  * Event: `Hands-on AI & Deep Learning Workshop` (ID: 3)
  * Participant: `Sameer Kulkarni`
* **Expected Result:**
  * Capacity verified (`Current < MaxCapacity`).
  * No existing registration found.
  * `INSERT INTO REGISTRATION ...` succeeds.
  * `con.commit()` executes.
  * Seats remaining count decrements by 1.
* **Actual Result:** Transaction committed, registration ID assigned, seats remaining updated.
* **Status:** **PASS**

---

### TC09: Duplicate Registration Prevention (Unique Constraint)
* **Objective:** Verify that the system blocks registering the same participant twice for the same event.
* **Input:**
  * Event: `Tech Innovation Summit 2026` (ID: 1)
  * Participant: `Aarav Patel` (Participant ID: 1, already registered)
* **Expected Result:**
  * Pre-check detects existing active registration row.
  * Transaction rolls back (`con.rollback()`).
  * Error dialog: `"Participant 'Aarav Patel' is already registered for 'Tech Innovation Summit 2026'"`.
* **Actual Result:** Transaction rolled back, clear error dialog displayed, duplicate prevented.
* **Status:** **PASS**

---

### TC10: Registration when Event is Full (Capacity Reached)
* **Objective:** Verify that registration is rejected when current registrations equal or exceed maximum capacity.
* **Input:**
  * Create/Select test event with `MAX_CAPACITY = 1`.
  * Register Participant A (takes 1st seat).
  * Attempt to register Participant B.
* **Expected Result:**
  * Check `currentCount >= maxCapacity` evaluates to true.
  * Registration rejected.
  * Error dialog: `"Registration failed: Event capacity reached (1/1)."`.
  * Transaction rolled back.
* **Actual Result:** Capacity limit strictly enforced, registration blocked.
* **Status:** **PASS**

---

### TC11: Invalid Email Format Validation
* **Objective:** Verify frontend regular expression validation rejects malformed email addresses before querying the database.
* **Input:**
  * Name: `Vikram Test`
  * Email: `notanemail@`
  * College: `BITS`
* **Expected Result:**
  * `ValidationUtil.isValidEmail()` returns false.
  * Error dialog: `"Valid participant email is required."`
  * No SQL executed.
* **Actual Result:** Rejected on frontend before touching Oracle.
* **Status:** **PASS**

---

### TC12: Invalid Event Capacity vs Venue Capacity
* **Objective:** Verify that event capacity cannot exceed the host venue's physical seating capacity.
* **Input:**
  * Venue: `Alan Turing Computing Lab` (Seating Capacity: 120)
  * Event Max Capacity: `500` (exceeds venue capacity)
* **Expected Result:**
  * Validation checks `event.getMaxCapacity() > venue.getCapacity()`.
  * Error dialog: `"Event capacity (500) cannot exceed venue capacity (120)."`.
  * Insert blocked.
* **Actual Result:** Blocked with informative error message.
* **Status:** **PASS**

---

### TC13: Database Connection Failure Handling
* **Objective:** Verify graceful error handling when database credentials or listener are unavailable.
* **Input:**
  * Temporarily set invalid port or password in `config/db.properties`.
  * Launch application / test connection.
* **Expected Result:**
  * Catches `SQLException` (`ORA-01017` or `ORA-12541`).
  * User-friendly error dialog explains the listener or credential issue without crashing the JVM.
* **Actual Result:** Dialog displayed: `"Database Error (ORA-01017): Invalid database username or password."`
* **Status:** **PASS**

---

### TC14: Transaction Rollback on Failure
* **Objective:** Verify that an operation failing midway cleanly triggers `con.rollback()` and leaves no partial database changes.
* **Input:**
  * Attempt transactional booking for an invalid participant ID or during a simulated constraint failure.
* **Expected Result:**
  * Exception caught in `catch (SQLException ex)`.
  * `conn.rollback()` invoked.
  * Database remains in consistent pre-transaction state.
* **Actual Result:** Rollback confirmed in logs: `"[RegistrationDAO] Rolling back transaction due to error"`. No orphan rows left.
* **Status:** **PASS**

---

### TC15: Foreign Key Violation Handling
* **Objective:** Verify that attempting to delete an Organizer or Venue that is referenced by active Events is rejected with an explanatory message.
* **Input:**
  * Attempt to delete Venue ID: `1` (`Dr. APJ Abdul Kalam Auditorium`), which hosts active events.
* **Expected Result:**
  * Pre-deletion check `venueDAO.hasEvents(venueId)` returns true.
  * Delete query halted.
  * User-friendly message: `"Cannot delete this venue because events are scheduled in it. Reassign or cancel the events first."`
* **Actual Result:** Deletion rejected, database referential integrity preserved.
* **Status:** **PASS**
