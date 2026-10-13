package ui;

import model.Event;
import model.Organizer;
import model.Venue;
import service.AuthenticationService;
import service.EventService;
import service.OrganizerService;
import service.VenueService;
import util.DialogUtil;
import util.UITheme;
import util.ValidationUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
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
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

/**
 * Event Management Panel providing full CRUD, capacity validation,
 * venue availability check, searching, and live Oracle JDBC execution.
 */
public class EventManagementPanel extends JPanel {

    private final EventService eventService;
    private final OrganizerService organizerService;
    private final VenueService venueService;
    private final JFrame parentFrame;

    // Form Components
    private JTextField txtEventId;
    private JTextField txtEventName;
    private JComboBox<String> cmbEventType;
    private JTextArea txtDescription;
    private JTextField txtEventDate;
    private JTextField txtStartTime;
    private JTextField txtEndTime;
    private JComboBox<Organizer> cmbOrganizer;
    private JComboBox<Venue> cmbVenue;
    private JTextField txtMaxCapacity;
    private JTextField txtRegistrationFee;
    private JComboBox<String> cmbStatus;

    // Search and Filter Components
    private JTextField txtSearch;
    private JComboBox<String> cmbFilterStatus;
    private JComboBox<String> cmbFilterType;

    // Table
    private JTable tblEvents;
    private DefaultTableModel tableModel;

    // Cached lists for reference
    private List<Event> currentEventList;

    public EventManagementPanel(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.eventService = new EventService();
        this.organizerService = new OrganizerService();
        this.venueService = new VenueService();

        initComponents();
        loadDropdownData();
        loadEvents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.COLOR_BG);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // 1. Top Bar: Title & Search/Filter Controls
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        titlePanel.setOpaque(false);
        JLabel lblTitle = new JLabel("Event Management");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        JLabel lblSub = new JLabel("Create, update, search, and monitor events with real-time Oracle JDBC persistence");
        lblSub.setFont(UITheme.FONT_BODY);
        lblSub.setForeground(UITheme.COLOR_TEXT_MUTED);
        titlePanel.add(lblTitle);
        titlePanel.add(lblSub);
        topPanel.add(titlePanel, BorderLayout.WEST);

        // Filter / Search Toolbar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filterBar.setOpaque(false);

        txtSearch = UITheme.createTextField(14);
        txtSearch.setToolTipText("Search by Event ID, Name, Venue or Organizer");

        cmbFilterStatus = new JComboBox<>(new String[]{"ALL", "UPCOMING", "ONGOING", "COMPLETED", "CANCELLED"});
        cmbFilterStatus.setFont(UITheme.FONT_BODY);

        cmbFilterType = new JComboBox<>(new String[]{"ALL", "Conference", "Cultural", "Workshop", "Hackathon", "Competition", "Seminar", "Exhibition", "Sports"});
        cmbFilterType.setFont(UITheme.FONT_BODY);

        JButton btnSearch = UITheme.createPrimaryButton("Search");
        JButton btnResetFilter = UITheme.createSecondaryButton("Reset");

        filterBar.add(new JLabel("Status:"));
        filterBar.add(cmbFilterStatus);
        filterBar.add(new JLabel("Type:"));
        filterBar.add(cmbFilterType);
        filterBar.add(txtSearch);
        filterBar.add(btnSearch);
        filterBar.add(btnResetFilter);

