# EVENTIFY – College Event Management System

> **A Complete Desktop Database Application demonstrating Java Front-End Database Connectivity using JDBC with an Oracle Relational Database.**

---

## 1. Project Overview & Problem Statement

College and university campuses organize numerous co-curricular and extracurricular events throughout the academic year, including technical symposia, hackathons, workshops, cultural festivals, sports tournaments, and entrepreneurship summits.

Traditional manual registration and spreadsheet-based tracking methods lead to:
- Frequent double-booking of physical campus auditoriums and seminar halls.
- Exceeding room capacities resulting in safety and logistical hazards.
- Accidental duplicate participant registrations.
- Lack of centralized, real-time analytics for department chairs and faculty advisors.
- Inconsistent data integrity without relational constraints.

**EVENTIFY** is a robust desktop Event Management System developed for a college Database Systems Laboratory project. Built with a clean layered architecture (**Java Swing** frontend, **Service / DAO** layers, standard **JDBC API**, and **Oracle Database** backend), the system demonstrates how core database design principles, referential integrity, and ACID transactions are implemented in Java applications.

---

## 2. Key Objectives & Features

- **Relational Integrity:** Normalized Oracle schema enforcing Primary Keys, Foreign Keys, Unique constraints, and Check constraints.
- **Parametric JDBC Security:** All dynamic SQL statements utilize `PreparedStatement` with bind variables. PreparedStatement greatly reduces the risk of SQL injection by separating SQL structure from user-supplied values.
- **Row-Level Locking & ACID Transactions:** Atomic participant booking with programmatic `con.setAutoCommit(false)`, pessimistic row locking (`SELECT ... FOR UPDATE` on the `EVENT` record) to serialize concurrent seat reservation checks, followed by `con.commit()` on success and `con.rollback()` on error.
- **Capacity & Availability Guards:** Prevents scheduling events with capacities greater than the venue's physical limit and blocks booking when an event is full.
- **Dynamic Analytical Dashboard:** Live KPI cards calculating statistics directly from Oracle aggregates (`COUNT`, `GROUP BY`, `NVL`).
- **10 Analytical SQL Reports:** Multi-table relational reports joining `EVENT`, `ORGANIZER`, `VENUE`, and `REGISTRATION` with one-click **CSV export**.
- **Role-Based Access Control:** Separate roles for `ADMIN` (full system access) and `ORGANIZER` (restricted to own events).
- **Modern Swing UI:** Slate-and-navy aesthetic, card containers, responsive layouts, styled tables with alternating row colors, and intuitive dialogs.


---

## 3. Technology Stack

| Layer | Technology | Details |
| :--- | :--- | :--- |
| **User Interface** | Java Swing / AWT | Segoe UI modern styling, CardLayout, JSplitPane, custom tables |
| **Language & Runtime** | Java JDK 8+ / JDK 24 | Object-Oriented Architecture, Try-With-Resources, Exception Handling |
| **Database Connectivity** | Java Database Connectivity (JDBC) | `DriverManager`, `Connection`, `PreparedStatement`, `ResultSet` |
| **JDBC Driver** | Oracle JDBC Thin Driver (`ojdbc.jar`) | Type 4 pure Java thin driver connecting directly over TCP/IP |
| **Relational Database** | Oracle Database (11g / 12c / 19c / 21c / 23c) | Relational engine, Sequences, Constraints, ACID Transactions |
| **Configuration** | Java Properties (`db.properties`) | Decoupled connection parameters (URL, user, password, driver) |

> **Note:** Strictly built without ORM frameworks (No Hibernate, No Spring Boot, No JPA) to fulfill Database Systems Laboratory evaluation criteria.

---

## 4. System Architecture

EVENTIFY strictly adheres to the layered separation of concerns:

