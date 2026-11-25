package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Dialog Modal Kustom Penanganan Eksepsi (CustomDialog)
 * Mereplikasi secara 1:1 desain ke-3 pop-up penanganan eksepsi dari Figma:
 * 1. Koneksi database gagal (Database icon + x, "Tutup" & "Coba lagi")
 * 2. Periksa kembali NIK (Warning icon (!), "Tutup" & "Perbaiki data")
 * 3. Kendaraan tidak tersedia (Crossed-out car icon, "Tutup" & "Pilih kendaraan")
 */
@SuppressWarnings("serial")
public class CustomDialog extends JDialog {
    public enum IconType { DATABASE_ERROR, NIK_WARNING, CAR_UNAVAILABLE, GENERAL_ERROR, SUCCESS }

    private int mouseX, mouseY;

    public CustomDialog(Window parent, IconType iconType, String title, String message,
                        String primaryBtnText, Runnable onPrimaryAction) {
        super(parent, ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setSize(416, 232);
        setLocationRelativeTo(parent);

        // Panel Kontainer Utama dengan Sudut Membulat (rx=24)
        JPanel container = new JPanel(new BorderLayout(0, 14)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Latar Belakang Modal Gelap (#212228)
                g2.setColor(new Color(0x21, 0x22, 0x28));
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 24, 24));

                // Border Halus (#363842)
                g2.setColor(new Color(0x36, 0x38, 0x42));
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(0.6f, 0.6f, w - 1.2f, h - 1.2f, 24, 24));

                g2.dispose();
                super.paintComponent(g);
            }
        };
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(22, 24, 22, 24));

        // Memungkinkan dialog digeser dengan mouse
        container.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }
        });
        container.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                setLocation(getX() + e.getX() - mouseX, getY() + e.getY() - mouseY);
            }
        });

        // 1. Header Row (Icon Kotak 44x44 di kiri + Tombol Close Vektor 'X' di kanan)
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);

        JPanel iconBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Kotak ikon gelap membulat (44x44, rx=12)
                g2.setColor(new Color(0x2A, 0x2C, 0x34));
                g2.fill(new RoundRectangle2D.Float(0, 0, 44, 44, 12, 12));
                g2.setColor(new Color(0x3E, 0x40, 0x4C));
                g2.setStroke(new BasicStroke(1.0f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, 43, 43, 12, 12));

                int cx = 22;
                int cy = 22;

                if (iconType == IconType.DATABASE_ERROR) {
                    // Ikon Database Stack dengan tanda silang 'x' merah muda (#FFA5AB)
                    g2.setColor(Theme.BADGE_RED_TXT);
                    g2.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    // Elips atas silinder
                    g2.drawOval(cx - 8, cy - 9, 15, 6);
                    // Dinding kiri & kanan silinder
                    g2.drawLine(cx - 8, cy - 6, cx - 8, cy + 5);
                    g2.drawLine(cx + 7, cy - 6, cx + 7, cy);
                    // Lengkung tengah & bawah silinder
                    g2.drawArc(cx - 8, cy - 3, 15, 6, 180, 160);
                    g2.drawArc(cx - 8, cy + 2, 15, 6, 180, 125);
                    // Tanda silang 'x' di kanan bawah
                    g2.drawLine(cx + 3, cy + 3, cx + 9, cy + 9);
                    g2.drawLine(cx + 9, cy + 3, cx + 3, cy + 9);

                } else if (iconType == IconType.NIK_WARNING) {
                    // Ikon Warning Circle (!) warna Amber (#FFD18A)
                    g2.setColor(Theme.BADGE_AMBER_TXT);
                    g2.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawOval(cx - 9, cy - 9, 18, 18);
                    g2.drawLine(cx, cy - 4, cx, cy + 1);
                    g2.fillOval(cx - 1, cy + 4, 2, 2);

                } else if (iconType == IconType.CAR_UNAVAILABLE) {
                    // Ikon Mobil dengan garis coret diagonal warna Amber/Oranye (#FFD18A)
                    g2.setColor(Theme.BADGE_AMBER_TXT);
                    g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    // Atap trapesium mobil
                    g2.drawLine(cx - 7, cy - 1, cx - 5, cy - 6);
                    g2.drawLine(cx - 5, cy - 6, cx + 5, cy - 6);
                    g2.drawLine(cx + 5, cy - 6, cx + 7, cy - 1);
                    // Bodi mobil
                    g2.drawRoundRect(cx - 8, cy - 1, 16, 6, 2, 2);
                    // Roda kiri & kanan
                    g2.drawLine(cx - 6, cy + 5, cx - 6, cy + 7);
                    g2.drawLine(cx + 6, cy + 5, cx + 6, cy + 7);
                    // Lampu depan
                    g2.drawLine(cx - 5, cy + 2, cx - 4, cy + 2);
                    g2.drawLine(cx + 4, cy + 2, cx + 5, cy + 2);
                    // Garis coret diagonal dari kiri-atas ke kanan-bawah
                    g2.drawLine(cx - 9, cy - 8, cx + 9, cy + 8);

                } else if (iconType == IconType.SUCCESS) {
                    g2.setColor(Theme.BADGE_GREEN_TXT);
                    g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawOval(cx - 9, cy - 9, 18, 18);
                    g2.drawLine(cx - 4, cy, cx - 1, cy + 3);
                    g2.drawLine(cx - 1, cy + 3, cx + 5, cy - 3);

                } else {
                    g2.setColor(Theme.BADGE_RED_TXT);
                    g2.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawOval(cx - 9, cy - 9, 18, 18);
                    g2.drawLine(cx - 4, cy - 4, cx + 4, cy + 4);
                    g2.drawLine(cx + 4, cy - 4, cx - 4, cy + 4);
                }

                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(44, 44);
            }
        };
        iconBox.setOpaque(false);
        headerRow.add(iconBox, BorderLayout.WEST);

        // Tombol Close 'X' Vektor di Kanan Atas (bebas masalah font missing glyph)
        JPanel btnClose = new JPanel() {
            private boolean hovered = false;
            {
                setOpaque(false);
                setPreferredSize(new Dimension(28, 28));
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        dispose();
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hovered = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hovered = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hovered ? Theme.TEXT_WHITE : Theme.TEXT_DIM);
                g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = getWidth() / 2;
                int cy = getHeight() / 2 - 4;
                g2.drawLine(cx - 4, cy - 4, cx + 4, cy + 4);
                g2.drawLine(cx + 4, cy - 4, cx - 4, cy + 4);
                g2.dispose();
            }
        };
        headerRow.add(btnClose, BorderLayout.EAST);

        // 2. Center Content (Title & Message)
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(4, 0, 6, 0));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font(Theme.FONT_NAME, Font.BOLD, 18));
        lblTitle.setForeground(Theme.TEXT_WHITE);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        contentPanel.add(lblTitle);
        contentPanel.add(Box.createVerticalStrut(6));

        String[] lines = message.split("\n");
        for (int i = 0; i < lines.length; i++) {
            JLabel lblMsg = new JLabel(lines[i]);
            lblMsg.setFont(Theme.bodyRegular());
            lblMsg.setForeground(Theme.TEXT_MUTED);
            lblMsg.setAlignmentX(Component.LEFT_ALIGNMENT);
            contentPanel.add(lblMsg);
            if (i < lines.length - 1) {
                contentPanel.add(Box.createVerticalStrut(2));
            }
        }

        // 3. Bottom Button Row ("Tutup" & Primary Action Button) - Grid 2 Kolom Penuh
        JPanel btnRow = new JPanel(new GridLayout(1, 2, 12, 0));
        btnRow.setOpaque(false);
        btnRow.setPreferredSize(new Dimension(0, 44));

        UIComponents.ModernButton btnTutup = new UIComponents.ModernButton("Tutup", UIComponents.ModernButton.Type.SECONDARY);
        btnTutup.addActionListener(e -> dispose());

        UIComponents.ModernButton btnAction = new UIComponents.ModernButton(primaryBtnText, UIComponents.ModernButton.Type.PRIMARY);
        btnAction.addActionListener(e -> {
            dispose();
            if (onPrimaryAction != null) {
                onPrimaryAction.run();
            }
        });

        btnRow.add(btnTutup);
        btnRow.add(btnAction);

        container.add(headerRow, BorderLayout.NORTH);
        container.add(contentPanel, BorderLayout.CENTER);
        container.add(btnRow, BorderLayout.SOUTH);

        setContentPane(container);
    }

    // =========================================================================
    // 3 METODE UTAMA POP-UP PENANGANAN EKSEPSI SESUAI DESAIN MOCKUP FIGMA
    // =========================================================================

    /**
     * Pop-up Eksepsi 1: "Koneksi database gagal"
     * Dipanggil saat terjadi SQLException / kegagalan penyimpanan basis data.
     */
    public static void showDatabaseError(Window parent, Runnable onRetry) {
        CustomDialog dialog = new CustomDialog(
                parent,
                IconType.DATABASE_ERROR,
                "Koneksi database gagal",
                "Data belum tersimpan. Periksa koneksi\ndatabase, lalu coba lagi.",
                "Coba lagi",
                onRetry
        );
        dialog.setVisible(true);
    }

    /**
     * Pop-up Eksepsi 2: "Periksa kembali NIK"
     * Dipanggil saat validasi input NIK KTP tidak memenuhi 16 digit angka.
     */
    public static void showNikError(Window parent, Runnable onFix) {
        CustomDialog dialog = new CustomDialog(
                parent,
                IconType.NIK_WARNING,
                "Periksa kembali NIK",
                "NIK KTP harus terdiri dari 16 digit angka.\nPerbaiki data untuk melanjutkan.",
                "Perbaiki data",
                onFix
        );
        dialog.setVisible(true);
    }

    /**
     * Pop-up Eksepsi 3: "Kendaraan tidak tersedia"
     * Dipanggil saat pengguna memilih unit kendaraan yang sedang berstatus 'Disewa'.
     */
    public static void showVehicleUnavailable(Window parent, Runnable onSelect) {
        CustomDialog dialog = new CustomDialog(
                parent,
                IconType.CAR_UNAVAILABLE,
                "Kendaraan tidak tersedia",
                "Unit ini sedang disewa pelanggan lain.\nPilih unit berstatus Tersedia untuk melanjutkan.",
                "Pilih kendaraan",
                onSelect
        );
        dialog.setVisible(true);
    }

    /**
     * Pop-up Peringatan Validasi Umum
     */
    public static void showWarning(Window parent, String title, String message, String actionText, Runnable onAction) {
        CustomDialog dialog = new CustomDialog(
                parent,
                IconType.NIK_WARNING,
                title,
                message,
                actionText,
                onAction
        );
        dialog.setVisible(true);
    }

    /**
     * Pop-up Konfirmasi Berhasil
     */
    public static void showSuccess(Window parent, String title, String message) {
        CustomDialog dialog = new CustomDialog(
                parent,
                IconType.SUCCESS,
                title,
                message,
                "Selesai",
                null
        );
        dialog.setVisible(true);
    }
}
