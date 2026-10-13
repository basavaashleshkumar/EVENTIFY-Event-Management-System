package ui;

import model.Event;
import model.Participant;
import model.Registration;
import service.EventService;
import service.ParticipantService;
import service.RegistrationService;
import util.DialogUtil;
import util.UITheme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.List;

/**
 * Event Registration Management Screen.
 * Demonstrates transactional participant booking, seat availability calculation,
 * and duplicate registration prevention.
 */
public class RegistrationPanel extends JPanel {

    private final RegistrationService registrationService;
    private final EventService eventService;
    private final ParticipantService participantService;
    private final JFrame parentFrame;

    // Controls
    private JComboBox<Event> cmbEventSelect;
    private JComboBox<Participant> cmbParticipantSelect;
    private JLabel lblEventCapacityInfo;

    // Filter controls
    private JComboBox<String> cmbFilterEvent;
    private JComboBox<String> cmbFilterStatus;
    private JTextField txtSearch;

    // Table
    private JTable tblRegistrations;
    private DefaultTableModel tableModel;
    private List<Registration> currentList;
    private List<Event> allEvents;

    public RegistrationPanel(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.registrationService = new RegistrationService();
        this.eventService = new EventService();
        this.participantService = new ParticipantService();

        initComponents();
        loadDropdownData();
        loadRegistrations();
    }

    private void initComponents() {
        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.COLOR_BG);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // Top Header
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        titlePanel.setOpaque(false);
        JLabel lblTitle = new JLabel("Event Registrations & Bookings");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        JLabel lblSub = new JLabel("Transactional participant enrollment with strict capacity enforcement and ACID rollback");
        lblSub.setFont(UITheme.FONT_BODY);
        lblSub.setForeground(UITheme.COLOR_TEXT_MUTED);
        titlePanel.add(lblTitle);
        titlePanel.add(lblSub);
        topPanel.add(titlePanel, BorderLayout.WEST);

        // Filter / Search Toolbar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filterBar.setOpaque(false);

        txtSearch = UITheme.createTextField(14);
        txtSearch.setToolTipText("Search attendee by name or email");

        cmbFilterStatus = new JComboBox<>(new String[]{"ALL", "REGISTERED", "ATTENDED", "CANCELLED"});
        cmbFilterStatus.setFont(UITheme.FONT_BODY);

        JButton btnSearch = UITheme.createPrimaryButton("Search");
        JButton btnReset = UITheme.createSecondaryButton("Reset");

        filterBar.add(new JLabel("Status:"));
        filterBar.add(cmbFilterStatus);
        filterBar.add(txtSearch);
        filterBar.add(btnSearch);
        filterBar.add(btnReset);

