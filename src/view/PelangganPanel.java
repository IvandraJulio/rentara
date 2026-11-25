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
@SuppressWarnings("serial")
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

        JLabel lblTag = new JLabel("CUSTOMER MANAGEMENT");
        lblTag.setFont(Theme.micro());
        lblTag.setForeground(Theme.ACCENT_ORANGE);
        lblTag.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTitle = new JLabel("Data pelanggan");
        lblTitle.setFont(Theme.titleLarge());
        lblTitle.setForeground(Theme.TEXT_WHITE);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSub = new JLabel("Data penyewa yang rapi, pelayanan yang lebih personal.");
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
        UIComponents.ModernButton btnTambah = new UIComponents.ModernButton("+ Tambah pelanggan", UIComponents.ModernButton.Type.PRIMARY);
        btnTambah.setPreferredSize(new Dimension(176, 42));
        btnTambah.addActionListener(e -> resetFormToAddNew());
        btnRightWrap.add(btnTambah);

        headerPanel.add(headerLeft, BorderLayout.WEST);
        headerPanel.add(btnRightWrap, BorderLayout.EAST);

        // 2. 3 Stat Cards Row
        JPanel statsRow = new JPanel(new GridLayout(1, 3, 16, 0));
        statsRow.setOpaque(false);
        statsRow.setBorder(new EmptyBorder(12, 0, 4, 0));
        statsRow.setPreferredSize(new Dimension(0, 114));

        cardTotal = new UIComponents.StatCard("Total pelanggan", "0", "", Theme.TEXT_WHITE);
        cardAktif = new UIComponents.StatCard("Aktif menyewa", "0", "", Theme.ACCENT_ORANGE);
        cardBaru = new UIComponents.StatCard("Memiliki SIM A", "0", "", Theme.BADGE_GREEN_TXT);

        statsRow.add(cardTotal);
        statsRow.add(cardAktif);
        statsRow.add(cardBaru);

        // 3. Split Body: Table (Left 65%) + Form Card (Right 35%)
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
        northContainer.add(statsRow, BorderLayout.SOUTH);

        add(northContainer, BorderLayout.NORTH);
        add(bodyPanel, BorderLayout.CENTER);
    }

    private JPanel createTableContainer() {
        UIComponents.ModernCard card = new UIComponents.ModernCard(Theme.BG_CARD, Theme.BORDER, 22);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(18, 20, 16, 20));

        // Search and Filter Row inside top of Table Card
        JPanel searchRow = new JPanel(new BorderLayout(12, 0));
        searchRow.setOpaque(false);

        txtSearch = new UIComponents.ModernTextField("Cari nama atau NIK pelanggan...");
        txtSearch.setPreferredSize(new Dimension(380, 40));
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterData(); }
            public void removeUpdate(DocumentEvent e) { filterData(); }
            public void changedUpdate(DocumentEvent e) { filterData(); }
        });

        cmbFilterSim = new JComboBox<>(new String[]{"Semua pelanggan", "Punya SIM A", "Tanpa SIM A"});
        UIComponents.styleComboBox(cmbFilterSim);
        cmbFilterSim.setPreferredSize(new Dimension(210, 40));
        cmbFilterSim.addActionListener(e -> filterData());

        searchRow.add(txtSearch, BorderLayout.CENTER);
        searchRow.add(cmbFilterSim, BorderLayout.EAST);

        String[] cols = {"ID", "Nama Pelanggan", "NIK KTP", "Kontak", "SIM A", "Aksi"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablePelanggan = new JTable(tableModel);
        UIComponents.styleTable(tablePelanggan);
        tablePelanggan.setRowHeight(48);

        tablePelanggan.getColumnModel().getColumn(0).setPreferredWidth(80);
        tablePelanggan.getColumnModel().getColumn(1).setPreferredWidth(170);
        tablePelanggan.getColumnModel().getColumn(2).setPreferredWidth(155);
        tablePelanggan.getColumnModel().getColumn(3).setPreferredWidth(125);
        tablePelanggan.getColumnModel().getColumn(4).setPreferredWidth(65);
        tablePelanggan.getColumnModel().getColumn(5).setPreferredWidth(125);

        // Custom Painted Renderers
        tablePelanggan.getColumnModel().getColumn(0).setCellRenderer(new UIComponents.ColoredTextTableRenderer(Theme.TEXT_MUTED, true));
        tablePelanggan.getColumnModel().getColumn(1).setCellRenderer(new UIComponents.ColoredTextTableRenderer(Theme.TEXT_WHITE, true));
        tablePelanggan.getColumnModel().getColumn(2).setCellRenderer(new UIComponents.ColoredTextTableRenderer(Theme.TEXT_MUTED, false));
        tablePelanggan.getColumnModel().getColumn(3).setCellRenderer(new UIComponents.ColoredTextTableRenderer(Theme.TEXT_WHITE, false));
        tablePelanggan.getColumnModel().getColumn(4).setCellRenderer(new UIComponents.ColoredTextTableRenderer(null, true));
        tablePelanggan.getColumnModel().getColumn(5).setCellRenderer(new UIComponents.StackedActionTableRenderer());

        tablePelanggan.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tablePelanggan.rowAtPoint(e.getPoint());
                int col = tablePelanggan.columnAtPoint(e.getPoint());
                if (row >= 0 && currentPelangganList != null && row < currentPelangganList.size()) {
                    Pelanggan p = currentPelangganList.get(row);
                    tablePelanggan.setRowSelectionInterval(row, row);
                    if (col == 5) {
                        Rectangle cellRect = tablePelanggan.getCellRect(row, col, false);
                        int xInCell = e.getX() - cellRect.x;
                        if (xInCell <= 59) {
                            populateFormForEdit(p);
                        } else {
                            confirmHapusPelanggan(p);
                        }
                    } else if (e.getClickCount() == 2) {
                        populateFormForEdit(p);
                    }
                }
            }
        });

        // Footer Info Data
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(6, 4, 0, 4));
        lblPagination = new JLabel("Menampilkan 0 pelanggan");
        lblPagination.setFont(Theme.caption());
        lblPagination.setForeground(Theme.TEXT_DIM);

        footer.add(lblPagination, BorderLayout.WEST);

        card.add(searchRow, BorderLayout.NORTH);
        card.add(UIComponents.wrapTableWithHeader(tablePelanggan, null), BorderLayout.CENTER);
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

        lblFormTitle = new JLabel("Tambah pelanggan");
        lblFormTitle.setFont(new Font(Theme.FONT_NAME, Font.BOLD, 18));
        lblFormTitle.setForeground(Theme.TEXT_WHITE);
        lblFormTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblFormSubtitle = new JLabel("Lengkapi identitas sebelum menyimpan.");
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

        // Nama Lengkap
        formFields.add(createFieldLabel("Nama lengkap"));
        formFields.add(Box.createVerticalStrut(6));
        txtNama = new UIComponents.ModernTextField("Masukkan nama lengkap");
        txtNama.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtNama.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        txtNama.setPreferredSize(new Dimension(280, 40));
        formFields.add(txtNama);
        formFields.add(Box.createVerticalStrut(14));

        // NIK KTP + Live Validation Message
        formFields.add(createFieldLabel("NIK KTP"));
        formFields.add(Box.createVerticalStrut(6));
        txtNik = new UIComponents.ModernTextField("Masukkan 16 digit NIK KTP");
        txtNik.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtNik.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        txtNik.setPreferredSize(new Dimension(280, 40));
        formFields.add(txtNik);

        lblNikValidation = new JLabel("Masukkan 16 digit angka NIK KTP. (0/16)");
        lblNikValidation.setFont(Theme.caption());
        lblNikValidation.setForeground(Theme.TEXT_DIM);
        lblNikValidation.setBorder(new EmptyBorder(6, 2, 8, 0));
        lblNikValidation.setAlignmentX(Component.LEFT_ALIGNMENT);
        formFields.add(lblNikValidation);

        txtNik.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { validateNikInput(); }
            public void removeUpdate(DocumentEvent e) { validateNikInput(); }
            public void changedUpdate(DocumentEvent e) { validateNikInput(); }
        });

        // Nomor Telepon
        formFields.add(createFieldLabel("Nomor telepon"));
        formFields.add(Box.createVerticalStrut(6));
        txtTelepon = new UIComponents.ModernTextField("Contoh: 081234567890");
        txtTelepon.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtTelepon.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        txtTelepon.setPreferredSize(new Dimension(280, 40));
        formFields.add(txtTelepon);
        formFields.add(Box.createVerticalStrut(14));

        // Alamat
        formFields.add(createFieldLabel("Alamat"));
        formFields.add(Box.createVerticalStrut(6));
        txtAlamat = new UIComponents.ModernTextField("Masukkan alamat lengkap");
        txtAlamat.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtAlamat.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        txtAlamat.setPreferredSize(new Dimension(280, 48));
        formFields.add(txtAlamat);
        formFields.add(Box.createVerticalStrut(16));

        // Checkbox SIM A
        chkSimA = new JCheckBox("Memiliki SIM A yang masih berlaku");
        UIComponents.styleCheckBox(chkSimA);
        chkSimA.setForeground(Theme.TEXT_WHITE);
        chkSimA.setFont(Theme.captionBold());
        chkSimA.setSelected(true);
        chkSimA.setAlignmentX(Component.LEFT_ALIGNMENT);
        formFields.add(chkSimA);
        formFields.add(Box.createVerticalStrut(10));

        lblWarningNik = new JLabel("Periksa kembali NIK untuk melanjutkan.");
        lblWarningNik.setFont(Theme.caption());
        lblWarningNik.setForeground(Theme.BADGE_RED_TXT);
        lblWarningNik.setAlignmentX(Component.LEFT_ALIGNMENT);
        formFields.add(lblWarningNik);

        // Buttons (65% / 35%)
        JPanel btnRow = new JPanel(new GridBagLayout());
        btnRow.setOpaque(false);
        btnRow.setBorder(new EmptyBorder(16, 0, 0, 0));
        GridBagConstraints bgbc = new GridBagConstraints();
        bgbc.fill = GridBagConstraints.BOTH;
        bgbc.weighty = 1.0;

        btnSimpan = new UIComponents.ModernButton("Simpan pelanggan", UIComponents.ModernButton.Type.PRIMARY);
        btnSimpan.setPreferredSize(new Dimension(180, 44));
        btnSimpan.addActionListener(e -> simpanDataPelanggan());

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

        validateNikInput();
        return card;
    }

    private JLabel createFieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.caption());
        l.setForeground(Theme.TEXT_MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private void validateNikInput() {
        String nik = txtNik.getText().replaceAll("[^0-9]", "");
        int len = nik.length();
        if (len == 0) {
            lblNikValidation.setText("Masukkan 16 digit angka NIK KTP. (0/16)");
            lblNikValidation.setForeground(Theme.TEXT_DIM);
            lblWarningNik.setText(" ");
            txtNik.setError(false);
        } else if (len == 16) {
            lblNikValidation.setText("NIK valid 16 digit. (16/16)");
            lblNikValidation.setForeground(Theme.BADGE_GREEN_TXT);
            lblWarningNik.setText(" ");
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
            List<Pelanggan> all = controller.getAllPelanggan();
            int total = all.size();
            int aktif = (int) controller.getDashboardStats().getOrDefault("aktifMenyewa", 0);
            long punyaSim = all.stream().filter(Pelanggan::isMemilikiSimA).count();
            cardTotal.updateData(String.valueOf(total), "");
            cardAktif.updateData(String.valueOf(aktif), "");
            cardBaru.updateData(String.valueOf(punyaSim), "");
        } catch (SQLException ignored) {}
    }

    private void filterData() {
        String kw = txtSearch.getText();
        String filterSim = (String) cmbFilterSim.getSelectedItem();

        try {
            int totalAll = controller.getAllPelanggan().size();
            currentPelangganList = controller.filterPelanggan(kw);

            if (filterSim != null && !filterSim.equalsIgnoreCase("Semua pelanggan")) {
                boolean requireSim = filterSim.equalsIgnoreCase("Punya SIM A");
                currentPelangganList.removeIf(p -> p.isMemilikiSimA() != requireSim);
            }

            tableModel.setRowCount(0);
            for (Pelanggan p : currentPelangganList) {
                tableModel.addRow(new Object[]{
                        p.getIdPelanggan(),
                        p.getNamaLengkap(),
                        p.getNikKtp(),
                        p.getNoTelepon(),
                        p.isMemilikiSimA() ? "Ada" : "Tidak",
                        "Ubah | Hapus"
                });
            }
            lblPagination.setText("Menampilkan " + currentPelangganList.size() + " dari " + totalAll + " pelanggan");

        } catch (SQLException e) {
            System.err.println("Gagal memfilter data pelanggan: " + e.getMessage());
        }
    }

    private String formatPhone(String raw) {
        if (raw == null) return "";
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.length() == 12) {
            return digits.substring(0, 4) + " " + digits.substring(4, 8) + " " + digits.substring(8);
        }
        return raw;
    }

    private void populateFormForEdit(Pelanggan p) {
        selectedPelangganId = p.getIdPelanggan();
        lblFormTitle.setText("Edit pelanggan");
        lblFormSubtitle.setText(p.getIdPelanggan() + "  /  " + p.getNamaLengkap());
        btnSimpan.setText("Simpan perubahan");

        txtNama.setText(p.getNamaLengkap());
        txtNik.setText(p.getNikKtp());
        txtTelepon.setText(formatPhone(p.getNoTelepon()));
        txtAlamat.setText(p.getAlamat());
        chkSimA.setSelected(p.isMemilikiSimA());

        validateNikInput();
    }

    private void resetFormToAddNew() {
        selectedPelangganId = null;
        lblFormTitle.setText("Tambah pelanggan");
        lblFormSubtitle.setText("Lengkapi identitas sebelum menyimpan.");
        btnSimpan.setText("Simpan pelanggan");

        txtNama.setText("");
        txtNik.setText("");
        txtTelepon.setText("");
        txtAlamat.setText("");
        chkSimA.setSelected(true);
        if (tablePelanggan != null) {
            tablePelanggan.clearSelection();
        }
        validateNikInput();
    }

    private void simpanDataPelanggan() {
        String nik = txtNik.getText().replaceAll("[^0-9]", "");
        if (nik.length() != 16) {
            CustomDialog.showNikError(SwingUtilities.getWindowAncestor(this), () -> {
                txtNik.requestFocus();
                txtNik.selectAll();
            });
            return;
        }

        try {
            String nama = txtNama.getText().trim();
            String telp = txtTelepon.getText().replaceAll("\\s+", "");
            String alamat = txtAlamat.getText().trim();
            boolean simA = chkSimA.isSelected();

            String targetId = (selectedPelangganId != null && !selectedPelangganId.trim().isEmpty())
                    ? selectedPelangganId
                    : controller.generateNextPelangganId();

            controller.simpanPelanggan(targetId, nik, nama, telp, alamat, simA);

            // Reset filter agar pelanggan yang baru ditambahkan langsung terlihat di tabel
            if (!txtSearch.getText().trim().isEmpty()) {
                txtSearch.setText("");
            }
            if (cmbFilterSim.getSelectedIndex() != 0) {
                cmbFilterSim.setSelectedIndex(0);
            }

            resetFormToAddNew();
            loadData();

            // Pilih dan scroll ke baris pelanggan yang baru disimpan
            if (currentPelangganList != null) {
                for (int i = 0; i < currentPelangganList.size(); i++) {
                    if (currentPelangganList.get(i).getIdPelanggan().equalsIgnoreCase(targetId)) {
                        tablePelanggan.setRowSelectionInterval(i, i);
                        tablePelanggan.scrollRectToVisible(tablePelanggan.getCellRect(i, 0, true));
                        break;
                    }
                }
            }

            mainFrame.getDashboardPanel().refreshData();
            mainFrame.getTransaksiPanel().loadDropdowns();

            CustomDialog.showSuccess(SwingUtilities.getWindowAncestor(this), "Pelanggan Disimpan", "Data pelanggan " + nama + " (" + targetId + ") berhasil tersimpan.");

        } catch (SQLException dbEx) {
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
