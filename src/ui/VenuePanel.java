package ui;

import model.Venue;
import service.AuthenticationService;
import service.VenueService;
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
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.List;

/**
 * Venue Management Screen for auditoriums, seminar halls, and campus rooms.
 */
public class VenuePanel extends JPanel {

    private final VenueService venueService;
    private final JFrame parentFrame;

    // Form
    private JTextField txtVenueId;
    private JTextField txtVenueName;
    private JTextField txtLocation;
    private JTextField txtCapacity;
    private JComboBox<String> cmbAvailable;

    // Search
    private JTextField txtSearch;

    // Table
    private JTable tblVenues;
    private DefaultTableModel tableModel;
    private List<Venue> currentList;

    public VenuePanel(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.venueService = new VenueService();
        initComponents();
        loadVenues();
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
        JLabel lblTitle = new JLabel("Venue & Hall Management");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        JLabel lblSub = new JLabel("Manage auditorium capacities, lecture theaters, and availability for scheduling");
        lblSub.setFont(UITheme.FONT_BODY);
        lblSub.setForeground(UITheme.COLOR_TEXT_MUTED);
        titlePanel.add(lblTitle);
        titlePanel.add(lblSub);
        topPanel.add(titlePanel, BorderLayout.WEST);

        // Search Bar
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchBar.setOpaque(false);
        txtSearch = UITheme.createTextField(16);
        txtSearch.setToolTipText("Search by Venue Name or Location");

        JButton btnSearch = UITheme.createPrimaryButton("Search");
        JButton btnReset = UITheme.createSecondaryButton("Reset");

        searchBar.add(new JLabel("Search:"));
        searchBar.add(txtSearch);
        searchBar.add(btnSearch);
        searchBar.add(btnReset);
        topPanel.add(searchBar, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Split Pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.35);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);

        // Left Form Card
        JPanel formCard = UITheme.createCardPanel();
        formCard.setLayout(new BorderLayout(0, 10));

        JLabel lblFormTitle = new JLabel("Venue Configuration");
        lblFormTitle.setFont(UITheme.FONT_HEADER);
        lblFormTitle.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        formCard.add(lblFormTitle, BorderLayout.NORTH);

        JPanel formFields = new JPanel(new GridBagLayout());
        formFields.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        txtVenueId = UITheme.createTextField(10);
        txtVenueId.setEditable(false);
        txtVenueId.setBackground(new Color(241, 245, 249));
        addFormField(formFields, gbc, 0, "ID (Auto):", txtVenueId);

        txtVenueName = UITheme.createTextField(15);
        addFormField(formFields, gbc, 1, "Venue Name *:", txtVenueName);

        txtLocation = UITheme.createTextField(15);
        addFormField(formFields, gbc, 2, "Campus Location:", txtLocation);

        txtCapacity = UITheme.createTextField(10);
        addFormField(formFields, gbc, 3, "Seating Capacity *:", txtCapacity);

        cmbAvailable = new JComboBox<>(new String[]{"YES", "NO"});
        cmbAvailable.setFont(UITheme.FONT_BODY);
        addFormField(formFields, gbc, 4, "Is Available *:", cmbAvailable);

        formCard.add(formFields, BorderLayout.CENTER);

        // Action Buttons
        JPanel actionPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        actionPanel.setOpaque(false);
        actionPanel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        JButton btnAdd = UITheme.createSuccessButton("ADD VENUE");
        JButton btnUpdate = UITheme.createPrimaryButton("UPDATE DETAILS");
        JButton btnDelete = UITheme.createDangerButton("DELETE");
        JButton btnClear = UITheme.createSecondaryButton("CLEAR FORM");

        actionPanel.add(btnAdd);
        actionPanel.add(btnUpdate);
        actionPanel.add(btnDelete);
        actionPanel.add(btnClear);

        // Restrict if not admin
        boolean isAdmin = AuthenticationService.isAdmin();
        btnAdd.setEnabled(isAdmin);
        btnUpdate.setEnabled(isAdmin);
        btnDelete.setEnabled(isAdmin);

        formCard.add(actionPanel, BorderLayout.SOUTH);
        splitPane.setLeftComponent(formCard);

