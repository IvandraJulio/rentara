package view;

import controller.RentalController;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Jendela Utama Aplikasi (MainFrame)
 * Memuat tata letak menyeluruh: Sidebar navigasi vertikal membulat, Top Header bar,
 * dan CardLayout antarmuka utama (Dashboard, Armada, Pelanggan, Transaksi).
 */
public class MainFrame extends JFrame {
    private final RentalController controller;

    private CardLayout rootCardLayout;
    private JPanel rootCardPanel;

    private CardLayout workspaceCardLayout;
    private JPanel workspaceCardPanel;

    // Panels
    private LoginPanel loginPanel;
    private DashboardPanel dashboardPanel;
    private KendaraanPanel kendaraanPanel;
    private PelangganPanel pelangganPanel;
    private TransaksiPanel transaksiPanel;

    // Header Controls
    private JLabel lblBreadcrumb;
    private JLabel lblHeaderTitle;
    private BufferedImage adminAvatar;

    // Sidebar Nav Buttons
    private SidebarNavButton btnNavDashboard;
    private SidebarNavButton btnNavArmada;
    private SidebarNavButton btnNavPelanggan;
    private SidebarNavButton btnNavTransaksi;

    public MainFrame() {
        this.controller = new RentalController();

        setTitle("Rentara Management - Sistem Informasi Rental Kendaraan (MVC & DAO)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1440, 960);
        setMinimumSize(new Dimension(1120, 740));
        setLocationRelativeTo(null);

        loadAssets();
        initUI();
    }

    private void loadAssets() {
        try {
            File f = new File("assets/02_img_0.png");
            if (f.exists()) {
                adminAvatar = ImageIO.read(f);
            }
        } catch (Exception e) {
            System.err.println("Gagal memuat avatar admin: " + e.getMessage());
        }
    }

    private void initUI() {
        rootCardLayout = new CardLayout();
        rootCardPanel = new JPanel(rootCardLayout);
        rootCardPanel.setBackground(Theme.BG_DARK);

        // 1. Login Panel View
        loginPanel = new LoginPanel(controller, this);
        rootCardPanel.add(loginPanel, "LOGIN");

        // 2. Main Workspace View
        JPanel workspaceView = createWorkspaceView();
        rootCardPanel.add(workspaceView, "WORKSPACE");

        setContentPane(rootCardPanel);

        // Awal mula tampilkan Login
        rootCardLayout.show(rootCardPanel, "LOGIN");
    }

    private JPanel createWorkspaceView() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Theme.BG_DARK);

        // A. Left Floating Sidebar
        JPanel sidebarWrapper = new JPanel(new BorderLayout());
        sidebarWrapper.setOpaque(false);
        sidebarWrapper.setBorder(new EmptyBorder(16, 16, 16, 8));

        JPanel sidebar = createSidebar();
        sidebarWrapper.add(sidebar, BorderLayout.CENTER);
        main.add(sidebarWrapper, BorderLayout.WEST);

        // B. Right Main Area: Top Bar + Content Panels
        JPanel rightArea = new JPanel(new BorderLayout());
        rightArea.setBackground(Theme.BG_DARK);

        JPanel topBar = createTopBar();
        rightArea.add(topBar, BorderLayout.NORTH);

        // Panels CardLayout
        workspaceCardLayout = new CardLayout();
        workspaceCardPanel = new JPanel(workspaceCardLayout);
        workspaceCardPanel.setBackground(Theme.BG_DARK);

        dashboardPanel = new DashboardPanel(controller, this);
        kendaraanPanel = new KendaraanPanel(controller, this);
        pelangganPanel = new PelangganPanel(controller, this);
        transaksiPanel = new TransaksiPanel(controller, this);

        workspaceCardPanel.add(dashboardPanel, "DASHBOARD");
        workspaceCardPanel.add(kendaraanPanel, "ARMADA");
        workspaceCardPanel.add(pelangganPanel, "PELANGGAN");
        workspaceCardPanel.add(transaksiPanel, "TRANSAKSI");

        rightArea.add(workspaceCardPanel, BorderLayout.CENTER);
        main.add(rightArea, BorderLayout.CENTER);

