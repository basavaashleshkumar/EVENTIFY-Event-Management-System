package ui;

import model.Event;
import util.UITheme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

/**
 * High-value Event Details Modal Dialog.
 * Displays calculated capacity, spots remaining, and full event breakdown.
 */
public class EventDetailDialog extends JDialog {

    public EventDetailDialog(JFrame parent, Event event) {
        super(parent, "Event Details: " + (event != null ? event.getEventName() : ""), true);
        initComponents(event);
    }

    private void initComponents(Event event) {
        setSize(560, 580);
        setLocationRelativeTo(getParent());
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UITheme.COLOR_BG);

        // Header
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        headerPanel.setBackground(UITheme.COLOR_PRIMARY);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JLabel lblTitle = new JLabel(event != null ? event.getEventName() : "Event Details");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(Color.WHITE);

        JLabel lblType = new JLabel((event != null ? event.getEventType() : "") + " | Status: " + (event != null ? event.getStatus() : ""));
        lblType.setFont(UITheme.FONT_SUBHEADER);
        lblType.setForeground(new Color(224, 231, 255));

        headerPanel.add(lblTitle);
        headerPanel.add(lblType);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Details Panel
        JPanel content = UITheme.createCardPanel();
        content.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 12, 6, 12);
        gbc.gridx = 0;

        if (event != null) {
            int row = 0;
            row = addDetailRow(content, gbc, row, "Event ID:", String.valueOf(event.getEventId()));
            row = addDetailRow(content, gbc, row, "Organizer:", event.getOrganizerName() != null ? event.getOrganizerName() : "ID #" + event.getOrganizerId());
            row = addDetailRow(content, gbc, row, "Venue:", event.getVenueName() != null ? event.getVenueName() : "ID #" + event.getVenueId());
            row = addDetailRow(content, gbc, row, "Event Date:", event.getEventDate() != null ? event.getEventDate().toString() : "-");
            row = addDetailRow(content, gbc, row, "Timing:", event.getStartTime() + " to " + event.getEndTime());
            row = addDetailRow(content, gbc, row, "Maximum Capacity:", String.valueOf(event.getMaxCapacity()));
            row = addDetailRow(content, gbc, row, "Current Registrations:", String.valueOf(event.getCurrentRegistrations()));
            
            // Seats Remaining with color indicator
            int seatsLeft = event.getSeatsRemaining();
            String seatStatus = seatsLeft + " seats available" + (event.isFull() ? " (HOUSEFULL)" : "");
            row = addDetailRow(content, gbc, row, "Seats Remaining:", seatStatus, event.isFull() ? UITheme.COLOR_DANGER : UITheme.COLOR_SUCCESS);

            row = addDetailRow(content, gbc, row, "Registration Fee:", "₹ " + String.format("%.2f", event.getRegistrationFee()));
            row = addDetailRow(content, gbc, row, "Description:", "<html><body style='width: 250px;'>" + (event.getDescription() != null ? event.getDescription() : "None") + "</body></html>");
        }

        mainPanel.add(content, BorderLayout.CENTER);

        // Footer with Close Button
        JPanel footer = new JPanel();
        footer.setBackground(UITheme.COLOR_BG);
        footer.setBorder(BorderFactory.createEmptyBorder(12, 12, 16, 12));
        JButton btnClose = UITheme.createPrimaryButton("Close");
        btnClose.addActionListener(e -> dispose());
        footer.add(btnClose);
        mainPanel.add(footer, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private int addDetailRow(JPanel panel, GridBagConstraints gbc, int row, String label, String value) {
        return addDetailRow(panel, gbc, row, label, value, UITheme.COLOR_TEXT_PRIMARY);
    }

    private int addDetailRow(JPanel panel, GridBagConstraints gbc, int row, String label, String value, Color valueColor) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0.35;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BODY_BOLD);
        lbl.setForeground(UITheme.COLOR_TEXT_MUTED);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        JLabel val = new JLabel(value);
        val.setFont(UITheme.FONT_BODY);
        val.setForeground(valueColor);
        panel.add(val, gbc);

        return row + 1;
    }
}
