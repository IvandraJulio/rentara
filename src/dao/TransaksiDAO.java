package dao;

import model.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) untuk TransaksiSewa
 * Mengelola operasi basis data untuk penyewaan, pembaruan status denda,
 * dan penyelesaian pengembalian armada dengan query SQL terparameterisasi.
 */
public class TransaksiDAO implements CrudRepository<TransaksiSewa> {
    private final KendaraanDAO kendaraanDAO = new KendaraanDAO();

    private static final String SELECT_JOIN_SQL =
            "SELECT t.id_transaksi, t.id_pelanggan, t.id_kendaraan, " +
            "       t.tanggal_pinjam, t.tanggal_rencana_kembali, t.tanggal_aktual_kembali, " +
            "       t.total_biaya, t.total_denda, t.status_transaksi, " +
            "       p.nik_ktp, p.nama_lengkap, p.no_telepon, p.alamat, p.memiliki_sim_a, " +
            "       k.kategori, k.plat_nomor, k.merk, k.tahun, k.tarif_dasar_harian, k.status_tersedia, " +
            "       k.jumlah_kursi, k.jenis_transmisi, k.termasuk_supir, " +
            "       k.kapasitas_cc, k.tipe_motor, k.jumlah_helm, " +
            "       k.kapasitas_penumpang, k.kelas_bus, k.fasilitas_toilet " +
            "FROM transaksi t " +
            "JOIN pelanggan p ON t.id_pelanggan = p.id_pelanggan " +
            "JOIN kendaraan k ON t.id_kendaraan = k.id_kendaraan ";

