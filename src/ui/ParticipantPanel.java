package ui;

import model.Participant;
import service.ParticipantService;
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
 * Participant Management Screen for student & attendee records.
 */
public class ParticipantPanel extends JPanel {

    private final ParticipantService participantService;
    private final JFrame parentFrame;

    // Form Fields
    private JTextField txtParticipantId;
    private JTextField txtName;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JTextField txtCollege;
    private JTextField txtDepartment;

    // Search
    private JTextField txtSearch;

    // Table
    private JTable tblParticipants;
    private DefaultTableModel tableModel;
    private List<Participant> currentList;

    public ParticipantPanel(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.participantService = new ParticipantService();
        initComponents();
        loadParticipants();
    }

    private void initComponents() {
        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.COLOR_BG);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // Top Header & Search Toolbar
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        titlePanel.setOpaque(false);
        JLabel lblTitle = new JLabel("Participant Management");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        JLabel lblSub = new JLabel("Manage student registrations, verify contact information, and prevent duplicates");
        lblSub.setFont(UITheme.FONT_BODY);
        lblSub.setForeground(UITheme.COLOR_TEXT_MUTED);
        titlePanel.add(lblTitle);
        titlePanel.add(lblSub);
        topPanel.add(titlePanel, BorderLayout.WEST);

        // Search Bar
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchBar.setOpaque(false);
        txtSearch = UITheme.createTextField(16);
        txtSearch.setToolTipText("Search by Name, Email, College or Dept");

        JButton btnSearch = UITheme.createPrimaryButton("Search");
        JButton btnReset = UITheme.createSecondaryButton("Reset");

