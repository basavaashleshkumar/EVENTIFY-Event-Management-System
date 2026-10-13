package ui;

import model.User;
import service.AuthenticationService;
import util.DialogUtil;
import util.UITheme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.sql.SQLException;

/**
 * Modern Login Screen for EVENTIFY Event Management System.
 * Authenticates users directly against the Oracle USERS table using JDBC.
 */
public class LoginFrame extends JFrame {

    private final AuthenticationService authService;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JLabel lblStatus;

    public LoginFrame() {
        this.authService = new AuthenticationService();
        initComponents();
    }

    private void initComponents() {
        setTitle("EVENTIFY - Secure Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 560);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UITheme.COLOR_BG);

        // Header Panel (Branding)
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        headerPanel.setBackground(UITheme.COLOR_PRIMARY);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(28, 24, 28, 24));

        JLabel lblTitle = new JLabel("EVENTIFY", SwingConstants.CENTER);
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSubtitle = new JLabel("College Event Management System", SwingConstants.CENTER);
        lblSubtitle.setFont(UITheme.FONT_SUBHEADER);
        lblSubtitle.setForeground(new Color(224, 231, 255));

        headerPanel.add(lblTitle);
        headerPanel.add(lblSubtitle);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Form Card Panel
        JPanel formContainer = new JPanel(new GridBagLayout());
        formContainer.setBackground(UITheme.COLOR_BG);
        formContainer.setBorder(BorderFactory.createEmptyBorder(20, 32, 20, 32));

        JPanel card = UITheme.createCardPanel();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 12, 8, 12);
        gbc.gridx = 0;

        // Form Heading
        gbc.gridy = 0;
        JLabel lblLoginHeader = new JLabel("Sign In to Account");
        lblLoginHeader.setFont(UITheme.FONT_HEADER);
        lblLoginHeader.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        card.add(lblLoginHeader, gbc);

        // Username Label
        gbc.gridy = 1;
        gbc.insets = new Insets(12, 12, 2, 12);
        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(UITheme.FONT_BODY_BOLD);
        lblUser.setForeground(UITheme.COLOR_TEXT_MUTED);
        card.add(lblUser, gbc);

        // Username Input
        gbc.gridy = 2;
        gbc.insets = new Insets(2, 12, 10, 12);
        txtUsername = UITheme.createTextField(20);
        txtUsername.setText("admin"); // prefilled for easy demonstration
        card.add(txtUsername, gbc);

        // Password Label
        gbc.gridy = 3;
        gbc.insets = new Insets(6, 12, 2, 12);
        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(UITheme.FONT_BODY_BOLD);
        lblPass.setForeground(UITheme.COLOR_TEXT_MUTED);
        card.add(lblPass, gbc);

        // Password Input
        gbc.gridy = 4;
        gbc.insets = new Insets(2, 12, 14, 12);
        txtPassword = new JPasswordField(20);
        txtPassword.setFont(UITheme.FONT_BODY);
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.COLOR_BORDER, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        txtPassword.setPreferredSize(new Dimension(txtPassword.getPreferredSize().width, 34));
        txtPassword.setText("admin123"); // prefilled for easy demonstration
        card.add(txtPassword, gbc);

        // Status / Message Label
        gbc.gridy = 5;
        gbc.insets = new Insets(2, 12, 10, 12);
        lblStatus = new JLabel(" ", SwingConstants.CENTER);
        lblStatus.setFont(UITheme.FONT_SMALL);
        lblStatus.setForeground(UITheme.COLOR_DANGER);
        card.add(lblStatus, gbc);

        // Buttons Panel
        gbc.gridy = 6;
        gbc.insets = new Insets(4, 12, 12, 12);
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setOpaque(false);

        JButton btnLogin = UITheme.createPrimaryButton("Login");
        JButton btnExit = UITheme.createSecondaryButton("Exit");
        buttonPanel.add(btnLogin);
        buttonPanel.add(btnExit);
        card.add(buttonPanel, gbc);

        // Demo Credentials Hint
        gbc.gridy = 7;
        gbc.insets = new Insets(8, 12, 4, 12);
        JLabel lblDemo = new JLabel("<html><center><small style='color:#64748B;'>" +
            "Demo Accounts:<br>" +
            "<b>Admin:</b> admin / admin123 &nbsp;|&nbsp; <b>Organizer:</b> organizer / organizer123" +
            "</small></center></html>", SwingConstants.CENTER);
        card.add(lblDemo, gbc);

        formContainer.add(card);
        mainPanel.add(formContainer, BorderLayout.CENTER);

        setContentPane(mainPanel);

        // Action Listeners
        btnLogin.addActionListener(e -> performLogin());
        btnExit.addActionListener(e -> System.exit(0));
        txtPassword.addActionListener(e -> performLogin());
        txtUsername.addActionListener(e -> txtPassword.requestFocusInWindow());
    }

    private void performLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            lblStatus.setText("Username and password are required.");
            lblStatus.setForeground(UITheme.COLOR_DANGER);
            return;
        }

        lblStatus.setText("Connecting to Oracle Database...");
        lblStatus.setForeground(UITheme.COLOR_PRIMARY);

        SwingUtilities.invokeLater(() -> {
            try {
                User user = authService.login(username, password);
                if (user != null) {
                    lblStatus.setText("Login successful!");
                    lblStatus.setForeground(UITheme.COLOR_SUCCESS);

                    // Launch Dashboard
                    DashboardFrame dashboard = new DashboardFrame(user);
                    dashboard.setVisible(true);
                    dispose(); // Close login window
                } else {
                    lblStatus.setText("Invalid username or password.");
                    lblStatus.setForeground(UITheme.COLOR_DANGER);
                }
            } catch (SQLException ex) {
                lblStatus.setText("Database error. Check Oracle connection.");
                DialogUtil.handleSQLException(this, ex, "Authentication Error");
            } catch (Exception ex) {
                lblStatus.setText("Login failed: " + ex.getMessage());
                DialogUtil.showError(this, ex.getMessage(), "Login Error");
            }
        });
    }
}
