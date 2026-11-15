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
import java.util.ArrayList;
import java.util.List;

/**
 * Panel Transaksi & Pengembalian (TransaksiPanel)
 * Mengintegrasikan kalkulasi biaya sewa otomatis dan perhitungan denda polimorfik,
 * serta pembaruan status armada secara real-time.
 */
public class TransaksiPanel extends JPanel {
    private final RentalController controller;
    private final MainFrame mainFrame;

    // Komponen Penyewaan Baru (Kiri)
    private JComboBox<Pelanggan> cmbPelanggan;
    private JComboBox<Kendaraan> cmbKendaraan;
    private JSpinner spnTglPinjam;
    private JSpinner spnTglKembali;
    private JSpinner spnDurasi;
    private JCheckBox chkSupir;

    private JLabel lblBreakdownTarif;
    private JLabel lblBreakdownTambahan;
    private JLabel lblTotalSewa;

    // Komponen Pengembalian (Kanan)
    private JComboBox<TransaksiSewa> cmbTransaksiAktif;
    private JLabel lblDetailPelangganUnit;
    private JLabel lblJadwalKembali;
    private JSpinner spnTglAktual;
    private JPanel panelLateBanner;
    private JLabel lblLateBannerText;
    private JLabel lblBiayaSewaAwal;
    private JLabel lblBiayaDenda;
    private JLabel lblTotalAkhir;

    // Tabel Transaksi Berjalan (Bawah)
    private JLabel lblActiveCount;
    private JTable tableTransaksi;
    private DefaultTableModel tableModel;
    private List<TransaksiSewa> listSemuaTransaksi;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter DTF_SHORT = DateTimeFormatter.ofPattern("dd MMM");

    public TransaksiPanel(RentalController controller, MainFrame mainFrame) {
        this.controller = controller;
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(0, 16));
        setBackground(Theme.BG_DARK);
        setBorder(new EmptyBorder(24, 28, 24, 28));

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

        JLabel lblTitle = new JLabel("Transaksi & pengembalian");
        lblTitle.setFont(Theme.titleLarge());
        lblTitle.setForeground(Theme.TEXT_WHITE);

        JLabel lblSub = new JLabel("Satu alur untuk menyewa, menghitung, dan mengembalikan.");
        lblSub.setFont(Theme.bodyRegular());
        lblSub.setForeground(Theme.TEXT_MUTED);

        headerLeft.add(lblTag);
        headerLeft.add(Box.createVerticalStrut(2));
        headerLeft.add(lblTitle);
        headerLeft.add(Box.createVerticalStrut(2));
        headerLeft.add(lblSub);

        UIComponents.ModernButton btnRiwayat = new UIComponents.ModernButton("↓ Riwayat transaksi", UIComponents.ModernButton.Type.SECONDARY);
        btnRiwayat.addActionListener(e -> showRiwayatDialog());

        headerPanel.add(headerLeft, BorderLayout.WEST);
        headerPanel.add(btnRiwayat, BorderLayout.EAST);

        // 2. Center Content: Scrollable
        JPanel contentBox = new JPanel();
        contentBox.setLayout(new BoxLayout(contentBox, BoxLayout.Y_AXIS));
        contentBox.setOpaque(false);

        // A. Top Row: Two side-by-side cards (Penyewaan Baru & Pengembalian)
        JPanel topTwoCards = new JPanel(new GridLayout(1, 2, 16, 0));
        topTwoCards.setOpaque(false);

        JPanel cardPenyewaan = createPenyewaanCard();
        JPanel cardPengembalian = createPengembalianCard();

        topTwoCards.add(cardPenyewaan);
        topTwoCards.add(cardPengembalian);

        contentBox.add(topTwoCards);
        contentBox.add(Box.createVerticalStrut(16));

        // B. Bottom Card: Transaksi Berjalan Table
        JPanel cardTable = createTransaksiBerjalanCard();
        contentBox.add(cardTable);