```
+-------------------------------------------------------------------------+
|                         JAVA SWING FRONT-END                            |
|  [LoginFrame] [DashboardFrame] [EventManagementPanel] [RegistrationPanel] |
|       [ParticipantPanel] [OrganizerPanel] [VenuePanel] [ReportsPanel]   |
+-------------------------------------------------------------------------+
                                    |
                                    v
+-------------------------------------------------------------------------+
|                         CONTROLLER / SERVICE LAYER                      |
|  AuthenticationService | EventService | RegistrationService             |
|  ParticipantService    | VenueService | OrganizerService | ReportService|
+-------------------------------------------------------------------------+
                                    |
                                    v
+-------------------------------------------------------------------------+
|                         DATA ACCESS OBJECT (DAO) LAYER                  |
|  UserDAO | EventDAO | ParticipantDAO | VenueDAO | RegistrationDAO | ...  |
+-------------------------------------------------------------------------+
                                    |
                                    v
+-------------------------------------------------------------------------+
|                             JDBC API & DRIVER                           |
|  DriverManager.getConnection() | PreparedStatement | ResultSet          |
|  Oracle JDBC Thin Driver (ojdbc.jar)                                    |
+-------------------------------------------------------------------------+
                                    |
                                    | TCP/IP (Port 1521)
                                    v
+-------------------------------------------------------------------------+
|                         ORACLE RELATIONAL DATABASE                      |
|  Tables: USERS, ORGANIZER, VENUE, EVENT, PARTICIPANT, REGISTRATION      |
|  Sequences: SEQ_EVENT, SEQ_REGISTRATION, SEQ_USERS, ...                 |
+-------------------------------------------------------------------------+
```

---

## 5. Normalized Database Design

The database schema consists of six normalized relational tables:

```
  +-----------+          +-----------+          +-----------+
  |   USERS   |          | ORGANIZER |          |   VENUE   |
  +-----------+          +-----------+          +-----------+
  | USER_ID   |          | ORG_ID    |          | VENUE_ID  |
  | USERNAME  |          | NAME      |          | NAME      |
  | PASSWORD  |          | EMAIL     |          | LOCATION  |
  | ROLE      |          | PHONE     |          | CAPACITY  |
  | FULL_NAME |          | DEPT      |          | AVAILABLE |
  | EMAIL     |          | USERNAME  |          +-----------+
  +-----------+          +-----------+                |
                               |                      |
                               | 1:N                  | 1:N
                               v                      v
                         +----------------------------------+
                         |              EVENT               |
                         +----------------------------------+
                         | EVENT_ID (PK)                    |
                         | EVENT_NAME, EVENT_TYPE           |
                         | EVENT_DATE, START_TIME, END_TIME |
                         | ORGANIZER_ID (FK), VENUE_ID (FK) |
                         | MAX_CAPACITY, REG_FEE, STATUS    |
                         +----------------------------------+
                                          |
                                          | 1:N
                                          v
+-----------------+              +----------------------------------+
|   PARTICIPANT   | 1:N          |           REGISTRATION           |
+-----------------+------------->+----------------------------------+
| PARTICIPANT_ID  |              | REGISTRATION_ID (PK)             |
| NAME, EMAIL     |              | EVENT_ID (FK)                    |
| PHONE, COLLEGE  |              | PARTICIPANT_ID (FK)              |
| DEPARTMENT      |              | REGISTRATION_DATE, STATUS        |
+-----------------+              | UNIQUE (EVENT_ID, PARTICIPANT_ID)|
                                 +----------------------------------+
```

### Table Descriptions & Constraints

1. **`USERS`**:
   - `USER_ID` (NUMBER, PK)
   - `USERNAME` (VARCHAR2(50), UNIQUE, NOT NULL)
   - `PASSWORD` (VARCHAR2(100), NOT NULL)
   - `ROLE` (VARCHAR2(20), CHECK IN ('ADMIN', 'ORGANIZER'))
   - `FULL_NAME` (VARCHAR2(100))
   - `EMAIL` (VARCHAR2(100))

