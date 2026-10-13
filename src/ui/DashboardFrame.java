package ui;

import model.User;
import service.AuthenticationService;
import util.DialogUtil;
import util.UITheme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

/**
 * Main Application Window for EVENTIFY.
 * Houses the modern sidebar navigation, top header, and CardLayout content manager.
 */
public class DashboardFrame extends JFrame {

    private final User loggedInUser;
    private final CardLayout cardLayout;
    private final JPanel contentArea;
    private final List<JButton> navButtons = new ArrayList<>();

    // Child Views
    private DashboardPanel dashboardPanel;
    private EventManagementPanel eventPanel;
    private ParticipantPanel participantPanel;
    private OrganizerPanel organizerPanel;
    private VenuePanel venuePanel;
    private RegistrationPanel registrationPanel;
    private ReportsPanel reportsPanel;

    public DashboardFrame(User user) {
        this.loggedInUser = user;
        this.cardLayout = new CardLayout();
        this.contentArea = new JPanel(cardLayout);

        initComponents();
    }

    private void initComponents() {
        setTitle("EVENTIFY - Event Management System [Logged in as: " + loggedInUser.getUsername() + "]");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 820);
        setMinimumSize(new Dimension(1024, 700));
        setLocationRelativeTo(null);

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(UITheme.COLOR_BG);