        JScrollPane scroll = new JScrollPane(contentBox);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(headerPanel, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel createPenyewaanCard() {
        UIComponents.ModernCard card = new UIComponents.ModernCard(Theme.BG_CARD, Theme.BORDER, 18);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 22, 20, 22));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);

        JLabel lblTitle = new JLabel("Penyewaan baru");
        lblTitle.setFont(Theme.titleSmall());
        lblTitle.setForeground(Theme.TEXT_WHITE);

        JLabel lblSub = new JLabel("Biaya dihitung otomatis sesuai jenis kendaraan.");
        lblSub.setFont(Theme.caption());
        lblSub.setForeground(Theme.TEXT_MUTED);

        header.add(lblTitle);
        header.add(Box.createVerticalStrut(2));
        header.add(lblSub);
        header.add(Box.createVerticalStrut(14));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        // Row Pelanggan & Kendaraan
        JPanel rowSelect = new JPanel(new GridLayout(1, 2, 10, 0));
        rowSelect.setOpaque(false);

        JPanel pPelanggan = new JPanel();
        pPelanggan.setLayout(new BoxLayout(pPelanggan, BoxLayout.Y_AXIS));
        pPelanggan.setOpaque(false);
        pPelanggan.add(createFieldLabel("Pelanggan"));
        cmbPelanggan = new JComboBox<>();
        cmbPelanggan.setBackground(Theme.BG_INPUT);
        cmbPelanggan.setForeground(Theme.TEXT_WHITE);
        cmbPelanggan.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        pPelanggan.add(cmbPelanggan);

        JPanel pKendaraan = new JPanel();
        pKendaraan.setLayout(new BoxLayout(pKendaraan, BoxLayout.Y_AXIS));
        pKendaraan.setOpaque(false);
        pKendaraan.add(createFieldLabel("Kendaraan tersedia"));
        cmbKendaraan = new JComboBox<>();
        cmbKendaraan.setBackground(Theme.BG_INPUT);
        cmbKendaraan.setForeground(Theme.TEXT_WHITE);
        cmbKendaraan.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        cmbKendaraan.addActionListener(e -> updateEstimasiSewa());
        pKendaraan.add(cmbKendaraan);

        rowSelect.add(pPelanggan);
        rowSelect.add(pKendaraan);
        form.add(rowSelect);
        form.add(Box.createVerticalStrut(10));

        // Row Tanggal Pinjam, Tanggal Kembali, Durasi Sewa
        JPanel rowDates = new JPanel(new GridLayout(1, 3, 10, 0));
        rowDates.setOpaque(false);

        JPanel pTgl1 = new JPanel();
        pTgl1.setLayout(new BoxLayout(pTgl1, BoxLayout.Y_AXIS));
        pTgl1.setOpaque(false);
        pTgl1.add(createFieldLabel("Tanggal pinjam"));
        spnTglPinjam = new JSpinner(new SpinnerDateModel());
        spnTglPinjam.setEditor(new JSpinner.DateEditor(spnTglPinjam, "dd MMM yyyy"));
        spnTglPinjam.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        spnTglPinjam.addChangeListener(e -> onDateChanged());
        pTgl1.add(spnTglPinjam);

        JPanel pTgl2 = new JPanel();
        pTgl2.setLayout(new BoxLayout(pTgl2, BoxLayout.Y_AXIS));
        pTgl2.setOpaque(false);
        pTgl2.add(createFieldLabel("Tanggal kembali"));
        spnTglKembali = new JSpinner(new SpinnerDateModel());
        spnTglKembali.setEditor(new JSpinner.DateEditor(spnTglKembali, "dd MMM yyyy"));
        spnTglKembali.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        spnTglKembali.addChangeListener(e -> onDateChanged());
        pTgl2.add(spnTglKembali);

        JPanel pDurasi = new JPanel();
        pDurasi.setLayout(new BoxLayout(pDurasi, BoxLayout.Y_AXIS));
        pDurasi.setOpaque(false);
        pDurasi.add(createFieldLabel("Durasi sewa"));
        spnDurasi = new JSpinner(new SpinnerNumberModel(3, 1, 90, 1));
        spnDurasi.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        spnDurasi.addChangeListener(e -> onDurasiChanged());
        pDurasi.add(spnDurasi);

        rowDates.add(pTgl1);
        rowDates.add(pTgl2);
        rowDates.add(pDurasi);
        form.add(rowDates);
        form.add(Box.createVerticalStrut(10));

        // Checkbox Supir / Opsi Polimorfik
        chkSupir = new JCheckBox("Termasuk supir (+ Rp150.000 / hari)");
        chkSupir.setOpaque(false);
        chkSupir.setForeground(Theme.TEXT_WHITE);
        chkSupir.setFont(Theme.bodyRegular());
        chkSupir.setSelected(true);
        chkSupir.addActionListener(e -> updateEstimasiSewa());
        form.add(chkSupir);
        form.add(Box.createVerticalStrut(12));

        // Box Rincian Biaya
        UIComponents.ModernCard calcBox = new UIComponents.ModernCard(Theme.BG_INPUT, Theme.BORDER, 14);
        calcBox.setLayout(new BorderLayout());
        calcBox.setBorder(new EmptyBorder(12, 16, 12, 16));

        JPanel calcDetails = new JPanel();
        calcDetails.setLayout(new BoxLayout(calcDetails, BoxLayout.Y_AXIS));
        calcDetails.setOpaque(false);

        lblBreakdownTarif = new JLabel("Tarif dasar: 3 × Rp350.000   Rp1.050.000");
        lblBreakdownTarif.setFont(Theme.caption());
        lblBreakdownTarif.setForeground(Theme.TEXT_MUTED);

        lblBreakdownTambahan = new JLabel("Supir: 3 × Rp150.000   Rp450.000");
        lblBreakdownTambahan.setFont(Theme.caption());
        lblBreakdownTambahan.setForeground(Theme.TEXT_MUTED);

        calcDetails.add(lblBreakdownTarif);
        calcDetails.add(Box.createVerticalStrut(4));
        calcDetails.add(lblBreakdownTambahan);

        JPanel calcTotal = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        calcTotal.setOpaque(false);

        lblTotalSewa = new JLabel("Rp1.500.000");
        lblTotalSewa.setFont(new Font(Theme.FONT_NAME, Font.BOLD, 22));
        lblTotalSewa.setForeground(Theme.ACCENT_LIME); // Sesuai warna lime di wireframe
        calcTotal.add(lblTotalSewa);

        calcBox.add(calcDetails, BorderLayout.WEST);
        calcBox.add(calcTotal, BorderLayout.EAST);
        form.add(calcBox);
        form.add(Box.createVerticalStrut(8));

        JLabel lblNote = new JLabel("Status unit berubah menjadi Disewa setelah disimpan.");
        lblNote.setFont(Theme.micro());
        lblNote.setForeground(Theme.TEXT_DIM);
        form.add(lblNote);

        // Tombol Simpan Penyewaan
        UIComponents.ModernButton btnSimpan = new UIComponents.ModernButton("Simpan penyewaan", UIComponents.ModernButton.Type.PRIMARY);
        btnSimpan.setBorder(new EmptyBorder(12, 0, 12, 0));
        btnSimpan.addActionListener(e -> prosesSimpanPenyewaan());

        card.add(header, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        card.add(btnSimpan, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createPengembalianCard() {
        UIComponents.ModernCard card = new UIComponents.ModernCard(Theme.BG_CARD, Theme.BORDER, 18);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 22, 20, 22));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);

        JLabel lblTitle = new JLabel("Pengembalian");
        lblTitle.setFont(Theme.titleSmall());
        lblTitle.setForeground(Theme.TEXT_WHITE);

        JLabel lblSub = new JLabel("Selesaikan sewa dan perbarui status unit.");
        lblSub.setFont(Theme.caption());
        lblSub.setForeground(Theme.TEXT_MUTED);

        header.add(lblTitle);
        header.add(Box.createVerticalStrut(2));
        header.add(lblSub);
        header.add(Box.createVerticalStrut(14));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        // Pilih Transaksi Aktif
        form.add(createFieldLabel("Pilih transaksi aktif"));
        cmbTransaksiAktif = new JComboBox<>();
        cmbTransaksiAktif.setBackground(Theme.BG_INPUT);
        cmbTransaksiAktif.setForeground(Theme.TEXT_WHITE);
        cmbTransaksiAktif.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        cmbTransaksiAktif.addActionListener(e -> updateDetailPengembalian());
        form.add(cmbTransaksiAktif);
        form.add(Box.createVerticalStrut(4));

        lblDetailPelangganUnit = new JLabel("Bagas Saputra / B 3021 RFS");
        lblDetailPelangganUnit.setFont(Theme.caption());
        lblDetailPelangganUnit.setForeground(Theme.TEXT_DIM);
        form.add(lblDetailPelangganUnit);
        form.add(Box.createVerticalStrut(10));

        // Row Jadwal Kembali & Kembali Aktual
        JPanel rowDates = new JPanel(new GridLayout(1, 2, 10, 0));
        rowDates.setOpaque(false);

        JPanel pJadwal = new JPanel();
        pJadwal.setLayout(new BoxLayout(pJadwal, BoxLayout.Y_AXIS));
        pJadwal.setOpaque(false);
        pJadwal.add(createFieldLabel("Jadwal kembali"));
        lblJadwalKembali = new JLabel("02 Nov 2026");
        lblJadwalKembali.setFont(Theme.bodyBold());
        lblJadwalKembali.setForeground(Theme.TEXT_WHITE);
        lblJadwalKembali.setBorder(new EmptyBorder(8, 0, 8, 0));
        pJadwal.add(lblJadwalKembali);

        JPanel pAktual = new JPanel();
        pAktual.setLayout(new BoxLayout(pAktual, BoxLayout.Y_AXIS));
        pAktual.setOpaque(false);
        pAktual.add(createFieldLabel("Kembali aktual"));
        spnTglAktual = new JSpinner(new SpinnerDateModel());
        spnTglAktual.setEditor(new JSpinner.DateEditor(spnTglAktual, "dd MMM yyyy"));
        spnTglAktual.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        spnTglAktual.addChangeListener(e -> updatePerhitunganDenda());
        pAktual.add(spnTglAktual);

        rowDates.add(pJadwal);
        rowDates.add(pAktual);
        form.add(rowDates);
        form.add(Box.createVerticalStrut(10));

        // Late Banner
        panelLateBanner = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        panelLateBanner.setBackground(Theme.BADGE_AMBER_BG);
        panelLateBanner.setBorder(BorderFactory.createLineBorder(Theme.BADGE_AMBER_TXT, 1));
        panelLateBanner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        lblLateBannerText = new JLabel("! Terlambat 1 hari - Rp100.000 / hari");
        lblLateBannerText.setFont(Theme.captionBold());
        lblLateBannerText.setForeground(Theme.BADGE_AMBER_TXT);
        panelLateBanner.add(lblLateBannerText);
        form.add(panelLateBanner);
        form.add(Box.createVerticalStrut(10));

        // Breakdown Box Akhir
        UIComponents.ModernCard boxTotal = new UIComponents.ModernCard(Theme.BG_INPUT, Theme.BORDER, 14);
        boxTotal.setLayout(new BorderLayout());
        boxTotal.setBorder(new EmptyBorder(12, 16, 12, 16));

        JPanel detailAkhir = new JPanel();
        detailAkhir.setLayout(new BoxLayout(detailAkhir, BoxLayout.Y_AXIS));
        detailAkhir.setOpaque(false);

        lblBiayaSewaAwal = new JLabel("Biaya sewa awal: Rp600.000");
        lblBiayaSewaAwal.setFont(Theme.caption());
        lblBiayaSewaAwal.setForeground(Theme.TEXT_MUTED);

        lblBiayaDenda = new JLabel("Denda keterlambatan: Rp100.000");
        lblBiayaDenda.setFont(Theme.caption());
        lblBiayaDenda.setForeground(Theme.BADGE_AMBER_TXT);

        detailAkhir.add(lblBiayaSewaAwal);
        detailAkhir.add(Box.createVerticalStrut(4));
        detailAkhir.add(lblBiayaDenda);

        JPanel panelTotal = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panelTotal.setOpaque(false);

        lblTotalAkhir = new JLabel("Rp700.000");
        lblTotalAkhir.setFont(new Font(Theme.FONT_NAME, Font.BOLD, 22));
        lblTotalAkhir.setForeground(Theme.BADGE_AMBER_TXT);
        panelTotal.add(lblTotalAkhir);

        boxTotal.add(detailAkhir, BorderLayout.WEST);
        boxTotal.add(panelTotal, BorderLayout.EAST);
        form.add(boxTotal);

        // Tombol Selesaikan Pengembalian
        UIComponents.ModernButton btnSelesai = new UIComponents.ModernButton("Selesaikan pengembalian", UIComponents.ModernButton.Type.PRIMARY);
        btnSelesai.setBorder(new EmptyBorder(12, 0, 12, 0));
        btnSelesai.addActionListener(e -> prosesSelesaikanPengembalian());

        card.add(header, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        card.add(btnSelesai, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createTransaksiBerjalanCard() {
        UIComponents.ModernCard card = new UIComponents.ModernCard(Theme.BG_CARD, Theme.BORDER, 18);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(18, 22, 18, 22));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        JLabel lblTitle = new JLabel("Transaksi berjalan");
        lblTitle.setFont(Theme.titleSmall());
        lblTitle.setForeground(Theme.TEXT_WHITE);

        lblActiveCount = new JLabel("5 aktif");
        lblActiveCount.setFont(Theme.captionBold());
        lblActiveCount.setForeground(Theme.TEXT_DIM);

        left.add(lblTitle);
        left.add(lblActiveCount);

        JLabel lblAll = new JLabel("Lihat seluruh transaksi ↗");
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

        // Status Renderer
        tableTransaksi.getColumnModel().getColumn(5).setCellRenderer((table, value, isSelected, hasFocus, row, col) -> {
            String s = String.valueOf(value);
            UIComponents.BadgePill pill;
            if ("Terlambat".equalsIgnoreCase(s)) {
                pill = new UIComponents.BadgePill("Terlambat", Theme.BADGE_RED_BG, Theme.BADGE_RED_TXT);
            } else if ("Selesai".equalsIgnoreCase(s)) {
                pill = new UIComponents.BadgePill("Selesai", Theme.BADGE_GREEN_BG, Theme.BADGE_GREEN_TXT);
            } else {
                pill = new UIComponents.BadgePill("Disewa", Theme.BADGE_AMBER_BG, Theme.BADGE_AMBER_TXT);
            }
            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
            wrapper.setOpaque(false);
            wrapper.add(pill);
            return wrapper;
        });

        // Double click baris untuk cetak struk
        tableTransaksi.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tableTransaksi.getSelectedRow();
                    if (row >= 0 && row < listSemuaTransaksi.size()) {
                        showStrukDialog(listSemuaTransaksi.get(row));
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tableTransaksi);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.BG_CARD);
        scroll.setPreferredSize(new Dimension(800, 200));

        card.add(header, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private JLabel createFieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.captionBold());
        l.setForeground(Theme.TEXT_DIM);
        l.setBorder(new EmptyBorder(4, 0, 4, 0));
        return l;
    }

    private void onDateChanged() {
        java.util.Date d1 = (java.util.Date) spnTglPinjam.getValue();
        java.util.Date d2 = (java.util.Date) spnTglKembali.getValue();
        LocalDate l1 = d1.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        LocalDate l2 = d2.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        long days = ChronoUnit.DAYS.between(l1, l2);
        if (days <= 0) days = 1;
        spnDurasi.setValue((int) days);
        updateEstimasiSewa();
    }

    private void onDurasiChanged() {
        int days = (Integer) spnDurasi.getValue();
        java.util.Date d1 = (java.util.Date) spnTglPinjam.getValue();
        LocalDate l1 = d1.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        LocalDate l2 = l1.plusDays(days);
        spnTglKembali.setValue(java.sql.Date.valueOf(l2));
        updateEstimasiSewa();
    }

    private void updateEstimasiSewa() {
        Kendaraan k = (Kendaraan) cmbKendaraan.getSelectedItem();
        if (k == null) return;

        int durasi = (Integer) spnDurasi.getValue();
        boolean supir = chkSupir.isSelected();

        if (k instanceof Mobil) {
            chkSupir.setVisible(true);
            chkSupir.setText("Termasuk supir (+ Rp150.000 / hari)");
            double dasar = k.getTarifDasarHarian() * durasi;
            double biayaSupir = supir ? 150000.0 * durasi : 0.0;
            lblBreakdownTarif.setText(String.format("Tarif dasar: %d × Rp%,.0f   Rp%,.0f", durasi, k.getTarifDasarHarian(), dasar));
            lblBreakdownTambahan.setText(String.format("Supir: %d × Rp%,.0f   Rp%,.0f", durasi, (supir ? 150000.0 : 0.0), biayaSupir));
            lblTotalSewa.setText(String.format("Rp%,.0f", dasar + biayaSupir));
        } else if (k instanceof Motor) {
            chkSupir.setVisible(false);
            double total = k.hitungSewa(durasi);
            double normal = k.getTarifDasarHarian() * durasi;
            lblBreakdownTarif.setText(String.format("Tarif dasar: %d × Rp%,.0f   Rp%,.0f", durasi, k.getTarifDasarHarian(), normal));
            if (durasi >= 7) {
                lblBreakdownTambahan.setText(String.format("Diskon mingguan 10%%: -Rp%,.0f", normal * 0.10));
            } else {
                lblBreakdownTambahan.setText("Sewa >= 7 hari dapat diskon 10%");
            }
            lblTotalSewa.setText(String.format("Rp%,.0f", total));
        } else if (k instanceof Bus) {
            chkSupir.setVisible(false);
            double total = k.hitungSewa(durasi);
            double dasar = k.getTarifDasarHarian() * durasi;
            double asuransiKru = dasar * 0.15;
            lblBreakdownTarif.setText(String.format("Tarif dasar: %d × Rp%,.0f   Rp%,.0f", durasi, k.getTarifDasarHarian(), dasar));
            lblBreakdownTambahan.setText(String.format("Asuransi & Kru (15%%): Rp%,.0f", asuransiKru));
            lblTotalSewa.setText(String.format("Rp%,.0f", total));
        }
    }

    public void loadDropdowns() {
        try {
            // Pelanggan
            cmbPelanggan.removeAllItems();
            for (Pelanggan p : controller.getAllPelanggan()) {
                cmbPelanggan.addItem(p);
            }

            // Kendaraan Tersedia
            cmbKendaraan.removeAllItems();
            for (Kendaraan k : controller.getKendaraanTersedia()) {
                cmbKendaraan.addItem(k);
            }

            // Transaksi Aktif
            cmbTransaksiAktif.removeAllItems();
            List<TransaksiSewa> aktif = controller.getTransaksiAktif();
            for (TransaksiSewa t : aktif) {
                cmbTransaksiAktif.addItem(t);
            }

            updateEstimasiSewa();
            updateDetailPengembalian();

        } catch (SQLException e) {
            System.err.println("Gagal memuat data dropdown transaksi: " + e.getMessage());
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
            lblBiayaSewaAwal.setText("Biaya sewa awal: Rp0");
            lblBiayaDenda.setText("Denda: Rp0");
            lblTotalAkhir.setText("Rp0");
            return;
        }

        lblDetailPelangganUnit.setText(t.getPelanggan().getNamaLengkap() + " / " + t.getKendaraan().getPlatNomor());
        lblJadwalKembali.setText(t.getTanggalRencanaKembali().format(DTF));
        updatePerhitunganDenda();
    }

    private void updatePerhitunganDenda() {
        TransaksiSewa t = (TransaksiSewa) cmbTransaksiAktif.getSelectedItem();
        if (t == null) return;

        java.util.Date dAktual = (java.util.Date) spnTglAktual.getValue();
        LocalDate lAktual = dAktual.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();

        int hariTelat = t.getHariKeterlambatan(lAktual);
        double denda = t.hitungDendaAktual(lAktual);

        if (hariTelat > 0) {
            panelLateBanner.setVisible(true);
            double tarifDendaPerHari = t.getKendaraan().hitungDenda(1);
            lblLateBannerText.setText(String.format("! Terlambat %d hari - Rp%,.0f / hari", hariTelat, tarifDendaPerHari));
        } else {
            panelLateBanner.setVisible(false);
        }

        lblBiayaSewaAwal.setText(String.format("Biaya sewa awal: Rp%,.0f", t.getTotalBiaya()));
        lblBiayaDenda.setText(String.format("Denda keterlambatan: Rp%,.0f", denda));
        lblTotalAkhir.setText(String.format("Rp%,.0f", t.getTotalBiaya() + denda));
    }

    private void prosesSimpanPenyewaan() {
        try {
            Pelanggan p = (Pelanggan) cmbPelanggan.getSelectedItem();
            Kendaraan k = (Kendaraan) cmbKendaraan.getSelectedItem();

            if (p == null || k == null) {
                JOptionPane.showMessageDialog(this, "Silakan pilih pelanggan dan armada yang tersedia.", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            java.util.Date d1 = (java.util.Date) spnTglPinjam.getValue();
            java.util.Date d2 = (java.util.Date) spnTglKembali.getValue();
            LocalDate l1 = d1.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            LocalDate l2 = d2.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();

            boolean supir = chkSupir.isSelected();

            TransaksiSewa tBaru = controller.buatTransaksiSewa(p.getIdPelanggan(), k.getIdKendaraan(), l1, l2, supir);

            JOptionPane.showMessageDialog(this,
                    "Transaksi " + tBaru.getIdTransaksi() + " berhasil disimpan!\nStatus armada " + k.getMerk() + " diubah menjadi 'Disewa'.",
                    "Penyewaan Berhasil",
                    JOptionPane.INFORMATION_MESSAGE);

            showStrukDialog(tBaru);

            // Refresh seluruh tampilan
            loadDropdowns();
            loadTransaksiTable();
            mainFrame.getKendaraanPanel().loadData();
            mainFrame.getDashboardPanel().refreshData();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Gagal Menyimpan", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void prosesSelesaikanPengembalian() {
        try {
            TransaksiSewa t = (TransaksiSewa) cmbTransaksiAktif.getSelectedItem();
            if (t == null) {
                JOptionPane.showMessageDialog(this, "Pilih transaksi yang akan diselesaikan.", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            java.util.Date dAktual = (java.util.Date) spnTglAktual.getValue();
            LocalDate lAktual = dAktual.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();

            controller.prosesPengembalian(t.getIdTransaksi(), lAktual);

            JOptionPane.showMessageDialog(this,
                    "Pengembalian armada " + t.getKendaraan().getMerk() + " selesai!\nUnit kembali berstatus 'Tersedia'.",
                    "Pengembalian Sukses",
                    JOptionPane.INFORMATION_MESSAGE);

            showStrukDialog(t);

            // Refresh seluruh tampilan
            loadDropdowns();
            loadTransaksiTable();
            mainFrame.getKendaraanPanel().loadData();
            mainFrame.getDashboardPanel().refreshData();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Gagal Mengembalikan", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadTransaksiTable() {
        try {
            listSemuaTransaksi = controller.getAllTransaksi();
            tableModel.setRowCount(0);
            int aktifCount = 0;
            for (TransaksiSewa t : listSemuaTransaksi) {
                if (!"Selesai".equalsIgnoreCase(t.getStatusTransaksi())) {
                    aktifCount++;
                }
                tableModel.addRow(new Object[]{
                        t.getIdTransaksi(),
                        t.getPelanggan().getNamaLengkap(),
                        t.getKendaraan().getMerk() + " - " + t.getKendaraan().getPlatNomor(),
                        t.getTanggalPinjam().format(DTF_SHORT) + " – " + t.getTanggalRencanaKembali().format(DTF_SHORT),
                        String.format("Rp%,.0f", t.getTotalAkhir()),
                        t.getStatusTransaksi()
                });
            }
            lblActiveCount.setText(aktifCount + " aktif");
        } catch (SQLException e) {
            System.err.println("Gagal memuat tabel transaksi: " + e.getMessage());
        }
    }

    private void showStrukDialog(TransaksiSewa t) {
        JTextArea txtStruk = new JTextArea(t.cetakStruk());
        txtStruk.setFont(new Font("Consolas", Font.PLAIN, 13));
        txtStruk.setBackground(new Color(0x11, 0x12, 0x15));
        txtStruk.setForeground(Theme.ACCENT_ORANGE);
        txtStruk.setEditable(false);
        txtStruk.setBorder(new EmptyBorder(16, 20, 16, 20));

        JScrollPane sp = new JScrollPane(txtStruk);
        sp.setPreferredSize(new Dimension(460, 420));
        sp.setBorder(null);

        JOptionPane.showMessageDialog(this, sp, "Struk Bukti Transaksi", JOptionPane.PLAIN_MESSAGE);
    }

    private void showRiwayatDialog() {
        try {
            List<TransaksiSewa> all = controller.getAllTransaksi();
            String[] cols = {"ID Transaksi", "Pelanggan", "Kendaraan", "Tgl Pinjam", "Jadwal Kembali", "Kembali Aktual", "Biaya", "Denda", "Status"};
            DefaultTableModel tm = new DefaultTableModel(cols, 0);
            for (TransaksiSewa t : all) {
                tm.addRow(new Object[]{
                        t.getIdTransaksi(),
                        t.getPelanggan().getNamaLengkap(),
                        t.getKendaraan().getMerk(),
                        t.getTanggalPinjam(),
                        t.getTanggalRencanaKembali(),
                        t.getTanggalAktualKembali() != null ? t.getTanggalAktualKembali() : "-",
                        String.format("Rp%,.0f", t.getTotalBiaya()),
                        String.format("Rp%,.0f", t.getTotalDenda()),
                        t.getStatusTransaksi()
                });
            }
            JTable t = new JTable(tm);
            UIComponents.styleTable(t);
            JScrollPane sp = new JScrollPane(t);
            sp.setPreferredSize(new Dimension(850, 400));
            JOptionPane.showMessageDialog(this, sp, "Seluruh Riwayat Transaksi", JOptionPane.PLAIN_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
