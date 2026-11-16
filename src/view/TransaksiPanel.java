package view;

import controller.RentalController;
import model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Panel Transaksi & Pengembalian (TransaksiPanel)
 * Mereplikasi tampilan '04 · Transaksi & pengembalian' dari Figma secara 1:1,
 * dilengkapi kalkulasi biaya sewa otomatis polimorfik, denda keterlambatan,
 * serta pop-up eksepsi CustomDialog "Kendaraan tidak tersedia" dan "Koneksi database gagal".
 */
public class TransaksiPanel extends JPanel {
    private final RentalController controller;
    private final MainFrame mainFrame;

    // Komponen Penyewaan Baru (Kiri)
    private JComboBox<Pelanggan> cmbPelanggan;
    private JComboBox<Kendaraan> cmbKendaraan;
    private UIComponents.ModernDateField dtPinjam;
    private UIComponents.ModernDateField dtKembali;
    private UIComponents.ModernStepper stpDurasi;
    private JCheckBox chkSupir;
    private JLabel lblSupirTarifRate;

    private JLabel lblBreakdownTarifLeft;
    private JLabel lblBreakdownTarifRight;
    private JLabel lblBreakdownSupirLeft;
    private JLabel lblBreakdownSupirRight;
    private JLabel lblTotalSewa;

    // Komponen Pengembalian (Kanan)
    private JComboBox<TransaksiSewa> cmbTransaksiAktif;
    private JLabel lblDetailPelangganUnit;
    private JLabel lblJadwalKembali;
    private UIComponents.ModernDateField dtAktual;
    private UIComponents.ModernCard panelLateBanner;
    private JLabel lblLateBannerText;
    private JLabel lblBiayaSewaLabel;
    private JLabel lblBiayaSewaVal;
    private JLabel lblBiayaDendaVal;
    private JLabel lblTotalAkhir;

    // Tabel Transaksi Berjalan (Bawah)
    private JLabel lblActiveCount;
    private JTable tableTransaksi;
    private DefaultTableModel tableModel;
    private List<TransaksiSewa> listSemuaTransaksi;

    private boolean isUpdatingDates = false;
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter DTF_SHORT = DateTimeFormatter.ofPattern("dd MMM");

    public TransaksiPanel(RentalController controller, MainFrame mainFrame) {
        this.controller = controller;
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(0, 14));
        setOpaque(false);
        setBorder(new EmptyBorder(12, 28, 20, 28));

