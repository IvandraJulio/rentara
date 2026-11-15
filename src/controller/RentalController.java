package controller;

import dao.DatabaseConnection;
import dao.KendaraanDAO;
import dao.PelangganDAO;
import dao.TransaksiDAO;
import model.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller Utama (RentalController)
 * Bertindak sebagai jembatan MVC antara View dan Model/DAO.
 * Mengontrol logika bisnis, validasi masukan, kalkulasi polimorfik,
 * serta alur transaksi penyewaan dan pengembalian.
 */
public class RentalController {
    private final KendaraanDAO kendaraanDAO;
    private final PelangganDAO pelangganDAO;
    private final TransaksiDAO transaksiDAO;

    public RentalController() {
        this.kendaraanDAO = new KendaraanDAO();
        this.pelangganDAO = new PelangganDAO();
        this.transaksiDAO = new TransaksiDAO();
    }

    // ==========================================
    // 1. OTENTIKASI & USER
    // ==========================================
    public boolean login(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return false;
        }
        String sql = "SELECT * FROM users WHERE (username = ? OR username LIKE ?) AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            ps.setString(2, username.trim() + "%");
            ps.setString(3, password.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error otentikasi: " + e.getMessage());
            return false;
        }
    }

    // ==========================================
    // 2. DASHBOARD DATA & STATISTIK
    // ==========================================
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        try {
            int totalArmada = kendaraanDAO.hitungTotal();
            int armadaTersedia = kendaraanDAO.hitungTersedia();
            int armadaDisewa = kendaraanDAO.hitungDisewa();
            int mobilCount = kendaraanDAO.hitungKategori("Mobil");
            int motorCount = kendaraanDAO.hitungKategori("Motor");
            int busCount = kendaraanDAO.hitungKategori("Bus");

            int totalPelanggan = pelangganDAO.hitungTotal();
            int aktifMenyewa = pelangganDAO.hitungAktifMenyewa();

            double pendapatan = transaksiDAO.hitungPendapatanTotal();
            int totalTransaksi = transaksiDAO.hitungTotalTransaksi();

            List<TransaksiSewa> pengembalianHariIni = transaksiDAO.ambilPengembalianHariIni();

            stats.put("totalArmada", totalArmada);
            stats.put("armadaTersedia", armadaTersedia);
            stats.put("armadaDisewa", armadaDisewa);
            stats.put("mobilCount", mobilCount);
            stats.put("motorCount", motorCount);
            stats.put("busCount", busCount);
            stats.put("totalPelanggan", totalPelanggan);
            stats.put("aktifMenyewa", aktifMenyewa);
            stats.put("pendapatan", pendapatan);
            stats.put("totalTransaksi", totalTransaksi);
            stats.put("pengembalianHariIni", pengembalianHariIni);
        } catch (SQLException e) {
            System.err.println("Gagal memuat statistik dashboard: " + e.getMessage());
        }
        return stats;
    }

    // ==========================================
    // 3. CRUD KENDARAAN (MOBIL, MOTOR, BUS)
    // ==========================================
    public List<Kendaraan> getAllKendaraan() throws SQLException {
        return kendaraanDAO.ambilSemua();
    }

    public List<Kendaraan> getKendaraanTersedia() throws SQLException {
        return kendaraanDAO.ambilKendaraanTersedia();
    }

    public List<Kendaraan> filterKendaraan(String keyword, String kategori, String status) throws SQLException {
        return kendaraanDAO.cariBerdasarkanKriteria(keyword, kategori, status);
    }

    public void simpanKendaraan(String id, String kategori, String platNomor, String merk, int tahun,
                                double tarif, boolean tersedia,
                                Integer kursi, String transmisi, Boolean supir,
                                Integer cc, String tipeMotor, Integer helm,
                                Integer penumpang, String kelasBus, Boolean toilet) throws Exception {

        if (platNomor == null || platNomor.trim().isEmpty()) {
            throw new IllegalArgumentException("Plat nomor kendaraan tidak boleh kosong.");
        }
        if (merk == null || merk.trim().isEmpty()) {
            throw new IllegalArgumentException("Merk dan model kendaraan wajib diisi.");
        }
        if (tarif <= 0) {
            throw new IllegalArgumentException("Tarif sewa harian harus lebih besar dari Rp0.");
        }

        if (id == null || id.trim().isEmpty()) {
            id = kendaraanDAO.generateNextId();
        }

        Kendaraan kendaraan;
        if ("Mobil".equalsIgnoreCase(kategori)) {
            int k = (kursi != null && kursi > 0) ? kursi : 5;
            String t = (transmisi != null && !transmisi.isEmpty()) ? transmisi : "Otomatis (AT)";
            boolean s = (supir != null) && supir;
            kendaraan = new Mobil(id, platNomor, merk, tahun, tarif, tersedia, k, t, s);
        } else if ("Motor".equalsIgnoreCase(kategori)) {
            int c = (cc != null && cc > 0) ? cc : 150;
            String tip = (tipeMotor != null && !tipeMotor.isEmpty()) ? tipeMotor : "Matic";
            int h = (helm != null && helm >= 0) ? helm : 2;
            kendaraan = new Motor(id, platNomor, merk, tahun, tarif, tersedia, c, tip, h);
        } else if ("Bus".equalsIgnoreCase(kategori)) {
            int p = (penumpang != null && penumpang > 0) ? penumpang : 30;
            String kb = (kelasBus != null && !kelasBus.isEmpty()) ? kelasBus : "Pariwisata";
            boolean toi = (toilet != null) && toilet;
            kendaraan = new Bus(id, platNomor, merk, tahun, tarif, tersedia, p, kb, toi);
        } else {
            throw new IllegalArgumentException("Kategori kendaraan tidak valid: " + kategori);
        }

        Kendaraan existing = kendaraanDAO.cariBerdasarkanId(id);
        if (existing == null) {
            kendaraanDAO.tambah(kendaraan);
        } else {
            kendaraanDAO.ubah(kendaraan);
        }
    }

    public void hapusKendaraan(String id) throws Exception {
        Kendaraan k = kendaraanDAO.cariBerdasarkanId(id);
        if (k == null) {
            throw new IllegalArgumentException("Kendaraan dengan ID " + id + " tidak ditemukan.");
        }
        if (!k.isStatusTersedia()) {
            throw new IllegalStateException("Armada sedang dalam status 'Disewa'. Tidak dapat dihapus.");
        }
        kendaraanDAO.hapus(id);
    }

    public String generateNextKendaraanId() throws SQLException {
        return kendaraanDAO.generateNextId();
    }

    // ==========================================
    // 4. CRUD PELANGGAN
    // ==========================================
    public List<Pelanggan> getAllPelanggan() throws SQLException {
        return pelangganDAO.ambilSemua();
    }

    public List<Pelanggan> filterPelanggan(String keyword) throws SQLException {
        return pelangganDAO.cariBerdasarkanKriteria(keyword);
    }

    public void simpanPelanggan(String id, String nik, String nama, String telepon, String alamat, boolean simA) throws Exception {
        if (nama == null || nama.trim().isEmpty()) {
            throw new IllegalArgumentException("Nama lengkap pelanggan wajib diisi.");
        }
        if (nik == null || !nik.trim().matches("\\d{16}")) {
            throw new IllegalArgumentException("NIK KTP wajib terdiri tepat dari 16 digit angka.");
        }
        if (telepon == null || telepon.trim().isEmpty()) {
            throw new IllegalArgumentException("Nomor telepon tidak boleh kosong.");
        }

        if (id == null || id.trim().isEmpty()) {
            id = pelangganDAO.generateNextId();
        }

        Pelanggan p = new Pelanggan(id, nik, nama, telepon, alamat, simA);
        Pelanggan existing = pelangganDAO.cariBerdasarkanId(id);
        if (existing == null) {
            pelangganDAO.tambah(p);
        } else {
            pelangganDAO.ubah(p);
        }
    }

    public void hapusPelanggan(String id) throws Exception {
        Pelanggan p = pelangganDAO.cariBerdasarkanId(id);
        if (p == null) {
            throw new IllegalArgumentException("Pelanggan dengan ID " + id + " tidak ditemukan.");
        }
        pelangganDAO.hapus(id);
    }

    public String generateNextPelangganId() throws SQLException {
        return pelangganDAO.generateNextId();
    }

    // ==========================================
    // 5. TRANSAKSI SEWA & POLIMORFISME
    // ==========================================
    public List<TransaksiSewa> getAllTransaksi() throws SQLException {
        return transaksiDAO.ambilSemua();
    }

    public List<TransaksiSewa> getTransaksiAktif() throws SQLException {
        return transaksiDAO.ambilTransaksiAktif();
    }

    /**
     * Menghitung estimasi sewa polimorfik tanpa menyimpan ke database.
     */
    public double hitungEstimasiBiaya(Kendaraan k, int durasiHari, boolean denganSupir) {
        if (k == null || durasiHari <= 0) return 0.0;
        if (k instanceof Mobil) {
            Mobil m = (Mobil) k;
            boolean original = m.isTermasukSupir();
            m.setTermasukSupir(denganSupir);
            double biaya = m.hitungSewa(durasiHari);
            m.setTermasukSupir(original);
            return biaya;
        }
        return k.hitungSewa(durasiHari);
    }

    /**
     * Membuat dan memproses transaksi sewa baru dengan kalkulasi polimorfik.
     */
    public TransaksiSewa buatTransaksiSewa(String idPelanggan, String idKendaraan,
                                           LocalDate pinjam, LocalDate kembali,
                                           boolean opsiSupir) throws Exception {

        if (pinjam == null || kembali == null) {
            throw new IllegalArgumentException("Tanggal pinjam dan tanggal kembali harus dipilih.");
        }
        if (kembali.isBefore(pinjam)) {
            throw new IllegalArgumentException("Tanggal kembali tidak boleh sebelum tanggal pinjam.");
        }

        Pelanggan p = pelangganDAO.cariBerdasarkanId(idPelanggan);
        if (p == null) {
            throw new IllegalArgumentException("Pelanggan tidak ditemukan.");
        }

        Kendaraan k = kendaraanDAO.cariBerdasarkanId(idKendaraan);
        if (k == null) {
            throw new IllegalArgumentException("Kendaraan tidak ditemukan.");
        }
        if (!k.isStatusTersedia()) {
            throw new IllegalStateException("Kendaraan " + k.getMerk() + " saat ini sedang tidak tersedia.");
        }

        if (k instanceof Mobil) {
            ((Mobil) k).setTermasukSupir(opsiSupir);
            kendaraanDAO.ubah(k); // Simpan preferensi opsi supir jika mobil
        }

        String idTrans = transaksiDAO.generateNextId();
        TransaksiSewa t = new TransaksiSewa(idTrans, p, k, pinjam, kembali);

        transaksiDAO.tambah(t);
        return t;
    }

    /**
     * Memproses penyelesaian pengembalian unit sewa:
     * - Menghitung denda polimorfik berdasarkan jenis armada
     * - Mengubah status transaksi menjadi "Selesai"
     * - Mengubah status armada menjadi "Tersedia"
     */
    public void prosesPengembalian(String idTransaksi, LocalDate tanggalAktual) throws Exception {
        if (tanggalAktual == null) {
            tanggalAktual = LocalDate.now();
        }
        TransaksiSewa t = transaksiDAO.cariBerdasarkanId(idTransaksi);
        if (t == null) {
            throw new IllegalArgumentException("Transaksi " + idTransaksi + " tidak ditemukan.");
        }
        if ("Selesai".equalsIgnoreCase(t.getStatusTransaksi())) {
            throw new IllegalStateException("Transaksi ini sudah selesai sebelumnya.");
        }

        t.selesaikanPengembalian(tanggalAktual);
        transaksiDAO.ubah(t);
    }
}
