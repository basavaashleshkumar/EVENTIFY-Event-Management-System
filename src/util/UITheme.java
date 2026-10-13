package util;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

/**
 * Modern UI Design System for EVENTIFY Swing Application.
 * Provides a clean, modern color palette, custom styled buttons, tables, and cards.
 */
public class UITheme {

    // Color Palette
    public static final Color COLOR_PRIMARY = new Color(30, 58, 138);       // #1E3A8A Dark Navy
    public static final Color COLOR_PRIMARY_HOVER = new Color(37, 99, 235); // #2563EB Royal Blue
    public static final Color COLOR_SIDEBAR = new Color(15, 23, 42);        // #0F172A Dark Slate
    public static final Color COLOR_SIDEBAR_ACTIVE = new Color(30, 41, 59); // #1E293B
    public static final Color COLOR_BG = new Color(248, 250, 252);          // #F8FAFC Slate 50
    public static final Color COLOR_CARD_BG = Color.WHITE;
    public static final Color COLOR_BORDER = new Color(226, 232, 240);      // #E2E8F0
    public static final Color COLOR_TEXT_PRIMARY = new Color(15, 23, 42);
    public static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);
    public static final Color COLOR_SUCCESS = new Color(16, 185, 129);      // #10B981 Emerald
    public static final Color COLOR_DANGER = new Color(239, 68, 68);        // #EF4444 Rose Red
    public static final Color COLOR_WARNING = new Color(245, 158, 11);      // #F59E0B Amber
    public static final Color COLOR_ACCENT = new Color(14, 165, 233);       // #0EA5E9 Sky Blue

    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_SUBHEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 13);

    public static JButton createPrimaryButton(String text) {
        return createButton(text, COLOR_PRIMARY, Color.WHITE);
    }

    public static JButton createSuccessButton(String text) {
        return createButton(text, COLOR_SUCCESS, Color.WHITE);
    }

    public static JButton createDangerButton(String text) {
        return createButton(text, COLOR_DANGER, Color.WHITE);
    }

    public static JButton createSecondaryButton(String text) {
        return createButton(text, new Color(100, 116, 139), Color.WHITE);
    }

    public static JButton createAccentButton(String text) {
        return createButton(text, COLOR_ACCENT, Color.WHITE);
    }

    public static JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(bg.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bg.brighter());
                } else {
                    g2.setColor(bg);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BUTTON);
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(8, 16, 8, 16));
        return btn;
    }

    public static JTextField createTextField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        tf.setPreferredSize(new Dimension(tf.getPreferredSize().width, 34));
        return tf;
    }

    public static <T> JComboBox<T> createComboBox(T[] items) {
        JComboBox<T> cb = new JComboBox<>(items);
        cb.setFont(FONT_BODY);
        cb.setBackground(Color.WHITE);
        cb.setPreferredSize(new Dimension(cb.getPreferredSize().width, 34));
        return cb;
    }

    public static JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER, 1),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        return card;
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(32);
        table.setGridColor(new Color(241, 245, 249));
        table.setShowGrid(true);
        table.setSelectionBackground(new Color(219, 234, 254)); // #DBEAFE
        table.setSelectionForeground(new Color(30, 58, 138));
        table.getTableHeader().setReorderingAllowed(false);

        // Header Styling
        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BODY_BOLD);
        header.setBackground(COLOR_SIDEBAR);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 36));

        // Alternating row background
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    c.setForeground(COLOR_TEXT_PRIMARY);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return c;
            }
        });
    }

    public static JScrollPane createStyledScrollPane(JComponent comp) {
        JScrollPane sp = new JScrollPane(comp);
        sp.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }
}
