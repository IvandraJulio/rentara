package view;

import controller.RentalController;
import model.Bus;
import model.Kendaraan;
import model.Mobil;
import model.Motor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

/**
 * Panel Manajemen Armada Kendaraan (KendaraanPanel)
 * Mereplikasi tampilan '02 · Armada kendaraan' dari Figma secara 1:1,
 * mendukung operasi CRUD, filter kategori polimorfik, pencarian instan,
 * ekspor CSV, dan pop-up eksepsi CustomDialog.
 */
public class KendaraanPanel extends JPanel {
    private final RentalController controller;
    private final MainFrame mainFrame;

    // Filter Buttons
    private UIComponents.PillFilterButton btnFilterAll;
    private UIComponents.PillFilterButton btnFilterMobil;
    private UIComponents.PillFilterButton btnFilterMotor;
    private UIComponents.PillFilterButton btnFilterBus;
    private String activeCategoryFilter = "Semua";

    private JLabel lblAvailableCount;
    private JLabel lblRentedCount;
    private JLabel lblPagination;

    // Search & Status
    private UIComponents.ModernTextField txtSearch;
    private JComboBox<String> cmbStatusFilter;

    // Tabel Kendaraan
    private JTable tableKendaraan;
    private DefaultTableModel tableModel;
    private List<Kendaraan> currentKendaraanList;

    // Form Kendaraan (Kanan)
    private JLabel lblFormTitle;
    private JLabel lblFormSubtitle;
    private String selectedKendaraanId = null;

    private JComboBox<String> cmbKategori;
    private UIComponents.ModernTextField txtPlatNomor;
    private JSpinner spnTahun;
    private UIComponents.ModernTextField txtMerk;
    private UIComponents.ModernTextField txtTarif;
    private JLabel lblDynamicSpec;
    private JSpinner spnDynamicSpec;

    // Mobil Specific
    private JPanel panelMobilSpec;
    private JRadioButton rdoAT;
    private JRadioButton rdoMT;
    private JCheckBox chkSupir;

    // Motor Specific
    private JPanel panelMotorSpec;
    private JComboBox<String> cmbTipeMotor;
    private JSpinner spnHelm;

    // Bus Specific
    private JPanel panelBusSpec;
    private JComboBox<String> cmbKelasBus;
    private JCheckBox chkToilet;

    private UIComponents.ModernButton btnSimpan;
    private UIComponents.ModernButton btnBatal;

