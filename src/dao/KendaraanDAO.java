package dao;

import model.Bus;
import model.Kendaraan;
import model.Mobil;
import model.Motor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) untuk Kendaraan
 * Menerapkan CrudRepository<Kendaraan> dan menangani persistensi relasional
 * dengan pemetaan objek polimorfik (Mobil, Motor, Bus).
 */
public class KendaraanDAO implements CrudRepository<Kendaraan> {

    @Override
    public void tambah(Kendaraan k) throws SQLException {
        String sql = "INSERT INTO kendaraan (id_kendaraan, kategori, plat_nomor, merk, tahun, " +
                "tarif_dasar_harian, status_tersedia, jumlah_kursi, jenis_transmisi, termasuk_supir, " +
                "kapasitas_cc, tipe_motor, jumlah_helm, kapasitas_penumpang, kelas_bus, fasilitas_toilet) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, k.getIdKendaraan());
            ps.setString(2, k.getKategori());
            ps.setString(3, k.getPlatNomor());
            ps.setString(4, k.getMerk());
            ps.setInt(5, k.getTahun());
            ps.setDouble(6, k.getTarifDasarHarian());
            ps.setInt(7, k.isStatusTersedia() ? 1 : 0);

            if (k instanceof Mobil) {
                Mobil m = (Mobil) k;
                ps.setInt(8, m.getJumlahKursi());
                ps.setString(9, m.getJenisTransmisi());
                ps.setInt(10, m.isTermasukSupir() ? 1 : 0);
                ps.setNull(11, Types.INTEGER);
                ps.setNull(12, Types.VARCHAR);
                ps.setNull(13, Types.INTEGER);
                ps.setNull(14, Types.INTEGER);
                ps.setNull(15, Types.VARCHAR);
                ps.setNull(16, Types.INTEGER);
            } else if (k instanceof Motor) {
                Motor mot = (Motor) k;
                ps.setNull(8, Types.INTEGER);
                ps.setNull(9, Types.VARCHAR);
                ps.setNull(10, Types.INTEGER);
                ps.setInt(11, mot.getKapasitasCC());
                ps.setString(12, mot.getTipeMotor());
                ps.setInt(13, mot.getJumlahHelm());
                ps.setNull(14, Types.INTEGER);
                ps.setNull(15, Types.VARCHAR);
                ps.setNull(16, Types.INTEGER);
            } else if (k instanceof Bus) {
                Bus b = (Bus) k;
                ps.setNull(8, Types.INTEGER);
                ps.setNull(9, Types.VARCHAR);
                ps.setNull(10, Types.INTEGER);
                ps.setNull(11, Types.INTEGER);
                ps.setNull(12, Types.VARCHAR);
                ps.setNull(13, Types.INTEGER);
                ps.setInt(14, b.getKapasitasPenumpang());
                ps.setString(15, b.getKelasBus());
                ps.setInt(16, b.isFasilitasToilet() ? 1 : 0);
            }

            ps.executeUpdate();
        }
    }

    @Override
    public void ubah(Kendaraan k) throws SQLException {
        String sql = "UPDATE kendaraan SET kategori=?, plat_nomor=?, merk=?, tahun=?, " +
                "tarif_dasar_harian=?, status_tersedia=?, jumlah_kursi=?, jenis_transmisi=?, termasuk_supir=?, " +
                "kapasitas_cc=?, tipe_motor=?, jumlah_helm=?, kapasitas_penumpang=?, kelas_bus=?, fasilitas_toilet=? " +
                "WHERE id_kendaraan=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, k.getKategori());
            ps.setString(2, k.getPlatNomor());
            ps.setString(3, k.getMerk());
            ps.setInt(4, k.getTahun());
            ps.setDouble(5, k.getTarifDasarHarian());
            ps.setInt(6, k.isStatusTersedia() ? 1 : 0);

            if (k instanceof Mobil) {
                Mobil m = (Mobil) k;
                ps.setInt(7, m.getJumlahKursi());
                ps.setString(8, m.getJenisTransmisi());
                ps.setInt(9, m.isTermasukSupir() ? 1 : 0);
                ps.setNull(10, Types.INTEGER);
                ps.setNull(11, Types.VARCHAR);
                ps.setNull(12, Types.INTEGER);
                ps.setNull(13, Types.INTEGER);
                ps.setNull(14, Types.VARCHAR);
                ps.setNull(15, Types.INTEGER);
            } else if (k instanceof Motor) {
                Motor mot = (Motor) k;
                ps.setNull(7, Types.INTEGER);
                ps.setNull(8, Types.VARCHAR);
                ps.setNull(9, Types.INTEGER);
                ps.setInt(10, mot.getKapasitasCC());
                ps.setString(11, mot.getTipeMotor());
                ps.setInt(12, mot.getJumlahHelm());
                ps.setNull(13, Types.INTEGER);
                ps.setNull(14, Types.VARCHAR);
                ps.setNull(15, Types.INTEGER);
            } else if (k instanceof Bus) {
                Bus b = (Bus) k;
                ps.setNull(7, Types.INTEGER);
                ps.setNull(8, Types.VARCHAR);
                ps.setNull(9, Types.INTEGER);
                ps.setNull(10, Types.INTEGER);
                ps.setNull(11, Types.VARCHAR);
                ps.setNull(12, Types.INTEGER);
                ps.setInt(13, b.getKapasitasPenumpang());
                ps.setString(14, b.getKelasBus());
                ps.setInt(15, b.isFasilitasToilet() ? 1 : 0);
            }

            ps.setString(16, k.getIdKendaraan());
            ps.executeUpdate();
        }
    }

    @Override
    public void hapus(String id) throws SQLException {
        String sql = "DELETE FROM kendaraan WHERE id_kendaraan = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Kendaraan> ambilSemua() throws SQLException {
        List<Kendaraan> list = new ArrayList<>();
        String sql = "SELECT * FROM kendaraan ORDER BY id_kendaraan ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToKendaraan(rs));
            }
        }
        return list;
    }

    @Override
    public Kendaraan cariBerdasarkanId(String id) throws SQLException {
        String sql = "SELECT * FROM kendaraan WHERE id_kendaraan = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToKendaraan(rs);
                }
            }
        }
        return null;
    }

    public List<Kendaraan> cariBerdasarkanKriteria(String kataKunci, String kategoriFilter, String statusFilter) throws SQLException {
        List<Kendaraan> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM kendaraan WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (kataKunci != null && !kataKunci.trim().isEmpty()) {
            sql.append("AND (LOWER(merk) LIKE ? OR LOWER(plat_nomor) LIKE ?) ");
            String kw = "%" + kataKunci.trim().toLowerCase() + "%";
            params.add(kw);
            params.add(kw);
        }

        if (kategoriFilter != null && !kategoriFilter.equalsIgnoreCase("Semua") && !kategoriFilter.isEmpty()) {
            sql.append("AND kategori = ? ");
            params.add(kategoriFilter);
        }

        if (statusFilter != null && !statusFilter.equalsIgnoreCase("Semua") && !statusFilter.isEmpty()) {
            if (statusFilter.equalsIgnoreCase("Tersedia")) {
                sql.append("AND status_tersedia = 1 ");
            } else if (statusFilter.equalsIgnoreCase("Disewa")) {
                sql.append("AND status_tersedia = 0 ");
            }
        }

        sql.append("ORDER BY id_kendaraan ASC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToKendaraan(rs));
                }
            }
        }
        return list;
    }

    public List<Kendaraan> ambilKendaraanTersedia() throws SQLException {
        List<Kendaraan> list = new ArrayList<>();
        String sql = "SELECT * FROM kendaraan WHERE status_tersedia = 1 ORDER BY id_kendaraan ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToKendaraan(rs));
            }
        }
        return list;
    }

    public void updateStatusKetersediaan(String idKendaraan, boolean status) throws SQLException {
        String sql = "UPDATE kendaraan SET status_tersedia = ? WHERE id_kendaraan = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, status ? 1 : 0);
            ps.setString(2, idKendaraan);
            ps.executeUpdate();
        }
    }

    public int hitungTotal() throws SQLException {
        String sql = "SELECT COUNT(*) FROM kendaraan";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int hitungTersedia() throws SQLException {
        String sql = "SELECT COUNT(*) FROM kendaraan WHERE status_tersedia = 1";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int hitungDisewa() throws SQLException {
        String sql = "SELECT COUNT(*) FROM kendaraan WHERE status_tersedia = 0";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int hitungKategori(String kategori) throws SQLException {
        String sql = "SELECT COUNT(*) FROM kendaraan WHERE kategori = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kategori);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public String generateNextId() throws SQLException {
        String sql = "SELECT id_kendaraan FROM kendaraan ORDER BY id_kendaraan DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                String lastId = rs.getString(1); // KD-008
                if (lastId != null && lastId.startsWith("KD-")) {
                    int num = Integer.parseInt(lastId.substring(3));
                    return String.format("KD-%03d", num + 1);
                }
            }
        } catch (Exception ignored) {}
        return "KD-001";
    }

    private Kendaraan mapResultSetToKendaraan(ResultSet rs) throws SQLException {
        String id = rs.getString("id_kendaraan");
        String kategori = rs.getString("kategori");
        String plat = rs.getString("plat_nomor");
        String merk = rs.getString("merk");
        int tahun = rs.getInt("tahun");
        double tarif = rs.getDouble("tarif_dasar_harian");
        boolean tersedia = rs.getInt("status_tersedia") == 1;

        if ("Mobil".equalsIgnoreCase(kategori)) {
            int kursi = rs.getInt("jumlah_kursi");
            String transmisi = rs.getString("jenis_transmisi");
            boolean supir = rs.getInt("termasuk_supir") == 1;
            return new Mobil(id, plat, merk, tahun, tarif, tersedia, kursi, transmisi, supir);
        } else if ("Motor".equalsIgnoreCase(kategori)) {
            int cc = rs.getInt("kapasitas_cc");
            String tipe = rs.getString("tipe_motor");
            int helm = rs.getInt("jumlah_helm");
            return new Motor(id, plat, merk, tahun, tarif, tersedia, cc, tipe, helm);
        } else if ("Bus".equalsIgnoreCase(kategori)) {
            int penumpang = rs.getInt("kapasitas_penumpang");
            String kelas = rs.getString("kelas_bus");
            boolean toilet = rs.getInt("fasilitas_toilet") == 1;
            return new Bus(id, plat, merk, tahun, tarif, tersedia, penumpang, kelas, toilet);
        }
        // Fallback default ke Mobil jika tidak dikenal
        return new Mobil(id, plat, merk, tahun, tarif, tersedia, 5, "Manual (MT)", false);
    }
}
