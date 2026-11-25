package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Komponen Antarmuka Grafis Kustom (UIComponents)
 * Dirancang untuk mereplikasi estetika desain Figma pada Rentara Management System secara 1:1.
 */
@SuppressWarnings("serial")
public class UIComponents {

    /**
     * Panel Latar Belakang Ambient dengan Efek Cahaya Radial (Radial Glow) seperti di Figma
     */
    public static class AmbientBackgroundPanel extends JPanel {
        public AmbientBackgroundPanel(LayoutManager layout) {
            super(layout);
            setBackground(Theme.BG_DARK);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Base dark fill
            g2.setColor(Theme.BG_DARK);
            g2.fillRect(0, 0, w, h);

            // 1. Purple Ambient Glow (Left-Center: #8D72A7)
            if (w > 100 && h > 100) {
                RadialGradientPaint glowPurple = new RadialGradientPaint(
                        new Point((int) (w * 0.28), (int) (h * 0.45)),
                        Math.max(380, w * 0.35f),
                        new float[]{0f, 1f},
                        new Color[]{new Color(0x8D, 0x72, 0xA7, 48), new Color(0x8D, 0x72, 0xA7, 0)}
                );
                g2.setPaint(glowPurple);
                g2.fillRect(0, 0, w, h);

                // 2. Warm Orange Ambient Glow (Center-Right: #E18557)
                RadialGradientPaint glowOrange = new RadialGradientPaint(
                        new Point((int) (w * 0.62), (int) (h * 0.38)),
                        Math.max(340, w * 0.30f),
                        new float[]{0f, 1f},
                        new Color[]{new Color(0xE1, 0x85, 0x57, 42), new Color(0xE1, 0x85, 0x57, 0)}
                );
                g2.setPaint(glowOrange);
                g2.fillRect(0, 0, w, h);

                // 3. Sage Green Ambient Glow (Bottom-Right: #90A87B)
                RadialGradientPaint glowGreen = new RadialGradientPaint(
                        new Point((int) (w * 0.85), (int) (h * 0.92)),
                        Math.max(280, w * 0.25f),
                        new float[]{0f, 1f},
                        new Color[]{new Color(0x90, 0xA8, 0x7B, 25), new Color(0x90, 0xA8, 0x7B, 0)}
                );
                g2.setPaint(glowGreen);
                g2.fillRect(0, 0, w, h);
            }

            g2.dispose();
        }
    }

    /**
     * Panel Kartu Membulat Glassmorphic (ModernCard)
     */
    public static class ModernCard extends JPanel {
        private final int radius;
        private Color bgColor;
        private Color borderColor;

        public ModernCard() {
            this(new Color(0x23, 0x24, 0x2B, 215), new Color(0xFF, 0xFF, 0xFF, 24), 20);
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

            // Background Fill (Translucent Glassmorphism)
            if (bgColor.equals(Theme.BG_CARD)) {
                g2.setColor(new Color(0x22, 0x23, 0x2A, 210));
            } else if (bgColor.equals(Theme.BG_INPUT)) {
                g2.setColor(new Color(0x2B, 0x2D, 0x35, 210));
            } else {
                g2.setColor(bgColor);
            }
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, radius, radius));

            // Border
            if (borderColor != null) {
                if (borderColor.equals(Theme.BORDER)) {
                    g2.setColor(new Color(0xFF, 0xFF, 0xFF, 26));
                } else {
                    g2.setColor(borderColor);
                }
                g2.setStroke(new BasicStroke(1.1f));
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
            setBorder(new EmptyBorder(10, 20, 10, 20));

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
                    fill = isPressed ? Theme.ACCENT_ORANGE_DARK : (isHovered ? new Color(0xFF, 0xB8, 0x88) : Theme.ACCENT_ORANGE);
                    textCol = new Color(0x18, 0x19, 0x1D);
                    break;
                case SECONDARY:
                    fill = isPressed ? new Color(0x32, 0x34, 0x3E) : (isHovered ? new Color(0x2D, 0x2F, 0x39) : new Color(0x27, 0x29, 0x31));
                    border = new Color(0xFF, 0xFF, 0xFF, 32);
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
                g2.setStroke(new BasicStroke(1.1f));
                g2.draw(new RoundRectangle2D.Float(0.6f, 0.6f, w - 1.2f, h - 1.2f, radius, radius));
            }