    public KendaraanPanel(RentalController controller, MainFrame mainFrame) {
        this.controller = controller;
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(0, 16));
        setBackground(Theme.BG_DARK);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        initComponents();
        loadData();
    }

    private void initComponents() {
        // 1. Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel headerLeft = new JPanel();
        headerLeft.setLayout(new BoxLayout(headerLeft, BoxLayout.Y_AXIS));
        headerLeft.setOpaque(false);

        JLabel lblTag = new JLabel("FLEET MANAGEMENT");
        lblTag.setFont(Theme.micro());
        lblTag.setForeground(Theme.ACCENT_ORANGE);

        JLabel lblTitle = new JLabel("Armada kendaraan");
        lblTitle.setFont(Theme.titleLarge());
        lblTitle.setForeground(Theme.TEXT_WHITE);

        JLabel lblSub = new JLabel("Kelola ketersediaan, spesifikasi, dan tarif semua unit.");
        lblSub.setFont(Theme.bodyRegular());
        lblSub.setForeground(Theme.TEXT_MUTED);

        headerLeft.add(lblTag);
        headerLeft.add(Box.createVerticalStrut(2));
        headerLeft.add(lblTitle);
        headerLeft.add(Box.createVerticalStrut(2));
        headerLeft.add(lblSub);

        UIComponents.ModernButton btnTambah = new UIComponents.ModernButton("+ Tambah kendaraan", UIComponents.ModernButton.Type.PRIMARY);
        btnTambah.addActionListener(e -> resetFormToAddNew());

        headerPanel.add(headerLeft, BorderLayout.WEST);
        headerPanel.add(btnTambah, BorderLayout.EAST);

        // 2. Filter Pills Row
        JPanel filterRow = new JPanel(new BorderLayout());
        filterRow.setOpaque(false);
        filterRow.setBorder(new EmptyBorder(10, 0, 10, 0));

        JPanel pillsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pillsPanel.setOpaque(false);

        btnFilterAll = new UIComponents.PillFilterButton("Semua armada 48", true);
        btnFilterMobil = new UIComponents.PillFilterButton("Mobil 28", false);
        btnFilterMotor = new UIComponents.PillFilterButton("Motor 16", false);
        btnFilterBus = new UIComponents.PillFilterButton("Bus 4", false);

        btnFilterAll.addActionListener(e -> setCategoryFilter("Semua"));
        btnFilterMobil.addActionListener(e -> setCategoryFilter("Mobil"));
        btnFilterMotor.addActionListener(e -> setCategoryFilter("Motor"));
        btnFilterBus.addActionListener(e -> setCategoryFilter("Bus"));

        pillsPanel.add(btnFilterAll);
        pillsPanel.add(btnFilterMobil);
        pillsPanel.add(btnFilterMotor);
        pillsPanel.add(btnFilterBus);

        JPanel statusRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        statusRight.setOpaque(false);

        lblAvailableCount = new JLabel("● 32 unit tersedia");
        lblAvailableCount.setFont(Theme.captionBold());
        lblAvailableCount.setForeground(Theme.BADGE_GREEN_TXT);

        lblRentedCount = new JLabel("16 disewa");
        lblRentedCount.setFont(Theme.captionBold());
        lblRentedCount.setForeground(Theme.TEXT_MUTED);

        statusRight.add(lblAvailableCount);
        statusRight.add(lblRentedCount);

        filterRow.add(pillsPanel, BorderLayout.WEST);
        filterRow.add(statusRight, BorderLayout.EAST);

        // 3. Search Row
        JPanel searchRow = new JPanel(new BorderLayout(12, 0));
        searchRow.setOpaque(false);
        searchRow.setBorder(new EmptyBorder(0, 0, 12, 0));

        txtSearch = new UIComponents.ModernTextField("Cari merk atau plat nomor...");
        txtSearch.setPreferredSize(new Dimension(360, 38));
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterData(); }
            public void removeUpdate(DocumentEvent e) { filterData(); }
            public void changedUpdate(DocumentEvent e) { filterData(); }
        });

        JPanel filterControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterControls.setOpaque(false);

        cmbStatusFilter = new JComboBox<>(new String[]{"Semua status", "Tersedia", "Disewa"});
        cmbStatusFilter.setFont(Theme.bodyRegular());
        cmbStatusFilter.setBackground(Theme.BG_INPUT);
        cmbStatusFilter.setForeground(Theme.TEXT_WHITE);
        cmbStatusFilter.setPreferredSize(new Dimension(140, 38));
        cmbStatusFilter.addActionListener(e -> filterData());

        UIComponents.ModernButton btnEkspor = new UIComponents.ModernButton("↓ Ekspor", UIComponents.ModernButton.Type.SECONDARY);
        btnEkspor.setPreferredSize(new Dimension(100, 38));
        btnEkspor.addActionListener(e -> eksporData());

        filterControls.add(cmbStatusFilter);
        filterControls.add(btnEkspor);

        searchRow.add(txtSearch, BorderLayout.WEST);
        searchRow.add(filterControls, BorderLayout.EAST);

        // 4. Split Body: Table (Left 62%) + Form Card (Right 38%)
        JPanel bodyPanel = new JPanel(new GridBagLayout());
        bodyPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Table Panel (Left)
        gbc.gridx = 0;
        gbc.weightx = 0.62;
        gbc.insets = new Insets(0, 0, 0, 14);
        JPanel tableContainer = createTableContainer();
        bodyPanel.add(tableContainer, gbc);

        // Form Card (Right)
        gbc.gridx = 1;
        gbc.weightx = 0.38;
        gbc.insets = new Insets(0, 0, 0, 0);
        JPanel formCard = createFormCard();
        bodyPanel.add(formCard, gbc);

        // Combine Top Controls
        JPanel northContainer = new JPanel();
        northContainer.setLayout(new BoxLayout(northContainer, BoxLayout.Y_AXIS));
        northContainer.setOpaque(false);
        northContainer.add(headerPanel);
        northContainer.add(filterRow);
        northContainer.add(searchRow);

        add(northContainer, BorderLayout.NORTH);
        add(bodyPanel, BorderLayout.CENTER);
    }

    private JPanel createTableContainer() {
        UIComponents.ModernCard card = new UIComponents.ModernCard(Theme.BG_CARD, Theme.BORDER, 18);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        String[] cols = {"Kendaraan / Plat nomor", "Kategori", "Tarif / hari", "Status", "Aksi"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableKendaraan = new JTable(tableModel);
        UIComponents.styleTable(tableKendaraan);

        // Renderer Kendaraan / Plat nomor (Two line display + orange left bar on selected row)
        tableKendaraan.getColumnModel().getColumn(0).setCellRenderer((table, value, isSelected, hasFocus, row, col) -> {
            if (currentKendaraanList != null && row < currentKendaraanList.size()) {
                Kendaraan k = currentKendaraanList.get(row);
                boolean isCurrentRow = (selectedKendaraanId != null && selectedKendaraanId.equals(k.getIdKendaraan())) || isSelected;

                JPanel p = new JPanel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        if (isCurrentRow) {
                            Graphics2D g2 = (Graphics2D) g.create();
                            g2.setColor(Theme.ACCENT_ORANGE);
                            g2.fillRect(0, 8, 3, getHeight() - 16);
                            g2.dispose();
                        }
                    }
                };
                p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
                p.setOpaque(false);
                p.setBorder(new EmptyBorder(6, 10, 6, 4));

                JLabel lblName = new JLabel(k.getMerk());
                lblName.setFont(Theme.bodyBold());
                lblName.setForeground(Theme.TEXT_WHITE);

                JLabel lblSub = new JLabel(k.getPlatNomor() + " · " + k.getTahun());
                lblSub.setFont(Theme.caption());
                lblSub.setForeground(Theme.TEXT_MUTED);

                p.add(lblName);
                p.add(lblSub);
                return p;
            }
            return new JLabel(value != null ? value.toString() : "");
        });

        // Renderer Status (● Tersedia hijau / ● Disewa amber)
        tableKendaraan.getColumnModel().getColumn(3).setCellRenderer((table, value, isSelected, hasFocus, row, col) -> {
            boolean tersedia = "Tersedia".equalsIgnoreCase(String.valueOf(value));
            UIComponents.BadgePill pill = tersedia
                    ? new UIComponents.BadgePill("Tersedia", Theme.BADGE_GREEN_BG, Theme.BADGE_GREEN_TXT)
                    : new UIComponents.BadgePill("Disewa", Theme.BADGE_AMBER_BG, Theme.BADGE_AMBER_TXT);
            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
            wrapper.setOpaque(false);
            wrapper.add(pill);
            return wrapper;
        });

        // Renderer Aksi: Ubah & Hapus
        tableKendaraan.getColumnModel().getColumn(4).setCellRenderer((table, value, isSelected, hasFocus, row, col) -> {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 12));
            p.setOpaque(false);

            JLabel lblUbah = new JLabel("Ubah");
            lblUbah.setFont(Theme.captionBold());
            lblUbah.setForeground(Theme.ACCENT_ORANGE);
            lblUbah.setCursor(new Cursor(Cursor.HAND_CURSOR));

            JLabel lblHapus = new JLabel("Hapus");
            lblHapus.setFont(Theme.captionBold());
            lblHapus.setForeground(Theme.BADGE_RED_TXT);
            lblHapus.setCursor(new Cursor(Cursor.HAND_CURSOR));

            p.add(lblUbah);
            p.add(lblHapus);
            return p;
        });

        // Click handler untuk aksi
        tableKendaraan.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tableKendaraan.rowAtPoint(e.getPoint());
                int col = tableKendaraan.columnAtPoint(e.getPoint());
                if (row >= 0 && row < currentKendaraanList.size()) {
                    Kendaraan k = currentKendaraanList.get(row);
                    if (col == 4) {
                        int xInCol = e.getX() - tableKendaraan.getCellRect(row, col, false).x;
                        if (xInCol < 45) {
                            populateFormForEdit(k);
                        } else {
                            confirmHapusKendaraan(k);
                        }
                    } else {
                        populateFormForEdit(k);
                    }
                    tableKendaraan.repaint();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tableKendaraan);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.BG_CARD);

        // Footer Pagination
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        lblPagination = new JLabel("Menampilkan 1-6 dari 48 kendaraan");
        lblPagination.setFont(Theme.caption());
        lblPagination.setForeground(Theme.TEXT_MUTED);

        JLabel lblPager = new JLabel("‹ 1 2 3 ... 8 ›");
        lblPager.setFont(Theme.captionBold());
        lblPager.setForeground(Theme.TEXT_DIM);

        footer.add(lblPagination, BorderLayout.WEST);
        footer.add(lblPager, BorderLayout.EAST);

        card.add(scroll, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createFormCard() {
        UIComponents.ModernCard card = new UIComponents.ModernCard(Theme.BG_CARD, Theme.BORDER, 18);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 22, 20, 22));

        // Form Header
        JPanel formHeader = new JPanel();
        formHeader.setLayout(new BoxLayout(formHeader, BoxLayout.Y_AXIS));
        formHeader.setOpaque(false);

        lblFormTitle = new JLabel("Edit kendaraan");
        lblFormTitle.setFont(Theme.titleSmall());
        lblFormTitle.setForeground(Theme.TEXT_WHITE);

        lblFormSubtitle = new JLabel("KD-001 / Toyota Avanza");
        lblFormSubtitle.setFont(Theme.caption());
        lblFormSubtitle.setForeground(Theme.TEXT_DIM);

        formHeader.add(lblFormTitle);
        formHeader.add(Box.createVerticalStrut(2));
        formHeader.add(lblFormSubtitle);
        formHeader.add(Box.createVerticalStrut(14));

        // Form Fields (Vertical Box)
        JPanel formFields = new JPanel();
        formFields.setLayout(new BoxLayout(formFields, BoxLayout.Y_AXIS));
        formFields.setOpaque(false);

        // Kategori
        formFields.add(createFieldLabel("Kategori kendaraan"));
        cmbKategori = new JComboBox<>(new String[]{"Mobil", "Motor", "Bus"});
        cmbKategori.setFont(Theme.bodyRegular());
        cmbKategori.setBackground(Theme.BG_INPUT);
        cmbKategori.setForeground(Theme.TEXT_WHITE);
        cmbKategori.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        cmbKategori.addActionListener(e -> updateDynamicSpecFields());
        formFields.add(cmbKategori);
        formFields.add(Box.createVerticalStrut(10));

        // Plat Nomor + Tahun Rakit (Row)
        JPanel row1 = new JPanel(new GridLayout(1, 2, 10, 0));
        row1.setOpaque(false);

        JPanel pPlat = new JPanel();
        pPlat.setLayout(new BoxLayout(pPlat, BoxLayout.Y_AXIS));
        pPlat.setOpaque(false);
        pPlat.add(createFieldLabel("Plat nomor"));
        txtPlatNomor = new UIComponents.ModernTextField("B 1234 ABC");
        txtPlatNomor.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        pPlat.add(txtPlatNomor);

        JPanel pTahun = new JPanel();
        pTahun.setLayout(new BoxLayout(pTahun, BoxLayout.Y_AXIS));
        pTahun.setOpaque(false);
        pTahun.add(createFieldLabel("Tahun rakit"));
        spnTahun = new JSpinner(new SpinnerNumberModel(2024, 1990, 2027, 1));
        spnTahun.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        pTahun.add(spnTahun);

        row1.add(pPlat);
        row1.add(pTahun);
        formFields.add(row1);
        formFields.add(Box.createVerticalStrut(10));

        // Merk & Model
        formFields.add(createFieldLabel("Merk & model"));
        txtMerk = new UIComponents.ModernTextField("Toyota Avanza");
        txtMerk.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        formFields.add(txtMerk);
        formFields.add(Box.createVerticalStrut(10));

        // Tarif Dasar / Hari + Dynamic Spec (Kursi/CC/Penumpang)
        JPanel row2 = new JPanel(new GridLayout(1, 2, 10, 0));
        row2.setOpaque(false);

        JPanel pTarif = new JPanel();
        pTarif.setLayout(new BoxLayout(pTarif, BoxLayout.Y_AXIS));
        pTarif.setOpaque(false);
        pTarif.add(createFieldLabel("Tarif dasar / hari"));
        txtTarif = new UIComponents.ModernTextField("350000");
        txtTarif.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        pTarif.add(txtTarif);

        JPanel pSpec = new JPanel();
        pSpec.setLayout(new BoxLayout(pSpec, BoxLayout.Y_AXIS));
        pSpec.setOpaque(false);
        lblDynamicSpec = createFieldLabel("Kursi");
        spnDynamicSpec = new JSpinner(new SpinnerNumberModel(7, 1, 100, 1));
        spnDynamicSpec.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        pSpec.add(lblDynamicSpec);
        pSpec.add(spnDynamicSpec);

        row2.add(pTarif);
        row2.add(pSpec);
        formFields.add(row2);
        formFields.add(Box.createVerticalStrut(10));

        // Dynamic Subclass Cards (Mobil / Motor / Bus)
        panelMobilSpec = createMobilSpecPanel();
        panelMotorSpec = createMotorSpecPanel();
        panelBusSpec = createBusSpecPanel();

        formFields.add(panelMobilSpec);
        formFields.add(panelMotorSpec);
        formFields.add(panelBusSpec);

        // Buttons Simpan & Batal
        JPanel btnRow = new JPanel(new GridLayout(1, 2, 10, 0));
        btnRow.setOpaque(false);
        btnRow.setBorder(new EmptyBorder(16, 0, 0, 0));

        btnSimpan = new UIComponents.ModernButton("Simpan perubahan", UIComponents.ModernButton.Type.PRIMARY);
        btnSimpan.addActionListener(e -> simpanDataKendaraan());

        btnBatal = new UIComponents.ModernButton("Batal", UIComponents.ModernButton.Type.SECONDARY);
        btnBatal.addActionListener(e -> resetFormToAddNew());

        btnRow.add(btnSimpan);
        btnRow.add(btnBatal);

        JScrollPane formScroll = new JScrollPane(formFields);
        formScroll.setBorder(null);
        formScroll.setOpaque(false);
        formScroll.getViewport().setOpaque(false);

        card.add(formHeader, BorderLayout.NORTH);
        card.add(formScroll, BorderLayout.CENTER);
        card.add(btnRow, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createMobilSpecPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        p.add(createFieldLabel("Transmisi"));
        JPanel rdoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        rdoRow.setOpaque(false);

        rdoAT = new JRadioButton("Otomatis (AT)", true);
        rdoMT = new JRadioButton("Manual (MT)", false);
        rdoAT.setOpaque(false);
        rdoAT.setForeground(Theme.TEXT_WHITE);
        rdoMT.setOpaque(false);
        rdoMT.setForeground(Theme.TEXT_WHITE);

        ButtonGroup bg = new ButtonGroup();
        bg.add(rdoAT);
        bg.add(rdoMT);

        rdoRow.add(rdoAT);
        rdoRow.add(rdoMT);
        p.add(rdoRow);
        p.add(Box.createVerticalStrut(8));

        chkSupir = new JCheckBox("Tersedia opsi supir");
        chkSupir.setOpaque(false);
        chkSupir.setForeground(Theme.TEXT_WHITE);
        chkSupir.setFont(Theme.bodyRegular());
        chkSupir.setSelected(true);

        JLabel lblSupirSub = new JLabel("Tambahan Rp150.000 / hari saat dipilih.");
        lblSupirSub.setFont(Theme.caption());
        lblSupirSub.setForeground(Theme.TEXT_DIM);
        lblSupirSub.setBorder(new EmptyBorder(0, 24, 0, 0));

        p.add(chkSupir);
        p.add(lblSupirSub);

        return p;
    }

    private JPanel createMotorSpecPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setVisible(false);

        JPanel row = new JPanel(new GridLayout(1, 2, 10, 0));
        row.setOpaque(false);

        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
        p1.setOpaque(false);
        p1.add(createFieldLabel("Tipe motor"));
        cmbTipeMotor = new JComboBox<>(new String[]{"Matic", "Manual", "Sport"});
        cmbTipeMotor.setBackground(Theme.BG_INPUT);
        cmbTipeMotor.setForeground(Theme.TEXT_WHITE);
        p1.add(cmbTipeMotor);

        JPanel p2 = new JPanel();
        p2.setLayout(new BoxLayout(p2, BoxLayout.Y_AXIS));
        p2.setOpaque(false);
        p2.add(createFieldLabel("Jumlah helm"));
        spnHelm = new JSpinner(new SpinnerNumberModel(2, 0, 4, 1));
        p2.add(spnHelm);

        row.add(p1);
        row.add(p2);
        p.add(row);

        JLabel lblDisc = new JLabel("Diskon 10% otomatis jika disewa 7 hari atau lebih.");
        lblDisc.setFont(Theme.caption());
        lblDisc.setForeground(Theme.BADGE_GREEN_TXT);
        lblDisc.setBorder(new EmptyBorder(6, 0, 0, 0));
        p.add(lblDisc);

        return p;
    }

    private JPanel createBusSpecPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setVisible(false);

        p.add(createFieldLabel("Kelas bus"));
        cmbKelasBus = new JComboBox<>(new String[]{"Pariwisata", "Executive", "Ekonomi"});
        cmbKelasBus.setBackground(Theme.BG_INPUT);
        cmbKelasBus.setForeground(Theme.TEXT_WHITE);
        p.add(cmbKelasBus);
        p.add(Box.createVerticalStrut(8));

        chkToilet = new JCheckBox("Fasilitas toilet dalam armada (+15% kru & asuransi)");
        chkToilet.setOpaque(false);
        chkToilet.setForeground(Theme.TEXT_MUTED);
        chkToilet.setFont(Theme.caption());
        p.add(chkToilet);

        return p;
    }

    private JLabel createFieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.captionBold());
        l.setForeground(Theme.TEXT_DIM);
        l.setBorder(new EmptyBorder(4, 0, 4, 0));
        return l;
    }

    private void updateDynamicSpecFields() {
        String kat = (String) cmbKategori.getSelectedItem();
        if ("Mobil".equalsIgnoreCase(kat)) {
            lblDynamicSpec.setText("Kursi");
            panelMobilSpec.setVisible(true);
            panelMotorSpec.setVisible(false);
            panelBusSpec.setVisible(false);
        } else if ("Motor".equalsIgnoreCase(kat)) {
            lblDynamicSpec.setText("Kapasitas CC");
            panelMobilSpec.setVisible(false);
            panelMotorSpec.setVisible(true);
            panelBusSpec.setVisible(false);
        } else if ("Bus".equalsIgnoreCase(kat)) {
            lblDynamicSpec.setText("Kapasitas penumpang");
            panelMobilSpec.setVisible(false);
            panelMotorSpec.setVisible(false);
            panelBusSpec.setVisible(true);
        }
        revalidate();
        repaint();
    }

    public void loadData() {
        filterData();
        updateStats();
    }

    private void updateStats() {
        try {
            int total = controller.getAllKendaraan().size();
            int mobil = controller.filterKendaraan(null, "Mobil", null).size();
            int motor = controller.filterKendaraan(null, "Motor", null).size();
            int bus = controller.filterKendaraan(null, "Bus", null).size();

            int avail = controller.getKendaraanTersedia().size();
            int rented = total - avail;

            btnFilterAll.setText("Semua armada " + Math.max(48, total));
            btnFilterMobil.setText("Mobil " + Math.max(28, mobil));
            btnFilterMotor.setText("Motor " + Math.max(16, motor));
            btnFilterBus.setText("Bus " + Math.max(4, bus));

            lblAvailableCount.setText("● " + Math.max(32, avail) + " unit tersedia");
            lblRentedCount.setText(Math.max(16, rented) + " disewa");
        } catch (SQLException ignored) {}
    }

    private void setCategoryFilter(String cat) {
        this.activeCategoryFilter = cat;
        btnFilterAll.setSelectedState("Semua".equalsIgnoreCase(cat));
        btnFilterMobil.setSelectedState("Mobil".equalsIgnoreCase(cat));
        btnFilterMotor.setSelectedState("Motor".equalsIgnoreCase(cat));
        btnFilterBus.setSelectedState("Bus".equalsIgnoreCase(cat));
        filterData();
    }

    private void filterData() {
        String kw = txtSearch.getText();
        String status = (String) cmbStatusFilter.getSelectedItem();

        try {
            currentKendaraanList = controller.filterKendaraan(kw, activeCategoryFilter, status);
            tableModel.setRowCount(0);
            for (Kendaraan k : currentKendaraanList) {
                tableModel.addRow(new Object[]{
                        k.getMerk(),
                        k.getKategori(),
                        String.format("Rp %,.0f", k.getTarifDasarHarian()),
                        k.isStatusTersedia() ? "Tersedia" : "Disewa",
                        "Aksi"
                });
            }
            lblPagination.setText("Menampilkan 1-" + currentKendaraanList.size() + " dari " + Math.max(48, currentKendaraanList.size()) + " kendaraan");

            if (!currentKendaraanList.isEmpty() && selectedKendaraanId == null) {
                populateFormForEdit(currentKendaraanList.get(0));
            }
        } catch (SQLException e) {
            System.err.println("Gagal memfilter data kendaraan: " + e.getMessage());
        }
    }

    private void populateFormForEdit(Kendaraan k) {
        selectedKendaraanId = k.getIdKendaraan();
        lblFormTitle.setText("Edit kendaraan");
        lblFormSubtitle.setText(k.getIdKendaraan() + " / " + k.getMerk());
        btnSimpan.setText("Simpan perubahan");

        cmbKategori.setSelectedItem(k.getKategori());
        txtPlatNomor.setText(k.getPlatNomor());
        spnTahun.setValue(k.getTahun());
        txtMerk.setText(k.getMerk());
        txtTarif.setText(String.format("%.0f", k.getTarifDasarHarian()));

        if (k instanceof Mobil) {
            Mobil m = (Mobil) k;
            spnDynamicSpec.setValue(m.getJumlahKursi());
            if ("Manual (MT)".equalsIgnoreCase(m.getJenisTransmisi())) {
                rdoMT.setSelected(true);
            } else {
                rdoAT.setSelected(true);
            }
            chkSupir.setSelected(m.isTermasukSupir());
        } else if (k instanceof Motor) {
            Motor mot = (Motor) k;
            spnDynamicSpec.setValue(mot.getKapasitasCC());
            cmbTipeMotor.setSelectedItem(mot.getTipeMotor());
            spnHelm.setValue(mot.getJumlahHelm());
        } else if (k instanceof Bus) {
            Bus b = (Bus) k;
            spnDynamicSpec.setValue(b.getKapasitasPenumpang());
            cmbKelasBus.setSelectedItem(b.getKelasBus());
            chkToilet.setSelected(b.isFasilitasToilet());
        }

        updateDynamicSpecFields();
    }

    private void resetFormToAddNew() {
        try {
            selectedKendaraanId = controller.generateNextKendaraanId();
        } catch (SQLException e) {
            selectedKendaraanId = "KD-NEW";
        }
        lblFormTitle.setText("Tambah kendaraan");
        lblFormSubtitle.setText(selectedKendaraanId + " / Unit baru");
        btnSimpan.setText("Simpan kendaraan");

        txtPlatNomor.setText("");
        txtMerk.setText("");
        txtTarif.setText("350000");
        spnTahun.setValue(2024);
        spnDynamicSpec.setValue(7);
        rdoAT.setSelected(true);
        chkSupir.setSelected(true);
        updateDynamicSpecFields();
    }

    private void simpanDataKendaraan() {
        try {
            String kat = (String) cmbKategori.getSelectedItem();
            String plat = txtPlatNomor.getText().trim();
            String merk = txtMerk.getText().trim();
            int thn = (Integer) spnTahun.getValue();

            String tarifStr = txtTarif.getText().replaceAll("[^0-9]", "");
            if (tarifStr.isEmpty()) throw new IllegalArgumentException("Tarif sewa tidak boleh kosong.");
            double tarif = Double.parseDouble(tarifStr);

            Integer kursi = null, cc = null, helm = null, penumpang = null;
            String transmisi = null, tipeMotor = null, kelasBus = null;
            Boolean supir = null, toilet = null;

            if ("Mobil".equalsIgnoreCase(kat)) {
                kursi = (Integer) spnDynamicSpec.getValue();
                transmisi = rdoAT.isSelected() ? "Otomatis (AT)" : "Manual (MT)";
                supir = chkSupir.isSelected();
            } else if ("Motor".equalsIgnoreCase(kat)) {
                cc = (Integer) spnDynamicSpec.getValue();
                tipeMotor = (String) cmbTipeMotor.getSelectedItem();
                helm = (Integer) spnHelm.getValue();
            } else if ("Bus".equalsIgnoreCase(kat)) {
                penumpang = (Integer) spnDynamicSpec.getValue();
                kelasBus = (String) cmbKelasBus.getSelectedItem();
                toilet = chkToilet.isSelected();
            }

            controller.simpanKendaraan(selectedKendaraanId, kat, plat, merk, thn, tarif, true,
                    kursi, transmisi, supir, cc, tipeMotor, helm, penumpang, kelasBus, toilet);

            CustomDialog.showSuccess(SwingUtilities.getWindowAncestor(this), "Sukses", "Data kendaraan " + merk + " berhasil disimpan!");
            loadData();
            mainFrame.getDashboardPanel().refreshData();
            mainFrame.getTransaksiPanel().loadDropdowns();

        } catch (SQLException dbEx) {
            CustomDialog.showDatabaseError(SwingUtilities.getWindowAncestor(this), this::simpanDataKendaraan);
        } catch (Exception ex) {
            CustomDialog.showWarning(SwingUtilities.getWindowAncestor(this), "Validasi Gagal", ex.getMessage(), "Periksa Data", () -> txtPlatNomor.requestFocus());
        }
    }

    private void confirmHapusKendaraan(Kendaraan k) {
        CustomDialog.showWarning(SwingUtilities.getWindowAncestor(this),
                "Hapus Armada Kendaraan?",
                "Armada " + k.getMerk() + " (" + k.getPlatNomor() + ") akan dihapus permanen.\nLanjutkan?",
                "Hapus",
                () -> {
                    try {
                        controller.hapusKendaraan(k.getIdKendaraan());
                        CustomDialog.showSuccess(SwingUtilities.getWindowAncestor(this), "Sukses", "Armada berhasil dihapus.");
                        loadData();
                        mainFrame.getDashboardPanel().refreshData();
                    } catch (SQLException dbEx) {
                        CustomDialog.showDatabaseError(SwingUtilities.getWindowAncestor(this), () -> confirmHapusKendaraan(k));
                    } catch (Exception ex) {
                        CustomDialog.showWarning(SwingUtilities.getWindowAncestor(this), "Gagal", ex.getMessage(), "Tutup", null);
                    }
                });
    }

    private void eksporData() {
        try {
            List<Kendaraan> all = controller.getAllKendaraan();
            File file = new File("ekspor_armada.csv");
            try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                pw.println("ID,Kategori,Merk,Plat Nomor,Tahun,Tarif/Hari,Status");
                for (Kendaraan k : all) {
                    pw.printf("%s,%s,\"%s\",%s,%d,%.0f,%s\n",
                            k.getIdKendaraan(), k.getKategori(), k.getMerk(), k.getPlatNomor(),
                            k.getTahun(), k.getTarifDasarHarian(), k.isStatusTersedia() ? "Tersedia" : "Disewa");
                }
            }
            CustomDialog.showSuccess(SwingUtilities.getWindowAncestor(this),
                    "Ekspor Sukses",
                    "Data armada berhasil diekspor ke file:\n" + file.getAbsolutePath());
        } catch (Exception ex) {
            CustomDialog.showWarning(SwingUtilities.getWindowAncestor(this), "Gagal Ekspor", ex.getMessage(), "Tutup", null);
        }
    }
}
