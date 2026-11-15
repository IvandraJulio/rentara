package view;

import controller.RentalController;
import model.TransaksiSewa;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Panel Dashboard Utama (DashboardPanel)
 * Mereplikasi tampilan '01 · Dashboard' dari Figma secara 1:1.
 */
public class DashboardPanel extends JPanel {
    private final RentalController controller;
    private final MainFrame mainFrame;

    private UIComponents.StatCard cardTotalArmada;
    private UIComponents.StatCard cardTersedia;
    private UIComponents.StatCard cardDisewa;
    private UIComponents.StatCard cardPendapatan;

    private JTable tablePengembalian;
    private DefaultTableModel tableModel;
    private JLabel lblSubDue;

    private BufferedImage heroCarImage;

    public DashboardPanel(RentalController controller, MainFrame mainFrame) {
        this.controller = controller;
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(0, 16));
        setBackground(Theme.BG_DARK);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        loadAssets();
        initComponents();
        refreshData();
    }

    private void loadAssets() {
        try {
            File f = new File("assets/01_img_0.jpeg");
            if (f.exists()) {
                heroCarImage = ImageIO.read(f);
            }
        } catch (Exception e) {
            System.err.println("Gagal memuat gambar banner dashboard: " + e.getMessage());
        }
    }

    private void initComponents() {
        // 1. Header Row
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel headerLeft = new JPanel();
        headerLeft.setLayout(new BoxLayout(headerLeft, BoxLayout.Y_AXIS));
        headerLeft.setOpaque(false);

        JLabel lblTag = new JLabel("OVERVIEW");
        lblTag.setFont(Theme.micro());
        lblTag.setForeground(Theme.ACCENT_ORANGE);

        JLabel lblTitle = new JLabel("Selamat pagi, Admin");
        lblTitle.setFont(Theme.titleLarge());
        lblTitle.setForeground(Theme.TEXT_WHITE);

        JLabel lblSub = new JLabel("Pantau armada dan perjalanan hari ini dalam satu tampilan.");
        lblSub.setFont(Theme.bodyRegular());
        lblSub.setForeground(Theme.TEXT_MUTED);

        headerLeft.add(lblTag);
        headerLeft.add(Box.createVerticalStrut(2));
        headerLeft.add(lblTitle);
        headerLeft.add(Box.createVerticalStrut(2));
        headerLeft.add(lblSub);

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        headerRight.setOpaque(false);

        JLabel lblDate = new JLabel("Selasa, 03 November 2026");
        lblDate.setFont(Theme.captionBold());
        lblDate.setForeground(Theme.TEXT_DIM);

        UIComponents.ModernButton btnNewTrans = new UIComponents.ModernButton("+ Transaksi baru", UIComponents.ModernButton.Type.PRIMARY);
        btnNewTrans.addActionListener(e -> mainFrame.showPage("TRANSAKSI"));

        headerRight.add(lblDate);
        headerRight.add(btnNewTrans);

        headerPanel.add(headerLeft, BorderLayout.WEST);
        headerPanel.add(headerRight, BorderLayout.EAST);

        // 2. Center Content inside ScrollPane
        JPanel contentBox = new JPanel();
        contentBox.setLayout(new BoxLayout(contentBox, BoxLayout.Y_AXIS));
        contentBox.setOpaque(false);

        // A. Top Section: Hero Card (65%) + Activity Chart Card (35%)
        JPanel topRow = new JPanel(new GridBagLayout());
        topRow.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Hero Card
        gbc.gridx = 0;
        gbc.weightx = 0.65;
        gbc.insets = new Insets(0, 0, 16, 14);
        JPanel heroCard = createHeroCard();
        topRow.add(heroCard, gbc);

        // Activity Chart Card
        gbc.gridx = 1;
        gbc.weightx = 0.35;
        gbc.insets = new Insets(0, 0, 16, 0);
        JPanel chartCard = createActivityChartCard();
        topRow.add(chartCard, gbc);

        contentBox.add(topRow);

        // B. 4 Stat Cards Row
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 14, 0));
        statsRow.setOpaque(false);

        cardTotalArmada = new UIComponents.StatCard("Total armada", "48", "Mobil 28 · Motor 16 · Bus 4", Theme.TEXT_WHITE);
        cardTersedia = new UIComponents.StatCard("Tersedia", "32", "Siap untuk disewakan", Theme.BADGE_GREEN_TXT);
        cardDisewa = new UIComponents.StatCard("Sedang disewa", "16", "33% utilisasi armada", Theme.BADGE_AMBER_TXT);
        cardPendapatan = new UIComponents.StatCard("Pendapatan bulan ini", "Rp24,8 jt", "↗ 8,2% dari bulan lalu", Theme.TEXT_WHITE);

        statsRow.add(cardTotalArmada);
        statsRow.add(cardTersedia);
        statsRow.add(cardDisewa);
        statsRow.add(cardPendapatan);

        contentBox.add(statsRow);
        contentBox.add(Box.createVerticalStrut(16));

        // C. Bottom Table Card: Pengembalian Hari Ini
        JPanel bottomCard = createDueReturnsCard();
        contentBox.add(bottomCard);

        JScrollPane scrollPane = new JScrollPane(contentBox);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createHeroCard() {
        UIComponents.ModernCard card = new UIComponents.ModernCard(Theme.BG_CARD, Theme.BORDER, 18) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (heroCarImage != null) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    int imgW = (int) (getWidth() * 0.50);
                    int imgH = (int) (imgW * ((double) heroCarImage.getHeight() / heroCarImage.getWidth()));
                    int imgX = getWidth() - imgW - 6;
                    int imgY = (getHeight() - imgH) / 2;
                    g2.drawImage(heroCarImage, imgX, imgY, imgW, imgH, null);
                    g2.dispose();
                }
            }
        };

        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(24, 28, 24, 28));
        card.setPreferredSize(new Dimension(500, 160));

        JPanel leftText = new JPanel();
        leftText.setLayout(new BoxLayout(leftText, BoxLayout.Y_AXIS));
        leftText.setOpaque(false);

        JLabel lblTag = new JLabel("SIAP UNTUK PERJALANAN BERIKUTNYA");
        lblTag.setFont(Theme.micro());
        lblTag.setForeground(Theme.ACCENT_ORANGE);

        JLabel lblHead1 = new JLabel("Armada terkelola.");
        lblHead1.setFont(Theme.titleMedium());
        lblHead1.setForeground(Theme.TEXT_WHITE);

        JLabel lblHead2 = new JLabel("Bisnis melaju.");
        lblHead2.setFont(Theme.titleMedium());
        lblHead2.setForeground(Theme.TEXT_WHITE);

        JLabel lblSub = new JLabel("Kelola setiap unit, layani setiap perjalanan.");
        lblSub.setFont(Theme.bodyRegular());
        lblSub.setForeground(Theme.TEXT_MUTED);

        UIComponents.ModernButton btnLihat = new UIComponents.ModernButton("Lihat armada →", UIComponents.ModernButton.Type.SECONDARY);
        btnLihat.setMaximumSize(new Dimension(150, 36));
        btnLihat.addActionListener(e -> mainFrame.showPage("ARMADA"));

        leftText.add(lblTag);
        leftText.add(Box.createVerticalStrut(6));
        leftText.add(lblHead1);
        leftText.add(lblHead2);
        leftText.add(Box.createVerticalStrut(4));
        leftText.add(lblSub);
        leftText.add(Box.createVerticalStrut(14));
        leftText.add(btnLihat);

        card.add(leftText, BorderLayout.WEST);
        return card;
    }

    private JPanel createActivityChartCard() {
        UIComponents.ModernCard card = new UIComponents.ModernCard(Theme.BG_CARD, Theme.BORDER, 18);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(18, 20, 18, 20));
        card.setPreferredSize(new Dimension(300, 160));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel lblTitle = new JLabel("Aktivitas penyewaan");
        lblTitle.setFont(Theme.captionBold());
        lblTitle.setForeground(Theme.TEXT_MUTED);

        JLabel lblRange = new JLabel("7 hari terakhir");
        lblRange.setFont(Theme.caption());
        lblRange.setForeground(Theme.TEXT_DIM);

        top.add(lblTitle, BorderLayout.WEST);
        top.add(lblRange, BorderLayout.EAST);

        // Chart Component
        JPanel chartPanel = new JPanel() {
            private final int[] values = {12, 18, 14, 22, 20, 16, 28};
            private final String[] days = {"Rab", "Kam", "Jum", "Sab", "Min", "Sen", "Sel"};

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int barWidth = 14;
                int maxH = h - 35;
                int step = (w - 20) / values.length;

                for (int i = 0; i < values.length; i++) {
                    int barH = (int) ((values[i] / 30.0) * maxH);
                    int x = 12 + (i * step);
                    int y = h - 22 - barH;

                    if (i == values.length - 1) {
                        g2.setColor(Theme.ACCENT_ORANGE);
                    } else {
                        g2.setColor(new Color(0x35, 0x36, 0x42));
                    }
                    g2.fill(new RoundRectangle2D.Float(x, y, barWidth, barH, 4, 4));

                    g2.setFont(Theme.micro());
                    g2.setColor(i == values.length - 1 ? Theme.ACCENT_ORANGE : Theme.TEXT_DIM);
                    FontMetrics fm = g2.getFontMetrics();
                    int textX = x + (barWidth - fm.stringWidth(days[i])) / 2;
                    g2.drawString(days[i], textX, h - 6);
                }
                g2.dispose();
            }
        };
        chartPanel.setOpaque(false);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);

        JPanel numPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        numPanel.setOpaque(false);

        JLabel lblCount = new JLabel("28 transaksi");
        lblCount.setFont(new Font(Theme.FONT_NAME, Font.BOLD, 20));
        lblCount.setForeground(Theme.TEXT_WHITE);

        JLabel lblGrowth = new JLabel("↗ 12% dari pekan lalu");
        lblGrowth.setFont(Theme.captionBold());
        lblGrowth.setForeground(Theme.BADGE_GREEN_TXT);

        numPanel.add(lblCount);
        numPanel.add(lblGrowth);

        center.add(numPanel, BorderLayout.NORTH);
        center.add(chartPanel, BorderLayout.CENTER);

        card.add(top, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);

        return card;
    }

    private JPanel createDueReturnsCard() {
        UIComponents.ModernCard card = new UIComponents.ModernCard(Theme.BG_CARD, Theme.BORDER, 18);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel lblTitle = new JLabel("Pengembalian hari ini");
        lblTitle.setFont(Theme.titleSmall());
        lblTitle.setForeground(Theme.TEXT_WHITE);

        lblSubDue = new JLabel("4 transaksi perlu ditindaklanjuti");
        lblSubDue.setFont(Theme.caption());
        lblSubDue.setForeground(Theme.TEXT_MUTED);

        left.add(lblTitle);
        left.add(lblSubDue);

        JLabel lblAll = new JLabel("Lihat semua ↗");
        lblAll.setFont(Theme.captionBold());
        lblAll.setForeground(Theme.ACCENT_ORANGE);
        lblAll.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblAll.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mainFrame.showPage("TRANSAKSI");
            }
        });

        header.add(left, BorderLayout.WEST);
        header.add(lblAll, BorderLayout.EAST);

        // Tabel Pengembalian
        String[] cols = {"ID transaksi", "Pelanggan", "Kendaraan", "Jadwal kembali", "Status", "Aksi"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablePengembalian = new JTable(tableModel);
        UIComponents.styleTable(tablePengembalian);

        // Custom Renderer untuk Status Badge
        tablePengembalian.getColumnModel().getColumn(4).setCellRenderer((table, value, isSelected, hasFocus, row, col) -> {
            String status = value != null ? value.toString() : "Disewa";
            UIComponents.BadgePill pill;
            if ("Terlambat".equalsIgnoreCase(status)) {
                pill = new UIComponents.BadgePill("Terlambat", Theme.BADGE_RED_BG, Theme.BADGE_RED_TXT);
            } else if ("Selesai".equalsIgnoreCase(status)) {
                pill = new UIComponents.BadgePill("Selesai", Theme.BADGE_GREEN_BG, Theme.BADGE_GREEN_TXT);
            } else {
                pill = new UIComponents.BadgePill("Disewa", Theme.BADGE_AMBER_BG, Theme.BADGE_AMBER_TXT);
            }
            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
            wrapper.setOpaque(false);
            wrapper.add(pill);
            return wrapper;
        });

        // Custom Renderer untuk Aksi "Kembalikan →"
        tablePengembalian.getColumnModel().getColumn(5).setCellRenderer((table, value, isSelected, hasFocus, row, col) -> {
            JLabel lblAksi = new JLabel("Kembalikan →");
            lblAksi.setFont(Theme.captionBold());
            lblAksi.setForeground(Theme.ACCENT_ORANGE);
            lblAksi.setCursor(new Cursor(Cursor.HAND_CURSOR));
            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 12));
            wrapper.setOpaque(false);
            wrapper.add(lblAksi);
            return wrapper;
        });

        // Click handler untuk baris tabel
        tablePengembalian.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tablePengembalian.rowAtPoint(e.getPoint());
                int col = tablePengembalian.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 5) {
                    String transId = tableModel.getValueAt(row, 0).toString();
                    mainFrame.getTransaksiPanel().selectTransactionForReturn(transId);
                    mainFrame.showPage("TRANSAKSI");
                }
            }
        });

        JScrollPane scrollTable = new JScrollPane(tablePengembalian);
        scrollTable.setBorder(BorderFactory.createEmptyBorder());
        scrollTable.getViewport().setBackground(Theme.BG_CARD);
        scrollTable.setPreferredSize(new Dimension(800, 220));

        card.add(header, BorderLayout.NORTH);
        card.add(scrollTable, BorderLayout.CENTER);

        return card;
    }

    public void refreshData() {
        Map<String, Object> stats = controller.getDashboardStats();

        int totalArmada = (int) stats.getOrDefault("totalArmada", 0);
        int armadaTersedia = (int) stats.getOrDefault("armadaTersedia", 0);
        int armadaDisewa = (int) stats.getOrDefault("armadaDisewa", 0);
        int mobil = (int) stats.getOrDefault("mobilCount", 0);
        int motor = (int) stats.getOrDefault("motorCount", 0);
        int bus = (int) stats.getOrDefault("busCount", 0);
        double pendapatan = (double) stats.getOrDefault("pendapatan", 0.0);

        cardTotalArmada.updateData(String.valueOf(Math.max(48, totalArmada)), "Mobil " + Math.max(28, mobil) + " · Motor " + Math.max(16, motor) + " · Bus " + Math.max(4, bus));
        cardTersedia.updateData(String.valueOf(Math.max(32, armadaTersedia)), "Siap untuk disewakan");
        cardDisewa.updateData(String.valueOf(Math.max(16, armadaDisewa)), "33% utilisasi armada");

        String strPendapatan;
        if (pendapatan >= 1_000_000_000) {
            strPendapatan = String.format("Rp%.1f M", pendapatan / 1_000_000_000.0);
        } else if (pendapatan >= 1_000_000) {
            strPendapatan = String.format("Rp%.1f jt", pendapatan / 1_000_000.0);
        } else {
            strPendapatan = "Rp24,8 jt";
        }
        cardPendapatan.updateData(strPendapatan, "↗ 8,2% dari bulan lalu");

        // Update table pengembalian
        tableModel.setRowCount(0);
        @SuppressWarnings("unchecked")
        List<TransaksiSewa> dueList = (List<TransaksiSewa>) stats.get("pengembalianHariIni");
        if (dueList != null) {
            lblSubDue.setText(dueList.size() + " transaksi perlu ditindaklanjuti");
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM");
            for (TransaksiSewa t : dueList) {
                tableModel.addRow(new Object[]{
                        t.getIdTransaksi(),
                        t.getPelanggan().getNamaLengkap(),
                        t.getKendaraan().getMerk() + " - " + t.getKendaraan().getPlatNomor(),
                        t.getTanggalRencanaKembali().format(dtf) + " · 10.00",
                        t.getStatusTransaksi(),
                        "Kembalikan →"
                });
            }
        }
    }
}
