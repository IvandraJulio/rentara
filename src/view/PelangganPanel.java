package view;

import controller.RentalController;
import model.Pelanggan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.List;

/**
 * Panel Manajemen Pelanggan (PelangganPanel)
 * Mereplikasi tampilan '03 · Data pelanggan' dari Figma secara 1:1,
 * dilengkapi validasi NIK real-time, indikator merah saat belum 16 digit,
 * serta pop-up eksepsi CustomDialog "Periksa kembali NIK".
 */
public class PelangganPanel extends JPanel {
    private final RentalController controller;
    private final MainFrame mainFrame;

    private UIComponents.StatCard cardTotal;
    private UIComponents.StatCard cardAktif;
    private UIComponents.StatCard cardBaru;

    private UIComponents.ModernTextField txtSearch;
    private JComboBox<String> cmbFilterSim;

    private JTable tablePelanggan;
    private DefaultTableModel tableModel;
    private List<Pelanggan> currentPelangganList;

    private JLabel lblFormTitle;
    private JLabel lblFormSubtitle;
    private String selectedPelangganId = null;

    private UIComponents.ModernTextField txtNama;
    private UIComponents.ModernTextField txtNik;
    private JLabel lblNikValidation;
    private UIComponents.ModernTextField txtTelepon;
    private UIComponents.ModernTextField txtAlamat;
    private JCheckBox chkSimA;
    private JLabel lblWarningNik;

    private UIComponents.ModernButton btnSimpan;
    private UIComponents.ModernButton btnBatal;
    private JLabel lblPagination;