2. **`ORGANIZER`**:
   - `ORGANIZER_ID` (NUMBER, PK)
   - `NAME` (VARCHAR2(100), NOT NULL)
   - `EMAIL` (VARCHAR2(100), UNIQUE, NOT NULL)
   - `PHONE` (VARCHAR2(15))
   - `DEPARTMENT` (VARCHAR2(100))
   - `USERNAME` (VARCHAR2(50)) - Maps to `USERS.USERNAME` for role-based view filtering.


3. **`VENUE`**:
   - `VENUE_ID` (NUMBER, PK)
   - `VENUE_NAME` (VARCHAR2(100), NOT NULL)
   - `LOCATION` (VARCHAR2(200))
   - `CAPACITY` (NUMBER, CHECK: `CAPACITY > 0`)
   - `AVAILABLE` (VARCHAR2(5), CHECK: `AVAILABLE IN ('YES', 'NO')`)

4. **`EVENT`**:
   - `EVENT_ID` (NUMBER, PK)
   - `EVENT_NAME` (VARCHAR2(150), NOT NULL)
   - `EVENT_TYPE` (VARCHAR2(50), NOT NULL)
   - `DESCRIPTION` (VARCHAR2(500))
   - `EVENT_DATE` (DATE, NOT NULL)
   - `START_TIME`, `END_TIME` (VARCHAR2(20))
   - `ORGANIZER_ID` (NUMBER, FK -> `ORGANIZER`)
   - `VENUE_ID` (NUMBER, FK -> `VENUE`)
   - `MAX_CAPACITY` (NUMBER, CHECK: `MAX_CAPACITY > 0`)
   - `REGISTRATION_FEE` (NUMBER(10,2), CHECK: `REGISTRATION_FEE >= 0`)
   - `STATUS` (VARCHAR2(20), CHECK IN ('UPCOMING', 'ONGOING', 'COMPLETED', 'CANCELLED'))

5. **`PARTICIPANT`**:
   - `PARTICIPANT_ID` (NUMBER, PK)
   - `NAME` (VARCHAR2(100), NOT NULL)
   - `EMAIL` (VARCHAR2(100), UNIQUE, NOT NULL)
   - `PHONE` (VARCHAR2(15)), `COLLEGE` (VARCHAR2(150)), `DEPARTMENT` (VARCHAR2(100))

6. **`REGISTRATION`**:
   - `REGISTRATION_ID` (NUMBER, PK)
   - `EVENT_ID` (NUMBER, FK -> `EVENT`)
   - `PARTICIPANT_ID` (NUMBER, FK -> `PARTICIPANT`)
   - `REGISTRATION_DATE` (DATE, DEFAULT SYSDATE)
   - `STATUS` (VARCHAR2(20), CHECK IN ('REGISTERED', 'CANCELLED', 'ATTENDED'))
   - **`UNIQUE (EVENT_ID, PARTICIPANT_ID)`**: Strictly prevents duplicate registrations.

---

## 6. Directory Structure