            g2.dispose();

            setForeground(textCol);
            super.paintComponent(g);
        }
    }

    /**
     * Input Teks Modern (ModernTextField) dengan Placeholder, Search Icon, Prefix, Focus & Error Border
     */
    public static class ModernTextField extends JTextField {
        private String placeholder = "";
        private String prefixText = "";
        private boolean showSearchIcon = false;
        private boolean showShortcutBadge = false;
        private boolean isFocused = false;
        private boolean hasError = false;
        private int radius = 12;

        public ModernTextField() {
            this("");
        }

        public ModernTextField(String placeholder) {
            this.placeholder = placeholder;
            setFont(Theme.bodyRegular());
            setForeground(Theme.TEXT_WHITE);
            setCaretColor(Theme.ACCENT_ORANGE);
            setBackground(new Color(0x2B, 0x2D, 0x36));
            setOpaque(false);
            updatePadding();

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

        private void updatePadding() {
            int left = 14;
            if (showSearchIcon) left = 36;
            else if (!prefixText.isEmpty()) left = 36;
            int right = showShortcutBadge ? 48 : 14;
            setBorder(new EmptyBorder(9, left, 9, right));
        }

        public void setSearchStyle(boolean searchIcon, boolean shortcutBadge) {
            this.showSearchIcon = searchIcon;
            this.showShortcutBadge = shortcutBadge;
            updatePadding();
            repaint();
        }

        public void setPrefixText(String prefix) {
            this.prefixText = prefix != null ? prefix : "";
            updatePadding();
            repaint();
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

            g2.setColor(hasError ? new Color(0x32, 0x22, 0x27, 230) : new Color(0x2B, 0x2D, 0x36, 220));
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, radius, radius));

            if (hasError) {
                g2.setColor(Theme.BADGE_RED_TXT);
                g2.setStroke(new BasicStroke(1.4f));
            } else if (isFocused) {
                g2.setColor(Theme.ACCENT_ORANGE);
                g2.setStroke(new BasicStroke(1.4f));
            } else {
                g2.setColor(new Color(0xFF, 0xFF, 0xFF, 28));
                g2.setStroke(new BasicStroke(1.0f));
            }
            g2.draw(new RoundRectangle2D.Float(0.6f, 0.6f, w - 1.2f, h - 1.2f, radius, radius));

            // Search Icon
            if (showSearchIcon) {
                g2.setColor(Theme.TEXT_DIM);
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cy = h / 2;
                g2.drawOval(13, cy - 6, 10, 10);
                g2.drawLine(22, cy + 3, 25, cy + 6);
            } else if (!prefixText.isEmpty()) {
                g2.setColor(Theme.TEXT_DIM);
                g2.setFont(Theme.bodyRegular());
                FontMetrics fm = g2.getFontMetrics();
                int y = (h - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(prefixText, 14, y);
            }

            super.paintComponent(g2);

            // Placeholder
            if (getText().isEmpty() && !placeholder.isEmpty()) {
                g2.setColor(Theme.TEXT_DIM);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int y = (h - fm.getHeight()) / 2 + fm.getAscent();
                int leftX = (showSearchIcon || !prefixText.isEmpty()) ? 36 : 14;
                g2.drawString(placeholder, leftX, y);
            }

            // Shortcut Badge (Ctrl/Cmd K)
            if (showShortcutBadge) {
                g2.setColor(Theme.TEXT_DIM);
                g2.setFont(Theme.captionBold());
                FontMetrics fm = g2.getFontMetrics();
                // Draw clover/command symbol manually + K so it never shows a missing glyph box
                int bx = w - 36;
                int cy = h / 2;
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(bx, cy - 4, 8, 8, 2, 2);
                g2.drawOval(bx - 2, cy - 6, 4, 4);
                g2.drawOval(bx + 6, cy - 6, 4, 4);
                g2.drawOval(bx - 2, cy + 2, 4, 4);
                g2.drawOval(bx + 6, cy + 2, 4, 4);
                g2.drawString("K", bx + 14, (h - fm.getHeight()) / 2 + fm.getAscent());
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
            setBackground(new Color(0x2B, 0x2D, 0x36));
            setOpaque(false);
            setEchoChar('•');
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

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getX() >= getWidth() - 38) {
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

            g2.setColor(new Color(0x2B, 0x2D, 0x36, 220));
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 12, 12));

            if (isFocused) {
                g2.setColor(Theme.ACCENT_ORANGE);
                g2.setStroke(new BasicStroke(1.4f));
            } else {
                g2.setColor(new Color(0xFF, 0xFF, 0xFF, 28));
                g2.setStroke(new BasicStroke(1.0f));
            }
            g2.draw(new RoundRectangle2D.Float(0.6f, 0.6f, w - 1.2f, h - 1.2f, 12, 12));

            super.paintComponent(g2);

            if (getPassword().length == 0 && !placeholder.isEmpty()) {
                g2.setColor(Theme.TEXT_DIM);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int y = (h - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(placeholder, 14, y);
            }

            // Eye icon toggle di kanan
            g2.setColor(showPassword ? Theme.ACCENT_ORANGE : Theme.TEXT_DIM);
            g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int eyeX = w - 22;
            int eyeY = h / 2;
            g2.drawOval(eyeX - 7, eyeY - 4, 14, 8);
            g2.fillOval(eyeX - 2, eyeY - 2, 4, 4);

            g2.dispose();
        }
    }

    /**
     * Styling JComboBox Modern agar Membulat dan Gelap seperti di Figma
     */
    public static <T> void styleComboBox(JComboBox<T> combo) {
        combo.setFont(Theme.bodyRegular());
        combo.setForeground(Theme.TEXT_WHITE);
        combo.setBackground(new Color(0x2B, 0x2D, 0x36));
        combo.setOpaque(false);
        combo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        combo.setBorder(new EmptyBorder(6, 12, 6, 10));

        combo.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton btn = new JButton() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(Theme.TEXT_DIM);
                        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        int cx = getWidth() / 2 - 2;
                        int cy = getHeight() / 2;
                        g2.drawLine(cx - 4, cy - 2, cx, cy + 2);
                        g2.drawLine(cx, cy + 2, cx + 4, cy - 2);
                        g2.dispose();
                    }
                };
                btn.setOpaque(false);
                btn.setContentAreaFilled(false);
                btn.setBorderPainted(false);
                btn.setFocusPainted(false);
                return btn;
            }

            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = c.getWidth();
                int h = c.getHeight();
                g2.setColor(new Color(0x2B, 0x2D, 0x36, 220));
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 12, 12));
                g2.setColor(new Color(0xFF, 0xFF, 0xFF, 28));
                g2.setStroke(new BasicStroke(1.0f));
                g2.draw(new RoundRectangle2D.Float(0.6f, 0.6f, w - 1.2f, h - 1.2f, 12, 12));
                g2.dispose();
                super.paint(g, c);
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                // Jangan gambar background kotak abu-abu bawaan
            }
        });

        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                l.setFont(Theme.bodyRegular());
                l.setBorder(new EmptyBorder(6, 10, 6, 10));
                if (index == -1) {
                    l.setOpaque(false);
                    l.setForeground(Theme.TEXT_WHITE);
                } else {
                    l.setOpaque(true);
                    l.setBackground(isSelected ? new Color(0x3A, 0x3C, 0x48) : new Color(0x25, 0x27, 0x30));
                    l.setForeground(isSelected ? Theme.ACCENT_ORANGE : Theme.TEXT_WHITE);
                }
                return l;
            }
        });
    }

    /**
     * Stepper Angka Modern dengan tombol '-' dan '+' di sisi kanan seperti di Figma
     * Contoh: [ 2024    -  + ] atau [ 3 hari    -  + ]
     */
    public static class ModernStepper extends JPanel {
        private int value;
        private final int min;
        private final int max;
        private final int step;
        private final String suffix;
        private final JLabel lblVal;
        private final List<ChangeListener> listeners = new ArrayList<>();

        public ModernStepper(int initialValue, int min, int max, int step, String suffix) {
            this.value = initialValue;
            this.min = min;
            this.max = max;
            this.step = step;
            this.suffix = suffix != null ? suffix : "";

            setOpaque(false);
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(0, 14, 0, 10));
            setPreferredSize(new Dimension(120, 40));

            lblVal = new JLabel(formatValue());
            lblVal.setFont(Theme.bodyRegular());
            lblVal.setForeground(Theme.TEXT_WHITE);

            JPanel btnBox = new JPanel(new GridLayout(1, 2, 6, 0));
            btnBox.setOpaque(false);

            JLabel btnMinus = createStepBtn("–", -step);
            JLabel btnPlus = createStepBtn("+", step);

            btnBox.add(btnMinus);
            btnBox.add(btnPlus);

            add(lblVal, BorderLayout.CENTER);
            add(btnBox, BorderLayout.EAST);
        }

        private JLabel createStepBtn(String symbol, int delta) {
            JLabel l = new JLabel(symbol, SwingConstants.CENTER);
            l.setFont(Theme.bodyBold());
            l.setForeground(Theme.TEXT_DIM);
            l.setPreferredSize(new Dimension(22, 36));
            l.setCursor(new Cursor(Cursor.HAND_CURSOR));
            l.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    setValue(value + delta);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    l.setForeground(Theme.ACCENT_ORANGE);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    l.setForeground(Theme.TEXT_DIM);
                }
            });
            return l;
        }

        private String formatValue() {
            return suffix.isEmpty() ? String.valueOf(value) : (value + " " + suffix);
        }

        public int getValue() {
            return value;
        }

        public void setValue(int newVal) {
            int clamped = Math.max(min, Math.min(max, newVal));
            if (this.value != clamped) {
                this.value = clamped;
                lblVal.setText(formatValue());
                fireStateChanged();
            } else {
                lblVal.setText(formatValue());
            }
        }

        public void addChangeListener(ChangeListener l) {
            listeners.add(l);
        }

        private void fireStateChanged() {
            ChangeEvent evt = new ChangeEvent(this);
            for (ChangeListener l : listeners) {
                l.stateChanged(evt);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            g2.setColor(new Color(0x2B, 0x2D, 0x36, 220));
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 12, 12));
            g2.setColor(new Color(0xFF, 0xFF, 0xFF, 28));
            g2.setStroke(new BasicStroke(1.0f));
            g2.draw(new RoundRectangle2D.Float(0.6f, 0.6f, w - 1.2f, h - 1.2f, 12, 12));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Input Tanggal Modern dengan Ikon Kalender Vektor di Kanan seperti di Figma
     * Contoh: [ 03 Nov 2026    📅 ]
     */
    public static class ModernDateField extends JPanel {
        private LocalDate date;
        private final JLabel lblDate;
        private final List<ChangeListener> listeners = new ArrayList<>();
        private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

        public ModernDateField(LocalDate initialDate) {
            this.date = initialDate != null ? initialDate : LocalDate.of(2026, 11, 3);
            setOpaque(false);
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(0, 14, 0, 12));
            setPreferredSize(new Dimension(150, 40));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            lblDate = new JLabel(this.date.format(FMT));
            lblDate.setFont(Theme.bodyRegular());
            lblDate.setForeground(Theme.TEXT_WHITE);

            JPanel calIcon = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Theme.TEXT_DIM);
                    g2.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    int cx = getWidth() / 2;
                    int cy = getHeight() / 2;
                    g2.drawRoundRect(cx - 7, cy - 6, 14, 13, 3, 3);
                    g2.drawLine(cx - 7, cy - 2, cx + 7, cy - 2);
                    g2.drawLine(cx - 4, cy - 8, cx - 4, cy - 5);
                    g2.drawLine(cx + 4, cy - 8, cx + 4, cy - 5);
                    g2.dispose();
                }

                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(22, 36);
                }
            };
            calIcon.setOpaque(false);

            add(lblDate, BorderLayout.CENTER);
            add(calIcon, BorderLayout.EAST);

            // Klik kiri untuk +1 hari, klik kanan untuk -1 hari, atau scroll roda mouse
            MouseAdapter ma = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        setDate(date.minusDays(1));
                    } else {
                        setDate(date.plusDays(1));
                    }
                }

                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    if (e.getWheelRotation() < 0) {
                        setDate(date.plusDays(1));
                    } else {
                        setDate(date.minusDays(1));
                    }
                }
            };
            addMouseListener(ma);
            addMouseWheelListener(ma);
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate newDate) {
            if (newDate != null && !newDate.equals(this.date)) {
                this.date = newDate;
                lblDate.setText(this.date.format(FMT));
                fireStateChanged();
            } else if (newDate != null) {
                lblDate.setText(this.date.format(FMT));
            }
        }

        public void addChangeListener(ChangeListener l) {
            listeners.add(l);
        }

        private void fireStateChanged() {
            ChangeEvent evt = new ChangeEvent(this);
            for (ChangeListener l : listeners) {
                l.stateChanged(evt);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            g2.setColor(new Color(0x2B, 0x2D, 0x36, 220));
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 12, 12));
            g2.setColor(new Color(0xFF, 0xFF, 0xFF, 28));
            g2.setStroke(new BasicStroke(1.0f));
            g2.draw(new RoundRectangle2D.Float(0.6f, 0.6f, w - 1.2f, h - 1.2f, 12, 12));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Styling JCheckBox dengan Kotak Centang Oranye (#FFAA72) persis seperti Figma
     */
    public static void styleCheckBox(JCheckBox chk) {
        chk.setOpaque(false);
        chk.setFocusPainted(false);
        chk.setCursor(new Cursor(Cursor.HAND_CURSOR));
        chk.setIconTextGap(10);
        chk.setIcon(new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                AbstractButton b = (AbstractButton) c;
                if (b.isSelected()) {
                    g2.setColor(Theme.ACCENT_ORANGE);
                    g2.fill(new RoundRectangle2D.Float(x, y, 16, 16, 5, 5));
                    g2.setColor(new Color(0x18, 0x19, 0x1D));
                    g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(x + 4, y + 8, x + 7, y + 11);
                    g2.drawLine(x + 7, y + 11, x + 12, y + 5);
                } else {
                    g2.setColor(new Color(0x2B, 0x2D, 0x36));
                    g2.fill(new RoundRectangle2D.Float(x, y, 16, 16, 5, 5));
                    g2.setColor(new Color(0xFF, 0xFF, 0xFF, 45));
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.draw(new RoundRectangle2D.Float(x + 0.5f, y + 0.5f, 15, 15, 5, 5));
                }
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 16;
            }

            @Override
            public int getIconHeight() {
                return 16;
            }
        });
    }

    /**
     * Styling JRadioButton Modern seperti di Figma
     */
    public static void styleRadioButton(JRadioButton rdo) {
        rdo.setOpaque(false);
        rdo.setFocusPainted(false);
        rdo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        rdo.setIconTextGap(8);
        rdo.setIcon(new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                AbstractButton b = (AbstractButton) c;
                if (b.isSelected()) {
                    g2.setColor(Theme.TEXT_WHITE);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawOval(x + 1, y + 1, 14, 14);
                    g2.fillOval(x + 5, y + 5, 6, 6);
                } else {
                    g2.setColor(Theme.TEXT_DIM);
                    g2.setStroke(new BasicStroke(1.3f));
                    g2.drawOval(x + 1, y + 1, 14, 14);
                }
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 16;
            }

            @Override
            public int getIconHeight() {
                return 16;
            }
        });
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

            g2.setColor(textColor);
            int dotSize = 6;
            int dotY = (h - dotSize) / 2;
            g2.fillOval(10, dotY, dotSize, dotSize);

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
            super(new Color(0x26, 0x27, 0x30, 195), new Color(0xFF, 0xFF, 0xFF, 26), 18);
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(18, 22, 18, 22));

            lblTitle = new JLabel(title);
            lblTitle.setFont(Theme.caption());
            lblTitle.setForeground(Theme.TEXT_MUTED);

            lblValue = new JLabel(value);
            lblValue.setFont(new Font(Theme.FONT_NAME, Font.BOLD, 30));
            lblValue.setForeground(valueColor != null ? valueColor : Theme.TEXT_WHITE);
            lblValue.setBorder(new EmptyBorder(6, 0, 6, 0));

            lblSubtext = new JLabel(subtext);
            lblSubtext.setFont(Theme.caption());
            lblSubtext.setForeground(Theme.TEXT_DIM);

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
            if (subtext != null && !subtext.isEmpty()) {
                lblSubtext.setText(subtext);
            }
        }
    }

    /**
     * Tombol Filter Pill (PillFilterButton)
     * Mendukung teks kategori + angka di kanan (contoh: "Semua armada  48")
     */
    public static class PillFilterButton extends JButton {
        private boolean isSelected = false;

        public PillFilterButton(String text, boolean selected) {
            super(text);
            this.isSelected = selected;
            setFont(Theme.captionBold());
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(8, 18, 8, 18));
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
                g2.setColor(new Color(0x36, 0x35, 0x42, 230));
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 10, 10));
                g2.setColor(new Color(0xFF, 0xFF, 0xFF, 65));
                g2.setStroke(new BasicStroke(1.1f));
                g2.draw(new RoundRectangle2D.Float(0.6f, 0.6f, w - 1.2f, h - 1.2f, 10, 10));
                setForeground(Theme.ACCENT_ORANGE);
            } else {
                setForeground(Theme.TEXT_MUTED);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // =========================================================================
    // CUSTOM PAINTED TABLE CELL RENDERERS (100% BEBAS DARI MASALAH LAYOUT JTABLE)
    // =========================================================================

    /**
     * Renderer Sel 2 Baris (Baris 1: Judul Putih Tebal, Baris 2: Subteks Abu-abu)
     * Dilengkapi indikator garis vertikal oranye di tepi kiri untuk baris terpilih.
     */
    public static class TwoLineTableRenderer extends DefaultTableCellRenderer {
        private final boolean enableOrangeBar;
        private String line1 = "";
        private String line2 = "";
        private boolean showOrangeBar = false;

        public TwoLineTableRenderer() {
            this(true);
        }

        public TwoLineTableRenderer(boolean enableOrangeBar) {
            this.enableOrangeBar = enableOrangeBar;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
            String raw = value != null ? value.toString() : "";
            String[] parts = raw.split("\n", 2);
            this.line1 = parts.length > 0 ? parts[0] : "";
            this.line2 = parts.length > 1 ? parts[1] : "";
            this.showOrangeBar = enableOrangeBar && (isSelected || (table.getSelectedRow() == -1 && row == 0));
            setOpaque(true);
            setBackground(isSelected ? new Color(0x34, 0x34, 0x42) : (row % 2 == 0 ? new Color(0x28, 0x29, 0x33) : new Color(0x23, 0x24, 0x2C)));
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int h = getHeight();
            if (showOrangeBar) {
                g2.setColor(Theme.ACCENT_ORANGE);
                g2.fill(new RoundRectangle2D.Float(2, 10, 3.5f, h - 20, 3, 3));
            }

            int x = 16;
            g2.setFont(Theme.bodyBold());
            g2.setColor(Theme.TEXT_WHITE);
            g2.drawString(line1, x, h / 2 - 3);

            g2.setFont(Theme.caption());
            g2.setColor(Theme.TEXT_DIM);
            g2.drawString(line2, x, h / 2 + 14);

            g2.dispose();
        }
    }

    /**
     * Renderer Sel Badge Pill Status ("Tersedia", "Disewa", "Terlambat", "Selesai")
     */
    public static class StatusBadgeTableRenderer extends DefaultTableCellRenderer {
        private String statusText = "";

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
            this.statusText = value != null ? value.toString() : "";
            setOpaque(true);
            setBackground(isSelected ? new Color(0x34, 0x34, 0x42) : (row % 2 == 0 ? new Color(0x28, 0x29, 0x33) : new Color(0x23, 0x24, 0x2C)));
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color bg;
            Color fg;
            if ("Tersedia".equalsIgnoreCase(statusText) || "Selesai".equalsIgnoreCase(statusText)) {
                bg = new Color(0x22, 0x38, 0x2E);
                fg = Theme.BADGE_GREEN_TXT;
            } else if ("Terlambat".equalsIgnoreCase(statusText)) {
                bg = new Color(0x3D, 0x22, 0x28);
                fg = Theme.BADGE_RED_TXT;
            } else {
                bg = new Color(0x3B, 0x31, 0x22);
                fg = Theme.BADGE_AMBER_TXT;
            }

            g2.setFont(Theme.captionBold());
            FontMetrics fm = g2.getFontMetrics();
            int pillW = 24 + fm.stringWidth(statusText) + 14;
            int pillH = 26;
            int px = 8;
            int py = (getHeight() - pillH) / 2;

            g2.setColor(bg);
            g2.fill(new RoundRectangle2D.Float(px, py, pillW, pillH, pillH, pillH));

            g2.setColor(fg);
            g2.fillOval(px + 10, py + (pillH - 6) / 2, 6, 6);
            g2.drawString(statusText, px + 22, py + (pillH - fm.getHeight()) / 2 + fm.getAscent());

            g2.dispose();
        }
    }

    /**
     * Renderer Sel Aksi ("Ubah" di kiri & "Hapus" di kanan sebagai tombol aksi bersih)
     */
    public static class StackedActionTableRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
            setOpaque(true);
            setBackground(isSelected ? new Color(0x34, 0x34, 0x42) : (row % 2 == 0 ? new Color(0x28, 0x29, 0x33) : new Color(0x23, 0x24, 0x2C)));
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int h = getHeight();
            int btnH = 26;
            int y = (h - btnH) / 2;

            g2.setFont(Theme.captionBold());
            FontMetrics fm = g2.getFontMetrics();
            int textY = y + (btnH - fm.getHeight()) / 2 + fm.getAscent();

            // Tombol Ubah
            int ubahW = 48;
            int ubahX = 8;
            g2.setColor(new Color(0x3A, 0x2E, 0x26));
            g2.fill(new RoundRectangle2D.Float(ubahX, y, ubahW, btnH, 8, 8));
            g2.setColor(new Color(0xFF, 0xAA, 0x72, 90));
            g2.draw(new RoundRectangle2D.Float(ubahX, y, ubahW, btnH, 8, 8));
            g2.setColor(Theme.ACCENT_ORANGE);
            g2.drawString("Ubah", ubahX + (ubahW - fm.stringWidth("Ubah")) / 2, textY);

            // Tombol Hapus
            int hapusW = 54;
            int hapusX = ubahX + ubahW + 6;
            g2.setColor(new Color(0x3A, 0x22, 0x28));
            g2.fill(new RoundRectangle2D.Float(hapusX, y, hapusW, btnH, 8, 8));
            g2.setColor(new Color(0xFF, 0xA5, 0xAB, 90));
            g2.draw(new RoundRectangle2D.Float(hapusX, y, hapusW, btnH, 8, 8));
            g2.setColor(Theme.BADGE_RED_TXT);
            g2.drawString("Hapus", hapusX + (hapusW - fm.stringWidth("Hapus")) / 2, textY);

            g2.dispose();
        }
    }

    /**
     * Renderer Sel Teks Warna Kustom (contoh: "Kembalikan →" atau "Ada" / "Tidak")
     */
    public static class ColoredTextTableRenderer extends DefaultTableCellRenderer {
        private final Color fixedColor;
        private final boolean bold;

        public ColoredTextTableRenderer(Color fixedColor, boolean bold) {
            this.fixedColor = fixedColor;
            this.bold = bold;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            l.setFont(bold ? Theme.captionBold() : Theme.bodyRegular());
            l.setBorder(new EmptyBorder(0, 12, 0, 12));
            l.setOpaque(true);
            l.setBackground(isSelected ? new Color(0x34, 0x34, 0x42) : (row % 2 == 0 ? new Color(0x28, 0x29, 0x33) : new Color(0x23, 0x24, 0x2C)));

            String txt = value != null ? value.toString() : "";
            if (fixedColor != null) {
                l.setForeground(fixedColor);
            } else if ("Ada".equalsIgnoreCase(txt)) {
                l.setForeground(Theme.BADGE_GREEN_TXT);
            } else if ("Tidak".equalsIgnoreCase(txt)) {
                l.setForeground(Theme.TEXT_DIM);
            } else {
                l.setForeground(Theme.TEXT_WHITE);
            }
            return l;
        }
    }

    /**
     * Tabel Modern Gelap Terkonfigurasi seperti di Figma
     */
    public static void styleTable(JTable table) {
        table.setBackground(new Color(0x23, 0x24, 0x2C));
        table.setForeground(Theme.TEXT_WHITE);
        table.setFont(Theme.bodyRegular());
        table.setRowHeight(56);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 4));
        table.setSelectionBackground(new Color(0x34, 0x34, 0x42));
        table.setSelectionForeground(Theme.TEXT_WHITE);
        table.setFillsViewportHeight(true);

        // Default renderer untuk kolom biasa agar memiliki padding & warna baris selang-seling
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                l.setBorder(new EmptyBorder(0, 12, 0, 12));
                l.setFont(col == 1 ? Theme.bodyBold() : Theme.bodyRegular());
                l.setForeground(Theme.TEXT_WHITE);
                l.setOpaque(true);
                l.setBackground(isSelected ? new Color(0x34, 0x34, 0x42) : (row % 2 == 0 ? new Color(0x28, 0x29, 0x33) : new Color(0x23, 0x24, 0x2C)));
                return l;
            }
        });

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(0x1E, 0x20, 0x28));
        header.setForeground(Theme.TEXT_MUTED);
        header.setFont(Theme.captionBold());
        header.setPreferredSize(new Dimension(800, 38));
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                l.setOpaque(true);
                l.setBackground(new Color(0x1E, 0x20, 0x28));
                l.setForeground(Theme.TEXT_MUTED);
                l.setFont(Theme.captionBold());
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0x34, 0x36, 0x42)),
                        new EmptyBorder(6, 12, 6, 12)
                ));
                l.setHorizontalAlignment(SwingConstants.LEFT);
                return l;
            }
        });
        header.setOpaque(true);
    }

    /**
     * Membungkus JTable beserta JTableHeader-nya agar judul kolom selalu tampil rapi
     */
    public static JPanel wrapTableWithHeader(JTable table, Dimension preferredScrollSize) {
        JPanel wrap = new JPanel(new BorderLayout(0, 2));
        wrap.setOpaque(false);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        if (preferredScrollSize != null) {
            scroll.setPreferredSize(preferredScrollSize);
        }

        wrap.add(table.getTableHeader(), BorderLayout.NORTH);
        wrap.add(scroll, BorderLayout.CENTER);
        return wrap;
    }
}
