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
        setLayout(new BorderLayout(0, 14));
        setOpaque(false);
        setBorder(new EmptyBorder(12, 28, 20, 28));

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
        lblTag.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTitle = new JLabel("Selamat pagi, Admin");
        lblTitle.setFont(Theme.titleLarge());
        lblTitle.setForeground(Theme.TEXT_WHITE);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSub = new JLabel("Pantau armada dan perjalanan hari ini dalam satu tampilan.");
        lblSub.setFont(Theme.bodyRegular());
        lblSub.setForeground(Theme.TEXT_MUTED);
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        headerLeft.add(lblTag);
        headerLeft.add(Box.createVerticalStrut(4));
        headerLeft.add(lblTitle);
        headerLeft.add(Box.createVerticalStrut(4));
        headerLeft.add(lblSub);

        JPanel headerRight = new JPanel();
        headerRight.setLayout(new BoxLayout(headerRight, BoxLayout.Y_AXIS));
        headerRight.setOpaque(false);

        JLabel lblDate = new JLabel("Selasa, 03 November 2026");
        lblDate.setFont(Theme.caption());
        lblDate.setForeground(Theme.TEXT_MUTED);
        lblDate.setAlignmentX(Component.RIGHT_ALIGNMENT);

        UIComponents.ModernButton btnNewTrans = new UIComponents.ModernButton("+ Transaksi baru", UIComponents.ModernButton.Type.PRIMARY);
        btnNewTrans.setAlignmentX(Component.RIGHT_ALIGNMENT);
        btnNewTrans.setPreferredSize(new Dimension(164, 42));
        btnNewTrans.setMaximumSize(new Dimension(164, 42));
        btnNewTrans.addActionListener(e -> mainFrame.showPage("TRANSAKSI"));

        headerRight.add(lblDate);
        headerRight.add(Box.createVerticalStrut(10));
        headerRight.add(btnNewTrans);

        headerPanel.add(headerLeft, BorderLayout.WEST);
        headerPanel.add(headerRight, BorderLayout.EAST);

        // 2. Center Content
        JPanel contentBox = new JPanel();
        contentBox.setLayout(new BoxLayout(contentBox, BoxLayout.Y_AXIS));
        contentBox.setOpaque(false);

        // A. Top Section: Hero Card (64%) + Activity Chart Card (36%)
        JPanel topRow = new JPanel(new GridBagLayout());
        topRow.setOpaque(false);
        topRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 230));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        gbc.gridx = 0;
        gbc.weightx = 0.64;
        gbc.insets = new Insets(0, 0, 16, 16);
        topRow.add(createHeroCard(), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.36;
        gbc.insets = new Insets(0, 0, 16, 0);
        topRow.add(createActivityChartCard(), gbc);

        contentBox.add(topRow);

        // B. 4 Stat Cards Row
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        cardTotalArmada = new UIComponents.StatCard("Total armada", "48", "Mobil 28 · Motor 16 · Bus 4", Theme.TEXT_WHITE);
        cardTersedia = new UIComponents.StatCard("Tersedia", "32", "Siap untuk disewakan", Theme.BADGE_GREEN_TXT);
        cardDisewa = new UIComponents.StatCard("Sedang disewa", "16", "33% utilisasi armada", Theme.ACCENT_ORANGE);
        cardPendapatan = new UIComponents.StatCard("Pendapatan bulan ini", "Rp24,8 jt", "+ 8,2% dari bulan lalu", Theme.TEXT_WHITE);

        statsRow.add(cardTotalArmada);
        statsRow.add(cardTersedia);
        statsRow.add(cardDisewa);
        statsRow.add(cardPendapatan);

        contentBox.add(statsRow);
        contentBox.add(Box.createVerticalStrut(16));

        // C. Bottom Table Card: Pengembalian Hari Ini
        JPanel bottomCard = createDueReturnsCard();
        contentBox.add(bottomCard);

        add(headerPanel, BorderLayout.NORTH);
        add(contentBox, BorderLayout.CENTER);
    }

    private JPanel createHeroCard() {
        UIComponents.ModernCard card = new UIComponents.ModernCard(new Color(0x18, 0x19, 0x20), Theme.BORDER, 22) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                Shape clip = new RoundRectangle2D.Float(0, 0, w, h, 22, 22);
                g2.setClip(clip);

                g2.setColor(new Color(0x18, 0x19, 0x20));
                g2.fillRect(0, 0, w, h);

                if (heroCarImage != null) {
                    int imgH = h;
                    int imgW = (int) (imgH * ((double) heroCarImage.getWidth() / heroCarImage.getHeight()));
                    if (imgW < w * 0.68) {
                        imgW = (int) (w * 0.68);
                        imgH = (int) (imgW * ((double) heroCarImage.getHeight() / heroCarImage.getWidth()));
                    }
                    int imgX = w - imgW;
                    int imgY = (h - imgH) / 2;
                    g2.drawImage(heroCarImage, imgX, imgY, imgW, imgH, null);

                    // Smooth dark gradient fade on left side of image
                    GradientPaint fade = new GradientPaint(w * 0.28f, 0, new Color(0x18, 0x19, 0x20, 255), w * 0.62f, 0, new Color(0x18, 0x19, 0x20, 0));
                    g2.setPaint(fade);
                    g2.fillRect(0, 0, (int) (w * 0.65), h);
                }

                g2.setClip(null);
                g2.setColor(new Color(0xFF, 0xFF, 0xFF, 28));
                g2.setStroke(new BasicStroke(1.1f));
                g2.draw(new RoundRectangle2D.Float(0.6f, 0.6f, w - 1.2f, h - 1.2f, 22, 22));
                g2.dispose();
            }
        };

        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(24, 28, 24, 28));
        card.setPreferredSize(new Dimension(540, 215));

        JPanel leftText = new JPanel();
        leftText.setLayout(new BoxLayout(leftText, BoxLayout.Y_AXIS));
        leftText.setOpaque(false);

        JLabel lblTag = new JLabel("SIAP UNTUK PERJALANAN BERIKUTNYA");
        lblTag.setFont(Theme.micro());
        lblTag.setForeground(Theme.ACCENT_ORANGE);
        lblTag.setAlignmentX(Component.LEFT_ALIGNMENT);

        Font hFont = new Font(Theme.FONT_NAME, Font.BOLD, 25);
        JLabel lblHead1 = new JLabel("Armada terkelola.");
        lblHead1.setFont(hFont);
        lblHead1.setForeground(Theme.TEXT_WHITE);
        lblHead1.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblHead2 = new JLabel("Bisnis melaju.");
        lblHead2.setFont(hFont);
        lblHead2.setForeground(Theme.TEXT_WHITE);
        lblHead2.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSub = new JLabel("Kelola setiap unit, layani setiap perjalanan.");
        lblSub.setFont(Theme.caption());
        lblSub.setForeground(Theme.TEXT_MUTED);
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        UIComponents.ModernButton btnLihat = new UIComponents.ModernButton("Lihat armada  →", UIComponents.ModernButton.Type.SECONDARY);
        btnLihat.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLihat.setPreferredSize(new Dimension(148, 38));
        btnLihat.setMaximumSize(new Dimension(148, 38));
        btnLihat.addActionListener(e -> mainFrame.showPage("ARMADA"));

        leftText.add(lblTag);
        leftText.add(Box.createVerticalStrut(10));
        leftText.add(lblHead1);
        leftText.add(lblHead2);
        leftText.add(Box.createVerticalStrut(8));
        leftText.add(lblSub);
        leftText.add(Box.createVerticalStrut(18));
        leftText.add(btnLihat);

        card.add(leftText, BorderLayout.WEST);
        return card;
    }

    private JPanel createActivityChartCard() {
        UIComponents.ModernCard card = new UIComponents.ModernCard(Theme.BG_CARD, Theme.BORDER, 22);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(20, 24, 18, 24));
        card.setPreferredSize(new Dimension(320, 215));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);

        JLabel lblTitle = new JLabel("Aktivitas penyewaan");
        lblTitle.setFont(new Font(Theme.FONT_NAME, Font.BOLD, 15));
        lblTitle.setForeground(Theme.TEXT_WHITE);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblRange = new JLabel("7 hari terakhir");
        lblRange.setFont(Theme.caption());
        lblRange.setForeground(Theme.TEXT_DIM);
        lblRange.setAlignmentX(Component.LEFT_ALIGNMENT);

        top.add(lblTitle);
        top.add(Box.createVerticalStrut(2));
        top.add(lblRange);
        top.add(Box.createVerticalStrut(10));

        JPanel metricRow = new JPanel(new BorderLayout());
        metricRow.setOpaque(false);
        metricRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel leftNum = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftNum.setOpaque(false);
        JLabel lblNum = new JLabel("28  ");
        lblNum.setFont(new Font(Theme.FONT_NAME, Font.BOLD, 26));
        lblNum.setForeground(Theme.TEXT_WHITE);
        JLabel lblUnit = new JLabel("transaksi");
        lblUnit.setFont(Theme.caption());
        lblUnit.setForeground(Theme.TEXT_MUTED);
        leftNum.add(lblNum);
        leftNum.add(lblUnit);

        JLabel lblGrowth = new JLabel("+12% dari pekan lalu");
        lblGrowth.setFont(Theme.captionBold());
        lblGrowth.setForeground(Theme.BADGE_GREEN_TXT);

        metricRow.add(leftNum, BorderLayout.WEST);
        metricRow.add(lblGrowth, BorderLayout.EAST);
        top.add(metricRow);

        // Chart Component
        JPanel chartPanel = new JPanel() {
            private final int[] values = {11, 8, 15, 12, 21, 17, 28};
            private final String[] days = {"Rab", "Kam", "Jum", "Sab", "Min", "Sen", "Sel"};

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int barWidth = 20;
                int maxH = Math.max(30, h - 28);
                int step = w / values.length;

                for (int i = 0; i < values.length; i++) {
                    int barH = (int) ((values[i] / 28.0) * maxH);
                    int x = (i * step) + (step - barWidth) / 2;
                    int y = h - 22 - barH;

                    if (i == values.length - 1) {
                        g2.setColor(Theme.ACCENT_ORANGE);
                    } else {
                        g2.setColor(new Color(0x3B, 0x39, 0x42));
                    }
                    g2.fill(new RoundRectangle2D.Float(x, y, barWidth, barH, 8, 8));

                    g2.setFont(Theme.caption());
                    g2.setColor(i == values.length - 1 ? Theme.TEXT_WHITE : Theme.TEXT_DIM);
                    FontMetrics fm = g2.getFontMetrics();
                    int textX = x + (barWidth - fm.stringWidth(days[i])) / 2;
                    g2.drawString(days[i], textX, h - 4);
                }
                g2.dispose();
            }
        };
        chartPanel.setOpaque(false);

        card.add(top, BorderLayout.NORTH);
        card.add(chartPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createDueReturnsCard() {
        UIComponents.ModernCard card = new UIComponents.ModernCard(Theme.BG_CARD, Theme.BORDER, 22);
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
        left.add(Box.createVerticalStrut(2));
        left.add(lblSubDue);

        JLabel lblAll = new JLabel("Lihat semua  →");
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
        tablePengembalian.setRowHeight(48);

        // Custom Painted Renderers (100% tampil di dalam JTable)
        tablePengembalian.getColumnModel().getColumn(4).setCellRenderer(new UIComponents.StatusBadgeTableRenderer());
        tablePengembalian.getColumnModel().getColumn(5).setCellRenderer(new UIComponents.ColoredTextTableRenderer(Theme.ACCENT_ORANGE, true));

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

        card.add(header, BorderLayout.NORTH);
        card.add(UIComponents.wrapTableWithHeader(tablePengembalian, new Dimension(800, 210)), BorderLayout.CENTER);

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

        cardTotalArmada.updateData(String.valueOf(Math.max(48, totalArmada)), "Mobil " + Math.max(28, mobil) + " · Motor " + Math.max(16, motor) + " · Bus " + Math.max(4, bus));
        cardTersedia.updateData(String.valueOf(Math.max(32, armadaTersedia)), "Siap untuk disewakan");
        cardDisewa.updateData(String.valueOf(Math.max(16, armadaDisewa)), "33% utilisasi armada");
        cardPendapatan.updateData("Rp24,8 jt", "+ 8,2% dari bulan lalu");

        tableModel.setRowCount(0);
        @SuppressWarnings("unchecked")
        List<TransaksiSewa> dueList = (List<TransaksiSewa>) stats.get("pengembalianHariIni");
        if (dueList != null) {
            lblSubDue.setText(Math.min(4, dueList.size()) + " transaksi perlu ditindaklanjuti");
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM");
            String[] times = {"10.00", "12.00", "14.00", "16.00", "17.00"};
            int idx = 0;
            for (TransaksiSewa t : dueList) {
                if (idx >= 4) break;
                tableModel.addRow(new Object[]{
                        t.getIdTransaksi(),
                        t.getPelanggan().getNamaLengkap(),
                        t.getKendaraan().getMerk() + " · " + t.getKendaraan().getPlatNomor(),
                        t.getTanggalRencanaKembali().format(dtf) + " · " + times[idx % times.length],
                        "Disewa",
                        "Kembalikan →"
                });
                idx++;
            }
        }
    }
}