        topPanel.add(filterBar, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Split Pane (Booking Card on Left, Table on Right)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.36);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);

        // --- Left: Enrollment Form Card ---
        JPanel bookingCard = UITheme.createCardPanel();
        bookingCard.setLayout(new BorderLayout(0, 12));

        JLabel lblCardTitle = new JLabel("Enroll Participant in Event");
        lblCardTitle.setFont(UITheme.FONT_HEADER);
        lblCardTitle.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        bookingCard.add(lblCardTitle, BorderLayout.NORTH);

        JPanel formFields = new JPanel(new GridBagLayout());
        formFields.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 6, 8, 6);
        gbc.gridx = 0;

        // Select Event
        gbc.gridy = 0;
        JLabel lblEvent = new JLabel("1. Select Event *:");
        lblEvent.setFont(UITheme.FONT_BODY_BOLD);
        formFields.add(lblEvent, gbc);

        gbc.gridy = 1;
        cmbEventSelect = new JComboBox<>();
        cmbEventSelect.setFont(UITheme.FONT_BODY);
        cmbEventSelect.addActionListener(e -> updateSelectedEventCapacityDisplay());
        formFields.add(cmbEventSelect, gbc);

        // Capacity & Availability Badge
        gbc.gridy = 2;
        lblEventCapacityInfo = new JLabel("Select an event to see capacity.");
        lblEventCapacityInfo.setFont(UITheme.FONT_SMALL);
        lblEventCapacityInfo.setForeground(UITheme.COLOR_TEXT_MUTED);
        formFields.add(lblEventCapacityInfo, gbc);

        // Select Participant
        gbc.gridy = 3;
        gbc.insets = new Insets(16, 6, 6, 6);
        JLabel lblParticipant = new JLabel("2. Select Participant *:");
        lblParticipant.setFont(UITheme.FONT_BODY_BOLD);
        formFields.add(lblParticipant, gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(6, 6, 8, 6);
        cmbParticipantSelect = new JComboBox<>();
        cmbParticipantSelect.setFont(UITheme.FONT_BODY);
        formFields.add(cmbParticipantSelect, gbc);

        bookingCard.add(formFields, BorderLayout.CENTER);

        // Booking Action Button
        JPanel actionPanel = new JPanel(new GridLayout(2, 1, 8, 8));
        actionPanel.setOpaque(false);
        actionPanel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        JButton btnRegister = UITheme.createSuccessButton("CONFIRM REGISTRATION (JDBC)");
        btnRegister.setPreferredSize(new Dimension(btnRegister.getPreferredSize().width, 40));
        JButton btnClear = UITheme.createSecondaryButton("CLEAR SELECTION");

        actionPanel.add(btnRegister);
        actionPanel.add(btnClear);

        bookingCard.add(actionPanel, BorderLayout.SOUTH);
        splitPane.setLeftComponent(bookingCard);

        // --- Right: Table of Registrations ---
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 10));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);
        JLabel lblTableTitle = new JLabel("Active Registrations Master Table");
        lblTableTitle.setFont(UITheme.FONT_HEADER);
        lblTableTitle.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        tableHeader.add(lblTableTitle, BorderLayout.WEST);

        JPanel tableButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        tableButtons.setOpaque(false);
        JButton btnAttended = UITheme.createButton("Mark Attended", new Color(16, 185, 129), Color.WHITE);
        JButton btnCancel = UITheme.createButton("Cancel Reg", new Color(245, 158, 11), Color.WHITE);
        JButton btnDelete = UITheme.createDangerButton("Delete");
        JButton btnRefresh = UITheme.createSecondaryButton("Refresh");

        tableButtons.add(btnAttended);
        tableButtons.add(btnCancel);
        tableButtons.add(btnDelete);
        tableButtons.add(btnRefresh);
        tableHeader.add(tableButtons, BorderLayout.EAST);

        tableCard.add(tableHeader, BorderLayout.NORTH);

        String[] columns = {"Reg ID", "Event Name", "Participant", "Email", "College", "Reg Date", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblRegistrations = new JTable(tableModel);
        UITheme.styleTable(tblRegistrations);

        tableCard.add(UITheme.createStyledScrollPane(tblRegistrations), BorderLayout.CENTER);
        splitPane.setRightComponent(tableCard);

        add(splitPane, BorderLayout.CENTER);

        // Listeners
        btnRegister.addActionListener(e -> handleRegister());
        btnClear.addActionListener(e -> {
            if (cmbEventSelect.getItemCount() > 0) cmbEventSelect.setSelectedIndex(0);
            if (cmbParticipantSelect.getItemCount() > 0) cmbParticipantSelect.setSelectedIndex(0);
        });
        btnAttended.addActionListener(e -> handleMarkAttended());
        btnCancel.addActionListener(e -> handleCancelRegistration());
        btnDelete.addActionListener(e -> handleDeleteRegistration());
        btnSearch.addActionListener(e -> handleSearch());
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            cmbFilterStatus.setSelectedIndex(0);
            loadRegistrations();
        });
        btnRefresh.addActionListener(e -> {
            loadDropdownData();
            loadRegistrations();
        });
    }

    private void loadDropdownData() {
        try {
            // Events
            cmbEventSelect.removeAllItems();
            allEvents = eventService.getAllEvents();
            for (Event ev : allEvents) {
                cmbEventSelect.addItem(ev);
            }

            // Participants
            cmbParticipantSelect.removeAllItems();
            List<Participant> participants = participantService.getAllParticipants();
            for (Participant p : participants) {
                cmbParticipantSelect.addItem(p);
            }

            updateSelectedEventCapacityDisplay();
        } catch (SQLException ex) {
            DialogUtil.handleSQLException(this, ex, "Failed to load events/participants");
        }
    }

    private void updateSelectedEventCapacityDisplay() {
        Event ev = (Event) cmbEventSelect.getSelectedItem();
        if (ev != null) {
            int seatsLeft = ev.getSeatsRemaining();
            boolean isFull = ev.isFull();
            lblEventCapacityInfo.setText("<html>Capacity: <b>" + ev.getMaxCapacity() +
                                         "</b> | Booked: <b>" + ev.getCurrentRegistrations() +
                                         "</b> | Left: <b style='color:" + (isFull ? "#EF4444" : "#10B981") + ";'>" +
                                         seatsLeft + (isFull ? " (FULL)" : "") + "</b></html>");
        } else {
            lblEventCapacityInfo.setText("No event selected.");
        }
    }

    public void loadRegistrations() {
        SwingUtilities.invokeLater(() -> {
            try {
                currentList = registrationService.getAllRegistrations();
                tableModel.setRowCount(0);
                for (Registration r : currentList) {
                    tableModel.addRow(new Object[]{
                        r.getRegistrationId(),
                        r.getEventName(),
                        r.getParticipantName(),
                        r.getParticipantEmail(),
                        r.getParticipantCollege(),
                        r.getRegistrationDate() != null ? r.getRegistrationDate().toString() : "",
                        r.getStatus()
                    });
                }
            } catch (SQLException ex) {
                DialogUtil.handleSQLException(this, ex, "Failed to load registrations");
            }
        });
    }

    private void handleRegister() {
        Event ev = (Event) cmbEventSelect.getSelectedItem();
        Participant p = (Participant) cmbParticipantSelect.getSelectedItem();

        if (ev == null) {
            DialogUtil.showWarning(this, "Please select an event.", "Validation");
            return;
        }
        if (p == null) {
            DialogUtil.showWarning(this, "Please select a participant.", "Validation");
            return;
        }

        try {
            // Execute Transaction
            boolean success = registrationService.register(ev.getEventId(), p.getParticipantId());
            if (success) {
                DialogUtil.showInfo(this,
                    "Registration successful!\n\n" +
                    "Event: " + ev.getEventName() + "\n" +
                    "Participant: " + p.getName() + " (" + p.getEmail() + ")\n" +
                    "Status: Transaction COMMITTED to Oracle Database.",
                    "Registration Confirmed");
                loadDropdownData();
                loadRegistrations();
            }
        } catch (SQLException ex) {
            // Displays transaction rollback and failure reason
            DialogUtil.showError(this,
                ex.getMessage() + "\n\n(Transaction has been safely ROLLED BACK)",
                "Registration Failed");
        }
    }

    private void handleMarkAttended() {
        int row = tblRegistrations.getSelectedRow();
        if (row < 0) {
            DialogUtil.showWarning(this, "Please select a registration row from the table.", "No Selection");
            return;
        }

        Registration r = currentList.get(row);
        try {
            registrationService.markAttended(r.getRegistrationId());
            DialogUtil.showInfo(this, "Participant status marked as ATTENDED.", "Attendance Recorded");
            loadRegistrations();
        } catch (SQLException ex) {
            DialogUtil.handleSQLException(this, ex, "Status Update Failed");
        }
    }

    private void handleCancelRegistration() {
        int row = tblRegistrations.getSelectedRow();
        if (row < 0) {
            DialogUtil.showWarning(this, "Please select a registration row from the table.", "No Selection");
            return;
        }

        Registration r = currentList.get(row);
        boolean confirm = DialogUtil.showConfirm(this,
            "Are you sure you want to cancel registration for: " + r.getParticipantName() + " in " + r.getEventName() + "?\n" +
            "This will free up a seat for other attendees.",
            "Confirm Cancellation");
        if (!confirm) return;

        try {
            registrationService.cancelRegistration(r.getRegistrationId());
            DialogUtil.showInfo(this, "Registration cancelled successfully.", "Cancelled");
            loadDropdownData();
            loadRegistrations();
        } catch (SQLException ex) {
            DialogUtil.handleSQLException(this, ex, "Cancel Registration Failed");
        }
    }

    private void handleDeleteRegistration() {
        int row = tblRegistrations.getSelectedRow();
        if (row < 0) {
            DialogUtil.showWarning(this, "Please select a registration row to delete.", "No Selection");
            return;
        }

        Registration r = currentList.get(row);
        boolean confirm = DialogUtil.showConfirm(this,
            "Are you sure you want to permanently delete Registration #" + r.getRegistrationId() + "?",
            "Confirm Deletion");
        if (!confirm) return;

        try {
            registrationService.deleteRegistration(r.getRegistrationId());
            DialogUtil.showInfo(this, "Registration record removed.", "Deleted");
            loadDropdownData();
            loadRegistrations();
        } catch (SQLException ex) {
            DialogUtil.handleSQLException(this, ex, "Delete Registration Failed");
        }
    }

    private void handleSearch() {
        String keyword = txtSearch.getText().trim();
        String status = (String) cmbFilterStatus.getSelectedItem();

        try {
            currentList = registrationService.searchRegistrations(keyword, null, status);
            tableModel.setRowCount(0);
            for (Registration r : currentList) {
                tableModel.addRow(new Object[]{
                    r.getRegistrationId(),
                    r.getEventName(),
                    r.getParticipantName(),
                    r.getParticipantEmail(),
                    r.getParticipantCollege(),
                    r.getRegistrationDate() != null ? r.getRegistrationDate().toString() : "",
                    r.getStatus()
                });
            }
        } catch (SQLException ex) {
            DialogUtil.handleSQLException(this, ex, "Search Failed");
        }
    }
}