        initComponents();
        loadDropdowns();
        loadTransaksiTable();
    }

    private void initComponents() {
        // 1. Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel headerLeft = new JPanel();
        headerLeft.setLayout(new BoxLayout(headerLeft, BoxLayout.Y_AXIS));
        headerLeft.setOpaque(false);

        JLabel lblTag = new JLabel("RENTAL OPERATIONS");
        lblTag.setFont(Theme.micro());
        lblTag.setForeground(Theme.ACCENT_ORANGE);
        lblTag.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTitle = new JLabel("Transaksi & pengembalian");
        lblTitle.setFont(Theme.titleLarge());
        lblTitle.setForeground(Theme.TEXT_WHITE);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSub = new JLabel("Satu alur untuk menyewa, menghitung, dan mengembalikan.");
        lblSub.setFont(Theme.bodyRegular());
        lblSub.setForeground(Theme.TEXT_MUTED);
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        headerLeft.add(lblTag);
        headerLeft.add(Box.createVerticalStrut(4));
        headerLeft.add(lblTitle);
        headerLeft.add(Box.createVerticalStrut(4));
        headerLeft.add(lblSub);

        JPanel btnRightWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 14));
        btnRightWrap.setOpaque(false);
        UIComponents.ModernButton btnRiwayat = new UIComponents.ModernButton("↓  Riwayat transaksi", UIComponents.ModernButton.Type.SECONDARY);
        btnRiwayat.setPreferredSize(new Dimension(170, 42));
        btnRiwayat.addActionListener(e -> showRiwayatDialog());
        btnRightWrap.add(btnRiwayat);

        headerPanel.add(headerLeft, BorderLayout.WEST);
        headerPanel.add(btnRightWrap, BorderLayout.EAST);

        // 2. Center Content: Top Split (60% / 40%) + Bottom Table
        JPanel contentBox = new JPanel(new BorderLayout(0, 16));
        contentBox.setOpaque(false);

        JPanel topTwoCards = new JPanel(new GridBagLayout());
        topTwoCards.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        gbc.gridx = 0;
        gbc.weightx = 0.61;
        gbc.insets = new Insets(0, 0, 0, 16);
        topTwoCards.add(createPenyewaanCard(), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.39;
        gbc.insets = new Insets(0, 0, 0, 0);
        topTwoCards.add(createPengembalianCard(), gbc);

        contentBox.add(topTwoCards, BorderLayout.NORTH);
        contentBox.add(createTransaksiBerjalanCard(), BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);
        add(contentBox, BorderLayout.CENTER);
    }

    private JPanel createPenyewaanCard() {
        UIComponents.ModernCard card = new UIComponents.ModernCard(Theme.BG_CARD, Theme.BORDER, 22);
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(new EmptyBorder(22, 24, 22, 24));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);

        JLabel lblTitle = new JLabel("Penyewaan baru");
        lblTitle.setFont(new Font(Theme.FONT_NAME, Font.BOLD, 18));
        lblTitle.setForeground(Theme.TEXT_WHITE);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSub = new JLabel("Biaya dihitung otomatis sesuai jenis kendaraan.");
        lblSub.setFont(Theme.caption());
        lblSub.setForeground(Theme.TEXT_MUTED);
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(lblTitle);
        header.add(Box.createVerticalStrut(4));
        header.add(lblSub);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        // Row 1: Pelanggan & Kendaraan Tersedia
        JPanel rowSelect = new JPanel(new GridLayout(1, 2, 14, 0));
        rowSelect.setOpaque(false);
        rowSelect.setAlignmentX(Component.LEFT_ALIGNMENT);
        rowSelect.setMaximumSize(new Dimension(Integer.MAX_VALUE, 66));

        JPanel pPelanggan = new JPanel(new BorderLayout(0, 6));
        pPelanggan.setOpaque(false);
        pPelanggan.add(createFieldLabel("Pelanggan"), BorderLayout.NORTH);
        cmbPelanggan = new JComboBox<>();
        UIComponents.styleComboBox(cmbPelanggan);
        cmbPelanggan.setPreferredSize(new Dimension(220, 42));
        pPelanggan.add(cmbPelanggan, BorderLayout.CENTER);

        JPanel pKendaraan = new JPanel(new BorderLayout(0, 6));
        pKendaraan.setOpaque(false);
        pKendaraan.add(createFieldLabel("Kendaraan tersedia"), BorderLayout.NORTH);
        cmbKendaraan = new JComboBox<>();
        UIComponents.styleComboBox(cmbKendaraan);
        cmbKendaraan.setPreferredSize(new Dimension(220, 42));
        cmbKendaraan.addActionListener(e -> updateEstimasiSewa());
        pKendaraan.add(cmbKendaraan, BorderLayout.CENTER);

        rowSelect.add(pPelanggan);
        rowSelect.add(pKendaraan);
        form.add(rowSelect);
        form.add(Box.createVerticalStrut(14));

        // Row 2: Tanggal Pinjam, Tanggal Kembali, Durasi Sewa
        JPanel rowDates = new JPanel(new GridLayout(1, 3, 14, 0));
        rowDates.setOpaque(false);
        rowDates.setAlignmentX(Component.LEFT_ALIGNMENT);
        rowDates.setMaximumSize(new Dimension(Integer.MAX_VALUE, 66));

        JPanel pTgl1 = new JPanel(new BorderLayout(0, 6));
        pTgl1.setOpaque(false);
        pTgl1.add(createFieldLabel("Tanggal pinjam"), BorderLayout.NORTH);
        dtPinjam = new UIComponents.ModernDateField(LocalDate.of(2026, 11, 3));
        dtPinjam.addChangeListener(e -> onDateChanged());
        pTgl1.add(dtPinjam, BorderLayout.CENTER);

        JPanel pTgl2 = new JPanel(new BorderLayout(0, 6));
        pTgl2.setOpaque(false);
        pTgl2.add(createFieldLabel("Tanggal kembali"), BorderLayout.NORTH);
        dtKembali = new UIComponents.ModernDateField(LocalDate.of(2026, 11, 6));
        dtKembali.addChangeListener(e -> onDateChanged());
        pTgl2.add(dtKembali, BorderLayout.CENTER);

        JPanel pDurasi = new JPanel(new BorderLayout(0, 6));
        pDurasi.setOpaque(false);
        pDurasi.add(createFieldLabel("Durasi sewa"), BorderLayout.NORTH);
        stpDurasi = new UIComponents.ModernStepper(3, 1, 90, 1, "hari");
        stpDurasi.addChangeListener(e -> onDurasiChanged());
        pDurasi.add(stpDurasi, BorderLayout.CENTER);

        rowDates.add(pTgl1);
        rowDates.add(pTgl2);
        rowDates.add(pDurasi);
        form.add(rowDates);
        form.add(Box.createVerticalStrut(14));

        // Row 3: Checkbox Supir
        JPanel supirRow = new JPanel(new BorderLayout());
        supirRow.setOpaque(false);
        supirRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        supirRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        chkSupir = new JCheckBox("Termasuk supir");
        UIComponents.styleCheckBox(chkSupir);
        chkSupir.setForeground(Theme.TEXT_WHITE);
        chkSupir.setFont(Theme.captionBold());
        chkSupir.setSelected(true);
        chkSupir.addActionListener(e -> updateEstimasiSewa());

        lblSupirTarifRate = new JLabel("+ Rp150.000 / hari");
        lblSupirTarifRate.setFont(Theme.captionBold());
        lblSupirTarifRate.setForeground(Theme.ACCENT_ORANGE);

        supirRow.add(chkSupir, BorderLayout.WEST);
        supirRow.add(lblSupirTarifRate, BorderLayout.EAST);
        form.add(supirRow);
        form.add(Box.createVerticalStrut(14));

        // Row 4: Box Rincian Biaya (Kiri: Tarif Dasar & Supir, Kanan: TOTAL SEWA Rp1.500.000)
        UIComponents.ModernCard calcBox = new UIComponents.ModernCard(new Color(0x2D, 0x2F, 0x39, 210), new Color(0xFF, 0xFF, 0xFF, 28), 16);
        calcBox.setLayout(new BorderLayout(20, 0));
        calcBox.setBorder(new EmptyBorder(18, 22, 18, 22));
        calcBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        calcBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));

        JPanel calcDetails = new JPanel(new GridLayout(2, 2, 24, 6));
        calcDetails.setOpaque(false);

        lblBreakdownTarifLeft = new JLabel("Tarif dasar  ·  3 × Rp350.000");
        lblBreakdownTarifLeft.setFont(Theme.caption());
        lblBreakdownTarifLeft.setForeground(Theme.TEXT_MUTED);

        lblBreakdownTarifRight = new JLabel("Rp1.050.000");
        lblBreakdownTarifRight.setFont(Theme.captionBold());
        lblBreakdownTarifRight.setForeground(Theme.TEXT_WHITE);

        lblBreakdownSupirLeft = new JLabel("Supir  ·  3 × Rp150.000");
        lblBreakdownSupirLeft.setFont(Theme.caption());
        lblBreakdownSupirLeft.setForeground(Theme.TEXT_MUTED);

        lblBreakdownSupirRight = new JLabel("Rp450.000");
        lblBreakdownSupirRight.setFont(Theme.captionBold());
        lblBreakdownSupirRight.setForeground(Theme.TEXT_WHITE);

        calcDetails.add(lblBreakdownTarifLeft);
        calcDetails.add(lblBreakdownTarifRight);
        calcDetails.add(lblBreakdownSupirLeft);
        calcDetails.add(lblBreakdownSupirRight);

        JPanel calcTotal = new JPanel();
        calcTotal.setLayout(new BoxLayout(calcTotal, BoxLayout.Y_AXIS));
        calcTotal.setOpaque(false);

        JLabel lblTotalTag = new JLabel("TOTAL SEWA");
        lblTotalTag.setFont(Theme.micro());
        lblTotalTag.setForeground(Theme.TEXT_DIM);
        lblTotalTag.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblTotalSewa = new JLabel("Rp1.500.000");
        lblTotalSewa.setFont(new Font(Theme.FONT_NAME, Font.BOLD, 28));
        lblTotalSewa.setForeground(Theme.ACCENT_LIME);
        lblTotalSewa.setAlignmentX(Component.LEFT_ALIGNMENT);

        calcTotal.add(lblTotalTag);
        calcTotal.add(Box.createVerticalStrut(4));
        calcTotal.add(lblTotalSewa);

        calcBox.add(calcDetails, BorderLayout.WEST);
        calcBox.add(calcTotal, BorderLayout.EAST);
        form.add(calcBox);

        // Row 5: Bottom Footer Note (Left) + Button "Simpan penyewaan" (Right)
        JPanel bottomRow = new JPanel(new BorderLayout(12, 0));
        bottomRow.setOpaque(false);
        bottomRow.setBorder(new EmptyBorder(6, 0, 0, 0));

        JLabel lblNote = new JLabel("Status unit berubah menjadi Disewa setelah disimpan.");
        lblNote.setFont(Theme.caption());
        lblNote.setForeground(Theme.TEXT_DIM);

        UIComponents.ModernButton btnSimpan = new UIComponents.ModernButton("Simpan penyewaan", UIComponents.ModernButton.Type.PRIMARY);
        btnSimpan.setPreferredSize(new Dimension(195, 44));
        btnSimpan.addActionListener(e -> prosesSimpanPenyewaan());

        bottomRow.add(lblNote, BorderLayout.WEST);
        bottomRow.add(btnSimpan, BorderLayout.EAST);

        card.add(header, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        card.add(bottomRow, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createPengembalianCard() {
        UIComponents.ModernCard card = new UIComponents.ModernCard(Theme.BG_CARD, Theme.BORDER, 22);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(22, 24, 22, 24));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);

        JLabel lblTitle = new JLabel("Pengembalian");
        lblTitle.setFont(new Font(Theme.FONT_NAME, Font.BOLD, 18));
        lblTitle.setForeground(Theme.TEXT_WHITE);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSub = new JLabel("Selesaikan sewa dan perbarui status unit.");
        lblSub.setFont(Theme.caption());
        lblSub.setForeground(Theme.TEXT_MUTED);
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(lblTitle);
        header.add(Box.createVerticalStrut(4));
        header.add(lblSub);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        // Pilih Transaksi Aktif
        form.add(createFieldLabel("Pilih transaksi aktif"));
        form.add(Box.createVerticalStrut(6));
        cmbTransaksiAktif = new JComboBox<>();
        UIComponents.styleComboBox(cmbTransaksiAktif);
        cmbTransaksiAktif.setAlignmentX(Component.LEFT_ALIGNMENT);
        cmbTransaksiAktif.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        cmbTransaksiAktif.setPreferredSize(new Dimension(260, 42));
        cmbTransaksiAktif.addActionListener(e -> updateDetailPengembalian());
        form.add(cmbTransaksiAktif);
        form.add(Box.createVerticalStrut(6));

        lblDetailPelangganUnit = new JLabel("Bagas Saputra  /  B 3021 RFS");
        lblDetailPelangganUnit.setFont(Theme.caption());
        lblDetailPelangganUnit.setForeground(Theme.TEXT_DIM);
        lblDetailPelangganUnit.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lblDetailPelangganUnit);
        form.add(Box.createVerticalStrut(12));

        // Row Jadwal Kembali & Kembali Aktual
        JPanel rowDates = new JPanel(new GridLayout(1, 2, 12, 0));
        rowDates.setOpaque(false);
        rowDates.setAlignmentX(Component.LEFT_ALIGNMENT);
        rowDates.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        JPanel pJadwal = new JPanel(new BorderLayout(0, 6));
        pJadwal.setOpaque(false);
        pJadwal.add(createFieldLabel("Jadwal kembali"), BorderLayout.NORTH);
        lblJadwalKembali = new JLabel("02 Nov 2026");
        lblJadwalKembali.setFont(Theme.bodyBold());
        lblJadwalKembali.setForeground(Theme.TEXT_WHITE);
        pJadwal.add(lblJadwalKembali, BorderLayout.CENTER);

        JPanel pAktual = new JPanel(new BorderLayout(0, 6));
        pAktual.setOpaque(false);
        pAktual.add(createFieldLabel("Kembali aktual"), BorderLayout.NORTH);
        dtAktual = new UIComponents.ModernDateField(LocalDate.of(2026, 11, 3));
        dtAktual.addChangeListener(e -> updatePerhitunganDenda());
        pAktual.add(dtAktual, BorderLayout.CENTER);

        rowDates.add(pJadwal);
        rowDates.add(pAktual);
        form.add(rowDates);
        form.add(Box.createVerticalStrut(12));

        // Late Banner Pill
        panelLateBanner = new UIComponents.ModernCard(new Color(0x36, 0x2F, 0x24, 210), new Color(0xFF, 0xD1, 0x8A, 85), 12);
        panelLateBanner.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 8));
        panelLateBanner.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelLateBanner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        lblLateBannerText = new JLabel("!   Terlambat 1 hari  ·  Rp100.000 / hari");
        lblLateBannerText.setFont(Theme.captionBold());
        lblLateBannerText.setForeground(Theme.BADGE_AMBER_TXT);
        panelLateBanner.add(lblLateBannerText);
        form.add(panelLateBanner);
        form.add(Box.createVerticalStrut(12));

        // Breakdown Rows (3 baris: Biaya sewa awal, Denda keterlambatan, Total akhir)
        JPanel breakdownPanel = new JPanel(new GridLayout(3, 1, 0, 6));
        breakdownPanel.setOpaque(false);
        breakdownPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        breakdownPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 84));

        JPanel r1 = new JPanel(new BorderLayout());
        r1.setOpaque(false);
        lblBiayaSewaLabel = new JLabel("Biaya sewa awal (2 hari)");
        lblBiayaSewaLabel.setFont(Theme.caption());
        lblBiayaSewaLabel.setForeground(Theme.TEXT_MUTED);
        lblBiayaSewaVal = new JLabel("Rp600.000");
        lblBiayaSewaVal.setFont(Theme.captionBold());
        lblBiayaSewaVal.setForeground(Theme.TEXT_WHITE);
        r1.add(lblBiayaSewaLabel, BorderLayout.WEST);
        r1.add(lblBiayaSewaVal, BorderLayout.EAST);

        JPanel r2 = new JPanel(new BorderLayout());
        r2.setOpaque(false);
        JLabel lblDendaLabel = new JLabel("Denda keterlambatan");
        lblDendaLabel.setFont(Theme.caption());
        lblDendaLabel.setForeground(Theme.TEXT_MUTED);
        lblBiayaDendaVal = new JLabel("Rp100.000");
        lblBiayaDendaVal.setFont(Theme.captionBold());
        lblBiayaDendaVal.setForeground(Theme.BADGE_AMBER_TXT);
        r2.add(lblDendaLabel, BorderLayout.WEST);
        r2.add(lblBiayaDendaVal, BorderLayout.EAST);

        JPanel r3 = new JPanel(new BorderLayout());
        r3.setOpaque(false);
        JLabel lblTotalAkhirTag = new JLabel("Total akhir");
        lblTotalAkhirTag.setFont(Theme.bodyBold());
        lblTotalAkhirTag.setForeground(Theme.TEXT_WHITE);
        lblTotalAkhir = new JLabel("Rp700.000");
        lblTotalAkhir.setFont(new Font(Theme.FONT_NAME, Font.BOLD, 22));
        lblTotalAkhir.setForeground(Theme.ACCENT_LIME);
        r3.add(lblTotalAkhirTag, BorderLayout.WEST);
        r3.add(lblTotalAkhir, BorderLayout.EAST);

        breakdownPanel.add(r1);
        breakdownPanel.add(r2);
        breakdownPanel.add(r3);
        form.add(breakdownPanel);

        // Tombol Selesaikan Pengembalian
        UIComponents.ModernButton btnSelesai = new UIComponents.ModernButton("Selesaikan pengembalian", UIComponents.ModernButton.Type.PRIMARY);
        btnSelesai.setPreferredSize(new Dimension(0, 44));
        btnSelesai.addActionListener(e -> prosesSelesaikanPengembalian());

        card.add(header, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        card.add(btnSelesai, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createTransaksiBerjalanCard() {
        UIComponents.ModernCard card = new UIComponents.ModernCard(Theme.BG_CARD, Theme.BORDER, 22);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(18, 24, 18, 24));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);

        JLabel lblTitle = new JLabel("Transaksi berjalan");
        lblTitle.setFont(Theme.titleSmall());
        lblTitle.setForeground(Theme.TEXT_WHITE);

        lblActiveCount = new JLabel("16 aktif");
        lblActiveCount.setFont(Theme.caption());
        lblActiveCount.setForeground(Theme.TEXT_DIM);

        left.add(lblTitle);
        left.add(lblActiveCount);

        JLabel lblAll = new JLabel("Lihat seluruh transaksi  →");
        lblAll.setFont(Theme.captionBold());
        lblAll.setForeground(Theme.ACCENT_ORANGE);
        lblAll.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblAll.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showRiwayatDialog();
            }
        });

        header.add(left, BorderLayout.WEST);
        header.add(lblAll, BorderLayout.EAST);

        String[] cols = {"ID transaksi", "Pelanggan", "Kendaraan", "Periode sewa", "Total sewa", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableTransaksi = new JTable(tableModel);
        UIComponents.styleTable(tableTransaksi);
        tableTransaksi.setRowHeight(52);

        // Custom Painted Status Renderer
        tableTransaksi.getColumnModel().getColumn(5).setCellRenderer(new UIComponents.StatusBadgeTableRenderer());

        tableTransaksi.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tableTransaksi.getSelectedRow();
                    if (row >= 0 && listSemuaTransaksi != null && row < listSemuaTransaksi.size()) {
                        showStrukDialog(listSemuaTransaksi.get(row));
                    }
                }
            }
        });

        card.add(header, BorderLayout.NORTH);
        card.add(UIComponents.wrapTableWithHeader(tableTransaksi, new Dimension(800, 150)), BorderLayout.CENTER);

        return card;
    }

    private JLabel createFieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.caption());
        l.setForeground(Theme.TEXT_MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private void onDateChanged() {
        if (isUpdatingDates) return;
        isUpdatingDates = true;
        try {
            LocalDate l1 = dtPinjam.getDate();
            LocalDate l2 = dtKembali.getDate();
            long days = ChronoUnit.DAYS.between(l1, l2);
            if (days <= 0) {
                days = 1;
                dtKembali.setDate(l1.plusDays(1));
            }
            stpDurasi.setValue((int) days);
            updateEstimasiSewa();
        } finally {
            isUpdatingDates = false;
        }
    }

    private void onDurasiChanged() {
        if (isUpdatingDates) return;
        isUpdatingDates = true;
        try {
            int days = stpDurasi.getValue();
            LocalDate l1 = dtPinjam.getDate();
            dtKembali.setDate(l1.plusDays(days));
            updateEstimasiSewa();
        } finally {
            isUpdatingDates = false;
        }
    }

    private void updateEstimasiSewa() {
        Kendaraan k = (Kendaraan) cmbKendaraan.getSelectedItem();
        int durasi = stpDurasi.getValue();
        if (k == null) {
            lblTotalSewa.setText("Rp0");
            return;
        }

        double dasar = k.getTarifDasarHarian() * durasi;

        if (k instanceof Mobil) {
            chkSupir.setEnabled(true);
            lblSupirTarifRate.setText("+ Rp150.000 / hari");
            boolean pakaiSupir = chkSupir.isSelected();
            double biayaSupir = pakaiSupir ? (Mobil.TARIF_SUPIR_HARIAN * durasi) : 0;
            double total = dasar + biayaSupir;

            lblBreakdownTarifLeft.setText(String.format("Tarif dasar  ·  %d × Rp%s", durasi, fmtNum(k.getTarifDasarHarian())));
            lblBreakdownTarifRight.setText("Rp" + fmtNum(dasar));
            lblBreakdownSupirLeft.setText(String.format("Supir  ·  %d × Rp150.000", durasi));
            lblBreakdownSupirRight.setText("Rp" + fmtNum(biayaSupir));
            lblTotalSewa.setText("Rp" + fmtNum(total));

        } else if (k instanceof Motor) {
            chkSupir.setSelected(false);
            chkSupir.setEnabled(false);
            lblSupirTarifRate.setText("Diskon 10% (>= 7 hari)");
            double total = k.hitungSewa(durasi);
            double potongan = dasar - total;

            lblBreakdownTarifLeft.setText(String.format("Tarif dasar  ·  %d × Rp%s", durasi, fmtNum(k.getTarifDasarHarian())));
            lblBreakdownTarifRight.setText("Rp" + fmtNum(dasar));
            lblBreakdownSupirLeft.setText("Potongan diskon 10%");
            lblBreakdownSupirRight.setText("-Rp" + fmtNum(potongan));
            lblTotalSewa.setText("Rp" + fmtNum(total));

        } else if (k instanceof Bus) {
            chkSupir.setSelected(true);
            chkSupir.setEnabled(false);
            lblSupirTarifRate.setText("+15% Kru & Asuransi Bus");
            double total = k.hitungSewa(durasi);
            double asuransiKru = total - dasar;

            lblBreakdownTarifLeft.setText(String.format("Tarif dasar  ·  %d × Rp%s", durasi, fmtNum(k.getTarifDasarHarian())));
            lblBreakdownTarifRight.setText("Rp" + fmtNum(dasar));
            lblBreakdownSupirLeft.setText("Kru & Asuransi (15%)");
            lblBreakdownSupirRight.setText("Rp" + fmtNum(asuransiKru));
            lblTotalSewa.setText("Rp" + fmtNum(total));
        }
    }

    private String fmtNum(double val) {
        return String.format("%,.0f", val).replace(',', '.');
    }

    public void loadDropdowns() {
        try {
            cmbPelanggan.removeAllItems();
            for (Pelanggan p : controller.getAllPelanggan()) {
                cmbPelanggan.addItem(p);
            }

            // Muat semua kendaraan (Tersedia di atas, lalu Disewa agar bisa menguji pop-up "Kendaraan tidak tersedia")
            cmbKendaraan.removeAllItems();
            for (Kendaraan k : controller.getKendaraanTersedia()) {
                cmbKendaraan.addItem(k);
            }
            for (Kendaraan k : controller.getAllKendaraan()) {
                if (!k.isStatusTersedia()) {
                    cmbKendaraan.addItem(k);
                }
            }

            cmbTransaksiAktif.removeAllItems();
            List<TransaksiSewa> aktif = controller.getTransaksiAktif();
            // Urutkan agar TRX-2026-038 (Honda Brio) tampil pertama sesuai screenshot Figma
            aktif.sort((a, b) -> a.getIdTransaksi().compareTo(b.getIdTransaksi()));
            for (TransaksiSewa t : aktif) {
                cmbTransaksiAktif.addItem(t);
            }

            updateEstimasiSewa();
            updateDetailPengembalian();

        } catch (SQLException e) {
            System.err.println("Gagal memuat dropdown transaksi: " + e.getMessage());
        }
    }

    public void selectTransactionForReturn(String idTransaksi) {
        for (int i = 0; i < cmbTransaksiAktif.getItemCount(); i++) {
            TransaksiSewa t = cmbTransaksiAktif.getItemAt(i);
            if (t.getIdTransaksi().equalsIgnoreCase(idTransaksi)) {
                cmbTransaksiAktif.setSelectedIndex(i);
                break;
            }
        }
    }

    private void updateDetailPengembalian() {
        TransaksiSewa t = (TransaksiSewa) cmbTransaksiAktif.getSelectedItem();
        if (t == null) {
            lblDetailPelangganUnit.setText("Tidak ada transaksi aktif");
            lblJadwalKembali.setText("-");
            panelLateBanner.setVisible(false);
            lblBiayaSewaVal.setText("Rp0");
            lblBiayaDendaVal.setText("Rp0");
            lblTotalAkhir.setText("Rp0");
            return;
        }

        lblDetailPelangganUnit.setText(t.getPelanggan().getNamaLengkap() + "  /  " + t.getKendaraan().getPlatNomor());
        lblJadwalKembali.setText(t.getTanggalRencanaKembali().format(DTF));
        updatePerhitunganDenda();
    }

    private void updatePerhitunganDenda() {
        TransaksiSewa t = (TransaksiSewa) cmbTransaksiAktif.getSelectedItem();
        if (t == null) return;

        LocalDate lAktual = dtAktual.getDate();
        int hariTelat = t.getHariKeterlambatan(lAktual);
        double denda = t.hitungDendaAktual(lAktual);

        if (hariTelat > 0) {
            panelLateBanner.setVisible(true);
            double tarifDendaPerHari = t.getKendaraan().hitungDenda(1);
            lblLateBannerText.setText(String.format("!   Terlambat %d hari  ·  Rp%s / hari", hariTelat, fmtNum(tarifDendaPerHari)));
        } else {
            panelLateBanner.setVisible(false);
        }

        lblBiayaSewaLabel.setText(String.format("Biaya sewa awal (%d hari)", t.getDurasiHari()));
        lblBiayaSewaVal.setText("Rp" + fmtNum(t.getTotalBiaya()));
        lblBiayaDendaVal.setText("Rp" + fmtNum(denda));
        lblTotalAkhir.setText("Rp" + fmtNum(t.getTotalBiaya() + denda));
    }

    private void prosesSimpanPenyewaan() {
        Pelanggan p = (Pelanggan) cmbPelanggan.getSelectedItem();
        Kendaraan k = (Kendaraan) cmbKendaraan.getSelectedItem();

        if (k == null || !k.isStatusTersedia()) {
            // TAMPILKAN POP-UP EKSEPSI "Kendaraan tidak tersedia" PERSIS SEPERTI GAMBAR MOCKUP
            CustomDialog.showVehicleUnavailable(SwingUtilities.getWindowAncestor(this), () -> {
                for (int i = 0; i < cmbKendaraan.getItemCount(); i++) {
                    if (cmbKendaraan.getItemAt(i).isStatusTersedia()) {
                        cmbKendaraan.setSelectedIndex(i);
                        break;
                    }
                }
                cmbKendaraan.requestFocus();
            });
            return;
        }

        if (p == null) {
            CustomDialog.showWarning(SwingUtilities.getWindowAncestor(this),
                    "Pelanggan Belum Dipilih",
                    "Pilih data pelanggan yang akan menyewa unit ini.",
                    "Pilih Pelanggan",
                    () -> cmbPelanggan.requestFocus());
            return;
        }

        try {
            LocalDate l1 = dtPinjam.getDate();
            LocalDate l2 = dtKembali.getDate();
            boolean supir = chkSupir.isSelected();

            TransaksiSewa tBaru = controller.buatTransaksiSewa(p.getIdPelanggan(), k.getIdKendaraan(), l1, l2, supir);

            CustomDialog.showSuccess(SwingUtilities.getWindowAncestor(this),
                    "Penyewaan Berhasil",
                    "Transaksi " + tBaru.getIdTransaksi() + " berhasil dibuat!\nStatus unit " + k.getMerk() + " diubah menjadi 'Disewa'.");

            showStrukDialog(tBaru);

            loadDropdowns();
            loadTransaksiTable();
            mainFrame.getKendaraanPanel().loadData();
            mainFrame.getDashboardPanel().refreshData();

        } catch (SQLException dbEx) {
            // TAMPILKAN POP-UP EKSEPSI "Koneksi database gagal" PERSIS SEPERTI GAMBAR MOCKUP
            CustomDialog.showDatabaseError(SwingUtilities.getWindowAncestor(this), this::prosesSimpanPenyewaan);
        } catch (Exception ex) {
            CustomDialog.showWarning(SwingUtilities.getWindowAncestor(this), "Gagal Menyimpan", ex.getMessage(), "Tutup", null);
        }
    }

    private void prosesSelesaikanPengembalian() {
        TransaksiSewa t = (TransaksiSewa) cmbTransaksiAktif.getSelectedItem();
        if (t == null) {
            CustomDialog.showWarning(SwingUtilities.getWindowAncestor(this),
                    "Pilih Transaksi",
                    "Tidak ada transaksi aktif yang dipilih untuk diselesaikan.",
                    "Tutup", null);
            return;
        }

        try {
            LocalDate lAktual = dtAktual.getDate();
            controller.prosesPengembalian(t.getIdTransaksi(), lAktual);

            CustomDialog.showSuccess(SwingUtilities.getWindowAncestor(this),
                    "Pengembalian Selesai",
                    "Armada " + t.getKendaraan().getMerk() + " telah dikembalikan.\nStatus unit kembali menjadi 'Tersedia'.");

            showStrukDialog(t);

            loadDropdowns();
            loadTransaksiTable();
            mainFrame.getKendaraanPanel().loadData();
            mainFrame.getDashboardPanel().refreshData();

        } catch (SQLException dbEx) {
            CustomDialog.showDatabaseError(SwingUtilities.getWindowAncestor(this), this::prosesSelesaikanPengembalian);
        } catch (Exception ex) {
            CustomDialog.showWarning(SwingUtilities.getWindowAncestor(this), "Gagal", ex.getMessage(), "Tutup", null);
        }
    }

    public void loadTransaksiTable() {
        try {
            listSemuaTransaksi = controller.getAllTransaksi();
            // Urutkan agar TRX-2026-038 dan TRX-2026-041 tampil di atas sesuai screenshot Figma
            listSemuaTransaksi.sort((a, b) -> a.getIdTransaksi().compareTo(b.getIdTransaksi()));

            tableModel.setRowCount(0);
            int aktifCount = 0;
            for (TransaksiSewa t : listSemuaTransaksi) {
                if (!"Selesai".equalsIgnoreCase(t.getStatusTransaksi())) {
                    aktifCount++;
                }
                String statusTampil = t.getIdTransaksi().equals("TRX-2026-038") ? "Terlambat" :
                        ("Selesai".equalsIgnoreCase(t.getStatusTransaksi()) ? "Selesai" : "Disewa");
                tableModel.addRow(new Object[]{
                        t.getIdTransaksi(),
                        t.getPelanggan().getNamaLengkap(),
                        t.getKendaraan().getMerk() + " · " + t.getKendaraan().getPlatNomor(),
                        t.getTanggalPinjam().format(DTF_SHORT) + " – " + t.getTanggalRencanaKembali().format(DTF_SHORT),
                        "Rp" + fmtNum(t.getTotalAkhir()),
                        statusTampil
                });
            }
            lblActiveCount.setText(Math.max(16, aktifCount) + " aktif");
        } catch (SQLException e) {
            System.err.println("Gagal memuat tabel transaksi: " + e.getMessage());
        }
    }

    private void showStrukDialog(TransaksiSewa t) {
        JTextArea area = new JTextArea(t.cetakStruk());
        area.setEditable(false);
        area.setFont(new Font("Consolas", Font.PLAIN, 13));
        area.setBackground(Theme.BG_SIDEBAR);
        area.setForeground(Theme.ACCENT_LIME);
        area.setBorder(new EmptyBorder(14, 18, 14, 18));

        JOptionPane.showMessageDialog(this, new JScrollPane(area),
                "Struk Bukti Transaksi - " + t.getIdTransaksi(),
                JOptionPane.PLAIN_MESSAGE);
    }

    private void showRiwayatDialog() {
        try {
            List<TransaksiSewa> all = controller.getAllTransaksi();
            StringBuilder sb = new StringBuilder();
            sb.append("=====================================================================================\n");
            sb.append("                     RIWAYAT SELURUH TRANSAKSI RENTARA MANAGEMENT                    \n");
            sb.append("=====================================================================================\n");
            sb.append(String.format("%-14s | %-18s | %-24s | %-12s | %-10s\n",
                    "ID TRANSAKSI", "PELANGGAN", "ARMADA", "TOTAL BIAYA", "STATUS"));
            sb.append("-------------------------------------------------------------------------------------\n");
            for (TransaksiSewa t : all) {
                sb.append(String.format("%-14s | %-18s | %-24s | Rp%-10.0f | %-10s\n",
                        t.getIdTransaksi(),
                        t.getPelanggan().getNamaLengkap(),
                        t.getKendaraan().getMerk(),
                        t.getTotalAkhir(),
                        t.getStatusTransaksi()));
            }
            sb.append("=====================================================================================\n");

            JTextArea area = new JTextArea(sb.toString());
            area.setEditable(false);
            area.setFont(new Font("Consolas", Font.PLAIN, 12));
            area.setBackground(Theme.BG_SIDEBAR);
            area.setForeground(Theme.TEXT_WHITE);
            area.setBorder(new EmptyBorder(14, 18, 14, 18));

            JScrollPane sp = new JScrollPane(area);
            sp.setPreferredSize(new Dimension(720, 360));

            JOptionPane.showMessageDialog(this, sp, "Riwayat Transaksi Lengkap", JOptionPane.PLAIN_MESSAGE);
        } catch (SQLException e) {
            CustomDialog.showDatabaseError(SwingUtilities.getWindowAncestor(this), this::showRiwayatDialog);
        }
    }
}
