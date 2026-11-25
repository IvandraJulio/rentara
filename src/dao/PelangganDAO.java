package dao;

import model.Pelanggan;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) untuk Pelanggan
 * Mengelola persistensi data pelanggan pada database SQLite.
 */
public class PelangganDAO implements CrudRepository<Pelanggan> {

    @Override
    public void tambah(Pelanggan p) throws SQLException {
        String sql = "INSERT INTO pelanggan (id_pelanggan, nik_ktp, nama_lengkap, no_telepon, alamat, memiliki_sim_a) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getIdPelanggan());
            ps.setString(2, p.getNikKtp());
            ps.setString(3, p.getNamaLengkap());
            ps.setString(4, p.getNoTelepon());
            ps.setString(5, p.getAlamat());
            ps.setInt(6, p.isMemilikiSimA() ? 1 : 0);
            ps.executeUpdate();
        }
    }

    @Override
    public void ubah(Pelanggan p) throws SQLException {
        String sql = "UPDATE pelanggan SET nik_ktp = ?, nama_lengkap = ?, no_telepon = ?, " +
                "alamat = ?, memiliki_sim_a = ? WHERE id_pelanggan = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNikKtp());
            ps.setString(2, p.getNamaLengkap());
            ps.setString(3, p.getNoTelepon());
            ps.setString(4, p.getAlamat());
            ps.setInt(5, p.isMemilikiSimA() ? 1 : 0);
            ps.setString(6, p.getIdPelanggan());
            ps.executeUpdate();
        }
    }

    @Override
    public void hapus(String id) throws SQLException {
        String sql = "DELETE FROM pelanggan WHERE id_pelanggan = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Pelanggan> ambilSemua() throws SQLException {
        List<Pelanggan> list = new ArrayList<>();
        String sql = "SELECT * FROM pelanggan ORDER BY id_pelanggan ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToPelanggan(rs));
            }
        }
        return list;
    }

    @Override
    public Pelanggan cariBerdasarkanId(String id) throws SQLException {
        String sql = "SELECT * FROM pelanggan WHERE id_pelanggan = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPelanggan(rs);
                }
            }
        }
        return null;
    }

    public Pelanggan cariBerdasarkanNik(String nik) throws SQLException {
        String sql = "SELECT * FROM pelanggan WHERE nik_ktp = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nik);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPelanggan(rs);
                }
            }
        }
        return null;
    }

    public List<Pelanggan> cariBerdasarkanKriteria(String kataKunci) throws SQLException {
        List<Pelanggan> list = new ArrayList<>();
        String sql = "SELECT * FROM pelanggan WHERE LOWER(nama_lengkap) LIKE ? OR LOWER(nik_ktp) LIKE ? OR LOWER(id_pelanggan) LIKE ? ORDER BY id_pelanggan ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String kw = "%" + (kataKunci == null ? "" : kataKunci.trim().toLowerCase()) + "%";
            ps.setString(1, kw);
            ps.setString(2, kw);
            ps.setString(3, kw);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToPelanggan(rs));
                }
            }
        }
        return list;
    }

    public int hitungTotal() throws SQLException {
        String sql = "SELECT COUNT(*) FROM pelanggan";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int hitungAktifMenyewa() throws SQLException {
        String sql = "SELECT COUNT(DISTINCT id_pelanggan) FROM transaksi WHERE status_transaksi IN ('Disewa', 'Terlambat')";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public String generateNextId() throws SQLException {
        String sql = "SELECT id_pelanggan FROM pelanggan";
        int maxNum = 0;
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String id = rs.getString(1);
                if (id != null && id.startsWith("PLG-")) {
                    try {
                        int num = Integer.parseInt(id.substring(4).trim());
                        if (num > maxNum) {
                            maxNum = num;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return String.format("PLG-%03d", maxNum + 1);
    }

    private Pelanggan mapResultSetToPelanggan(ResultSet rs) throws SQLException {
        return new Pelanggan(
                rs.getString("id_pelanggan"),
                rs.getString("nik_ktp"),
                rs.getString("nama_lengkap"),
                rs.getString("no_telepon"),
                rs.getString("alamat"),
                rs.getInt("memiliki_sim_a") == 1
        );
    }
}
