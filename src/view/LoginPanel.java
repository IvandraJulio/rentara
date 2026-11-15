package view;

import controller.RentalController;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Panel Login Petugas (LoginPanel)
 * Mereplikasi tampilan '00 · Login petugas' dari Figma secara 100% presisi.
 */
public class LoginPanel extends JPanel {
    private final RentalController controller;
    private final MainFrame mainFrame;

    private BufferedImage heroCarImage;
    private UIComponents.ModernTextField txtUsername;
    private UIComponents.ModernPasswordField txtPassword;
    private JComboBox<String> cmbRole;
    private JCheckBox chkRemember;

    public LoginPanel(RentalController controller, MainFrame mainFrame) {
        this.controller = controller;
        this.mainFrame = mainFrame;
        setLayout(new GridLayout(1, 2));
        setBackground(Theme.BG_DARK);

        loadAssets();
        initComponents();
    }

    private void loadAssets() {
        try {
            File f = new File("assets/00_img_0.jpeg");
            if (f.exists()) {
                heroCarImage = ImageIO.read(f);
            }
        } catch (Exception e) {
            System.err.println("Gagal memuat gambar login: " + e.getMessage());
        }
    }

    private void initComponents() {
        // ==========================================
        // SISI KIRI: HERO CARD DENGAN GAMBAR & SLOGAN
        // ==========================================
        JPanel leftContainer = new JPanel(new BorderLayout());
        leftContainer.setOpaque(false);
        leftContainer.setBorder(new EmptyBorder(24, 24, 24, 16));

        UIComponents.ModernCard leftCard = new UIComponents.ModernCard(new Color(0x15, 0x16, 0x1B), Theme.BORDER, 24) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (heroCarImage != null) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    // Gambar Mobil di tengah/bawah
                    int imgW = (int) (getWidth() * 0.90);
                    int imgH = (int) (imgW * ((double) heroCarImage.getHeight() / heroCarImage.getWidth()));
                    int imgX = (getWidth() - imgW) / 2;
                    int imgY = getHeight() - imgH - 110;
                    g2.drawImage(heroCarImage, imgX, imgY, imgW, imgH, null);
                    g2.dispose();
                }
            }
        };
        leftCard.setLayout(new BorderLayout());
        leftCard.setBorder(new EmptyBorder(36, 40, 36, 40));

        // Top Content
        JPanel leftTop = new JPanel();
        leftTop.setLayout(new BoxLayout(leftTop, BoxLayout.Y_AXIS));
        leftTop.setOpaque(false);

        // Logo Rentara
        JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        logoRow.setOpaque(false);

        JPanel logoBadge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.ACCENT_ORANGE);
                g2.fill(new RoundRectangle2D.Float(0, 0, 34, 34, 10, 10));

                // Car icon
                g2.setColor(new Color(0x18, 0x19, 0x1D));
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(10, 20, 13, 14);
                g2.drawLine(13, 14, 21, 14);
                g2.drawLine(21, 14, 24, 20);
                g2.drawRoundRect(8, 19, 18, 7, 3, 3);
                g2.fillOval(11, 23, 3, 3);
                g2.fillOval(20, 23, 3, 3);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(34, 34);
            }
        };
        logoBadge.setOpaque(false);

        JLabel lblLogoText = new JLabel("rentara");
        lblLogoText.setFont(new Font(Theme.FONT_NAME, Font.BOLD, 22));
        lblLogoText.setForeground(Theme.TEXT_WHITE);

        logoRow.add(logoBadge);
        logoRow.add(lblLogoText);

        JLabel lblSloganTag = new JLabel("SETIAP PERJALANAN, LEBIH TERENCANA.");
        lblSloganTag.setFont(Theme.micro());
        lblSloganTag.setForeground(Theme.ACCENT_ORANGE);

        JLabel lblHeadline1 = new JLabel("Kelola armada.");
        lblHeadline1.setFont(new Font(Theme.FONT_NAME, Font.BOLD, 36));
        lblHeadline1.setForeground(Theme.TEXT_WHITE);

        JLabel lblHeadline2 = new JLabel("Buka lebih banyak kemungkinan.");
        lblHeadline2.setFont(new Font(Theme.FONT_NAME, Font.BOLD, 36));
        lblHeadline2.setForeground(Theme.TEXT_WHITE);

        JLabel lblSubtext = new JLabel("Dari kunci pertama hingga perjalanan selesai, semua ada dalam kendalimu.");
        lblSubtext.setFont(Theme.bodyRegular());
        lblSubtext.setForeground(Theme.TEXT_MUTED);

        leftTop.add(logoRow);
        leftTop.add(Box.createVerticalStrut(36));
        leftTop.add(lblSloganTag);
        leftTop.add(Box.createVerticalStrut(8));
        leftTop.add(lblHeadline1);
        leftTop.add(lblHeadline2);
        leftTop.add(Box.createVerticalStrut(12));
        leftTop.add(lblSubtext);

        // Bottom Frosted Pill
        UIComponents.ModernCard bottomPill = new UIComponents.ModernCard(new Color(0x23, 0x25, 0x2E), Theme.BORDER_LIGHT, 16);
        bottomPill.setLayout(new BorderLayout());
        bottomPill.setBorder(new EmptyBorder(14, 20, 14, 20));
        bottomPill.setPreferredSize(new Dimension(300, 56));

        JPanel pillContent = new JPanel(new BorderLayout());
        pillContent.setOpaque(false);

        JLabel lblPillNum = new JLabel("01   ");
        lblPillNum.setFont(Theme.titleMedium());
        lblPillNum.setForeground(Theme.ACCENT_ORANGE);

        JPanel pillText = new JPanel();
        pillText.setLayout(new BoxLayout(pillText, BoxLayout.Y_AXIS));
        pillText.setOpaque(false);

        JLabel lblPillTitle = new JLabel("Satu workspace untuk semua armada");
        lblPillTitle.setFont(Theme.bodyBold());
        lblPillTitle.setForeground(Theme.TEXT_WHITE);

        JLabel lblPillSub = new JLabel("Mobil · Motor · Bus");
        lblPillSub.setFont(Theme.caption());
        lblPillSub.setForeground(Theme.TEXT_MUTED);

        pillText.add(lblPillTitle);
        pillText.add(lblPillSub);

        pillContent.add(lblPillNum, BorderLayout.WEST);
        pillContent.add(pillText, BorderLayout.CENTER);
        bottomPill.add(pillContent, BorderLayout.CENTER);

        leftCard.add(leftTop, BorderLayout.NORTH);
        leftCard.add(bottomPill, BorderLayout.SOUTH);
        leftContainer.add(leftCard, BorderLayout.CENTER);

        // ==========================================
        // SISI KANAN: FORM LOGIN OTENTIKASI
        // ==========================================
        JPanel rightContainer = new JPanel(new GridBagLayout());
        rightContainer.setOpaque(false);
        rightContainer.setBorder(new EmptyBorder(40, 50, 40, 80));

        JPanel formBox = new JPanel();
        formBox.setLayout(new BoxLayout(formBox, BoxLayout.Y_AXIS));
        formBox.setOpaque(false);
        formBox.setMaximumSize(new Dimension(440, 700));

        JLabel lblWorkspaceTag = new JLabel("RENTARA / WORKSPACE");
        lblWorkspaceTag.setFont(Theme.micro());
        lblWorkspaceTag.setForeground(Theme.ACCENT_ORANGE);

        JLabel lblWelcome = new JLabel("Selamat datang kembali.");
        lblWelcome.setFont(new Font(Theme.FONT_NAME, Font.BOLD, 32));
        lblWelcome.setForeground(Theme.TEXT_WHITE);

        JLabel lblDesc = new JLabel("Masuk untuk mengelola armada dan transaksi rental.");
        lblDesc.setFont(Theme.bodyRegular());
        lblDesc.setForeground(Theme.TEXT_MUTED);

        formBox.add(lblWorkspaceTag);
        formBox.add(Box.createVerticalStrut(8));
        formBox.add(lblWelcome);
        formBox.add(Box.createVerticalStrut(4));
        formBox.add(lblDesc);
        formBox.add(Box.createVerticalStrut(32));

        // Input 1: Email atau username
        formBox.add(createInputLabel("Email atau username"));
        txtUsername = new UIComponents.ModernTextField("admin@rentara.id");
        txtUsername.setText("admin@rentara.id");
        txtUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        formBox.add(txtUsername);
        formBox.add(Box.createVerticalStrut(16));

        // Input 2: Kata sandi
        formBox.add(createInputLabel("Kata sandi"));
        txtPassword = new UIComponents.ModernPasswordField("••••••••••••");
        txtPassword.setText("admin");
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        formBox.add(txtPassword);
        formBox.add(Box.createVerticalStrut(16));

        // Input 3: Masuk sebagai
        formBox.add(createInputLabel("Masuk sebagai"));
        cmbRole = new JComboBox<>(new String[]{"Admin", "Petugas Rental"});
        cmbRole.setFont(Theme.bodyRegular());
        cmbRole.setBackground(Theme.BG_INPUT);
        cmbRole.setForeground(Theme.TEXT_WHITE);
        cmbRole.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        formBox.add(cmbRole);
        formBox.add(Box.createVerticalStrut(16));

        // Row: Ingat saya & Lupa kata sandi
        JPanel rememberRow = new JPanel(new BorderLayout());
        rememberRow.setOpaque(false);

        chkRemember = new JCheckBox("Ingat saya");
        chkRemember.setOpaque(false);
        chkRemember.setForeground(Theme.TEXT_MUTED);
        chkRemember.setFont(Theme.caption());
        chkRemember.setSelected(true);

        JLabel lblForgot = new JLabel("Lupa kata sandi?");
        lblForgot.setFont(Theme.caption());
        lblForgot.setForeground(Theme.TEXT_DIM);
        lblForgot.setCursor(new Cursor(Cursor.HAND_CURSOR));

        rememberRow.add(chkRemember, BorderLayout.WEST);
        rememberRow.add(lblForgot, BorderLayout.EAST);
        formBox.add(rememberRow);
        formBox.add(Box.createVerticalStrut(24));

        // Tombol Masuk
        UIComponents.ModernButton btnLogin = new UIComponents.ModernButton("Masuk ke workspace →", UIComponents.ModernButton.Type.PRIMARY);
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        btnLogin.addActionListener(e -> doLogin());
        formBox.add(btnLogin);
        formBox.add(Box.createVerticalStrut(12));

        JLabel lblNotice = new JLabel("Akses khusus admin dan petugas rental.");
        lblNotice.setFont(Theme.micro());
        lblNotice.setForeground(Theme.TEXT_DIM);
        lblNotice.setAlignmentX(Component.CENTER_ALIGNMENT);
        formBox.add(lblNotice);
        formBox.add(Box.createVerticalStrut(40));

        // Footer
        JLabel lblHelp = new JLabel("Butuh akses? Hubungi administrator rental.");
        lblHelp.setFont(Theme.caption());
        lblHelp.setForeground(Theme.TEXT_MUTED);

        JLabel lblCopyright = new JLabel("© 2026 Rentara · Rental Management System");
        lblCopyright.setFont(Theme.micro());
        lblCopyright.setForeground(Theme.TEXT_DIM);

        formBox.add(lblHelp);
        formBox.add(Box.createVerticalStrut(4));
        formBox.add(lblCopyright);

        rightContainer.add(formBox);

        add(leftContainer);
        add(rightContainer);
    }

    private JLabel createInputLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.captionBold());
        l.setForeground(Theme.TEXT_DIM);
        l.setBorder(new EmptyBorder(0, 0, 6, 0));
        return l;
    }

    private void doLogin() {
        String u = txtUsername.getText().trim();
        String p = new String(txtPassword.getPassword()).trim();

        if (controller.login(u, p)) {
            mainFrame.showMainWorkspace();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Username atau password salah.\nDefault login: admin@rentara.id / admin",
                    "Gagal Masuk",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
