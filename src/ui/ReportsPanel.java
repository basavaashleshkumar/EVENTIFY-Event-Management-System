package ui;

import dao.ReportDAO.ReportResult;
import model.Event;
import service.EventService;
import service.ReportService;
import util.DialogUtil;
import util.UITheme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Analytical Reports & Business Intelligence Panel.
 * Demonstrates complex SQL JOINs, aggregate queries, and CSV report export.
 */
public class ReportsPanel extends JPanel {

    private final ReportService reportService;
    private final EventService eventService;
    private final JFrame parentFrame;

    private JComboBox<String> cmbReportSelect;
    private JComboBox<Event> cmbEventFilter;
    private JLabel lblEventFilterPrompt;
    private JLabel lblStatusInfo;

    private JTable tblReport;
    private DefaultTableModel tableModel;
    private ReportResult currentReportResult;

    private final String[] reportNames = {
        "1. All Events Directory (Full Relational Details)",
        "2. Upcoming Events & Seat Availability",
        "3. Completed Events & Attendance Summary",
        "4. Events Categorized by Organizer",
        "5. Events Categorized by Venue",
        "6. Participants Enrolled in Selected Event",
        "7. Registration Breakdown per Event (Active / Attended / Cancelled)",
        "8. Top Events Ranked by Registration Demand (Occupancy %)",
        "9. Available Campus Venues & Seating Capacities",
        "10. Cancelled Events Log"
    };

    public ReportsPanel(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.reportService = new ReportService();
        this.eventService = new EventService();

        initComponents();
        loadEventFilterData();
        generateReport(1); // load default report
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
        JLabel lblTitle = new JLabel("Database Analytical Reports");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        JLabel lblSub = new JLabel("Execute normalized SQL JOIN queries and export aggregated datasets to CSV");
        lblSub.setFont(UITheme.FONT_BODY);
        lblSub.setForeground(UITheme.COLOR_TEXT_MUTED);
        titlePanel.add(lblTitle);
        titlePanel.add(lblSub);
        topPanel.add(titlePanel, BorderLayout.WEST);

        add(topPanel, BorderLayout.NORTH);

        // Center Panel: Controls + Table
        JPanel centerPanel = new JPanel(new BorderLayout(12, 12));
        centerPanel.setOpaque(false);

        // Query Configuration Card
        JPanel controlCard = UITheme.createCardPanel();
        controlCard.setLayout(new BorderLayout(10, 10));

        JPanel controlsLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        controlsLeft.setOpaque(false);

        JLabel lblChoose = new JLabel("Select Report Query:");
        lblChoose.setFont(UITheme.FONT_BODY_BOLD);
        controlsLeft.add(lblChoose);

        cmbReportSelect = new JComboBox<>(reportNames);
        cmbReportSelect.setFont(UITheme.FONT_BODY);
        cmbReportSelect.setPreferredSize(new Dimension(380, 34));
        cmbReportSelect.addActionListener(e -> onReportSelected());
        controlsLeft.add(cmbReportSelect);

        lblEventFilterPrompt = new JLabel("Select Event:");
        lblEventFilterPrompt.setFont(UITheme.FONT_BODY_BOLD);
        lblEventFilterPrompt.setVisible(false);
        controlsLeft.add(lblEventFilterPrompt);

        cmbEventFilter = new JComboBox<>();
        cmbEventFilter.setFont(UITheme.FONT_BODY);
        cmbEventFilter.setPreferredSize(new Dimension(240, 34));
        cmbEventFilter.setVisible(false);
        controlsLeft.add(cmbEventFilter);

        controlCard.add(controlsLeft, BorderLayout.CENTER);

        // Action Buttons on Right
        JPanel controlsRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        controlsRight.setOpaque(false);

        JButton btnGenerate = UITheme.createPrimaryButton("Run Query");
        JButton btnExportCsv = UITheme.createSuccessButton("Export CSV");

        controlsRight.add(btnGenerate);
        controlsRight.add(btnExportCsv);
        controlCard.add(controlsRight, BorderLayout.EAST);

        centerPanel.add(controlCard, BorderLayout.NORTH);

        // Table Card
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 10));

        lblStatusInfo = new JLabel("Report ready.");
        lblStatusInfo.setFont(UITheme.FONT_BODY_BOLD);
        lblStatusInfo.setForeground(UITheme.COLOR_PRIMARY);
        tableCard.add(lblStatusInfo, BorderLayout.NORTH);

        tableModel = new DefaultTableModel();
        tblReport = new JTable(tableModel) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        UITheme.styleTable(tblReport);

        tableCard.add(UITheme.createStyledScrollPane(tblReport), BorderLayout.CENTER);
        centerPanel.add(tableCard, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // Listeners
        btnGenerate.addActionListener(e -> {
            int selectedIndex = cmbReportSelect.getSelectedIndex() + 1;
            generateReport(selectedIndex);
        });

        btnExportCsv.addActionListener(e -> handleExportCsv());
    }

    private void loadEventFilterData() {
        try {
            cmbEventFilter.removeAllItems();
            List<Event> events = eventService.getAllEvents();
            for (Event ev : events) {
                cmbEventFilter.addItem(ev);
            }
        } catch (SQLException ex) {
            DialogUtil.handleSQLException(this, ex, "Failed to load events for filter");
        }
    }

    private void onReportSelected() {
        int reportIndex = cmbReportSelect.getSelectedIndex() + 1;
        boolean needsEventFilter = (reportIndex == 6);
        lblEventFilterPrompt.setVisible(needsEventFilter);
        cmbEventFilter.setVisible(needsEventFilter);
        revalidate();
        repaint();
    }

    private void generateReport(int reportIndex) {
        SwingUtilities.invokeLater(() -> {
            try {
                int eventId = 0;
                if (reportIndex == 6) {
                    Event selectedEvent = (Event) cmbEventFilter.getSelectedItem();
                    if (selectedEvent != null) {
                        eventId = selectedEvent.getEventId();
                    }
                }

                currentReportResult = reportService.getReport(reportIndex, eventId);

                // Update Table Model
                tableModel.setDataVector(
                    currentReportResult.getData().toArray(new Object[0][0]),
                    currentReportResult.getColumns()
                );
                UITheme.styleTable(tblReport);

                int rowCount = currentReportResult.getData().size();
                lblStatusInfo.setText("Query executed successfully. Returned " + rowCount + " records from Oracle Database.");

            } catch (SQLException ex) {
                DialogUtil.handleSQLException(this, ex, "Report Query Execution Error");
            }
        });
    }

    private void handleExportCsv() {
        if (currentReportResult == null || currentReportResult.getData().isEmpty()) {
            DialogUtil.showWarning(this, "No records to export. Please run a report first.", "No Data");
            return;
        }

        String reportTitle = "report_" + (cmbReportSelect.getSelectedIndex() + 1) + ".csv";
        File file = DialogUtil.chooseCsvSaveFile(this, reportTitle);
        if (file == null) return;

        try {
            reportService.exportToCsv(currentReportResult, file);
            DialogUtil.showInfo(this, "Report successfully exported to:\n" + file.getAbsolutePath(), "Export Complete");
        } catch (IOException ex) {
            DialogUtil.showError(this, "Failed to write CSV file: " + ex.getMessage(), "File Export Error");
        }
    }
}