        // Right Table Card
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 10));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);
        JLabel lblTableTitle = new JLabel("Campus Venues & Facilities Directory");
        lblTableTitle.setFont(UITheme.FONT_HEADER);
        lblTableTitle.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        tableHeader.add(lblTableTitle, BorderLayout.WEST);

        JButton btnRefresh = UITheme.createSecondaryButton("Refresh");
        tableHeader.add(btnRefresh, BorderLayout.EAST);
        tableCard.add(tableHeader, BorderLayout.NORTH);

        String[] columns = {"ID", "Venue Name", "Location", "Seating Capacity", "Availability Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblVenues = new JTable(tableModel);
        UITheme.styleTable(tblVenues);

        tblVenues.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateForm();
            }
        });

        tableCard.add(UITheme.createStyledScrollPane(tblVenues), BorderLayout.CENTER);
        splitPane.setRightComponent(tableCard);

        add(splitPane, BorderLayout.CENTER);

        // Action Listeners
        btnAdd.addActionListener(e -> handleAdd());
        btnUpdate.addActionListener(e -> handleUpdate());
        btnDelete.addActionListener(e -> handleDelete());
        btnClear.addActionListener(e -> clearForm());
        btnSearch.addActionListener(e -> handleSearch());
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            loadVenues();
        });
        btnRefresh.addActionListener(e -> loadVenues());
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

    public void loadVenues() {
        SwingUtilities.invokeLater(() -> {
            try {
                currentList = venueService.getAllVenues();
                tableModel.setRowCount(0);
                for (Venue v : currentList) {
                    tableModel.addRow(new Object[]{
                        v.getVenueId(),
                        v.getVenueName(),
                        v.getLocation(),
                        v.getCapacity(),
                        v.getAvailable()
                    });
                }
            } catch (SQLException ex) {
                DialogUtil.handleSQLException(this, ex, "Failed to load venues");
            }
        });
    }

    private void populateForm() {
        int row = tblVenues.getSelectedRow();
        if (row < 0 || currentList == null || row >= currentList.size()) return;

        Venue v = currentList.get(row);
        txtVenueId.setText(String.valueOf(v.getVenueId()));
        txtVenueName.setText(v.getVenueName());
        txtLocation.setText(v.getLocation());
        txtCapacity.setText(String.valueOf(v.getCapacity()));
        cmbAvailable.setSelectedItem(v.getAvailable());
    }

    private void handleAdd() {
        try {
            Venue v = new Venue(
                txtVenueName.getText().trim(),
                txtLocation.getText().trim(),
                Integer.parseInt(txtCapacity.getText().trim()),
                (String) cmbAvailable.getSelectedItem()
            );
            venueService.addVenue(v);
            DialogUtil.showInfo(this, "Venue record added successfully!", "Venue Created");
            clearForm();
            loadVenues();
        } catch (NumberFormatException ex) {
            DialogUtil.showError(this, "Capacity must be a valid integer number.", "Invalid Number");
        } catch (IllegalArgumentException ex) {
            DialogUtil.showError(this, ex.getMessage(), "Validation Error");
        } catch (SQLException ex) {
            DialogUtil.handleSQLException(this, ex, "Add Venue Failed");
        }
    }

    private void handleUpdate() {
        if (txtVenueId.getText().trim().isEmpty()) {
            DialogUtil.showWarning(this, "Please select a venue to update.", "No Selection");
            return;
        }

        try {
            int id = Integer.parseInt(txtVenueId.getText().trim());
            Venue v = new Venue(
                id,
                txtVenueName.getText().trim(),
                txtLocation.getText().trim(),
                Integer.parseInt(txtCapacity.getText().trim()),
                (String) cmbAvailable.getSelectedItem()
            );
            venueService.updateVenue(v);
            DialogUtil.showInfo(this, "Venue updated successfully!", "Updated");
            loadVenues();
        } catch (NumberFormatException ex) {
            DialogUtil.showError(this, "Capacity must be a valid integer number.", "Invalid Number");
        } catch (IllegalArgumentException ex) {
            DialogUtil.showError(this, ex.getMessage(), "Validation Error");
        } catch (SQLException ex) {
            DialogUtil.handleSQLException(this, ex, "Update Venue Failed");
        }
    }

    private void handleDelete() {
        if (txtVenueId.getText().trim().isEmpty()) {
            DialogUtil.showWarning(this, "Please select a venue to delete.", "No Selection");
            return;
        }

        int id = Integer.parseInt(txtVenueId.getText().trim());
        String name = txtVenueName.getText().trim();

        boolean confirm = DialogUtil.showConfirm(this,
            "Are you sure you want to delete venue: '" + name + "' (ID: " + id + ")?",
            "Confirm Venue Deletion");
        if (!confirm) return;

        try {
            venueService.deleteVenue(id);
            DialogUtil.showInfo(this, "Venue record deleted.", "Deleted");
            clearForm();
            loadVenues();
        } catch (SQLException ex) {
            DialogUtil.showError(this, ex.getMessage(), "Cannot Delete Venue");
        }
    }

    private void handleSearch() {
        String keyword = txtSearch.getText().trim();
        try {
            currentList = venueService.searchVenues(keyword);
            tableModel.setRowCount(0);
            for (Venue v : currentList) {
                tableModel.addRow(new Object[]{
                    v.getVenueId(),
                    v.getVenueName(),
                    v.getLocation(),
                    v.getCapacity(),
                    v.getAvailable()
                });
            }
        } catch (SQLException ex) {
            DialogUtil.handleSQLException(this, ex, "Search Failed");
        }
    }

    private void clearForm() {
        txtVenueId.setText("");
        txtVenueName.setText("");
        txtLocation.setText("");
        txtCapacity.setText("");
        cmbAvailable.setSelectedIndex(0);
        tblVenues.clearSelection();
    }
}