    @Override
    public void tambah(TransaksiSewa t) throws SQLException {
        String sql = "INSERT INTO transaksi (id_transaksi, id_pelanggan, id_kendaraan, " +
                "tanggal_pinjam, tanggal_rencana_kembali, tanggal_aktual_kembali, total_biaya, total_denda, status_transaksi) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, t.getIdTransaksi());
            ps.setString(2, t.getPelanggan().getIdPelanggan());
            ps.setString(3, t.getKendaraan().getIdKendaraan());
            ps.setString(4, t.getTanggalPinjam().toString());
            ps.setString(5, t.getTanggalRencanaKembali().toString());
            ps.setString(6, t.getTanggalAktualKembali() != null ? t.getTanggalAktualKembali().toString() : null);
            ps.setDouble(7, t.getTotalBiaya());
            ps.setDouble(8, t.getTotalDenda());
            ps.setString(9, t.getStatusTransaksi());
            ps.executeUpdate();

            // Perbarui status kendaraan menjadi Disewa (0)
            kendaraanDAO.updateStatusKetersediaan(t.getKendaraan().getIdKendaraan(), false);
        }
    }

    @Override
    public void ubah(TransaksiSewa t) throws SQLException {
        String sql = "UPDATE transaksi SET id_pelanggan = ?, id_kendaraan = ?, tanggal_pinjam = ?, " +
                "tanggal_rencana_kembali = ?, tanggal_aktual_kembali = ?, total_biaya = ?, total_denda = ?, status_transaksi = ? " +
                "WHERE id_transaksi = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, t.getPelanggan().getIdPelanggan());
            ps.setString(2, t.getKendaraan().getIdKendaraan());
            ps.setString(3, t.getTanggalPinjam().toString());
            ps.setString(4, t.getTanggalRencanaKembali().toString());
            ps.setString(5, t.getTanggalAktualKembali() != null ? t.getTanggalAktualKembali().toString() : null);
            ps.setDouble(6, t.getTotalBiaya());
            ps.setDouble(7, t.getTotalDenda());
            ps.setString(8, t.getStatusTransaksi());
            ps.setString(9, t.getIdTransaksi());
            ps.executeUpdate();

            // Jika transaksi selesai, pastikan unit kendaraan kembali tersedia
            if ("Selesai".equalsIgnoreCase(t.getStatusTransaksi())) {
                kendaraanDAO.updateStatusKetersediaan(t.getKendaraan().getIdKendaraan(), true);
            }
        }
    }

    @Override
    public void hapus(String id) throws SQLException {
        TransaksiSewa t = cariBerdasarkanId(id);
        if (t != null && !"Selesai".equalsIgnoreCase(t.getStatusTransaksi())) {
            kendaraanDAO.updateStatusKetersediaan(t.getKendaraan().getIdKendaraan(), true);
        }

        String sql = "DELETE FROM transaksi WHERE id_transaksi = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<TransaksiSewa> ambilSemua() throws SQLException {
        List<TransaksiSewa> list = new ArrayList<>();
        String sql = SELECT_JOIN_SQL + "ORDER BY t.id_transaksi DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToTransaksi(rs));
            }
        }
        return list;
    }

    @Override
    public TransaksiSewa cariBerdasarkanId(String id) throws SQLException {
        String sql = SELECT_JOIN_SQL + "WHERE t.id_transaksi = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTransaksi(rs);
                }
            }
        }
        return null;
    }

    public List<TransaksiSewa> ambilTransaksiAktif() throws SQLException {
        List<TransaksiSewa> list = new ArrayList<>();
        String sql = SELECT_JOIN_SQL + "WHERE t.status_transaksi IN ('Disewa', 'Terlambat') ORDER BY t.tanggal_rencana_kembali ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToTransaksi(rs));
            }
        }
        return list;
    }

    public List<TransaksiSewa> ambilPengembalianHariIni() throws SQLException {
        List<TransaksiSewa> list = new ArrayList<>();
        String sql = SELECT_JOIN_SQL + "WHERE t.status_transaksi IN ('Disewa', 'Terlambat') ORDER BY t.tanggal_rencana_kembali ASC LIMIT 6";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToTransaksi(rs));
            }
        }
        return list;
    }

    public double hitungPendapatanTotal() throws SQLException {
        String sql = "SELECT SUM(total_biaya + total_denda) FROM transaksi";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getDouble(1) : 0.0;
        }
    }

    public int hitungTotalTransaksi() throws SQLException {
        String sql = "SELECT COUNT(*) FROM transaksi";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public String generateNextId() throws SQLException {
        String sql = "SELECT id_transaksi FROM transaksi ORDER BY id_transaksi DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                String lastId = rs.getString(1); // TRX-2026-044
                String[] parts = lastId.split("-");
                if (parts.length == 3) {
                    int num = Integer.parseInt(parts[2]);
                    return String.format("TRX-2026-%03d", num + 1);
                }
            }
        } catch (Exception ignored) {}
        return "TRX-2026-001";
    }

    private TransaksiSewa mapResultSetToTransaksi(ResultSet rs) throws SQLException {
        String idTrans = rs.getString("id_transaksi");
        LocalDate pinjam = LocalDate.parse(rs.getString("tanggal_pinjam"));
        LocalDate rencanaKembali = LocalDate.parse(rs.getString("tanggal_rencana_kembali"));
        String aktualStr = rs.getString("tanggal_aktual_kembali");
        LocalDate aktualKembali = (aktualStr != null && !aktualStr.isEmpty()) ? LocalDate.parse(aktualStr) : null;
        double biaya = rs.getDouble("total_biaya");
        double denda = rs.getDouble("total_denda");
        String status = rs.getString("status_transaksi");

        // Map Pelanggan
        Pelanggan p = new Pelanggan(
                rs.getString("id_pelanggan"),
                rs.getString("nik_ktp"),
                rs.getString("nama_lengkap"),
                rs.getString("no_telepon"),
                rs.getString("alamat"),
                rs.getInt("memiliki_sim_a") == 1
        );

        // Map Kendaraan
        String kat = rs.getString("kategori");
        String idKend = rs.getString("id_kendaraan");
        String plat = rs.getString("plat_nomor");
        String merk = rs.getString("merk");
        int tahun = rs.getInt("tahun");
        double tarif = rs.getDouble("tarif_dasar_harian");
        boolean tersedia = rs.getInt("status_tersedia") == 1;

        Kendaraan k;
        if ("Mobil".equalsIgnoreCase(kat)) {
            k = new Mobil(idKend, plat, merk, tahun, tarif, tersedia,
                    rs.getInt("jumlah_kursi"), rs.getString("jenis_transmisi"), rs.getInt("termasuk_supir") == 1);
        } else if ("Motor".equalsIgnoreCase(kat)) {
            k = new Motor(idKend, plat, merk, tahun, tarif, tersedia,
                    rs.getInt("kapasitas_cc"), rs.getString("tipe_motor"), rs.getInt("jumlah_helm"));
        } else {
            k = new Bus(idKend, plat, merk, tahun, tarif, tersedia,
                    rs.getInt("kapasitas_penumpang"), rs.getString("kelas_bus"), rs.getInt("fasilitas_toilet") == 1);
        }

        // Auto update status Terlambat jika belum kembali dan sudah melewati jadwal kembali
        if ("Disewa".equalsIgnoreCase(status) && LocalDate.now().isAfter(rencanaKembali)) {
            status = "Terlambat";
        }

        return new TransaksiSewa(idTrans, p, k, pinjam, rencanaKembali, aktualKembali, biaya, denda, status);
    }
}