        topPanel.add(filterBar, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // 2. Main Content Split Pane: Form on Left, Table on Right
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.38);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);

        // --- LEFT: Event Form Card ---
        JPanel formCard = UITheme.createCardPanel();
        formCard.setLayout(new BorderLayout(0, 10));

        JLabel lblFormTitle = new JLabel("Event Details Form");
        lblFormTitle.setFont(UITheme.FONT_HEADER);
        lblFormTitle.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        formCard.add(lblFormTitle, BorderLayout.NORTH);

        JPanel formFields = new JPanel(new GridBagLayout());
        formFields.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 6, 4, 6);

        // Event ID
        txtEventId = UITheme.createTextField(10);
        txtEventId.setEditable(false);
        txtEventId.setBackground(new Color(241, 245, 249));
        addFormField(formFields, gbc, 0, "Event ID (Auto):", txtEventId);

        // Event Name
        txtEventName = UITheme.createTextField(15);
        addFormField(formFields, gbc, 1, "Event Name *:", txtEventName);

        // Event Type
        String[] types = {"Conference", "Cultural", "Workshop", "Hackathon", "Competition", "Seminar", "Exhibition", "Sports", "Other"};
        cmbEventType = new JComboBox<>(types);
        cmbEventType.setEditable(true);
        cmbEventType.setFont(UITheme.FONT_BODY);
        addFormField(formFields, gbc, 2, "Event Type *:", cmbEventType);

        // Event Date
        txtEventDate = UITheme.createTextField(10);
        txtEventDate.setToolTipText("Format: YYYY-MM-DD (e.g., 2026-10-25)");
        addFormField(formFields, gbc, 3, "Event Date (YYYY-MM-DD) *:", txtEventDate);

        // Timing (Start & End in one line)
        JPanel timePanel = new JPanel(new GridLayout(1, 2, 6, 0));
        timePanel.setOpaque(false);
        txtStartTime = UITheme.createTextField(6);
        txtStartTime.setText("09:30 AM");
        txtEndTime = UITheme.createTextField(6);
        txtEndTime.setText("04:30 PM");
        timePanel.add(txtStartTime);
        timePanel.add(txtEndTime);
        addFormField(formFields, gbc, 4, "Start / End Time *:", timePanel);

        // Organizer
        cmbOrganizer = new JComboBox<>();
        cmbOrganizer.setFont(UITheme.FONT_BODY);
        addFormField(formFields, gbc, 5, "Organizer *:", cmbOrganizer);

        // Venue
        cmbVenue = new JComboBox<>();
        cmbVenue.setFont(UITheme.FONT_BODY);
        addFormField(formFields, gbc, 6, "Venue *:", cmbVenue);

        // Capacity & Fee in one line
        JPanel capFeePanel = new JPanel(new GridLayout(1, 2, 6, 0));
        capFeePanel.setOpaque(false);
        txtMaxCapacity = UITheme.createTextField(6);
        txtRegistrationFee = UITheme.createTextField(6);
        txtRegistrationFee.setText("0.00");
        capFeePanel.add(txtMaxCapacity);
        capFeePanel.add(txtRegistrationFee);
        addFormField(formFields, gbc, 7, "Max Capacity / Fee (INR) *:", capFeePanel);

        // Status
        cmbStatus = new JComboBox<>(new String[]{"UPCOMING", "ONGOING", "COMPLETED", "CANCELLED"});
        cmbStatus.setFont(UITheme.FONT_BODY);
        addFormField(formFields, gbc, 8, "Event Status *:", cmbStatus);

        // Description
        txtDescription = new JTextArea(3, 15);
        txtDescription.setFont(UITheme.FONT_BODY);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(txtDescription);
        descScroll.setPreferredSize(new Dimension(descScroll.getPreferredSize().width, 60));
        addFormField(formFields, gbc, 9, "Description:", descScroll);

        formCard.add(new JScrollPane(formFields), BorderLayout.CENTER);

        // CRUD Action Buttons
        JPanel actionPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        actionPanel.setOpaque(false);
        actionPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton btnAdd = UITheme.createSuccessButton("ADD EVENT");
        JButton btnUpdate = UITheme.createPrimaryButton("UPDATE EVENT");
        JButton btnDelete = UITheme.createDangerButton("DELETE EVENT");
        JButton btnClear = UITheme.createSecondaryButton("CLEAR FORM");

        actionPanel.add(btnAdd);
        actionPanel.add(btnUpdate);
        actionPanel.add(btnDelete);
        actionPanel.add(btnClear);

        formCard.add(actionPanel, BorderLayout.SOUTH);
        splitPane.setLeftComponent(formCard);

        // --- RIGHT: Table & Action Bar ---
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 10));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);
        JLabel lblTableTitle = new JLabel("Registered Events Directory");
        lblTableTitle.setFont(UITheme.FONT_HEADER);
        lblTableTitle.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        tableHeader.add(lblTableTitle, BorderLayout.WEST);

        JPanel tableButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        tableButtons.setOpaque(false);
        JButton btnViewDetails = UITheme.createAccentButton("View Details");
        JButton btnCancelEvent = UITheme.createButton("Cancel Event", new Color(245, 158, 11), Color.WHITE);
        JButton btnRefresh = UITheme.createSecondaryButton("Refresh");
        tableButtons.add(btnViewDetails);
        tableButtons.add(btnCancelEvent);
        tableButtons.add(btnRefresh);
        tableHeader.add(tableButtons, BorderLayout.EAST);

        tableCard.add(tableHeader, BorderLayout.NORTH);

        // Table Model
        String[] columns = {"ID", "Event Name", "Type", "Date", "Venue", "Booked / Cap", "Fee (₹)", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblEvents = new JTable(tableModel);
        UITheme.styleTable(tblEvents);
        tblEvents.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateFormFromSelectedRow();
            }
        });

        tableCard.add(UITheme.createStyledScrollPane(tblEvents), BorderLayout.CENTER);
        splitPane.setRightComponent(tableCard);

        add(splitPane, BorderLayout.CENTER);

        // Action Listeners
        btnAdd.addActionListener(e -> handleAddEvent());
        btnUpdate.addActionListener(e -> handleUpdateEvent());
        btnDelete.addActionListener(e -> handleDeleteEvent());
        btnClear.addActionListener(e -> clearForm());
        btnSearch.addActionListener(e -> handleSearch());
        btnResetFilter.addActionListener(e -> {
            txtSearch.setText("");
            cmbFilterStatus.setSelectedIndex(0);
            cmbFilterType.setSelectedIndex(0);
            loadEvents();
        });
        btnRefresh.addActionListener(e -> {
            loadDropdownData();
            loadEvents();
        });
        btnViewDetails.addActionListener(e -> handleViewDetails());
        btnCancelEvent.addActionListener(e -> handleCancelEvent());
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String label, javax.swing.JComponent comp) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0.35;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BODY_BOLD);
        lbl.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        panel.add(comp, gbc);
    }

    private void loadDropdownData() {
        try {
            // Organizers
            cmbOrganizer.removeAllItems();
            List<Organizer> organizers = organizerService.getAllOrganizers();
            for (Organizer o : organizers) {
                cmbOrganizer.addItem(o);
            }

            // Venues (all venues for dropdown, but we validate availability during add)
            cmbVenue.removeAllItems();
            List<Venue> venues = venueService.getAllVenues();
            for (Venue v : venues) {
                cmbVenue.addItem(v);
            }
        } catch (SQLException ex) {
            DialogUtil.handleSQLException(this, ex, "Failed to load dropdown items");
        }
    }

    public void loadEvents() {
        SwingUtilities.invokeLater(() -> {
            try {
                // If logged in as organizer, filter to their events
                if (AuthenticationService.isOrganizer()) {
                    String username = AuthenticationService.getCurrentUser().getUsername();
                    currentEventList = eventService.getEventsForOrganizerUsername(username);
                } else {
                    currentEventList = eventService.getAllEvents();
                }

                tableModel.setRowCount(0);
                for (Event ev : currentEventList) {
                    tableModel.addRow(new Object[]{
                        ev.getEventId(),
                        ev.getEventName(),
                        ev.getEventType(),
                        ev.getEventDate() != null ? ev.getEventDate().toString() : "",
                        ev.getVenueName(),
                        ev.getCurrentRegistrations() + " / " + ev.getMaxCapacity(),
                        String.format("%.2f", ev.getRegistrationFee()),
                        ev.getStatus()
                    });
                }
            } catch (SQLException ex) {
                DialogUtil.handleSQLException(this, ex, "Failed to load events");
            }
        });
    }

    private void populateFormFromSelectedRow() {
        int selectedRow = tblEvents.getSelectedRow();
        if (selectedRow < 0 || currentEventList == null || selectedRow >= currentEventList.size()) {
            return;
        }

        Event e = currentEventList.get(selectedRow);
        txtEventId.setText(String.valueOf(e.getEventId()));
        txtEventName.setText(e.getEventName());
        cmbEventType.setSelectedItem(e.getEventType());
        txtEventDate.setText(e.getEventDate() != null ? e.getEventDate().toString() : "");
        txtStartTime.setText(e.getStartTime());
        txtEndTime.setText(e.getEndTime());
        txtMaxCapacity.setText(String.valueOf(e.getMaxCapacity()));
        txtRegistrationFee.setText(String.format("%.2f", e.getRegistrationFee()));
        cmbStatus.setSelectedItem(e.getStatus());
        txtDescription.setText(e.getDescription());

        // Select Organizer in dropdown
        for (int i = 0; i < cmbOrganizer.getItemCount(); i++) {
            Organizer org = cmbOrganizer.getItemAt(i);
            if (org.getOrganizerId() == e.getOrganizerId()) {
                cmbOrganizer.setSelectedIndex(i);
                break;
            }
        }

        // Select Venue in dropdown
        for (int i = 0; i < cmbVenue.getItemCount(); i++) {
            Venue ven = cmbVenue.getItemAt(i);
            if (ven.getVenueId() == e.getVenueId()) {
                cmbVenue.setSelectedIndex(i);
                break;
            }
        }
    }

    private void handleAddEvent() {
        try {
            Event event = extractEventFromForm(true);
            int rows = eventService.addEvent(event);
            DialogUtil.showInfo(this, "Event successfully added to Oracle database!\nRows affected: " + rows, "Event Added");
            clearForm();
            loadEvents();
        } catch (IllegalArgumentException ex) {
            DialogUtil.showError(this, ex.getMessage(), "Input Validation Error");
        } catch (SQLException ex) {
            DialogUtil.handleSQLException(this, ex, "Add Event Failed");
        }
    }

    private void handleUpdateEvent() {
        if (txtEventId.getText().trim().isEmpty()) {
            DialogUtil.showWarning(this, "Please select an event from the table to update.", "No Event Selected");
            return;
        }

        try {
            Event event = extractEventFromForm(false);
            int rows = eventService.updateEvent(event);
            DialogUtil.showInfo(this, "Event updated successfully!\nRows updated: " + rows, "Event Updated");
            loadEvents();
        } catch (IllegalArgumentException ex) {
            DialogUtil.showError(this, ex.getMessage(), "Input Validation Error");
        } catch (SQLException ex) {
            DialogUtil.handleSQLException(this, ex, "Update Event Failed");
        }
    }

    private void handleDeleteEvent() {
        if (txtEventId.getText().trim().isEmpty()) {
            DialogUtil.showWarning(this, "Please select an event to delete.", "No Event Selected");
            return;
        }

        int eventId = Integer.parseInt(txtEventId.getText().trim());
        String eventName = txtEventName.getText().trim();

        boolean confirm = DialogUtil.showConfirm(this,
            "Are you sure you want to delete the event: '" + eventName + "' (ID: " + eventId + ")?\n" +
            "This operation cannot be undone.",
            "Confirm Event Deletion");

        if (!confirm) return;

        try {
            int rows = eventService.deleteEvent(eventId);
            DialogUtil.showInfo(this, "Event deleted successfully!\nRows affected: " + rows, "Deleted");
            clearForm();
            loadEvents();
        } catch (SQLException ex) {
            DialogUtil.showError(this, ex.getMessage(), "Cannot Delete Event");
        }
    }

    private void handleCancelEvent() {
        int selectedRow = tblEvents.getSelectedRow();
        if (selectedRow < 0) {
            DialogUtil.showWarning(this, "Please select an event from the table to cancel.", "No Selection");
            return;
        }

        Event e = currentEventList.get(selectedRow);
        boolean confirm = DialogUtil.showConfirm(this,
            "Are you sure you want to cancel the event: '" + e.getEventName() + "'?\n" +
            "Its status will be updated to CANCELLED.",
            "Confirm Cancellation");

        if (!confirm) return;

        try {
            eventService.cancelEvent(e.getEventId());
            DialogUtil.showInfo(this, "Event has been marked as CANCELLED.", "Status Updated");
            loadEvents();
        } catch (SQLException ex) {
            DialogUtil.handleSQLException(this, ex, "Cancel Event Failed");
        }
    }

    private void handleViewDetails() {
        int selectedRow = tblEvents.getSelectedRow();
        if (selectedRow < 0) {
            DialogUtil.showWarning(this, "Please select an event from the table to view details.", "No Selection");
            return;
        }

        Event event = currentEventList.get(selectedRow);
        EventDetailDialog dialog = new EventDetailDialog(parentFrame, event);
        dialog.setVisible(true);
    }

    private void handleSearch() {
        String keyword = txtSearch.getText().trim();
        String status = (String) cmbFilterStatus.getSelectedItem();
        String type = (String) cmbFilterType.getSelectedItem();

        try {
            currentEventList = eventService.searchEvents(keyword, status, type, null);
            tableModel.setRowCount(0);
            for (Event ev : currentEventList) {
                tableModel.addRow(new Object[]{
                    ev.getEventId(),
                    ev.getEventName(),
                    ev.getEventType(),
                    ev.getEventDate() != null ? ev.getEventDate().toString() : "",
                    ev.getVenueName(),
                    ev.getCurrentRegistrations() + " / " + ev.getMaxCapacity(),
                    String.format("%.2f", ev.getRegistrationFee()),
                    ev.getStatus()
                });
            }
        } catch (SQLException ex) {
            DialogUtil.handleSQLException(this, ex, "Search Failed");
        }
    }

    private void clearForm() {
        txtEventId.setText("");
        txtEventName.setText("");
        if (cmbEventType.getItemCount() > 0) cmbEventType.setSelectedIndex(0);
        txtEventDate.setText("");
        txtStartTime.setText("09:30 AM");
        txtEndTime.setText("04:30 PM");
        if (cmbOrganizer.getItemCount() > 0) cmbOrganizer.setSelectedIndex(0);
        if (cmbVenue.getItemCount() > 0) cmbVenue.setSelectedIndex(0);
        txtMaxCapacity.setText("");
        txtRegistrationFee.setText("0.00");
        cmbStatus.setSelectedIndex(0);
        txtDescription.setText("");
        tblEvents.clearSelection();
    }

    private Event extractEventFromForm(boolean isNew) {
        String name = txtEventName.getText().trim();
        String type = (String) cmbEventType.getSelectedItem();
        String dateStr = txtEventDate.getText().trim();
        String start = txtStartTime.getText().trim();
        String end = txtEndTime.getText().trim();
        String capStr = txtMaxCapacity.getText().trim();
        String feeStr = txtRegistrationFee.getText().trim();
        String status = (String) cmbStatus.getSelectedItem();
        String desc = txtDescription.getText().trim();

        if (!ValidationUtil.isNotEmpty(name)) {
            throw new IllegalArgumentException("Event name cannot be empty.");
        }
        if (!ValidationUtil.isNotEmpty(type)) {
            throw new IllegalArgumentException("Event type cannot be empty.");
        }
        if (!ValidationUtil.isValidDate(dateStr)) {
            throw new IllegalArgumentException("Invalid Event Date. Please use format YYYY-MM-DD (e.g. 2026-10-25).");
        }
        if (!ValidationUtil.isPositiveInteger(capStr)) {
            throw new IllegalArgumentException("Maximum capacity must be a positive integer.");
        }
        if (!ValidationUtil.isNonNegativeDouble(feeStr)) {
            throw new IllegalArgumentException("Registration fee must be a non-negative number.");
        }

        Organizer selectedOrg = (Organizer) cmbOrganizer.getSelectedItem();
        if (selectedOrg == null) {
            throw new IllegalArgumentException("Please select an Organizer.");
        }

        Venue selectedVenue = (Venue) cmbVenue.getSelectedItem();
        if (selectedVenue == null) {
            throw new IllegalArgumentException("Please select a Venue.");
        }

        Event e = new Event();
        if (!isNew) {
            e.setEventId(Integer.parseInt(txtEventId.getText().trim()));
        }
        e.setEventName(name);
        e.setEventType(type);
        e.setEventDate(Date.valueOf(dateStr));
        e.setStartTime(start);
        e.setEndTime(end);
        e.setOrganizerId(selectedOrg.getOrganizerId());
        e.setVenueId(selectedVenue.getVenueId());
        e.setMaxCapacity(Integer.parseInt(capStr));
        e.setRegistrationFee(Double.parseDouble(feeStr));
        e.setStatus(status);
        e.setDescription(desc);

        return e;
    }
}