```
Eventify/
├── config/
│   └── db.properties             # Oracle DB connection properties (URL, user, password)
├── database/
│   └── database.sql              # Complete Oracle DDL, constraints, sequences, sample data
├── lib/
│   └── ojdbc.jar                 # Oracle JDBC Thin Driver (Type 4)
├── src/
│   ├── db/
│   │   └── DatabaseConnection.java   # Centralized connection factory with resource management
│   ├── model/
│   │   ├── User.java
│   │   ├── Organizer.java
│   │   ├── Venue.java
│   │   ├── Event.java
│   │   ├── Participant.java
│   │   └── Registration.java
│   ├── dao/
│   │   ├── UserDAO.java
│   │   ├── OrganizerDAO.java
│   │   ├── VenueDAO.java
│   │   ├── EventDAO.java
│   │   ├── ParticipantDAO.java
│   │   ├── RegistrationDAO.java      # ACID transaction implementation
│   │   └── ReportDAO.java            # 10 complex analytical SQL queries
│   ├── service/
│   │   ├── AuthenticationService.java
│   │   ├── EventService.java
│   │   ├── RegistrationService.java
│   │   ├── ParticipantService.java
│   │   ├── VenueService.java
│   │   ├── OrganizerService.java
│   │   └── ReportService.java
│   ├── util/
│   │   ├── ValidationUtil.java       # Regex email/phone and numeric bounds check
│   │   ├── DialogUtil.java           # Standardized Swing alert dialogs & ORA error mapper
│   │   ├── UITheme.java              # Modern styling, palette, buttons, table renderers
│   │   └── VerificationTest.java     # Automated headless test suite
│   ├── ui/
│   │   ├── LoginFrame.java
│   │   ├── DashboardFrame.java
│   │   ├── DashboardPanel.java
│   │   ├── EventManagementPanel.java
│   │   ├── ParticipantPanel.java
│   │   ├── OrganizerPanel.java
│   │   ├── VenuePanel.java
│   │   ├── RegistrationPanel.java
│   │   ├── ReportsPanel.java
│   │   └── EventDetailDialog.java
│   └── Main.java                     # Application entry point
├── run.bat                           # 1-Click compiler and launcher (Windows)
├── setup_db.bat                      # 1-Click Oracle schema installer (Windows)
├── TESTING.md                        # Official test cases (TC01 to TC15)
└── README.md                         # Project documentation and viva guide
```

---

## 7. Setup & Installation Guide

### Step 1: Prerequisites
Ensure your environment has:
1. **Java Development Kit (JDK 8 or higher, including JDK 17, 21, 24)**.
2. **Oracle Database (Oracle 11g XE, 18c/19c/21c XE or Enterprise Edition)** installed and running.

### Step 2: Initialize Database Schema & Sample Records
Open a terminal / command prompt and connect to Oracle SQL*Plus:

```bash
# Connect as sysdba or DBA user
sqlplus / as sysdba

# Create user eventify
CREATE USER eventify IDENTIFIED BY eventify123;
GRANT CONNECT, RESOURCE, UNLIMITED TABLESPACE TO eventify;
GRANT CREATE VIEW TO eventify;
EXIT;
```

Now execute `database/database.sql`:
```bash
# Windows Batch:
setup_db.bat

# Or directly in SQL*Plus:
sqlplus eventify/eventify123@localhost:1521/xe @database\database.sql
```

### Step 3: Configure `config/db.properties`
Check `config/db.properties` to ensure credentials match your Oracle instance:
```properties
db.url=jdbc:oracle:thin:@localhost:1521/xe
db.username=eventify
db.password=eventify123
db.driver=oracle.jdbc.OracleDriver
```
*(For Oracle 19c/21c pluggable databases, use `jdbc:oracle:thin:@localhost:1521/XEPDB1`)*.

### Step 4: Run the Application
You can compile and launch the application with the included batch script or standard terminal commands:

#### Option A: Quick Batch Launcher (Windows)
Simply double-click `run.bat` or run:
```cmd
run.bat
```

#### Option B: Terminal Command
```bash
# 1. Compile all Java files into bin directory
javac -cp "lib/ojdbc.jar" -d bin src/model/*.java src/dao/*.java src/service/*.java src/db/*.java src/util/*.java src/ui/*.java src/Main.java

# 2. Run the application
java -cp "lib/ojdbc.jar;bin" Main
```

---

## 8. Sample Demonstration Credentials

| Role | Username | Password | Purpose & Privileges |
| :--- | :--- | :--- | :--- |
| **ADMIN** | `admin` | `admin123` | Full access: all modules, CRUD on events, organizers, venues, participants, reports. |
| **ORGANIZER** | `organizer` | `organizer123` | Dr. Rajesh Sharma: manage own events, view participants, view registrations and reports. |
| **ORGANIZER** | `ananya` | `organizer123` | Prof. Ananya Iyer: manage own events, view registered students. |