        // 1. Top Application Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UITheme.COLOR_PRIMARY);
        topBar.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));

        // Brand
        JPanel brandBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        brandBox.setOpaque(false);
        JLabel lblLogo = new JLabel("EVENTIFY");
        lblLogo.setFont(UITheme.FONT_TITLE);
        lblLogo.setForeground(Color.WHITE);

        JLabel lblTagline = new JLabel("|  College Event Management System");
        lblTagline.setFont(UITheme.FONT_SUBHEADER);
        lblTagline.setForeground(new Color(224, 231, 255));

        brandBox.add(lblLogo);
        brandBox.add(lblTagline);
        topBar.add(brandBox, BorderLayout.WEST);

        // User Info & Logout
        JPanel userBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        userBox.setOpaque(false);

        String roleBadge = loggedInUser.getRole().toUpperCase();
        JLabel lblUser = new JLabel("<html><span style='color:white;'>Welcome, <b>" +
            (loggedInUser.getFullName() != null ? loggedInUser.getFullName() : loggedInUser.getUsername()) +
            "</b></span> <span style='background:#2563EB;color:white;padding:2px 8px;border-radius:4px;'>&nbsp;" +
            roleBadge + "&nbsp;</span></html>");
        lblUser.setFont(UITheme.FONT_BODY);

        JButton btnLogout = UITheme.createButton("Logout", new Color(220, 38, 38), Color.WHITE);
        btnLogout.addActionListener(e -> handleLogout());

        userBox.add(lblUser);
        userBox.add(btnLogout);
        topBar.add(userBox, BorderLayout.EAST);

        rootPanel.add(topBar, BorderLayout.NORTH);

        // 2. Left Sidebar Navigation
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UITheme.COLOR_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(220, getHeight()));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 12, 20, 12));

        JLabel lblNavHeading = new JLabel("MAIN NAVIGATION");
        lblNavHeading.setFont(UITheme.FONT_SMALL);
        lblNavHeading.setForeground(new Color(148, 163, 184));
        lblNavHeading.setBorder(BorderFactory.createEmptyBorder(0, 12, 10, 0));
        sidebar.add(lblNavHeading);

        // Navigation Items
        JButton btnNavDashboard = createSidebarButton("Dashboard", "PANEL_DASHBOARD");
        JButton btnNavEvents = createSidebarButton("Events", "PANEL_EVENTS");
        JButton btnNavParticipants = createSidebarButton("Participants", "PANEL_PARTICIPANTS");
        JButton btnNavOrganizers = createSidebarButton("Organizers", "PANEL_ORGANIZERS");
        JButton btnNavVenues = createSidebarButton("Venues", "PANEL_VENUES");
        JButton btnNavRegistrations = createSidebarButton("Registrations", "PANEL_REGISTRATIONS");
        JButton btnNavReports = createSidebarButton("Reports", "PANEL_REPORTS");

        // If Organizer role, organizers management is view-only or restricted
        if (!loggedInUser.isAdmin()) {
            btnNavOrganizers.setToolTipText("Organizers management is restricted to Administrators.");
        }

        sidebar.add(btnNavDashboard);
        sidebar.add(Box.createRigidArea(new Dimension(0, 4)));
        sidebar.add(btnNavEvents);
        sidebar.add(Box.createRigidArea(new Dimension(0, 4)));
        sidebar.add(btnNavParticipants);
        sidebar.add(Box.createRigidArea(new Dimension(0, 4)));
        sidebar.add(btnNavOrganizers);
        sidebar.add(Box.createRigidArea(new Dimension(0, 4)));
        sidebar.add(btnNavVenues);
        sidebar.add(Box.createRigidArea(new Dimension(0, 4)));
        sidebar.add(btnNavRegistrations);
        sidebar.add(Box.createRigidArea(new Dimension(0, 4)));
        sidebar.add(btnNavReports);

        sidebar.add(Box.createVerticalGlue());

        JButton btnNavExit = createSidebarButton("Exit Application", "EXIT");
        btnNavExit.setBackground(new Color(15, 23, 42));
        btnNavExit.setForeground(new Color(248, 113, 113));
        sidebar.add(btnNavExit);

        rootPanel.add(sidebar, BorderLayout.WEST);

        // 3. Central Content Area (CardLayout)
        dashboardPanel = new DashboardPanel();
        eventPanel = new EventManagementPanel(this);
        participantPanel = new ParticipantPanel(this);
        organizerPanel = new OrganizerPanel(this);
        venuePanel = new VenuePanel(this);
        registrationPanel = new RegistrationPanel(this);
        reportsPanel = new ReportsPanel(this);

        contentArea.add(dashboardPanel, "PANEL_DASHBOARD");
        contentArea.add(eventPanel, "PANEL_EVENTS");
        contentArea.add(participantPanel, "PANEL_PARTICIPANTS");
        contentArea.add(organizerPanel, "PANEL_ORGANIZERS");
        contentArea.add(venuePanel, "PANEL_VENUES");
        contentArea.add(registrationPanel, "PANEL_REGISTRATIONS");
        contentArea.add(reportsPanel, "PANEL_REPORTS");

        rootPanel.add(contentArea, BorderLayout.CENTER);

        setContentPane(rootPanel);

        // Highlight first item
        setActiveNavButton(btnNavDashboard);
    }

    private JButton createSidebarButton(String text, String cardName) {
        JButton btn = new JButton(text);
        btn.setFont(UITheme.FONT_BUTTON);
        btn.setForeground(new Color(226, 232, 240));
        btn.setBackground(UITheme.COLOR_SIDEBAR);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setPreferredSize(new Dimension(200, 40));
        btn.setMargin(new Insets(6, 14, 6, 14));

        btn.addActionListener(e -> {
            if ("EXIT".equals(cardName)) {
                if (DialogUtil.showConfirm(this, "Are you sure you want to exit EVENTIFY?", "Exit")) {
                    System.exit(0);
                }
                return;
            }

            cardLayout.show(contentArea, cardName);
            setActiveNavButton(btn);

            // Trigger data refresh on tab activation
            switch (cardName) {
                case "PANEL_DASHBOARD":
                    dashboardPanel.loadDashboardData();
                    break;
                case "PANEL_EVENTS":
                    eventPanel.loadEvents();
                    break;
                case "PANEL_PARTICIPANTS":
                    participantPanel.loadParticipants();
                    break;
                case "PANEL_ORGANIZERS":
                    organizerPanel.loadOrganizers();
                    break;
                case "PANEL_VENUES":
                    venuePanel.loadVenues();
                    break;
                case "PANEL_REGISTRATIONS":
                    registrationPanel.loadRegistrations();
                    break;
            }
        });

        navButtons.add(btn);
        return btn;
    }

    private void setActiveNavButton(JButton activeBtn) {
        for (JButton btn : navButtons) {
            if (btn == activeBtn) {
                btn.setBackground(UITheme.COLOR_SIDEBAR_ACTIVE);
                btn.setForeground(Color.WHITE);
                btn.setBorder(BorderFactory.createMatteBorder(0, 4, 0, 0, UITheme.COLOR_ACCENT));
            } else {
                btn.setBackground(UITheme.COLOR_SIDEBAR);
                btn.setForeground(new Color(203, 213, 225));
                btn.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
            }
        }
    }

    private void handleLogout() {
        boolean confirm = DialogUtil.showConfirm(this, "Are you sure you want to log out?", "Confirm Logout");
        if (confirm) {
            AuthenticationService.logout();
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
            dispose();
        }
    }
}