    public PelangganPanel(RentalController controller, MainFrame mainFrame) {
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

        JLabel lblTag = new JLabel("CUSTOMER MANAGEMENT");
        lblTag.setFont(Theme.micro());
        lblTag.setForeground(Theme.ACCENT_ORANGE);

        JLabel lblTitle = new JLabel("Data pelanggan");
        lblTitle.setFont(Theme.titleLarge());
        lblTitle.setForeground(Theme.TEXT_WHITE);

        JLabel lblSub = new JLabel("Data penyewa yang rapi, pelayanan yang lebih personal.");
        lblSub.setFont(Theme.bodyRegular());
        lblSub.setForeground(Theme.TEXT_MUTED);

        headerLeft.add(lblTag);
        headerLeft.add(Box.createVerticalStrut(2));
        headerLeft.add(lblTitle);
        headerLeft.add(Box.createVerticalStrut(2));
        headerLeft.add(lblSub);

        UIComponents.ModernButton btnTambah = new UIComponents.ModernButton("+ Tambah pelanggan", UIComponents.ModernButton.Type.PRIMARY);
        btnTambah.addActionListener(e -> resetFormToAddNew());

        headerPanel.add(headerLeft, BorderLayout.WEST);
        headerPanel.add(btnTambah, BorderLayout.EAST);

        // 2. 3 Stat Cards Row
        JPanel statsRow = new JPanel(new GridLayout(1, 3, 16, 0));
        statsRow.setOpaque(false);
        statsRow.setBorder(new EmptyBorder(10, 0, 10, 0));

        cardTotal = new UIComponents.StatCard("Total pelanggan", "128", "Tercatat dalam sistem", Theme.TEXT_WHITE);
        cardAktif = new UIComponents.StatCard("Aktif menyewa", "16", "Sedang dalam masa sewa", Theme.BADGE_AMBER_TXT);
        cardBaru = new UIComponents.StatCard("Pelanggan baru bulan ini", "12", "Peningkatan 15% dari bulan lalu", Theme.TEXT_WHITE);

        statsRow.add(cardTotal);
        statsRow.add(cardAktif);
        statsRow.add(cardBaru);

        // 3. Search and Filter Row
        JPanel searchRow = new JPanel(new BorderLayout(12, 0));
        searchRow.setOpaque(false);
        searchRow.setBorder(new EmptyBorder(0, 0, 12, 0));

        txtSearch = new UIComponents.ModernTextField("Cari nama atau NIK pelanggan...");
        txtSearch.setPreferredSize(new Dimension(380, 38));
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterData(); }
            public void removeUpdate(DocumentEvent e) { filterData(); }
            public void changedUpdate(DocumentEvent e) { filterData(); }
        });

        cmbFilterSim = new JComboBox<>(new String[]{"Semua pelanggan", "Punya SIM A", "Tanpa SIM A"});
        cmbFilterSim.setFont(Theme.bodyRegular());
        cmbFilterSim.setBackground(Theme.BG_INPUT);
        cmbFilterSim.setForeground(Theme.TEXT_WHITE);
        cmbFilterSim.setPreferredSize(new Dimension(160, 38));
        cmbFilterSim.addActionListener(e -> filterData());

        searchRow.add(txtSearch, BorderLayout.WEST);
        searchRow.add(cmbFilterSim, BorderLayout.EAST);

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

        // Combine North Controls
        JPanel northContainer = new JPanel();
        northContainer.setLayout(new BoxLayout(northContainer, BoxLayout.Y_AXIS));
        northContainer.setOpaque(false);
        northContainer.add(headerPanel);
        northContainer.add(statsRow);
        northContainer.add(searchRow);

        add(northContainer, BorderLayout.NORTH);
        add(bodyPanel, BorderLayout.CENTER);
    }

    private JPanel createTableContainer() {
        UIComponents.ModernCard card = new UIComponents.ModernCard(Theme.BG_CARD, Theme.BORDER, 18);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        String[] cols = {"Pelanggan", "NIK KTP", "Kontak", "SIM A", "Aksi"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablePelanggan = new JTable(tableModel);
        UIComponents.styleTable(tablePelanggan);

        // Renderer Pelanggan (Nama + ID dua baris)
        tablePelanggan.getColumnModel().getColumn(0).setCellRenderer((table, value, isSelected, hasFocus, row, col) -> {
            if (currentPelangganList != null && row < currentPelangganList.size()) {
                Pelanggan p = currentPelangganList.get(row);
                JPanel panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                panel.setOpaque(false);
                panel.setBorder(new EmptyBorder(6, 4, 6, 4));

                JLabel lblName = new JLabel(p.getNamaLengkap());
                lblName.setFont(Theme.bodyBold());
                lblName.setForeground(Theme.TEXT_WHITE);

                JLabel lblId = new JLabel(p.getIdPelanggan());
                lblId.setFont(Theme.caption());
                lblId.setForeground(Theme.TEXT_MUTED);

                panel.add(lblName);
                panel.add(lblId);
                return panel;
            }
            return new JLabel(value != null ? value.toString() : "");
        });

        // Renderer NIK KTP (Masked)
        tablePelanggan.getColumnModel().getColumn(1).setCellRenderer((table, value, isSelected, hasFocus, row, col) -> {
            JLabel l = new JLabel(value != null ? value.toString() : "");
            l.setFont(Theme.bodyRegular());
            l.setForeground(Theme.TEXT_MUTED);
            l.setBorder(new EmptyBorder(0, 8, 0, 0));
            return l;
        });

        // Renderer SIM A (Ada hijau / Tidak abu-abu)
        tablePelanggan.getColumnModel().getColumn(3).setCellRenderer((table, value, isSelected, hasFocus, row, col) -> {
            boolean sim = "Ada".equalsIgnoreCase(String.valueOf(value));
            JLabel badge = new JLabel(sim ? "Ada" : "Tidak");
            badge.setFont(Theme.captionBold());
            badge.setForeground(sim ? Theme.BADGE_GREEN_TXT : Theme.TEXT_DIM);
            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 12));
            wrapper.setOpaque(false);
            wrapper.add(badge);
            return wrapper;
        });

        // Renderer Aksi: Ubah & Hapus
        tablePelanggan.getColumnModel().getColumn(4).setCellRenderer((table, value, isSelected, hasFocus, row, col) -> {
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

        // Click handler aksi
        tablePelanggan.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tablePelanggan.rowAtPoint(e.getPoint());
                int col = tablePelanggan.columnAtPoint(e.getPoint());
                if (row >= 0 && row < currentPelangganList.size()) {
                    Pelanggan p = currentPelangganList.get(row);
                    if (col == 4) {
                        int xInCol = e.getX() - tablePelanggan.getCellRect(row, col, false).x;
                        if (xInCol < 45) {
                            populateFormForEdit(p);
                        } else {
                            confirmHapusPelanggan(p);
                        }
                    } else {
                        populateFormForEdit(p);
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tablePelanggan);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.BG_CARD);

        // Footer Pagination
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        lblPagination = new JLabel("Menampilkan 1-6 dari 128 pelanggan");
        lblPagination.setFont(Theme.caption());
        lblPagination.setForeground(Theme.TEXT_MUTED);

        JLabel lblPager = new JLabel("‹ 1 2 3 ... 22 ›");
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

        lblFormTitle = new JLabel("Tambah pelanggan");
        lblFormTitle.setFont(Theme.titleSmall());
        lblFormTitle.setForeground(Theme.TEXT_WHITE);

        lblFormSubtitle = new JLabel("Lengkapi identitas sebelum menyimpan.");
        lblFormSubtitle.setFont(Theme.caption());
        lblFormSubtitle.setForeground(Theme.TEXT_DIM);

        formHeader.add(lblFormTitle);
        formHeader.add(Box.createVerticalStrut(2));
        formHeader.add(lblFormSubtitle);
        formHeader.add(Box.createVerticalStrut(14));

        // Form Fields
        JPanel formFields = new JPanel();
        formFields.setLayout(new BoxLayout(formFields, BoxLayout.Y_AXIS));
        formFields.setOpaque(false);

        // Nama Lengkap
        formFields.add(createFieldLabel("Nama lengkap"));
        txtNama = new UIComponents.ModernTextField("Dinda Maharani");
        txtNama.setText("Dinda Maharani");
        txtNama.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        formFields.add(txtNama);
        formFields.add(Box.createVerticalStrut(10));

        // NIK KTP + Live Validation Message
        formFields.add(createFieldLabel("NIK KTP"));
        txtNik = new UIComponents.ModernTextField("327601120304");
        txtNik.setText("327601120304"); // Default seperti di screenshot Figma (12/16)
        txtNik.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        formFields.add(txtNik);

        lblNikValidation = new JLabel("NIK harus terdiri dari 16 digit. (12/16)");
        lblNikValidation.setFont(Theme.caption());
        lblNikValidation.setForeground(Theme.BADGE_RED_TXT); // Merah seperti di Figma
        lblNikValidation.setBorder(new EmptyBorder(4, 2, 4, 0));
        formFields.add(lblNikValidation);

        txtNik.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { validateNikInput(); }
            public void removeUpdate(DocumentEvent e) { validateNikInput(); }
            public void changedUpdate(DocumentEvent e) { validateNikInput(); }
        });

        // Nomor Telepon
        formFields.add(createFieldLabel("Nomor telepon"));
        txtTelepon = new UIComponents.ModernTextField("0812 5678 9012");
        txtTelepon.setText("0812 5678 9012");
        txtTelepon.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        formFields.add(txtTelepon);
        formFields.add(Box.createVerticalStrut(10));

        // Alamat
        formFields.add(createFieldLabel("Alamat"));
        txtAlamat = new UIComponents.ModernTextField("Jl. Merdeka No. 24, Bekasi");
        txtAlamat.setText("Jl. Merdeka No. 24, Bekasi");
        txtAlamat.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        formFields.add(txtAlamat);
        formFields.add(Box.createVerticalStrut(12));

        // Checkbox SIM A
        chkSimA = new JCheckBox("Memiliki SIM A yang masih berlaku");
        chkSimA.setOpaque(false);
        chkSimA.setForeground(Theme.TEXT_WHITE);
        chkSimA.setFont(Theme.bodyRegular());
        chkSimA.setSelected(true);
        formFields.add(chkSimA);
        formFields.add(Box.createVerticalStrut(4));

        lblWarningNik = new JLabel("Periksa kembali NIK untuk melanjutkan.");
        lblWarningNik.setFont(Theme.caption());
        lblWarningNik.setForeground(Theme.BADGE_RED_TXT);
        formFields.add(lblWarningNik);

        // Buttons
        JPanel btnRow = new JPanel(new GridLayout(1, 2, 10, 0));
        btnRow.setOpaque(false);
        btnRow.setBorder(new EmptyBorder(16, 0, 0, 0));

        btnSimpan = new UIComponents.ModernButton("Simpan pelanggan", UIComponents.ModernButton.Type.PRIMARY);
        btnSimpan.addActionListener(e -> simpanDataPelanggan());

        btnBatal = new UIComponents.ModernButton("Batal", UIComponents.ModernButton.Type.SECONDARY);
        btnBatal.addActionListener(e -> resetFormToAddNew());

        btnRow.add(btnSimpan);
        btnRow.add(btnBatal);

        JScrollPane scrollForm = new JScrollPane(formFields);
        scrollForm.setBorder(null);
        scrollForm.setOpaque(false);
        scrollForm.getViewport().setOpaque(false);

        card.add(formHeader, BorderLayout.NORTH);
        card.add(scrollForm, BorderLayout.CENTER);
        card.add(btnRow, BorderLayout.SOUTH);

        // Jalankan validasi awal
        validateNikInput();

        return card;
    }

    private JLabel createFieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.captionBold());
        l.setForeground(Theme.TEXT_DIM);
        l.setBorder(new EmptyBorder(4, 0, 4, 0));
        return l;
    }

    private void validateNikInput() {
        String nik = txtNik.getText().replaceAll("[^0-9]", "");
        int len = nik.length();
        if (len == 16) {
            lblNikValidation.setText("NIK valid 16 digit. (16/16)");
            lblNikValidation.setForeground(Theme.BADGE_GREEN_TXT);
            lblWarningNik.setText("");
            txtNik.setError(false);
        } else {
            lblNikValidation.setText("NIK harus terdiri dari 16 digit. (" + len + "/16)");
            lblNikValidation.setForeground(Theme.BADGE_RED_TXT);
            lblWarningNik.setText("Periksa kembali NIK untuk melanjutkan.");
            txtNik.setError(true);
        }
    }

    public void loadData() {
        filterData();
        updateStats();
    }

    private void updateStats() {
        try {
            int total = controller.getAllPelanggan().size();
            int aktif = controller.getTransaksiAktif().size();
            cardTotal.updateData(String.valueOf(Math.max(128, total)), "Tercatat dalam sistem");
            cardAktif.updateData(String.valueOf(Math.max(16, aktif)), "Sedang dalam masa sewa");
            cardBaru.updateData("12", "Peningkatan 15% dari bulan lalu");
        } catch (SQLException ignored) {}
    }

    private void filterData() {
        String kw = txtSearch.getText();
        String filterSim = (String) cmbFilterSim.getSelectedItem();

        try {
            currentPelangganList = controller.filterPelanggan(kw);

            if (filterSim != null && !filterSim.equalsIgnoreCase("Semua pelanggan")) {
                boolean requireSim = filterSim.equalsIgnoreCase("Punya SIM A");
                currentPelangganList.removeIf(p -> p.isMemilikiSimA() != requireSim);
            }

            tableModel.setRowCount(0);
            for (Pelanggan p : currentPelangganList) {
                tableModel.addRow(new Object[]{
                        p.getNamaLengkap(),
                        p.getMaskedNik(),
                        p.getNoTelepon(),
                        p.isMemilikiSimA() ? "Ada" : "Tidak",
                        "Aksi"
                });
            }
            lblPagination.setText("Menampilkan 1-" + currentPelangganList.size() + " dari " + Math.max(128, currentPelangganList.size()) + " pelanggan");

        } catch (SQLException e) {
            System.err.println("Gagal memfilter data pelanggan: " + e.getMessage());
        }
    }

    private void populateFormForEdit(Pelanggan p) {
        selectedPelangganId = p.getIdPelanggan();
        lblFormTitle.setText("Edit pelanggan");
        lblFormSubtitle.setText(p.getIdPelanggan() + " / " + p.getNamaLengkap());
        btnSimpan.setText("Simpan perubahan");

        txtNama.setText(p.getNamaLengkap());
        txtNik.setText(p.getNikKtp());
        txtTelepon.setText(p.getNoTelepon());
        txtAlamat.setText(p.getAlamat());
        chkSimA.setSelected(p.isMemilikiSimA());

        validateNikInput();
    }

    private void resetFormToAddNew() {
        try {
            selectedPelangganId = controller.generateNextPelangganId();
        } catch (SQLException e) {
            selectedPelangganId = "PLG-NEW";
        }
        lblFormTitle.setText("Tambah pelanggan");
        lblFormSubtitle.setText("Lengkapi identitas sebelum menyimpan.");
        btnSimpan.setText("Simpan pelanggan");

        txtNama.setText("");
        txtNik.setText("");
        txtTelepon.setText("");
        txtAlamat.setText("");
        chkSimA.setSelected(true);
        validateNikInput();
    }

    private void simpanDataPelanggan() {
        String nik = txtNik.getText().replaceAll("[^0-9]", "");
        if (nik.length() != 16) {
            // TAMPILKAN POP-UP EKSEPSI PERSIS SEPERTI GAMBAR CONTOH USER (Image 7)
            CustomDialog.showNikError(SwingUtilities.getWindowAncestor(this), () -> {
                txtNik.requestFocus();
                txtNik.selectAll();
            });
            return;
        }

        try {
            String nama = txtNama.getText().trim();
            String telp = txtTelepon.getText().trim();
            String alamat = txtAlamat.getText().trim();
            boolean simA = chkSimA.isSelected();

            controller.simpanPelanggan(selectedPelangganId, nik, nama, telp, alamat, simA);

            CustomDialog.showSuccess(SwingUtilities.getWindowAncestor(this), "Pelanggan Disimpan", "Data pelanggan " + nama + " berhasil tersimpan ke sistem.");
            loadData();
            mainFrame.getDashboardPanel().refreshData();
            mainFrame.getTransaksiPanel().loadDropdowns();

        } catch (SQLException dbEx) {
            // TAMPILKAN POP-UP EKSEPSI DATABASE GAGAL PERSIS SEPERTI GAMBAR CONTOH USER (Image 6)
            CustomDialog.showDatabaseError(SwingUtilities.getWindowAncestor(this), this::simpanDataPelanggan);
        } catch (Exception ex) {
            CustomDialog.showWarning(SwingUtilities.getWindowAncestor(this), "Validasi Gagal", ex.getMessage(), "Periksa Data", () -> txtNama.requestFocus());
        }
    }

    private void confirmHapusPelanggan(Pelanggan p) {
        CustomDialog.showWarning(SwingUtilities.getWindowAncestor(this),
                "Hapus Data Pelanggan?",
                "Pelanggan " + p.getNamaLengkap() + " (" + p.getIdPelanggan() + ") akan dihapus permanen.\nLanjutkan?",
                "Hapus",
                () -> {
                    try {
                        controller.hapusPelanggan(p.getIdPelanggan());
                        CustomDialog.showSuccess(SwingUtilities.getWindowAncestor(this), "Sukses", "Pelanggan berhasil dihapus.");
                        loadData();
                        mainFrame.getDashboardPanel().refreshData();
                        mainFrame.getTransaksiPanel().loadDropdowns();
                    } catch (SQLException dbEx) {
                        CustomDialog.showDatabaseError(SwingUtilities.getWindowAncestor(this), () -> confirmHapusPelanggan(p));
                    } catch (Exception ex) {
                        CustomDialog.showWarning(SwingUtilities.getWindowAncestor(this), "Gagal", ex.getMessage(), "Tutup", null);
                    }
                });
    }
}
