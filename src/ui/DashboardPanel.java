package ui;

import dao.ReportDAO;
import model.Event;
import model.Registration;
import service.EventService;
import service.RegistrationService;
import service.ReportService;
import util.DialogUtil;
import util.UITheme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Modern Dashboard View showing live Oracle statistics, summary KPI cards,
 * upcoming events, and recent participant registrations.
 */
public class DashboardPanel extends JPanel {

    private final ReportService reportService;
    private final EventService eventService;
    private final RegistrationService registrationService;

    // Stat Value Labels
    private JLabel lblTotalEvents;
    private JLabel lblUpcomingEvents;
    private JLabel lblCompletedEvents;
    private JLabel lblTotalParticipants;
    private JLabel lblTotalRegistrations;
    private JLabel lblAvailableVenues;
    private JLabel lblTopEvent;
    private JLabel lblPopularType;

    // Tables
    private JTable tblUpcoming;
    private DefaultTableModel modelUpcoming;
    private JTable tblRegistrations;
    private DefaultTableModel modelRegistrations;

    public DashboardPanel() {
        this.reportService = new ReportService();
        this.eventService = new EventService();
        this.registrationService = new RegistrationService();
        initComponents();
        loadDashboardData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.COLOR_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Top Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBox.setOpaque(false);
        JLabel lblHeading = new JLabel("System Overview & Live Metrics");
        lblHeading.setFont(UITheme.FONT_TITLE);
        lblHeading.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Real-time operational statistics calculated directly from Oracle Database");
        lblSub.setFont(UITheme.FONT_BODY);
        lblSub.setForeground(UITheme.COLOR_TEXT_MUTED);

        titleBox.add(lblHeading);
        titleBox.add(lblSub);
        topPanel.add(titleBox, BorderLayout.WEST);

        JButton btnRefresh = UITheme.createPrimaryButton("Refresh Metrics");
        btnRefresh.addActionListener(e -> loadDashboardData());
        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnWrap.setOpaque(false);
        btnWrap.add(btnRefresh);
        topPanel.add(btnWrap, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Center Content Panel
        JPanel centerPanel = new JPanel(new BorderLayout(16, 16));
        centerPanel.setOpaque(false);

        // KPI Stat Cards Grid (2 rows x 3 columns)
        JPanel statsGrid = new JPanel(new GridLayout(2, 3, 14, 14));
        statsGrid.setOpaque(false);

        lblTotalEvents = new JLabel("0", SwingConstants.CENTER);
        lblUpcomingEvents = new JLabel("0", SwingConstants.CENTER);
        lblCompletedEvents = new JLabel("0", SwingConstants.CENTER);
        lblTotalParticipants = new JLabel("0", SwingConstants.CENTER);
        lblTotalRegistrations = new JLabel("0", SwingConstants.CENTER);
        lblAvailableVenues = new JLabel("0", SwingConstants.CENTER);

        statsGrid.add(createStatCard("Total Events", lblTotalEvents, UITheme.COLOR_PRIMARY));
        statsGrid.add(createStatCard("Upcoming Events", lblUpcomingEvents, UITheme.COLOR_ACCENT));
        statsGrid.add(createStatCard("Completed Events", lblCompletedEvents, UITheme.COLOR_SUCCESS));
        statsGrid.add(createStatCard("Total Participants", lblTotalParticipants, new Color(139, 92, 246))); // Purple
        statsGrid.add(createStatCard("Active Registrations", lblTotalRegistrations, new Color(234, 88, 12))); // Orange
        statsGrid.add(createStatCard("Available Venues", lblAvailableVenues, new Color(16, 185, 129))); // Green

        centerPanel.add(statsGrid, BorderLayout.NORTH);

        // Highlights Panel (Top Event + Popular Category)
        JPanel highlightsPanel = new JPanel(new GridLayout(1, 2, 14, 14));
        highlightsPanel.setOpaque(false);
        highlightsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        lblTopEvent = new JLabel("Loading...", SwingConstants.LEFT);
        lblTopEvent.setFont(UITheme.FONT_BODY_BOLD);
        lblTopEvent.setForeground(UITheme.COLOR_PRIMARY);

        lblPopularType = new JLabel("Loading...", SwingConstants.LEFT);
        lblPopularType.setFont(UITheme.FONT_BODY_BOLD);
        lblPopularType.setForeground(new Color(234, 88, 12));

        highlightsPanel.add(createBannerCard("Event with Maximum Registrations", lblTopEvent));
        highlightsPanel.add(createBannerCard("Most Popular Event Category", lblPopularType));

        // Bottom Split Panel: Upcoming Events & Recent Registrations
        JPanel tablesPanel = new JPanel(new GridLayout(1, 2, 14, 14));
        tablesPanel.setOpaque(false);

        // Left: Upcoming Events
        JPanel pnlUpcoming = UITheme.createCardPanel();
        pnlUpcoming.setLayout(new BorderLayout(0, 8));
        JLabel lblUpcomingTitle = new JLabel("Upcoming Events Schedule");
        lblUpcomingTitle.setFont(UITheme.FONT_SUBHEADER);
        lblUpcomingTitle.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        pnlUpcoming.add(lblUpcomingTitle, BorderLayout.NORTH);

        modelUpcoming = new DefaultTableModel(new String[]{"ID", "Event Name", "Date", "Venue", "Capacity", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblUpcoming = new JTable(modelUpcoming);
        UITheme.styleTable(tblUpcoming);
        pnlUpcoming.add(UITheme.createStyledScrollPane(tblUpcoming), BorderLayout.CENTER);

        // Right: Recent Registrations
        JPanel pnlReg = UITheme.createCardPanel();
        pnlReg.setLayout(new BorderLayout(0, 8));
        JLabel lblRegTitle = new JLabel("Recent Participant Registrations");
        lblRegTitle.setFont(UITheme.FONT_SUBHEADER);
        lblRegTitle.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        pnlReg.add(lblRegTitle, BorderLayout.NORTH);

        modelRegistrations = new DefaultTableModel(new String[]{"ID", "Participant", "Event", "Date", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblRegistrations = new JTable(modelRegistrations);
        UITheme.styleTable(tblRegistrations);
        pnlReg.add(UITheme.createStyledScrollPane(tblRegistrations), BorderLayout.CENTER);

        tablesPanel.add(pnlUpcoming);
        tablesPanel.add(pnlReg);

        // Assemble center
        JPanel middleWrap = new JPanel(new BorderLayout(0, 8));
        middleWrap.setOpaque(false);
        middleWrap.add(highlightsPanel, BorderLayout.NORTH);
        middleWrap.add(tablesPanel, BorderLayout.CENTER);

        centerPanel.add(middleWrap, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(0, 6));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UITheme.FONT_SMALL);
        lblTitle.setForeground(UITheme.COLOR_TEXT_MUTED);

        valueLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 28));
        valueLabel.setForeground(accentColor);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createBannerCard(String title, JLabel valueLabel) {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(0, 4));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UITheme.FONT_SMALL);
        lblTitle.setForeground(UITheme.COLOR_TEXT_MUTED);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    public void loadDashboardData() {
        SwingUtilities.invokeLater(() -> {
            try {
                // 1. Fetch live aggregated statistics
                Map<String, Object> stats = reportService.getDashboardStatistics();
                lblTotalEvents.setText(String.valueOf(stats.getOrDefault("totalEvents", 0)));
                lblUpcomingEvents.setText(String.valueOf(stats.getOrDefault("upcomingEvents", 0)));
                lblCompletedEvents.setText(String.valueOf(stats.getOrDefault("completedEvents", 0)));
                lblTotalParticipants.setText(String.valueOf(stats.getOrDefault("totalParticipants", 0)));
                lblTotalRegistrations.setText(String.valueOf(stats.getOrDefault("totalRegistrations", 0)));
                lblAvailableVenues.setText(String.valueOf(stats.getOrDefault("availableVenues", 0)));
                lblTopEvent.setText(String.valueOf(stats.getOrDefault("topEvent", "None")));
                lblPopularType.setText(String.valueOf(stats.getOrDefault("popularType", "N/A")));

                // 2. Fetch upcoming events
                modelUpcoming.setRowCount(0);
                List<Event> upcomingEvents = eventService.searchEvents(null, "UPCOMING", null, null);
                for (Event e : upcomingEvents) {
                    modelUpcoming.addRow(new Object[]{
                        e.getEventId(),
                        e.getEventName(),
                        e.getEventDate() != null ? e.getEventDate().toString() : "",
                        e.getVenueName(),
                        e.getCurrentRegistrations() + " / " + e.getMaxCapacity(),
                        e.getStatus()
                    });
                }

                // 3. Fetch recent registrations
                modelRegistrations.setRowCount(0);
                List<Registration> registrations = registrationService.getAllRegistrations();
                int count = 0;
                for (Registration r : registrations) {
                    if (count++ >= 8) break; // preview top 8
                    modelRegistrations.addRow(new Object[]{
                        r.getRegistrationId(),
                        r.getParticipantName(),
                        r.getEventName(),
                        r.getRegistrationDate() != null ? r.getRegistrationDate().toString() : "",
                        r.getStatus()
                    });
                }

            } catch (SQLException ex) {
                DialogUtil.handleSQLException(this, ex, "Dashboard Metrics Error");
            }
        });
    }
}
