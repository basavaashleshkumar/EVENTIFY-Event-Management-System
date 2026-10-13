package ui;

import model.Organizer;
import service.AuthenticationService;
import service.OrganizerService;
import util.DialogUtil;
import util.UITheme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
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
 * Organizer Management Screen for administrative control of event organizers.
 */
public class OrganizerPanel extends JPanel {

    private final OrganizerService organizerService;
    private final JFrame parentFrame;

    // Form
    private JTextField txtOrganizerId;
    private JTextField txtName;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JTextField txtDepartment;
    private JTextField txtUsername;

    // Search
    private JTextField txtSearch;

    // Table
    private JTable tblOrganizers;
    private DefaultTableModel tableModel;
    private List<Organizer> currentList;

    public OrganizerPanel(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.organizerService = new OrganizerService();
        initComponents();
        loadOrganizers();
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
        JLabel lblTitle = new JLabel("Organizer Management (Admin)");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        JLabel lblSub = new JLabel("Manage faculty advisors, department coordinators, and login account linkages");
        lblSub.setFont(UITheme.FONT_BODY);
        lblSub.setForeground(UITheme.COLOR_TEXT_MUTED);
        titlePanel.add(lblTitle);
        titlePanel.add(lblSub);
        topPanel.add(titlePanel, BorderLayout.WEST);

        // Search
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchBar.setOpaque(false);
        txtSearch = UITheme.createTextField(16);
        txtSearch.setToolTipText("Search by Name, Department, Email or Phone");

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

        JLabel lblFormTitle = new JLabel("Organizer Profile");
        lblFormTitle.setFont(UITheme.FONT_HEADER);
        lblFormTitle.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        formCard.add(lblFormTitle, BorderLayout.NORTH);

        JPanel formFields = new JPanel(new GridBagLayout());
        formFields.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        txtOrganizerId = UITheme.createTextField(10);
        txtOrganizerId.setEditable(false);
        txtOrganizerId.setBackground(new Color(241, 245, 249));
        addFormField(formFields, gbc, 0, "ID (Auto):", txtOrganizerId);

        txtName = UITheme.createTextField(15);
        addFormField(formFields, gbc, 1, "Organizer Name *:", txtName);

        txtEmail = UITheme.createTextField(15);
        addFormField(formFields, gbc, 2, "Email Address *:", txtEmail);

        txtPhone = UITheme.createTextField(15);
        addFormField(formFields, gbc, 3, "Phone Number:", txtPhone);

        txtDepartment = UITheme.createTextField(15);
        addFormField(formFields, gbc, 4, "Department:", txtDepartment);

        txtUsername = UITheme.createTextField(15);
        txtUsername.setToolTipText("Linked login username in USERS table");
        addFormField(formFields, gbc, 5, "Account Username:", txtUsername);

        formCard.add(formFields, BorderLayout.CENTER);

        // Action Buttons
        JPanel actionPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        actionPanel.setOpaque(false);
        actionPanel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        JButton btnAdd = UITheme.createSuccessButton("ADD ORGANIZER");
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
        JLabel lblTableTitle = new JLabel("Faculty & Staff Organizers Directory");
        lblTableTitle.setFont(UITheme.FONT_HEADER);
        lblTableTitle.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        tableHeader.add(lblTableTitle, BorderLayout.WEST);

        JButton btnRefresh = UITheme.createSecondaryButton("Refresh");
        tableHeader.add(btnRefresh, BorderLayout.EAST);
        tableCard.add(tableHeader, BorderLayout.NORTH);

        String[] columns = {"ID", "Organizer Name", "Email", "Phone", "Department", "Account Username"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblOrganizers = new JTable(tableModel);
        UITheme.styleTable(tblOrganizers);

        tblOrganizers.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateForm();
            }
        });

        tableCard.add(UITheme.createStyledScrollPane(tblOrganizers), BorderLayout.CENTER);
        splitPane.setRightComponent(tableCard);

        add(splitPane, BorderLayout.CENTER);

        // Listeners
        btnAdd.addActionListener(e -> handleAdd());
        btnUpdate.addActionListener(e -> handleUpdate());
        btnDelete.addActionListener(e -> handleDelete());
        btnClear.addActionListener(e -> clearForm());
        btnSearch.addActionListener(e -> handleSearch());
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            loadOrganizers();
        });
        btnRefresh.addActionListener(e -> loadOrganizers());
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

    public void loadOrganizers() {
        SwingUtilities.invokeLater(() -> {
            try {
                currentList = organizerService.getAllOrganizers();
                tableModel.setRowCount(0);
                for (Organizer o : currentList) {
                    tableModel.addRow(new Object[]{
                        o.getOrganizerId(),
                        o.getName(),
                        o.getEmail(),
                        o.getPhone(),
                        o.getDepartment(),
                        o.getUsername() != null ? o.getUsername() : "-"
                    });
                }
            } catch (SQLException ex) {
                DialogUtil.handleSQLException(this, ex, "Failed to load organizers");
            }
        });
    }

    private void populateForm() {
        int row = tblOrganizers.getSelectedRow();
        if (row < 0 || currentList == null || row >= currentList.size()) return;

        Organizer o = currentList.get(row);
        txtOrganizerId.setText(String.valueOf(o.getOrganizerId()));
        txtName.setText(o.getName());
        txtEmail.setText(o.getEmail());
        txtPhone.setText(o.getPhone());
        txtDepartment.setText(o.getDepartment());
        txtUsername.setText(o.getUsername() != null ? o.getUsername() : "");
    }

    private void handleAdd() {
        try {
            Organizer o = new Organizer(
                txtName.getText().trim(),
                txtEmail.getText().trim(),
                txtPhone.getText().trim(),
                txtDepartment.getText().trim(),
                txtUsername.getText().trim().isEmpty() ? null : txtUsername.getText().trim()
            );
            organizerService.addOrganizer(o);
            DialogUtil.showInfo(this, "Organizer record added successfully!", "Organizer Created");
            clearForm();
            loadOrganizers();
        } catch (IllegalArgumentException ex) {
            DialogUtil.showError(this, ex.getMessage(), "Validation Error");
        } catch (SQLException ex) {
            DialogUtil.handleSQLException(this, ex, "Add Organizer Failed");
        }
    }

    private void handleUpdate() {
        if (txtOrganizerId.getText().trim().isEmpty()) {
            DialogUtil.showWarning(this, "Please select an organizer to update.", "No Selection");
            return;
        }

        try {
            int id = Integer.parseInt(txtOrganizerId.getText().trim());
            Organizer o = new Organizer(
                id,
                txtName.getText().trim(),
                txtEmail.getText().trim(),
                txtPhone.getText().trim(),
                txtDepartment.getText().trim(),
                txtUsername.getText().trim().isEmpty() ? null : txtUsername.getText().trim()
            );
            organizerService.updateOrganizer(o);
            DialogUtil.showInfo(this, "Organizer details updated successfully!", "Updated");
            loadOrganizers();
        } catch (IllegalArgumentException ex) {
            DialogUtil.showError(this, ex.getMessage(), "Validation Error");
        } catch (SQLException ex) {
            DialogUtil.handleSQLException(this, ex, "Update Organizer Failed");
        }
    }

    private void handleDelete() {
        if (txtOrganizerId.getText().trim().isEmpty()) {
            DialogUtil.showWarning(this, "Please select an organizer to delete.", "No Selection");
            return;
        }

        int id = Integer.parseInt(txtOrganizerId.getText().trim());
        String name = txtName.getText().trim();

        boolean confirm = DialogUtil.showConfirm(this,
            "Are you sure you want to delete organizer: '" + name + "' (ID: " + id + ")?",
            "Confirm Organizer Deletion");
        if (!confirm) return;

        try {
            organizerService.deleteOrganizer(id);
            DialogUtil.showInfo(this, "Organizer record deleted.", "Deleted");
            clearForm();
            loadOrganizers();
        } catch (SQLException ex) {
            DialogUtil.showError(this, ex.getMessage(), "Cannot Delete Organizer");
        }
    }

    private void handleSearch() {
        String keyword = txtSearch.getText().trim();
        try {
            currentList = organizerService.searchOrganizers(keyword);
            tableModel.setRowCount(0);
            for (Organizer o : currentList) {
                tableModel.addRow(new Object[]{
                    o.getOrganizerId(),
                    o.getName(),
                    o.getEmail(),
                    o.getPhone(),
                    o.getDepartment(),
                    o.getUsername() != null ? o.getUsername() : "-"
                });
            }
        } catch (SQLException ex) {
            DialogUtil.handleSQLException(this, ex, "Search Failed");
        }
    }

    private void clearForm() {
        txtOrganizerId.setText("");
        txtName.setText("");
        txtEmail.setText("");
        txtPhone.setText("");
        txtDepartment.setText("");
        txtUsername.setText("");
        tblOrganizers.clearSelection();
    }
}
