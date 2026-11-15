package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Komponen Antarmuka Grafis Kustom (UIComponents)
 * Dirancang untuk mereplikasi estetika desain Figma pada Rentara Management System.
 */
public class UIComponents {

    /**
     * Panel Kartu Membulat (ModernCard)
     */
    public static class ModernCard extends JPanel {
        private final int radius;
        private Color bgColor;
        private Color borderColor;

        public ModernCard() {
            this(Theme.BG_CARD, Theme.BORDER, 18);
        }

        public ModernCard(Color bgColor, Color borderColor, int radius) {
            this.bgColor = bgColor;
            this.borderColor = borderColor;
            this.radius = radius;
            setOpaque(false);
        }

        public void setBgColor(Color bgColor) {
            this.bgColor = bgColor;
            repaint();
        }

        public void setBorderColor(Color borderColor) {
            this.borderColor = borderColor;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Background Fill
            g2.setColor(bgColor);
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, radius, radius));

            // Border
            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(0.6f, 0.6f, w - 1.2f, h - 1.2f, radius, radius));
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Tombol Modern Membulat (ModernButton)
     */
    public static class ModernButton extends JButton {
        public enum Type { PRIMARY, SECONDARY, DANGER, GHOST }

        private final Type type;
        private boolean isHovered = false;
        private boolean isPressed = false;
        private int radius = 12;

        public ModernButton(String text, Type type) {
            super(text);
            this.type = type;
            setFont(Theme.bodyBold());
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(10, 18, 10, 18));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    isPressed = true;
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    isPressed = false;
                    repaint();
                }
            });
        }

        public void setRadius(int r) {
            this.radius = r;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            Color fill;
            Color textCol;
            Color border = null;

            switch (type) {
                case PRIMARY:
                    fill = isPressed ? Theme.ACCENT_ORANGE_DARK : (isHovered ? new Color(0xFF, 0xB7, 0x88) : Theme.ACCENT_ORANGE);
                    textCol = new Color(0x18, 0x19, 0x1D); // Teks gelap kontras pada oranye
                    break;
                case SECONDARY:
                    fill = isPressed ? new Color(0x2F, 0x31, 0x3A) : (isHovered ? new Color(0x2A, 0x2C, 0x35) : Theme.BG_CARD_LIGHT);
                    border = Theme.BORDER_LIGHT;
                    textCol = Theme.TEXT_WHITE;
                    break;
                case DANGER:
                    fill = isPressed ? new Color(0x4A, 0x1A, 0x22) : (isHovered ? new Color(0x38, 0x1A, 0x20) : new Color(0x28, 0x15, 0x1A));
                    border = Theme.BADGE_RED_TXT;
                    textCol = Theme.BADGE_RED_TXT;
                    break;
                case GHOST:
                default:
                    fill = isHovered ? new Color(0xFF, 0xFF, 0xFF, 20) : new Color(0, 0, 0, 0);
                    textCol = isHovered ? Theme.ACCENT_ORANGE : Theme.TEXT_MUTED;
                    break;
            }

            g2.setColor(fill);
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, radius, radius));

            if (border != null) {
                g2.setColor(border);
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(0.6f, 0.6f, w - 1.2f, h - 1.2f, radius, radius));
            }

            g2.dispose();

            setForeground(textCol);
            super.paintComponent(g);
        }
    }

    /**
     * Input Teks Modern (ModernTextField) dengan Placeholder, Focus Border, & Error Border
     */
    public static class ModernTextField extends JTextField {
        private String placeholder = "";
        private boolean isFocused = false;
        private boolean hasError = false;
        private int radius = 10;

        public ModernTextField() {
            this("");
        }

        public ModernTextField(String placeholder) {
            this.placeholder = placeholder;
            setFont(Theme.bodyRegular());
            setForeground(Theme.TEXT_WHITE);
            setCaretColor(Theme.ACCENT_ORANGE);
            setBackground(Theme.BG_INPUT);
            setOpaque(false);
            setBorder(new EmptyBorder(9, 14, 9, 14));

            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    isFocused = true;
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    isFocused = false;
                    repaint();
                }
            });
        }

        public void setPlaceholder(String placeholder) {
            this.placeholder = placeholder;
            repaint();
        }

        public void setError(boolean error) {
            this.hasError = error;
            repaint();
        }

        public boolean isHasError() {
            return hasError;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(Theme.BG_INPUT);
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, radius, radius));

            // Border: Merah jika error, Oranye jika fokus, Abu-abu jika default
            if (hasError) {
                g2.setColor(Theme.BADGE_RED_TXT);
                g2.setStroke(new BasicStroke(1.5f));
            } else if (isFocused) {
                g2.setColor(Theme.ACCENT_ORANGE);
                g2.setStroke(new BasicStroke(1.5f));
            } else {
                g2.setColor(Theme.BORDER);
                g2.setStroke(new BasicStroke(1.0f));
            }
            g2.draw(new RoundRectangle2D.Float(0.6f, 0.6f, w - 1.2f, h - 1.2f, radius, radius));

            super.paintComponent(g2);

            // Placeholder
            if (getText().isEmpty() && !placeholder.isEmpty()) {
                g2.setColor(Theme.TEXT_DIM);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int y = (h - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(placeholder, 14, y);
            }

            g2.dispose();
        }
    }

    /**
     * Input Kata Sandi Modern (ModernPasswordField)
     */
    public static class ModernPasswordField extends JPasswordField {
        private String placeholder = "";
        private boolean isFocused = false;
        private boolean showPassword = false;

        public ModernPasswordField(String placeholder) {
            this.placeholder = placeholder;
            setFont(Theme.bodyRegular());
            setForeground(Theme.TEXT_WHITE);
            setCaretColor(Theme.ACCENT_ORANGE);
            setBackground(Theme.BG_INPUT);
            setOpaque(false);
            setBorder(new EmptyBorder(9, 14, 9, 40));

            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    isFocused = true;
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    isFocused = false;
                    repaint();
                }
            });

            // Klik ikon mata di sisi kanan untuk melihat password
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getX() >= getWidth() - 36) {
                        showPassword = !showPassword;
                        setEchoChar(showPassword ? (char) 0 : '•');
                        repaint();
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(Theme.BG_INPUT);
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 10, 10));

            if (isFocused) {
                g2.setColor(Theme.ACCENT_ORANGE);
                g2.setStroke(new BasicStroke(1.5f));
            } else {
                g2.setColor(Theme.BORDER);
                g2.setStroke(new BasicStroke(1.0f));
            }
            g2.draw(new RoundRectangle2D.Float(0.6f, 0.6f, w - 1.2f, h - 1.2f, 10, 10));

            super.paintComponent(g2);

            // Placeholder
            if (getPassword().length == 0 && !placeholder.isEmpty()) {
                g2.setColor(Theme.TEXT_DIM);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int y = (h - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(placeholder, 14, y);
            }

            // Eye icon toggle di kanan
            g2.setColor(showPassword ? Theme.ACCENT_ORANGE : Theme.TEXT_DIM);
            int eyeX = w - 28;
            int eyeY = h / 2;
            g2.drawOval(eyeX - 7, eyeY - 4, 14, 8);
            g2.fillOval(eyeX - 3, eyeY - 2, 6, 4);

            g2.dispose();
        }
    }

    /**
     * Badge Status Membulat (BadgePill)
     */
    public static class BadgePill extends JPanel {
        private String text;
        private Color bgColor;
        private Color textColor;

        public BadgePill(String text, Color bgColor, Color textColor) {
            this.text = text;
            this.bgColor = bgColor;
            this.textColor = textColor;
            setOpaque(false);
            setBorder(new EmptyBorder(4, 10, 4, 10));
        }

        public void setStatus(String text, Color bgColor, Color textColor) {
            this.text = text;
            this.bgColor = bgColor;
            this.textColor = textColor;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(bgColor);
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, h, h));

            // Dot
            g2.setColor(textColor);
            int dotSize = 6;
            int dotY = (h - dotSize) / 2;
            g2.fillOval(10, dotY, dotSize, dotSize);

            // Text
            g2.setFont(Theme.captionBold());
            FontMetrics fm = g2.getFontMetrics();
            int textY = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(text, 22, textY);

            g2.dispose();
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(Theme.captionBold());
            int w = 22 + fm.stringWidth(text) + 12;
            return new Dimension(w, 24);
        }
    }

    /**
     * Kartu Metrik Ringkasan (StatCard)
     */
    public static class StatCard extends ModernCard {
        private final JLabel lblTitle;
        private final JLabel lblValue;
        private final JLabel lblSubtext;

        public StatCard(String title, String value, String subtext, Color valueColor) {
            super(Theme.BG_CARD, Theme.BORDER, 16);
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(16, 20, 16, 20));

            lblTitle = new JLabel(title);
            lblTitle.setFont(Theme.captionBold());
            lblTitle.setForeground(Theme.TEXT_DIM);

            lblValue = new JLabel(value);
            lblValue.setFont(new Font(Theme.FONT_NAME, Font.BOLD, 28));
            lblValue.setForeground(valueColor != null ? valueColor : Theme.TEXT_WHITE);
            lblValue.setBorder(new EmptyBorder(4, 0, 4, 0));

            lblSubtext = new JLabel(subtext);
            lblSubtext.setFont(Theme.caption());
            lblSubtext.setForeground(Theme.TEXT_MUTED);

            JPanel center = new JPanel();
            center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
            center.setOpaque(false);
            center.add(lblTitle);
            center.add(lblValue);
            center.add(lblSubtext);

            add(center, BorderLayout.CENTER);
        }

        public void updateData(String value, String subtext) {
            lblValue.setText(value);
            lblSubtext.setText(subtext);
        }
    }

    /**
     * Tombol Filter Pill (PillFilterButton)
     */
    public static class PillFilterButton extends JButton {
        private boolean isSelected = false;

        public PillFilterButton(String text, boolean selected) {
            super(text);
            this.isSelected = selected;
            setFont(Theme.bodyBold());
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(8, 16, 8, 16));
        }

        public void setSelectedState(boolean selected) {
            this.isSelected = selected;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            if (isSelected) {
                g2.setColor(new Color(0x35, 0x36, 0x42));
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 12, 12));
                g2.setColor(Theme.ACCENT_ORANGE);
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(0.6f, 0.6f, w - 1.2f, h - 1.2f, 12, 12));
                setForeground(Theme.TEXT_WHITE);
            } else {
                g2.setColor(Theme.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 12, 12));
                g2.setColor(Theme.BORDER);
                g2.setStroke(new BasicStroke(1.0f));
                g2.draw(new RoundRectangle2D.Float(0.6f, 0.6f, w - 1.2f, h - 1.2f, 12, 12));
                setForeground(Theme.TEXT_MUTED);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Tabel Modern Gelap Terkonfigurasi
     */
    public static void styleTable(JTable table) {
        table.setBackground(Theme.BG_CARD);
        table.setForeground(Theme.TEXT_WHITE);
        table.setFont(Theme.bodyRegular());
        table.setRowHeight(46);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(new Color(0x2B, 0x2C, 0x38));
        table.setSelectionForeground(Theme.TEXT_WHITE);

        JTableHeader header = table.getTableHeader();
        header.setBackground(Theme.BG_CARD_LIGHT);
        header.setForeground(Theme.TEXT_DIM);
        header.setFont(Theme.captionBold());
        header.setPreferredSize(new Dimension(header.getWidth(), 38));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER));

        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);
    }
}
