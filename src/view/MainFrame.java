package view;

import controller.RentalController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Jendela Utama Aplikasi (MainFrame)
 * Memuat tata letak menyeluruh: Sidebar navigasi vertikal membulat, Top Header bar,
 * latar belakang ambient glow, dan CardLayout antarmuka utama (Dashboard, Armada, Pelanggan, Transaksi).
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

    // Sidebar Nav Buttons
    private SidebarNavButton btnNavDashboard;
    private SidebarNavButton btnNavArmada;
    private SidebarNavButton btnNavPelanggan;
    private SidebarNavButton btnNavTransaksi;

    public MainFrame() {
        this.controller = new RentalController();

        setTitle("Rentara Management - Sistem Informasi Rental Kendaraan (MVC & DAO)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1440, 1024);
        setMinimumSize(new Dimension(1160, 760));
        setLocationRelativeTo(null);

        initUI();
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
        UIComponents.AmbientBackgroundPanel main = new UIComponents.AmbientBackgroundPanel(new BorderLayout());

        // A. Left Floating Sidebar
        JPanel sidebarWrapper = new JPanel(new BorderLayout());
        sidebarWrapper.setOpaque(false);
        sidebarWrapper.setBorder(new EmptyBorder(16, 16, 16, 8));

        JPanel sidebar = createSidebar();
        sidebarWrapper.add(sidebar, BorderLayout.CENTER);
        main.add(sidebarWrapper, BorderLayout.WEST);

        // B. Right Main Area: Top Bar + Content Panels
        JPanel rightArea = new JPanel(new BorderLayout());
        rightArea.setOpaque(false);

        JPanel topBar = createTopBar();
        rightArea.add(topBar, BorderLayout.NORTH);

        // Panels CardLayout
        workspaceCardLayout = new CardLayout();
        workspaceCardPanel = new JPanel(workspaceCardLayout);
        workspaceCardPanel.setOpaque(false);

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
        JPanel sidebar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                g2.setColor(Theme.BG_SIDEBAR);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 28, 28));

                g2.setColor(new Color(0xFF, 0xFF, 0xFF, 36));
                g2.setStroke(new BasicStroke(1.1f));
                g2.draw(new RoundRectangle2D.Float(0.6f, 0.6f, w - 1.2f, h - 1.2f, 28, 28));

                g2.dispose();
            }
        };
        sidebar.setOpaque(false);
        sidebar.setPreferredSize(new Dimension(68, 0));
        sidebar.setBorder(new EmptyBorder(20, 10, 20, 10));

        // Top: Brand Icon Badge
        JPanel topLogo = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.ACCENT_ORANGE);
                g2.fill(new RoundRectangle2D.Float(4, 0, 40, 40, 13, 13));

                g2.setColor(new Color(0x18, 0x19, 0x1D));
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(15, 22, 18, 15);
                g2.drawLine(18, 15, 30, 15);
                g2.drawLine(30, 15, 33, 22);
                g2.drawRoundRect(13, 21, 22, 8, 3, 3);
                g2.fillOval(17, 26, 3, 3);
                g2.fillOval(28, 26, 3, 3);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(48, 42);
            }
        };
        topLogo.setOpaque(false);
        sidebar.add(topLogo, BorderLayout.NORTH);

        // Center: 4 Nav Buttons
        JPanel navCenter = new JPanel();
        navCenter.setLayout(new BoxLayout(navCenter, BoxLayout.Y_AXIS));
        navCenter.setOpaque(false);
        navCenter.add(Box.createVerticalStrut(36));

        btnNavDashboard = new SidebarNavButton("DASHBOARD", true);
        btnNavArmada = new SidebarNavButton("ARMADA", false);
        btnNavPelanggan = new SidebarNavButton("PELANGGAN", false);
        btnNavTransaksi = new SidebarNavButton("TRANSAKSI", false);

        btnNavDashboard.addActionListener(e -> showPage("DASHBOARD"));
        btnNavArmada.addActionListener(e -> showPage("ARMADA"));
        btnNavPelanggan.addActionListener(e -> showPage("PELANGGAN"));
        btnNavTransaksi.addActionListener(e -> showPage("TRANSAKSI"));

        navCenter.add(btnNavDashboard);
        navCenter.add(Box.createVerticalStrut(12));
        navCenter.add(btnNavArmada);
        navCenter.add(Box.createVerticalStrut(12));
        navCenter.add(btnNavPelanggan);
        navCenter.add(Box.createVerticalStrut(12));
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
        navBottom.add(Box.createVerticalStrut(10));
        navBottom.add(btnLogout);

        sidebar.add(navBottom, BorderLayout.SOUTH);

        return sidebar;
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(18, 28, 8, 28));

        // Left Breadcrumb & Title
        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);
        left.setPreferredSize(new Dimension(320, 48));

        lblBreadcrumb = new JLabel("Workspace  /  Dashboard");
        lblBreadcrumb.setFont(Theme.caption());
        lblBreadcrumb.setForeground(Theme.TEXT_MUTED);

        lblHeaderTitle = new JLabel("Rentara Management");
        lblHeaderTitle.setFont(new Font(Theme.FONT_NAME, Font.BOLD, 18));
        lblHeaderTitle.setForeground(Theme.TEXT_WHITE);

        left.add(lblBreadcrumb);
        left.add(lblHeaderTitle);

        // Right Controls
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        // Search Input with Search Icon & Shortcut Badge
        UIComponents.ModernTextField searchGlobal = new UIComponents.ModernTextField("Cari di workspace...");
        searchGlobal.setSearchStyle(true, true);
        searchGlobal.setPreferredSize(new Dimension(280, 42));
        right.add(searchGlobal);

        // Notification Bell Button
        JPanel bellBtn = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x27, 0x29, 0x32, 220));
                g2.fill(new RoundRectangle2D.Float(0, 0, 42, 42, 12, 12));
                g2.setColor(new Color(0xFF, 0xFF, 0xFF, 28));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, 41, 41, 12, 12));

                // Bell icon
                g2.setColor(Theme.TEXT_WHITE);
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawArc(15, 13, 12, 12, 0, 180);
                g2.drawLine(15, 19, 14, 24);
                g2.drawLine(27, 19, 28, 24);
                g2.drawLine(13, 24, 29, 24);
                g2.drawArc(19, 24, 4, 4, 180, 180);

                // Small orange notification dot
                g2.setColor(Theme.ACCENT_ORANGE);
                g2.fillOval(25, 12, 5, 5);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(42, 42);
            }
        };
        bellBtn.setOpaque(false);
        bellBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bellBtn.setToolTipText("Klik untuk menguji ke-3 Pop-up Eksepsi");
        bellBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showPopupsDemoMenu();
            }
        });
        right.add(bellBtn);

        // Admin Profile Pill (Person Icon + "Admin Rental" / "Petugas operasional")
        UIComponents.ModernCard profilePill = new UIComponents.ModernCard(new Color(0x27, 0x29, 0x32, 220), new Color(0xFF, 0xFF, 0xFF, 28), 14);
        profilePill.setLayout(new BorderLayout(10, 0));
        profilePill.setBorder(new EmptyBorder(6, 12, 6, 18));
        profilePill.setPreferredSize(new Dimension(190, 42));

        JPanel personIcon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.ACCENT_ORANGE);
                g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                g2.drawOval(cx - 4, cy - 7, 8, 8);
                g2.drawArc(cx - 8, cy + 2, 16, 10, 0, 180);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(24, 30);
            }
        };
        personIcon.setOpaque(false);

        JPanel profText = new JPanel(new GridLayout(2, 1, 0, 0));
        profText.setOpaque(false);

        JLabel lblAdminName = new JLabel("Admin Rental");
        lblAdminName.setFont(Theme.captionBold());
        lblAdminName.setForeground(Theme.TEXT_WHITE);

        JLabel lblAdminRole = new JLabel("Petugas operasional");
        lblAdminRole.setFont(new Font(Theme.FONT_NAME, Font.PLAIN, 10));
        lblAdminRole.setForeground(Theme.TEXT_DIM);

        profText.add(lblAdminName);
        profText.add(lblAdminRole);

        profilePill.add(personIcon, BorderLayout.WEST);
        profilePill.add(profText, BorderLayout.CENTER);

        right.add(profilePill);

        topBar.add(left, BorderLayout.WEST);
        topBar.add(right, BorderLayout.EAST);

        return topBar;
    }

    private void showPopupsDemoMenu() {
        String[] options = {
                "1. Koneksi database gagal",
                "2. Periksa kembali NIK",
                "3. Kendaraan tidak tersedia",
                "Tutup"
        };
        int choice = JOptionPane.showOptionDialog(this,
                "Pilih Pop-up Penanganan Eksepsi untuk ditampilkan:",
                "Uji Pop-up Penanganan Eksepsi",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]);

        if (choice == 0) {
            CustomDialog.showDatabaseError(this, () -> {});
        } else if (choice == 1) {
            CustomDialog.showNikError(this, () -> showPage("PELANGGAN"));
        } else if (choice == 2) {
            CustomDialog.showVehicleUnavailable(this, () -> showPage("TRANSAKSI"));
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
                lblBreadcrumb.setText("Workspace  /  Dashboard");
                dashboardPanel.refreshData();
                break;
            case "ARMADA":
                lblBreadcrumb.setText("Workspace  /  Armada kendaraan");
                kendaraanPanel.loadData();
                break;
            case "PELANGGAN":
                lblBreadcrumb.setText("Workspace  /  Data pelanggan");
                pelangganPanel.loadData();
                break;
            case "TRANSAKSI":
                lblBreadcrumb.setText("Workspace  /  Transaksi sewa");
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
        private boolean isActive;
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
                g2.setColor(new Color(0x28, 0x24, 0x24));
                g2.fill(new RoundRectangle2D.Float(4, 0, w - 4, h, 13, 13));
                g2.setColor(new Color(0xFF, 0xAA, 0x72, 75));
                g2.setStroke(new BasicStroke(1.1f));
                g2.draw(new RoundRectangle2D.Float(4.5f, 0.5f, w - 5f, h - 1f, 13, 13));

                // Indikator garis oranye tegak di sisi paling kiri persis seperti di Figma
                g2.setColor(Theme.ACCENT_ORANGE);
                g2.fill(new RoundRectangle2D.Float(0, (h - 20) / 2.0f, 3.2f, 20, 3, 3));

            } else if (isHover) {
                g2.setColor(new Color(0x20, 0x22, 0x29));
                g2.fill(new RoundRectangle2D.Float(4, 0, w - 4, h, 13, 13));
            }

            g2.setColor(isActive ? Theme.ACCENT_ORANGE : (isHover ? Theme.TEXT_WHITE : Theme.TEXT_DIM));
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            int cx = w / 2 + 2;
            int cy = h / 2;

            switch (type) {
                case "DASHBOARD":
                    g2.drawRoundRect(cx - 8, cy - 8, 6, 6, 2, 2);
                    g2.drawRoundRect(cx + 2, cy - 8, 6, 6, 2, 2);
                    g2.drawRoundRect(cx - 8, cy + 2, 6, 6, 2, 2);
                    g2.drawRoundRect(cx + 2, cy + 2, 6, 6, 2, 2);
                    break;
                case "ARMADA":
                    g2.drawLine(cx - 7, cy + 1, cx - 5, cy - 5);
                    g2.drawLine(cx - 5, cy - 5, cx + 5, cy - 5);
                    g2.drawLine(cx + 5, cy - 5, cx + 7, cy + 1);
                    g2.drawRoundRect(cx - 8, cy, 16, 6, 2, 2);
                    g2.fillOval(cx - 5, cy + 4, 3, 3);
                    g2.fillOval(cx + 2, cy + 4, 3, 3);
                    break;
                case "PELANGGAN":
                    // 2 People icon matching Figma
                    g2.drawOval(cx - 6, cy - 7, 6, 6);
                    g2.drawArc(cx - 9, cy + 1, 12, 8, 0, 180);
                    g2.drawOval(cx + 2, cy - 6, 5, 5);
                    g2.drawArc(cx, cy + 2, 10, 7, 0, 180);
                    break;
                case "TRANSAKSI":
                    g2.drawLine(cx - 7, cy - 3, cx + 7, cy - 3);
                    g2.drawLine(cx + 4, cy - 6, cx + 7, cy - 3);
                    g2.drawLine(cx + 4, cy, cx + 7, cy - 3);
                    g2.drawLine(cx + 7, cy + 4, cx - 7, cy + 4);
                    g2.drawLine(cx - 4, cy + 1, cx - 7, cy + 4);
                    g2.drawLine(cx - 4, cy + 7, cx - 7, cy + 4);
                    break;
                case "HELP":
                    g2.drawOval(cx - 8, cy - 8, 16, 16);
                    g2.setFont(new Font(Theme.FONT_NAME, Font.BOLD, 11));
                    g2.drawString("?", cx - 3, cy + 4);
                    break;
                case "LOGOUT":
                    g2.drawLine(cx - 2, cy - 7, cx - 7, cy - 7);
                    g2.drawLine(cx - 7, cy - 7, cx - 7, cy + 7);
                    g2.drawLine(cx - 7, cy + 7, cx - 2, cy + 7);
                    g2.drawLine(cx - 3, cy, cx + 7, cy);
                    g2.drawLine(cx + 4, cy - 3, cx + 7, cy);
                    g2.drawLine(cx + 4, cy + 3, cx + 7, cy);
                    break;
            }

            g2.dispose();
        }
    }
}