---

## 9. Live Viva Demonstration Checklist

During laboratory evaluation, follow this sequence to demonstrate key syllabus concepts:

1. **Authentication via JDBC:**
   - Log in with `admin` / `admin123`. Point out `UserDAO.authenticate()` using `PreparedStatement` with `?` parameters to block SQL injection.
2. **Dashboard Live Aggregation:**
   - Observe the live KPI cards (Total Events, Active Registrations, Available Venues). Point out they are calculated using Oracle SQL `COUNT(*)` aggregates, not hardcoded memory variables.
3. **Event Creation with Capacity Verification:**
   - Navigate to **Events** tab.
   - Select a venue with capacity 120 (e.g., Turing Tech Lab). Attempt to enter Max Capacity = `500`.
   - Observe the error dialog: `"Event capacity (500) cannot exceed venue capacity (120)."`.
   - Correct capacity to `80` and click **ADD EVENT**. Point out `SEQ_EVENT.NEXTVAL` primary key generation and `executeUpdate()` returning rows affected.
4. **Event Details & Seats Remaining:**
   - Select an event and click **View Details**.
   - Show the calculated dynamic seats remaining: `Seats Left = Max Capacity - Registered Participants`.
5. **Transactional Participant Registration (ACID):**
   - Navigate to **Registrations** tab.
   - Enroll an unregistered participant in an upcoming event.
   - Show code in `RegistrationDAO.registerParticipantTransactional()` demonstrating `con.setAutoCommit(false)`, capacity check, `INSERT`, and `con.commit()`.
6. **Integrity Enforcement (Duplicate Registration):**
   - Attempt to register the exact same participant again for that same event.
   - Observe the immediate rejection and transaction rollback: `"Participant is already registered for this event."`
7. **Referential Integrity on Deletion:**
   - Navigate back to **Events**. Select an event with existing registrations (e.g., Event ID 1) and click **DELETE EVENT**.
   - Observe the safe guard: `"This event has registered participants. Cancel the event instead of deleting it."`
8. **Relational Reports & CSV Export:**
   - Open **Reports** tab. Select Report 8 (*"Top Events Ranked by Registration Demand"*).
   - Click **Run Query** to demonstrate multi-table `JOIN` with `GROUP BY` and occupancy percentage calculation.
   - Click **Export CSV** to export the live table into a CSV file.

---

## 10. Viva Question & Answer Guide (Lab Examiner Prep)

### Q1: What is JDBC and why is it used?
**Answer:** JDBC (Java Database Connectivity) is a standard Java API (part of `java.sql`) that provides platform-independent access to relational databases. It enables Java applications to establish database connections, execute SQL queries and updates, retrieve and navigate result sets, and manage database transactions.

### Q2: What is the Oracle JDBC Thin Driver?
**Answer:** The Oracle Thin Driver is a Type 4 pure Java driver. It converts JDBC calls directly into Oracle's proprietary Net8/TNS network protocol over standard TCP/IP sockets without requiring any native client-side Oracle software installation (no OCI libraries needed).

### Q3: What is the difference between Statement and PreparedStatement?
**Answer:**
- `Statement`: Used for simple static SQL queries with no parameters. The database compiles and analyzes the query each time it is executed. It is vulnerable to SQL Injection if user input is concatenated.
- `PreparedStatement`: Pre-compiled by the database engine. It uses parameterized placeholders (`?`). PreparedStatement greatly reduces the risk of SQL injection by separating SQL structure from user-supplied values, and offers superior execution performance when executed repeatedly.

### Q4: Why should string concatenation NEVER be used in SQL queries?
**Answer:** String concatenation allows malicious user inputs to alter the intended SQL syntax. PreparedStatement greatly reduces the risk of SQL injection by separating SQL structure from user-supplied values and binding user inputs as pure data literals.


