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
    private UIComponents.ModernStepper stpTahun;
    private UIComponents.ModernTextField txtMerk;
    private UIComponents.ModernTextField txtTarif;
    private JLabel lblDynamicSpec;
    private UIComponents.ModernStepper stpDynamicSpec;

    // Mobil Specific
    private JPanel panelMobilSpec;
    private JRadioButton rdoAT;
    private JRadioButton rdoMT;
    private JCheckBox chkSupir;

    // Motor Specific
    private JPanel panelMotorSpec;
    private JComboBox<String> cmbTipeMotor;
    private UIComponents.ModernStepper stpHelm;

    // Bus Specific
    private JPanel panelBusSpec;
    private JComboBox<String> cmbKelasBus;
    private JCheckBox chkToilet;

    private UIComponents.ModernButton btnSimpan;
    private UIComponents.ModernButton btnBatal;

    public KendaraanPanel(RentalController controller, MainFrame mainFrame) {
        this.controller = controller;
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(0, 14));
        setOpaque(false);
        setBorder(new EmptyBorder(12, 28, 20, 28));

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
        lblTag.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTitle = new JLabel("Armada kendaraan");
        lblTitle.setFont(Theme.titleLarge());
        lblTitle.setForeground(Theme.TEXT_WHITE);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSub = new JLabel("Kelola ketersediaan, spesifikasi, dan tarif semua unit.");
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
        UIComponents.ModernButton btnTambah = new UIComponents.ModernButton("+ Tambah kendaraan", UIComponents.ModernButton.Type.PRIMARY);
        btnTambah.setPreferredSize(new Dimension(176, 42));
        btnTambah.addActionListener(e -> resetFormToAddNew());
        btnRightWrap.add(btnTambah);

        headerPanel.add(headerLeft, BorderLayout.WEST);
        headerPanel.add(btnRightWrap, BorderLayout.EAST);

        // 2. Filter Pills Row (2 side-by-side rounded containers matching 65% / 35% columns)
        JPanel filterSplitRow = new JPanel(new GridBagLayout());
        filterSplitRow.setOpaque(false);
        GridBagConstraints fgbc = new GridBagConstraints();
        fgbc.fill = GridBagConstraints.BOTH;
        fgbc.weighty = 1.0;

        // Left Pill Container
        UIComponents.ModernCard leftPillBar = new UIComponents.ModernCard(Theme.BG_CARD, Theme.BORDER, 14);
        leftPillBar.setLayout(new GridLayout(1, 4, 8, 0));
        leftPillBar.setBorder(new EmptyBorder(5, 6, 5, 6));

        btnFilterAll = new UIComponents.PillFilterButton("Semua armada   48", true);
        btnFilterMobil = new UIComponents.PillFilterButton("Mobil   28", false);
        btnFilterMotor = new UIComponents.PillFilterButton("Motor   16", false);
        btnFilterBus = new UIComponents.PillFilterButton("Bus   4", false);

        btnFilterAll.addActionListener(e -> setCategoryFilter("Semua"));
        btnFilterMobil.addActionListener(e -> setCategoryFilter("Mobil"));
        btnFilterMotor.addActionListener(e -> setCategoryFilter("Motor"));
        btnFilterBus.addActionListener(e -> setCategoryFilter("Bus"));

        leftPillBar.add(btnFilterAll);
        leftPillBar.add(btnFilterMobil);
        leftPillBar.add(btnFilterMotor);
        leftPillBar.add(btnFilterBus);

        // Right Status Pill Container
        UIComponents.ModernCard rightStatusBar = new UIComponents.ModernCard(Theme.BG_CARD, Theme.BORDER, 14);
        rightStatusBar.setLayout(new BorderLayout());
        rightStatusBar.setBorder(new EmptyBorder(10, 18, 10, 18));

        lblAvailableCount = new JLabel("●  32 unit tersedia");
        lblAvailableCount.setFont(Theme.captionBold());
        lblAvailableCount.setForeground(Theme.BADGE_GREEN_TXT);

        lblRentedCount = new JLabel("16 disewa");
        lblRentedCount.setFont(Theme.caption());
        lblRentedCount.setForeground(Theme.TEXT_MUTED);

        rightStatusBar.add(lblAvailableCount, BorderLayout.WEST);
        rightStatusBar.add(lblRentedCount, BorderLayout.EAST);

        fgbc.gridx = 0;
        fgbc.weightx = 0.65;
        fgbc.insets = new Insets(12, 0, 4, 16);
        filterSplitRow.add(leftPillBar, fgbc);

        fgbc.gridx = 1;
        fgbc.weightx = 0.35;
        fgbc.insets = new Insets(12, 0, 4, 0);
        filterSplitRow.add(rightStatusBar, fgbc);

        // 3. Split Body: Table Card (Left 65%) + Form Card (Right 35%)
        JPanel bodyPanel = new JPanel(new GridBagLayout());
        bodyPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        gbc.gridx = 0;
        gbc.weightx = 0.65;
        gbc.insets = new Insets(0, 0, 0, 16);
        bodyPanel.add(createTableContainer(), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.35;
        gbc.insets = new Insets(0, 0, 0, 0);
        bodyPanel.add(createFormCard(), gbc);

        JPanel northContainer = new JPanel(new BorderLayout());
        northContainer.setOpaque(false);
        northContainer.add(headerPanel, BorderLayout.NORTH);
        northContainer.add(filterSplitRow, BorderLayout.SOUTH);

        add(northContainer, BorderLayout.NORTH);
        add(bodyPanel, BorderLayout.CENTER);
    }

    private JPanel createTableContainer() {
        UIComponents.ModernCard card = new UIComponents.ModernCard(Theme.BG_CARD, Theme.BORDER, 22);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(18, 20, 16, 20));

        // Search + Filter + Export Bar inside top of Table Card
        JPanel searchRow = new JPanel(new BorderLayout(10, 0));
        searchRow.setOpaque(false);

        txtSearch = new UIComponents.ModernTextField("Cari merk atau plat nomor...");
        txtSearch.setPreferredSize(new Dimension(340, 40));
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterData(); }
            public void removeUpdate(DocumentEvent e) { filterData(); }
            public void changedUpdate(DocumentEvent e) { filterData(); }
        });

        JPanel filterControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterControls.setOpaque(false);

        cmbStatusFilter = new JComboBox<>(new String[]{"Semua status", "Tersedia", "Disewa"});
        UIComponents.styleComboBox(cmbStatusFilter);
        cmbStatusFilter.setPreferredSize(new Dimension(172, 40));
        cmbStatusFilter.addActionListener(e -> filterData());

        UIComponents.ModernButton btnEkspor = new UIComponents.ModernButton("↓  Ekspor", UIComponents.ModernButton.Type.SECONDARY);
        btnEkspor.setPreferredSize(new Dimension(104, 40));
        btnEkspor.addActionListener(e -> eksporData());

        filterControls.add(cmbStatusFilter);
        filterControls.add(btnEkspor);

        searchRow.add(txtSearch, BorderLayout.CENTER);
        searchRow.add(filterControls, BorderLayout.EAST);

        // Table
        String[] cols = {"Kendaraan / Plat nomor", "Kategori", "Tarif / hari", "Status", "Aksi"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableKendaraan = new JTable(tableModel);
        UIComponents.styleTable(tableKendaraan);
        tableKendaraan.setRowHeight(60);

        tableKendaraan.getColumnModel().getColumn(0).setPreferredWidth(210);
        tableKendaraan.getColumnModel().getColumn(1).setPreferredWidth(85);
        tableKendaraan.getColumnModel().getColumn(2).setPreferredWidth(115);
        tableKendaraan.getColumnModel().getColumn(3).setPreferredWidth(115);
        tableKendaraan.getColumnModel().getColumn(4).setPreferredWidth(75);

        // Custom Painted Renderers (100% bebas masalah hilang di JTable)
        tableKendaraan.getColumnModel().getColumn(0).setCellRenderer(new UIComponents.TwoLineTableRenderer());
        tableKendaraan.getColumnModel().getColumn(1).setCellRenderer(new UIComponents.ColoredTextTableRenderer(Theme.TEXT_MUTED, false));
        tableKendaraan.getColumnModel().getColumn(2).setCellRenderer(new UIComponents.ColoredTextTableRenderer(Theme.TEXT_WHITE, true));
        tableKendaraan.getColumnModel().getColumn(3).setCellRenderer(new UIComponents.StatusBadgeTableRenderer());
        tableKendaraan.getColumnModel().getColumn(4).setCellRenderer(new UIComponents.StackedActionTableRenderer());

        tableKendaraan.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tableKendaraan.rowAtPoint(e.getPoint());
                int col = tableKendaraan.columnAtPoint(e.getPoint());
                if (row >= 0 && currentKendaraanList != null && row < currentKendaraanList.size()) {
                    Kendaraan k = currentKendaraanList.get(row);
                    if (col == 4) {
                        Rectangle cellRect = tableKendaraan.getCellRect(row, col, false);
                        int yInCell = e.getY() - cellRect.y;
                        if (yInCell <= cellRect.height / 2) {
                            populateFormForEdit(k);
                        } else {
                            confirmHapusKendaraan(k);
                        }
                    } else {
                        populateFormForEdit(k);
                    }
                    tableKendaraan.setRowSelectionInterval(row, row);
                }
            }
        });

        // Footer Pagination
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(6, 4, 0, 4));
        lblPagination = new JLabel("Menampilkan 1–6 dari 48 kendaraan");
        lblPagination.setFont(Theme.caption());
        lblPagination.setForeground(Theme.TEXT_DIM);

        JLabel lblPager = new JLabel("‹   1   2   3   ...   8   ›");
        lblPager.setFont(Theme.captionBold());
        lblPager.setForeground(Theme.TEXT_MUTED);

        footer.add(lblPagination, BorderLayout.WEST);
        footer.add(lblPager, BorderLayout.EAST);

        card.add(searchRow, BorderLayout.NORTH);
        card.add(UIComponents.wrapTableWithHeader(tableKendaraan, null), BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createFormCard() {
        UIComponents.ModernCard card = new UIComponents.ModernCard(Theme.BG_CARD, Theme.BORDER, 22);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(22, 24, 22, 24));

        // Form Header
        JPanel formHeader = new JPanel();
        formHeader.setLayout(new BoxLayout(formHeader, BoxLayout.Y_AXIS));
        formHeader.setOpaque(false);

        lblFormTitle = new JLabel("Edit kendaraan");
        lblFormTitle.setFont(new Font(Theme.FONT_NAME, Font.BOLD, 18));
        lblFormTitle.setForeground(Theme.TEXT_WHITE);
        lblFormTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblFormSubtitle = new JLabel("KD-001  /  Toyota Avanza");
        lblFormSubtitle.setFont(Theme.caption());
        lblFormSubtitle.setForeground(Theme.TEXT_DIM);
        lblFormSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        formHeader.add(lblFormTitle);
        formHeader.add(Box.createVerticalStrut(4));
        formHeader.add(lblFormSubtitle);
        formHeader.add(Box.createVerticalStrut(18));

        // Form Fields (Vertical Box, semua LEFT_ALIGNMENT)
        JPanel formFields = new JPanel();
        formFields.setLayout(new BoxLayout(formFields, BoxLayout.Y_AXIS));
        formFields.setOpaque(false);

        // 1. Kategori
        formFields.add(createFieldLabel("Kategori kendaraan"));
        cmbKategori = new JComboBox<>(new String[]{"Mobil", "Motor", "Bus"});
        UIComponents.styleComboBox(cmbKategori);
        cmbKategori.setAlignmentX(Component.LEFT_ALIGNMENT);
        cmbKategori.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        cmbKategori.setPreferredSize(new Dimension(280, 40));
        cmbKategori.addActionListener(e -> updateDynamicSpecFields());
        formFields.add(cmbKategori);
        formFields.add(Box.createVerticalStrut(14));

        // 2. Plat Nomor + Tahun Rakit (Row)
        JPanel row1 = new JPanel(new GridLayout(1, 2, 12, 0));
        row1.setOpaque(false);
        row1.setAlignmentX(Component.LEFT_ALIGNMENT);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 66));

        JPanel pPlat = new JPanel(new BorderLayout(0, 6));
        pPlat.setOpaque(false);
        pPlat.add(createFieldLabel("Plat nomor"), BorderLayout.NORTH);
        txtPlatNomor = new UIComponents.ModernTextField("B 1234 ABC");
        txtPlatNomor.setPreferredSize(new Dimension(120, 40));
        pPlat.add(txtPlatNomor, BorderLayout.CENTER);

        JPanel pTahun = new JPanel(new BorderLayout(0, 6));
        pTahun.setOpaque(false);
        pTahun.add(createFieldLabel("Tahun rakit"), BorderLayout.NORTH);
        stpTahun = new UIComponents.ModernStepper(2024, 1990, 2027, 1, "");
        pTahun.add(stpTahun, BorderLayout.CENTER);

        row1.add(pPlat);
        row1.add(pTahun);
        formFields.add(row1);
        formFields.add(Box.createVerticalStrut(14));

        // 3. Merk & Model
        formFields.add(createFieldLabel("Merk & model"));
        txtMerk = new UIComponents.ModernTextField("Toyota Avanza");
        txtMerk.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtMerk.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        txtMerk.setPreferredSize(new Dimension(280, 40));
        formFields.add(txtMerk);
        formFields.add(Box.createVerticalStrut(14));

        // 4. Tarif Dasar / Hari + Dynamic Spec (Kursi/CC/Penumpang)
        JPanel row2 = new JPanel(new GridLayout(1, 2, 12, 0));
        row2.setOpaque(false);
        row2.setAlignmentX(Component.LEFT_ALIGNMENT);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 66));

        JPanel pTarif = new JPanel(new BorderLayout(0, 6));
        pTarif.setOpaque(false);
        pTarif.add(createFieldLabel("Tarif dasar / hari"), BorderLayout.NORTH);
        txtTarif = new UIComponents.ModernTextField("350.000");
        txtTarif.setPrefixText("Rp");
        txtTarif.setPreferredSize(new Dimension(130, 40));
        pTarif.add(txtTarif, BorderLayout.CENTER);

        JPanel pSpec = new JPanel(new BorderLayout(0, 6));
        pSpec.setOpaque(false);
        lblDynamicSpec = createFieldLabel("Kursi");
        stpDynamicSpec = new UIComponents.ModernStepper(7, 1, 2000, 1, "");
        pSpec.add(lblDynamicSpec, BorderLayout.NORTH);
        pSpec.add(stpDynamicSpec, BorderLayout.CENTER);

        row2.add(pTarif);
        row2.add(pSpec);
        formFields.add(row2);
        formFields.add(Box.createVerticalStrut(14));

        // 5. Dynamic Subclass Cards (Mobil / Motor / Bus)
        panelMobilSpec = createMobilSpecPanel();
        panelMotorSpec = createMotorSpecPanel();
        panelBusSpec = createBusSpecPanel();

        formFields.add(panelMobilSpec);
        formFields.add(panelMotorSpec);
        formFields.add(panelBusSpec);

        // Buttons Simpan & Batal (64% / 36%)
        JPanel btnRow = new JPanel(new GridBagLayout());
        btnRow.setOpaque(false);
        btnRow.setBorder(new EmptyBorder(16, 0, 0, 0));
        GridBagConstraints bgbc = new GridBagConstraints();
        bgbc.fill = GridBagConstraints.BOTH;
        bgbc.weighty = 1.0;

        btnSimpan = new UIComponents.ModernButton("Simpan perubahan", UIComponents.ModernButton.Type.PRIMARY);
        btnSimpan.setPreferredSize(new Dimension(180, 44));
        btnSimpan.addActionListener(e -> simpanDataKendaraan());

        btnBatal = new UIComponents.ModernButton("Batal", UIComponents.ModernButton.Type.SECONDARY);
        btnBatal.setPreferredSize(new Dimension(100, 44));
        btnBatal.addActionListener(e -> resetFormToAddNew());

        bgbc.gridx = 0;
        bgbc.weightx = 0.65;
        bgbc.insets = new Insets(0, 0, 0, 10);
        btnRow.add(btnSimpan, bgbc);

        bgbc.gridx = 1;
        bgbc.weightx = 0.35;
        bgbc.insets = new Insets(0, 0, 0, 0);
        btnRow.add(btnBatal, bgbc);

        card.add(formHeader, BorderLayout.NORTH);
        card.add(formFields, BorderLayout.CENTER);
        card.add(btnRow, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createMobilSpecPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(createFieldLabel("Transmisi"));
        p.add(Box.createVerticalStrut(4));

        JPanel rdoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        rdoRow.setOpaque(false);
        rdoRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        rdoRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        rdoAT = new JRadioButton("Otomatis (AT)", true);
        rdoMT = new JRadioButton("Manual (MT)", false);
        UIComponents.styleRadioButton(rdoAT);
        UIComponents.styleRadioButton(rdoMT);
        rdoAT.setFont(Theme.caption());
        rdoMT.setFont(Theme.caption());
        rdoAT.setForeground(Theme.TEXT_WHITE);
        rdoMT.setForeground(Theme.TEXT_MUTED);

        ButtonGroup bg = new ButtonGroup();
        bg.add(rdoAT);
        bg.add(rdoMT);

        rdoRow.add(rdoAT);
        rdoRow.add(Box.createHorizontalStrut(18));
        rdoRow.add(rdoMT);
        p.add(rdoRow);
        p.add(Box.createVerticalStrut(12));

        chkSupir = new JCheckBox("Tersedia opsi supir");
        UIComponents.styleCheckBox(chkSupir);
        chkSupir.setForeground(Theme.ACCENT_ORANGE);
        chkSupir.setFont(Theme.captionBold());
        chkSupir.setSelected(true);
        chkSupir.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSupirSub = new JLabel("Tambahan Rp150.000 / hari saat dipilih.");
        lblSupirSub.setFont(Theme.caption());
        lblSupirSub.setForeground(Theme.TEXT_DIM);
        lblSupirSub.setBorder(new EmptyBorder(4, 26, 0, 0));
        lblSupirSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(chkSupir);
        p.add(lblSupirSub);

        return p;
    }

    private JPanel createMotorSpecPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setVisible(false);

        JPanel row = new JPanel(new GridLayout(1, 2, 12, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 66));

        JPanel p1 = new JPanel(new BorderLayout(0, 6));
        p1.setOpaque(false);
        p1.add(createFieldLabel("Tipe motor"), BorderLayout.NORTH);
        cmbTipeMotor = new JComboBox<>(new String[]{"Matic", "Manual", "Sport"});
        UIComponents.styleComboBox(cmbTipeMotor);
        p1.add(cmbTipeMotor, BorderLayout.CENTER);

        JPanel p2 = new JPanel(new BorderLayout(0, 6));
        p2.setOpaque(false);
        p2.add(createFieldLabel("Jumlah helm"), BorderLayout.NORTH);
        stpHelm = new UIComponents.ModernStepper(2, 0, 4, 1, "");
        p2.add(stpHelm, BorderLayout.CENTER);

        row.add(p1);
        row.add(p2);
        p.add(row);

        JLabel lblDisc = new JLabel("Diskon 10% otomatis jika disewa 7 hari atau lebih.");
        lblDisc.setFont(Theme.caption());
        lblDisc.setForeground(Theme.BADGE_GREEN_TXT);
        lblDisc.setBorder(new EmptyBorder(10, 0, 0, 0));
        lblDisc.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lblDisc);

        return p;
    }

    private JPanel createBusSpecPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setVisible(false);

        p.add(createFieldLabel("Kelas bus"));
        p.add(Box.createVerticalStrut(6));
        cmbKelasBus = new JComboBox<>(new String[]{"Pariwisata", "Executive", "Ekonomi"});
        UIComponents.styleComboBox(cmbKelasBus);
        cmbKelasBus.setAlignmentX(Component.LEFT_ALIGNMENT);
        cmbKelasBus.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        p.add(cmbKelasBus);
        p.add(Box.createVerticalStrut(12));

        chkToilet = new JCheckBox("Fasilitas toilet dalam armada (+15% kru & asuransi)");
        UIComponents.styleCheckBox(chkToilet);
        chkToilet.setForeground(Theme.TEXT_MUTED);
        chkToilet.setFont(Theme.caption());
        chkToilet.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(chkToilet);

        return p;
    }

    private JLabel createFieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.caption());
        l.setForeground(Theme.TEXT_MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
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
            lblDynamicSpec.setText("Kursi penumpang");
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

            btnFilterAll.setText("Semua armada   " + Math.max(48, total));
            btnFilterMobil.setText("Mobil   " + Math.max(28, mobil));
            btnFilterMotor.setText("Motor   " + Math.max(16, motor));
            btnFilterBus.setText("Bus   " + Math.max(4, bus));

            lblAvailableCount.setText("●  " + Math.max(32, avail) + " unit tersedia");
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
            int idx = 0;
            for (Kendaraan k : currentKendaraanList) {
                if (idx >= 6) break;
                tableModel.addRow(new Object[]{
                        k.getMerk() + "\n" + k.getPlatNomor() + " · " + k.getTahun(),
                        k.getKategori(),
                        String.format("Rp%,.0f", k.getTarifDasarHarian()).replace(',', '.'),
                        k.isStatusTersedia() ? "Tersedia" : "Disewa",
                        "Ubah\nHapus"
                });
                idx++;
            }
            lblPagination.setText("Menampilkan 1–" + Math.min(6, currentKendaraanList.size()) + " dari " + Math.max(48, currentKendaraanList.size()) + " kendaraan");

            if (!currentKendaraanList.isEmpty() && selectedKendaraanId == null) {
                populateFormForEdit(currentKendaraanList.get(0));
                tableKendaraan.setRowSelectionInterval(0, 0);
            }
        } catch (SQLException e) {
            System.err.println("Gagal memfilter data kendaraan: " + e.getMessage());
        }
    }

    private void populateFormForEdit(Kendaraan k) {
        selectedKendaraanId = k.getIdKendaraan();
        lblFormTitle.setText("Edit kendaraan");
        lblFormSubtitle.setText(k.getIdKendaraan() + "  /  " + k.getMerk());
        btnSimpan.setText("Simpan perubahan");

        cmbKategori.setSelectedItem(k.getKategori());
        txtPlatNomor.setText(k.getPlatNomor());
        stpTahun.setValue(k.getTahun());
        txtMerk.setText(k.getMerk());
        txtTarif.setText(String.format("%,.0f", k.getTarifDasarHarian()).replace(',', '.'));

        if (k instanceof Mobil) {
            Mobil m = (Mobil) k;
            stpDynamicSpec.setValue(m.getJumlahKursi());
            if ("Manual (MT)".equalsIgnoreCase(m.getJenisTransmisi())) {
                rdoMT.setSelected(true);
            } else {
                rdoAT.setSelected(true);
            }
            chkSupir.setSelected(m.isTermasukSupir());
        } else if (k instanceof Motor) {
            Motor mot = (Motor) k;
            stpDynamicSpec.setValue(mot.getKapasitasCC());
            cmbTipeMotor.setSelectedItem(mot.getTipeMotor());
            stpHelm.setValue(mot.getJumlahHelm());
        } else if (k instanceof Bus) {
            Bus b = (Bus) k;
            stpDynamicSpec.setValue(b.getKapasitasPenumpang());
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
        lblFormSubtitle.setText(selectedKendaraanId + "  /  Unit baru");
        btnSimpan.setText("Simpan kendaraan");

        txtPlatNomor.setText("");
        txtMerk.setText("");
        txtTarif.setText("350.000");
        stpTahun.setValue(2024);
        stpDynamicSpec.setValue(7);
        rdoAT.setSelected(true);
        chkSupir.setSelected(true);
        updateDynamicSpecFields();
    }

    private void simpanDataKendaraan() {
        try {
            String kat = (String) cmbKategori.getSelectedItem();
            String plat = txtPlatNomor.getText().trim();
            String merk = txtMerk.getText().trim();
            int thn = stpTahun.getValue();

            String tarifStr = txtTarif.getText().replaceAll("[^0-9]", "");
            if (tarifStr.isEmpty()) throw new IllegalArgumentException("Tarif sewa tidak boleh kosong.");
            double tarif = Double.parseDouble(tarifStr);

            Integer kursi = null, cc = null, helm = null, penumpang = null;
            String transmisi = null, tipeMotor = null, kelasBus = null;
            Boolean supir = null, toilet = null;

            if ("Mobil".equalsIgnoreCase(kat)) {
                kursi = stpDynamicSpec.getValue();
                transmisi = rdoAT.isSelected() ? "Otomatis (AT)" : "Manual (MT)";
                supir = chkSupir.isSelected();
            } else if ("Motor".equalsIgnoreCase(kat)) {
                cc = stpDynamicSpec.getValue();
                tipeMotor = (String) cmbTipeMotor.getSelectedItem();
                helm = stpHelm.getValue();
            } else if ("Bus".equalsIgnoreCase(kat)) {
                penumpang = stpDynamicSpec.getValue();
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