        return main;
    }

    private JPanel createSidebar() {
        // Sidebar Pill Berbingkai Membulat persis seperti di Figma
        JPanel sidebar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Background gelap sidebar
                g2.setColor(Theme.BG_SIDEBAR);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 28, 28));

                // Border halus
                g2.setColor(new Color(0x28, 0x2A, 0x33));
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(0.6f, 0.6f, w - 1.2f, h - 1.2f, 28, 28));

                g2.dispose();
            }
        };
        sidebar.setOpaque(false);
        sidebar.setPreferredSize(new Dimension(68, 0));
        sidebar.setBorder(new EmptyBorder(18, 10, 18, 10));

        // Top: Brand Icon Badge
        JPanel topLogo = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.ACCENT_ORANGE);
                g2.fill(new RoundRectangle2D.Float(2, 0, 44, 44, 14, 14));

                // Car icon glyph
                g2.setColor(new Color(0x18, 0x19, 0x1D));
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(15, 25, 18, 17);
                g2.drawLine(18, 17, 30, 17);
                g2.drawLine(30, 17, 33, 25);
                g2.drawRoundRect(13, 24, 22, 10, 4, 4);
                g2.fillOval(17, 29, 4, 4);
                g2.fillOval(27, 29, 4, 4);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(48, 44);
            }
        };
        topLogo.setOpaque(false);
        sidebar.add(topLogo, BorderLayout.NORTH);

        // Center: 4 Nav Buttons
        JPanel navCenter = new JPanel();
        navCenter.setLayout(new BoxLayout(navCenter, BoxLayout.Y_AXIS));
        navCenter.setOpaque(false);
        navCenter.add(Box.createVerticalStrut(34));

        btnNavDashboard = new SidebarNavButton("DASHBOARD", true);
        btnNavArmada = new SidebarNavButton("ARMADA", false);
        btnNavPelanggan = new SidebarNavButton("PELANGGAN", false);
        btnNavTransaksi = new SidebarNavButton("TRANSAKSI", false);

        btnNavDashboard.addActionListener(e -> showPage("DASHBOARD"));
        btnNavArmada.addActionListener(e -> showPage("ARMADA"));
        btnNavPelanggan.addActionListener(e -> showPage("PELANGGAN"));
        btnNavTransaksi.addActionListener(e -> showPage("TRANSAKSI"));

        navCenter.add(btnNavDashboard);
        navCenter.add(Box.createVerticalStrut(14));
        navCenter.add(btnNavArmada);
        navCenter.add(Box.createVerticalStrut(14));
        navCenter.add(btnNavPelanggan);
        navCenter.add(Box.createVerticalStrut(14));
        navCenter.add(btnNavTransaksi);

        sidebar.add(navCenter, BorderLayout.CENTER);

        // Bottom: Help & Logout
        JPanel navBottom = new JPanel();
        navBottom.setLayout(new BoxLayout(navBottom, BoxLayout.Y_AXIS));
        navBottom.setOpaque(false);

        SidebarNavButton btnHelp = new SidebarNavButton("HELP", false);
        btnHelp.addActionListener(e -> showPopupsDemoMenu());

        SidebarNavButton btnLogout = new SidebarNavButton("LOGOUT", false);
        btnLogout.addActionListener(e -> {
            CustomDialog.showWarning(this,
                    "Konfirmasi Logout",
                    "Apakah Anda ingin keluar ke layar masuk (login)?",
                    "Keluar",
                    () -> rootCardLayout.show(rootCardPanel, "LOGIN"));
        });

        navBottom.add(btnHelp);
        navBottom.add(Box.createVerticalStrut(12));
        navBottom.add(btnLogout);

        sidebar.add(navBottom, BorderLayout.SOUTH);

        return sidebar;
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Theme.BG_DARK);
        topBar.setBorder(new EmptyBorder(16, 24, 10, 24));

        // Left Breadcrumb & Title
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        lblBreadcrumb = new JLabel("Workspace / Dashboard");
        lblBreadcrumb.setFont(Theme.caption());
        lblBreadcrumb.setForeground(Theme.TEXT_DIM);

        lblHeaderTitle = new JLabel("Rentara Management");
        lblHeaderTitle.setFont(Theme.titleMedium());
        lblHeaderTitle.setForeground(Theme.TEXT_WHITE);

        left.add(lblBreadcrumb);
        left.add(lblHeaderTitle);

        // Center / Right Controls
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        right.setOpaque(false);

        // Search Input
        UIComponents.ModernTextField searchGlobal = new UIComponents.ModernTextField("Cari di workspace...   ⌘ K");
        searchGlobal.setPreferredSize(new Dimension(280, 38));
        right.add(searchGlobal);

        // Notification Bell
        JPanel bellBtn = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, 38, 38, 10, 10));
                g2.setColor(Theme.BORDER);
                g2.draw(new RoundRectangle2D.Float(0.6f, 0.6f, 36.8f, 36.8f, 10, 10));

                // Bell icon
                g2.setColor(Theme.TEXT_MUTED);
                g2.drawOval(15, 12, 8, 10);
                g2.drawLine(13, 22, 25, 22);
                g2.fillOval(17, 24, 4, 3);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(38, 38);
            }
        };
        bellBtn.setOpaque(false);
        bellBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bellBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showPopupsDemoMenu();
            }
        });
        right.add(bellBtn);

        // Admin Profile Pill
        UIComponents.ModernCard profilePill = new UIComponents.ModernCard(Theme.BG_CARD, Theme.BORDER, 14);
        profilePill.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 6));
        profilePill.setBorder(new EmptyBorder(2, 6, 2, 10));

        JLabel lblAvatar = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (adminAvatar != null) {
                    g2.setClip(new RoundRectangle2D.Float(0, 0, 28, 28, 8, 8));
                    g2.drawImage(adminAvatar, 0, 0, 28, 28, null);
                } else {
                    g2.setColor(Theme.ACCENT_ORANGE);
                    g2.fillOval(0, 0, 28, 28);
                    g2.setColor(Theme.BG_DARK);
                    g2.setFont(Theme.captionBold());
                    g2.drawString("A", 10, 18);
                }
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(28, 28);
            }
        };

        JPanel profText = new JPanel();
        profText.setLayout(new BoxLayout(profText, BoxLayout.Y_AXIS));
        profText.setOpaque(false);

        JLabel lblAdminName = new JLabel("Admin Rental");
        lblAdminName.setFont(Theme.captionBold());
        lblAdminName.setForeground(Theme.TEXT_WHITE);

        JLabel lblAdminRole = new JLabel("Petugas operasional");
        lblAdminRole.setFont(Theme.micro());
        lblAdminRole.setForeground(Theme.TEXT_DIM);

        profText.add(lblAdminName);
        profText.add(lblAdminRole);

        profilePill.add(lblAvatar);
        profilePill.add(profText);

        right.add(profilePill);

        topBar.add(left, BorderLayout.WEST);
        topBar.add(right, BorderLayout.EAST);

        return topBar;
    }

    private void showPopupsDemoMenu() {
        String[] options = {
                "1. Pop-up Koneksi database gagal",
                "2. Pop-up Periksa kembali NIK",
                "3. Pop-up Kendaraan tidak tersedia",
                "Batal"
        };
        int choice = JOptionPane.showOptionDialog(this,
                "Pilih contoh Pop-up Eksepsi yang ingin diuji:",
                "Demonstrasi Pop-up Eksepsi",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]);

        if (choice == 0) {
            CustomDialog.showDatabaseError(this, () -> System.out.println("Retry DB"));
        } else if (choice == 1) {
            CustomDialog.showNikError(this, () -> {
                showPage("PELANGGAN");
            });
        } else if (choice == 2) {
            CustomDialog.showVehicleUnavailable(this, () -> {
                showPage("TRANSAKSI");
            });
        }
    }

    public void showMainWorkspace() {
        rootCardLayout.show(rootCardPanel, "WORKSPACE");
        showPage("DASHBOARD");
    }

    public void showPage(String page) {
        btnNavDashboard.setActive("DASHBOARD".equalsIgnoreCase(page));
        btnNavArmada.setActive("ARMADA".equalsIgnoreCase(page));
        btnNavPelanggan.setActive("PELANGGAN".equalsIgnoreCase(page));
        btnNavTransaksi.setActive("TRANSAKSI".equalsIgnoreCase(page));

        workspaceCardLayout.show(workspaceCardPanel, page);

        switch (page) {
            case "DASHBOARD":
                lblBreadcrumb.setText("Workspace / Dashboard");
                dashboardPanel.refreshData();
                break;
            case "ARMADA":
                lblBreadcrumb.setText("Workspace / Armada kendaraan");
                kendaraanPanel.loadData();
                break;
            case "PELANGGAN":
                lblBreadcrumb.setText("Workspace / Data pelanggan");
                pelangganPanel.loadData();
                break;
            case "TRANSAKSI":
                lblBreadcrumb.setText("Workspace / Transaksi sewa");
                transaksiPanel.loadDropdowns();
                transaksiPanel.loadTransaksiTable();
                break;
        }
    }

    public DashboardPanel getDashboardPanel() {
        return dashboardPanel;
    }

    public KendaraanPanel getKendaraanPanel() {
        return kendaraanPanel;
    }

    public PelangganPanel getPelangganPanel() {
        return pelangganPanel;
    }

    public TransaksiPanel getTransaksiPanel() {
        return transaksiPanel;
    }

    /**
     * Tombol Ikon Navigasi Sidebar Khusus dengan Bar Indikator Oranye Aktif di Tepi Kiri
     */
    private static class SidebarNavButton extends JButton {
        private final String type;
        private boolean isActive = false;
        private boolean isHover = false;

        public SidebarNavButton(String type, boolean active) {
            this.type = type;
            this.isActive = active;
            setPreferredSize(new Dimension(48, 44));
            setMaximumSize(new Dimension(48, 44));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHover = false;
                    repaint();
                }
            });
        }

        public void setActive(boolean active) {
            this.isActive = active;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            if (isActive) {
                // Background tombol aktif
                g2.setColor(new Color(0x28, 0x24, 0x24));
                g2.fill(new RoundRectangle2D.Float(6, 0, w - 8, h, 12, 12));
                g2.setColor(new Color(0x4A, 0x36, 0x32));
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(6.6f, 0.6f, w - 9.2f, h - 1.2f, 12, 12));

                // Indikator garis oranye tegak di sisi paling kiri persis seperti di Figma
                g2.setColor(Theme.ACCENT_ORANGE);
                g2.fill(new RoundRectangle2D.Float(0, (h - 22) / 2.0f, 3, 22, 2, 2));

            } else if (isHover) {
                g2.setColor(new Color(0x20, 0x22, 0x29));
                g2.fill(new RoundRectangle2D.Float(6, 0, w - 8, h, 12, 12));
            }

            // Draw Icon
            g2.setColor(isActive ? Theme.ACCENT_ORANGE : (isHover ? Theme.TEXT_WHITE : Theme.TEXT_DIM));
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            int cx = w / 2 + 3;
            int cy = h / 2;

            switch (type) {
                case "DASHBOARD":
                    // 4 squares icon
                    g2.drawRoundRect(cx - 9, cy - 9, 7, 7, 2, 2);
                    g2.drawRoundRect(cx + 2, cy - 9, 7, 7, 2, 2);
                    g2.drawRoundRect(cx - 9, cy + 2, 7, 7, 2, 2);
                    g2.drawRoundRect(cx + 2, cy + 2, 7, 7, 2, 2);
                    break;
                case "ARMADA":
                    // Car icon
                    g2.drawLine(cx - 7, cy + 3, cx - 4, cy - 5);
                    g2.drawLine(cx - 4, cy - 5, cx + 4, cy - 5);
                    g2.drawLine(cx + 4, cy - 5, cx + 7, cy + 3);
                    g2.drawRoundRect(cx - 9, cy + 2, 18, 7, 3, 3);
                    g2.fillOval(cx - 6, cy + 6, 3, 3);
                    g2.fillOval(cx + 3, cy + 6, 3, 3);
                    break;
                case "PELANGGAN":
                    // Users icon
                    g2.drawOval(cx - 4, cy - 8, 8, 8);
                    g2.drawArc(cx - 8, cy + 2, 16, 10, 0, 180);
                    break;
                case "TRANSAKSI":
                    // Transfer / Opposite Arrows icon
                    g2.drawLine(cx - 7, cy - 3, cx + 7, cy - 3);
                    g2.drawLine(cx + 3, cy - 7, cx + 7, cy - 3);
                    g2.drawLine(cx + 3, cy + 1, cx + 7, cy - 3);

                    g2.drawLine(cx + 7, cy + 4, cx - 7, cy + 4);
                    g2.drawLine(cx - 3, cy, cx - 7, cy + 4);
                    g2.drawLine(cx - 3, cy + 8, cx - 7, cy + 4);
                    break;
                case "HELP":
                    // Question mark (?)
                    g2.setFont(Theme.bodyBold());
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString("?", cx - fm.stringWidth("?") / 2, cy + fm.getAscent() / 2 - 2);
                    break;
                case "LOGOUT":
                    // Exit bracket with arrow
                    g2.drawPolyline(new int[]{cx - 2, cx - 7, cx - 7, cx - 2}, new int[]{cy - 8, cy - 8, cy + 8, cy + 8}, 4);
                    g2.drawLine(cx - 3, cy, cx + 8, cy);
                    g2.drawLine(cx + 4, cy - 4, cx + 8, cy);
                    g2.drawLine(cx + 4, cy + 4, cx + 8, cy);
                    break;
            }

            g2.dispose();
        }
    }
}