        searchBar.add(new JLabel("Search:"));
        searchBar.add(txtSearch);
        searchBar.add(btnSearch);
        searchBar.add(btnReset);
        topPanel.add(searchBar, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Split Pane (Form on Left, Table on Right)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.35);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);

        // --- Left Form Card ---
        JPanel formCard = UITheme.createCardPanel();
        formCard.setLayout(new BorderLayout(0, 10));

        JLabel lblFormTitle = new JLabel("Participant Information");
        lblFormTitle.setFont(UITheme.FONT_HEADER);
        lblFormTitle.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        formCard.add(lblFormTitle, BorderLayout.NORTH);

        JPanel formFields = new JPanel(new GridBagLayout());
        formFields.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        txtParticipantId = UITheme.createTextField(10);
        txtParticipantId.setEditable(false);
        txtParticipantId.setBackground(new Color(241, 245, 249));
        addFormField(formFields, gbc, 0, "ID (Auto):", txtParticipantId);

        txtName = UITheme.createTextField(15);
        addFormField(formFields, gbc, 1, "Full Name *:", txtName);

        txtEmail = UITheme.createTextField(15);
        addFormField(formFields, gbc, 2, "Email Address *:", txtEmail);

        txtPhone = UITheme.createTextField(15);
        addFormField(formFields, gbc, 3, "Phone Number:", txtPhone);

        txtCollege = UITheme.createTextField(15);
        addFormField(formFields, gbc, 4, "College / Univ *:", txtCollege);

        txtDepartment = UITheme.createTextField(15);
        addFormField(formFields, gbc, 5, "Department:", txtDepartment);

        formCard.add(formFields, BorderLayout.CENTER);

        // CRUD Action Buttons
        JPanel actionPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        actionPanel.setOpaque(false);
        actionPanel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        JButton btnAdd = UITheme.createSuccessButton("ADD PARTICIPANT");
        JButton btnUpdate = UITheme.createPrimaryButton("UPDATE DETAILS");
        JButton btnDelete = UITheme.createDangerButton("DELETE");
        JButton btnClear = UITheme.createSecondaryButton("CLEAR FORM");

        actionPanel.add(btnAdd);
        actionPanel.add(btnUpdate);
        actionPanel.add(btnDelete);
        actionPanel.add(btnClear);

        formCard.add(actionPanel, BorderLayout.SOUTH);
        splitPane.setLeftComponent(formCard);

        // --- Right Table Card ---
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 10));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);
        JLabel lblTableTitle = new JLabel("Enrolled Participants Directory");
        lblTableTitle.setFont(UITheme.FONT_HEADER);
        lblTableTitle.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        tableHeader.add(lblTableTitle, BorderLayout.WEST);

        JButton btnRefresh = UITheme.createSecondaryButton("Refresh");
        tableHeader.add(btnRefresh, BorderLayout.EAST);
        tableCard.add(tableHeader, BorderLayout.NORTH);

        String[] columns = {"ID", "Name", "Email", "Phone", "College / Institution", "Department"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblParticipants = new JTable(tableModel);
        UITheme.styleTable(tblParticipants);

        tblParticipants.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateForm();
            }
        });

        tableCard.add(UITheme.createStyledScrollPane(tblParticipants), BorderLayout.CENTER);
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
            loadParticipants();
        });
        btnRefresh.addActionListener(e -> loadParticipants());
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

    public void loadParticipants() {
        SwingUtilities.invokeLater(() -> {
            try {
                currentList = participantService.getAllParticipants();
                tableModel.setRowCount(0);
                for (Participant p : currentList) {
                    tableModel.addRow(new Object[]{
                        p.getParticipantId(),
                        p.getName(),
                        p.getEmail(),
                        p.getPhone(),
                        p.getCollege(),
                        p.getDepartment()
                    });
                }
            } catch (SQLException ex) {
                DialogUtil.handleSQLException(this, ex, "Failed to load participants");
            }
        });
    }

    private void populateForm() {
        int row = tblParticipants.getSelectedRow();
        if (row < 0 || currentList == null || row >= currentList.size()) return;

        Participant p = currentList.get(row);
        txtParticipantId.setText(String.valueOf(p.getParticipantId()));
        txtName.setText(p.getName());
        txtEmail.setText(p.getEmail());
        txtPhone.setText(p.getPhone());
        txtCollege.setText(p.getCollege());
        txtDepartment.setText(p.getDepartment());
    }

    private void handleAdd() {
        try {
            Participant p = new Participant(
                txtName.getText().trim(),
                txtEmail.getText().trim(),
                txtPhone.getText().trim(),
                txtCollege.getText().trim(),
                txtDepartment.getText().trim()
            );
            participantService.addParticipant(p);
            DialogUtil.showInfo(this, "Participant successfully added to database!", "Participant Added");
            clearForm();
            loadParticipants();
        } catch (IllegalArgumentException ex) {
            DialogUtil.showError(this, ex.getMessage(), "Validation Error");
        } catch (SQLException ex) {
            DialogUtil.handleSQLException(this, ex, "Add Participant Failed");
        }
    }

    private void handleUpdate() {
        if (txtParticipantId.getText().trim().isEmpty()) {
            DialogUtil.showWarning(this, "Please select a participant from the table to update.", "No Selection");
            return;
        }

        try {
            int id = Integer.parseInt(txtParticipantId.getText().trim());
            Participant p = new Participant(
                id,
                txtName.getText().trim(),
                txtEmail.getText().trim(),
                txtPhone.getText().trim(),
                txtCollege.getText().trim(),
                txtDepartment.getText().trim()
            );
            participantService.updateParticipant(p);
            DialogUtil.showInfo(this, "Participant information updated successfully!", "Updated");
            loadParticipants();
        } catch (IllegalArgumentException ex) {
            DialogUtil.showError(this, ex.getMessage(), "Validation Error");
        } catch (SQLException ex) {
            DialogUtil.handleSQLException(this, ex, "Update Participant Failed");
        }
    }

    private void handleDelete() {
        if (txtParticipantId.getText().trim().isEmpty()) {
            DialogUtil.showWarning(this, "Please select a participant to delete.", "No Selection");
            return;
        }

        int id = Integer.parseInt(txtParticipantId.getText().trim());
        String name = txtName.getText().trim();

        boolean confirm = DialogUtil.showConfirm(this,
            "Are you sure you want to delete participant: '" + name + "' (ID: " + id + ")?",
            "Confirm Participant Deletion");
        if (!confirm) return;

        try {
            participantService.deleteParticipant(id);
            DialogUtil.showInfo(this, "Participant deleted successfully.", "Deleted");
            clearForm();
            loadParticipants();
        } catch (SQLException ex) {
            DialogUtil.showError(this, ex.getMessage(), "Cannot Delete Participant");
        }
    }

    private void handleSearch() {
        String keyword = txtSearch.getText().trim();
        try {
            currentList = participantService.searchParticipants(keyword);
            tableModel.setRowCount(0);
            for (Participant p : currentList) {
                tableModel.addRow(new Object[]{
                    p.getParticipantId(),
                    p.getName(),
                    p.getEmail(),
                    p.getPhone(),
                    p.getCollege(),
                    p.getDepartment()
                });
            }
        } catch (SQLException ex) {
            DialogUtil.handleSQLException(this, ex, "Search Failed");
        }
    }

    private void clearForm() {
        txtParticipantId.setText("");
        txtName.setText("");
        txtEmail.setText("");
        txtPhone.setText("");
        txtCollege.setText("");
        txtDepartment.setText("");
        tblParticipants.clearSelection();
    }
}