### Q5: What is the difference between `executeQuery()` and `executeUpdate()`?
**Answer:**
- `executeQuery()`: Used for SQL `SELECT` statements. It returns a `ResultSet` object containing the tabular data produced by the query.
- `executeUpdate()`: Used for DML statements (`INSERT`, `UPDATE`, `DELETE`) or DDL statements. It returns an `int` representing the number of rows affected by the command.

### Q6: What is a Database Transaction and why is it required?
**Answer:** A transaction is a logical unit of work that contains one or more database operations. It must adhere to the **ACID** properties:
- **Atomicity:** All operations in the transaction succeed together, or all fail together (no partial updates).
- **Consistency:** The database transitions from one valid state to another valid state, satisfying all constraints.
- **Isolation:** Concurrent transactions execute without interfering with each other.
- **Durability:** Committed changes persist permanently even in case of power failure.

### Q7: How are transactions controlled in JDBC?
**Answer:**
1. Call `conn.setAutoCommit(false);` to disable automatic committing after each statement.
2. Execute the related SQL statements.
3. Call `conn.commit();` when all statements succeed.
4. If an exception occurs, catch `SQLException` and call `conn.rollback();` to undo any intermediate changes.
5. In the `finally` block, restore `conn.setAutoCommit(true);` and close the connection.

### Q8: What is a `ResultSet`?
**Answer:** A `ResultSet` is a Java object representing a database cursor over a tabular result produced by executing a `SELECT` query. The cursor initially points before the first row. Calling `rs.next()` moves the cursor to the next row, returning `false` when no more rows exist.

### Q9: Why is try-with-resources used for JDBC objects?
**Answer:** Classes like `Connection`, `PreparedStatement`, and `ResultSet` implement `java.lang.AutoCloseable`. The try-with-resources statement guarantees that each resource is closed deterministically when the block exits (even if an exception is thrown), preventing database cursor and socket leaks.

### Q10: What are Primary Keys, Foreign Keys, and Constraints?
**Answer:**
- **Primary Key:** A column or combination of columns that uniquely identifies each row in a table (`NOT NULL` and `UNIQUE`).
- **Foreign Key:** A column that establishes a referential link between data in two tables, ensuring child rows reference valid existing parent rows.
- **Unique Constraint:** Ensures all values in a column or set of columns are distinct (e.g. `(EVENT_ID, PARTICIPANT_ID)` to prevent duplicate bookings).
- **Check Constraint:** Enforces specific domain conditions on a column (e.g. `MAX_CAPACITY > 0`).

---

## 11. Troubleshooting

| Issue / Error | Cause | Resolution |
| :--- | :--- | :--- |
| `ORA-12541: TNS:no listener` | Oracle XE TNS Listener service is stopped. | Open Windows Services (`services.msc`) and start `OracleXETNSListener`. |
| `ORA-01017: invalid username/password` | Incorrect credentials in `config/db.properties`. | Verify username and password against Oracle database. |
| `ClassNotFoundException: oracle.jdbc.OracleDriver` | `ojdbc.jar` missing from classpath. | Ensure `lib/ojdbc.jar` exists and `-cp "lib/ojdbc.jar;bin"` is specified during execution. |
| `ORA-00001: unique constraint violated` | Duplicate entry (e.g. duplicate username, email, or registration). | Provide unique values or cancel existing duplicate entry. |
| `ORA-02292: integrity constraint violated` | Attempting to delete a record with dependent foreign key child records. | Remove or cancel dependent child records first. |

---

## 12. Authors & Acknowledgements
- **Project:** EVENTIFY – Event Management System
- **Curriculum:** Database Systems Laboratory Project
- **Stack:** Java Swing, JDBC, Oracle Relational Database (11g / 19c)
- **Year:** Academic Year 2026
