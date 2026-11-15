package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Dialog Modal Kustom (CustomDialog)
 * Mereplikasi secara 1:1 desain pop-up penanganan eksepsi dari Figma:
 * 1. Koneksi database gagal (Database icon + x, "Tutup" & "Coba lagi")
 * 2. Periksa kembali NIK (Warning icon (!), "Tutup" & "Perbaiki data")
 * 3. Kendaraan tidak tersedia (Crossed-out car icon, "Tutup" & "Pilih kendaraan")
 */
public class CustomDialog extends JDialog {
    public enum IconType { DATABASE_ERROR, NIK_WARNING, CAR_UNAVAILABLE, GENERAL_ERROR, SUCCESS }

    private int mouseX, mouseY;

    public CustomDialog(Window parent, IconType iconType, String title, String message,
                        String primaryBtnText, Runnable onPrimaryAction) {
        super(parent, ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0)); // Transparan untuk sudut membulat sempurna
        setSize(420, 230);
        setLocationRelativeTo(parent);

        // Panel Kontainer Utama dengan Sudut Membulat
        JPanel container = new JPanel(new BorderLayout(0, 14)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Latar Belakang Kartu Gelap
                g2.setColor(new Color(0x20, 0x22, 0x29));
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 24, 24));

                // Border Halus
                g2.setColor(new Color(0x38, 0x3A, 0x47));
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

        // 1. Header Row (Icon Kotak di kiri + Tombol Close '✕' di kanan)
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);

        // Icon Box Kotak Membulat
        JPanel iconBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Kotak abu-abu gelap
                g2.setColor(new Color(0x28, 0x2A, 0x33));
                g2.fill(new RoundRectangle2D.Float(0, 0, 44, 44, 14, 14));
                g2.setColor(new Color(0x3D, 0x40, 0x4E));
                g2.setStroke(new BasicStroke(1.0f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, 43, 43, 14, 14));

                int cx = 22;
                int cy = 22;

                if (iconType == IconType.DATABASE_ERROR) {
                    // Ikon Database Stack dengan tanda silang 'x' merah
                    g2.setColor(Theme.BADGE_RED_TXT);
                    g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    // Silinder atas
                    g2.drawOval(cx - 10, cy - 10, 20, 8);
                    // Dinding silinder
                    g2.drawLine(cx - 10, cy - 6, cx - 10, cy + 6);
                    g2.drawArc(cx - 10, cy - 1, 20, 8, 180, 180);
                    g2.drawArc(cx - 10, cy + 3, 20, 8, 180, 180);
                    // Silang kecil merah di kanan bawah
                    g2.drawLine(cx + 4, cy + 4, cx + 11, cy + 11);
                    g2.drawLine(cx + 11, cy + 4, cx + 4, cy + 11);

                } else if (iconType == IconType.NIK_WARNING) {
                    // Ikon Warning Circle (!) warna Amber
                    g2.setColor(Theme.BADGE_AMBER_TXT);
                    g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawOval(cx - 10, cy - 10, 20, 20);
                    g2.drawLine(cx, cy - 5, cx, cy + 1);
                    g2.fillOval(cx - 1, cy + 4, 3, 3);

                } else if (iconType == IconType.CAR_UNAVAILABLE) {
                    // Ikon Mobil dengan garis coret diagonal
                    g2.setColor(Theme.ACCENT_ORANGE);
                    g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    // Bentuk mobil
                    g2.drawLine(cx - 7, cy + 2, cx - 4, cy - 5);
                    g2.drawLine(cx - 4, cy - 5, cx + 4, cy - 5);
                    g2.drawLine(cx + 4, cy - 5, cx + 7, cy + 2);
                    g2.drawRoundRect(cx - 9, cy + 1, 18, 7, 2, 2);
                    g2.fillOval(cx - 6, cy + 5, 3, 3);
                    g2.fillOval(cx + 3, cy + 5, 3, 3);
                    // Garis coret diagonal
                    g2.drawLine(cx - 11, cy - 6, cx + 11, cy + 8);

                } else if (iconType == IconType.SUCCESS) {
                    g2.setColor(Theme.BADGE_GREEN_TXT);
                    g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawOval(cx - 10, cy - 10, 20, 20);
                    g2.drawLine(cx - 5, cy, cx - 1, cy + 4);
                    g2.drawLine(cx - 1, cy + 4, cx + 5, cy - 3);

                } else {
                    g2.setColor(Theme.BADGE_RED_TXT);
                    g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawOval(cx - 10, cy - 10, 20, 20);
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

        // Tombol Close '✕' di kanan
        JLabel btnClose = new JLabel("✕");
        btnClose.setFont(new Font(Theme.FONT_NAME, Font.PLAIN, 16));
        btnClose.setForeground(Theme.TEXT_DIM);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.setBorder(new EmptyBorder(0, 0, 16, 4));
        btnClose.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                btnClose.setForeground(Theme.TEXT_WHITE);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btnClose.setForeground(Theme.TEXT_DIM);
            }
        });
        headerRow.add(btnClose, BorderLayout.EAST);

        // 2. Center Content (Title & Message)
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(8, 0, 8, 0));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(Theme.titleSmall());
        lblTitle.setForeground(Theme.TEXT_WHITE);

        contentPanel.add(lblTitle);
        contentPanel.add(Box.createVerticalStrut(6));

        // Format pesan teks (bisa multi-line jika ada \n)
        String[] lines = message.split("\n");
        for (String line : lines) {
            JLabel lblMsg = new JLabel(line);
            lblMsg.setFont(Theme.bodyRegular());
            lblMsg.setForeground(Theme.TEXT_MUTED);
            contentPanel.add(lblMsg);
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

    // ==========================================
    // METODE STATIS PEMANGGILAN CEPAT
    // ==========================================

    /**
     * Menampilkan Pop-up Eksepsi: "Koneksi database gagal"
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
     * Menampilkan Pop-up Eksepsi: "Periksa kembali NIK"
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
     * Menampilkan Pop-up Eksepsi: "Kendaraan tidak tersedia"
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
     * Menampilkan Pop-up Umum: Peringatan Validasi
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
     * Menampilkan Pop-up Sukses
     */
    public static void showSuccess(Window parent, String title, String message) {
        CustomDialog dialog = new CustomDialog(
                parent,
                IconType.SUCCESS,
                title,
                message,
                "OK",
                null
        );
        dialog.setVisible(true);
    }
}
