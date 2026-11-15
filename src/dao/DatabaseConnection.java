package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Pengelola Koneksi Basis Data SQLite (DatabaseConnection)
 * Menghasilkan koneksi terkonfigurasi ke 'rentara.db' dengan dukungan WAL mode,
 * foreign keys, dan inisialisasi skema tabel otomatis.
 */
public class DatabaseConnection {
    private static final String DB_URL = "jdbc:sqlite:rentara.db";
    private static boolean initialized = false;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
            initDatabase();
        } catch (ClassNotFoundException e) {
            System.err.println("Driver SQLite JDBC tidak ditemukan: " + e.getMessage());
        }
    }

    private DatabaseConnection() {}

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
            stmt.execute("PRAGMA journal_mode = WAL;");
            stmt.execute("PRAGMA busy_timeout = 5000;");
        }
        return conn;
    }

    public static synchronized void initDatabase() {
        if (initialized) return;
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            stmt.execute("PRAGMA foreign_keys = ON;");
            stmt.execute("PRAGMA journal_mode = WAL;");

            // 1. Tabel Users (Otentikasi)
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT UNIQUE NOT NULL, " +
                    "password TEXT NOT NULL, " +
                    "nama_lengkap TEXT NOT NULL, " +
                    "role TEXT NOT NULL DEFAULT 'Admin'" +
                    ");");

            // 2. Tabel Kendaraan
            stmt.execute("CREATE TABLE IF NOT EXISTS kendaraan (" +
                    "id_kendaraan TEXT PRIMARY KEY, " +
                    "kategori TEXT NOT NULL, " +
                    "plat_nomor TEXT NOT NULL UNIQUE, " +
                    "merk TEXT NOT NULL, " +
                    "tahun INTEGER NOT NULL, " +
                    "tarif_dasar_harian REAL NOT NULL, " +
                    "status_tersedia INTEGER NOT NULL DEFAULT 1, " +
                    "jumlah_kursi INTEGER, " +
                    "jenis_transmisi TEXT, " +
                    "termasuk_supir INTEGER, " +
                    "kapasitas_cc INTEGER, " +
                    "tipe_motor TEXT, " +
                    "jumlah_helm INTEGER, " +
                    "kapasitas_penumpang INTEGER, " +
                    "kelas_bus TEXT, " +
                    "fasilitas_toilet INTEGER" +
                    ");");

            // 3. Tabel Pelanggan
            stmt.execute("CREATE TABLE IF NOT EXISTS pelanggan (" +
                    "id_pelanggan TEXT PRIMARY KEY, " +
                    "nik_ktp TEXT NOT NULL UNIQUE, " +
                    "nama_lengkap TEXT NOT NULL, " +
                    "no_telepon TEXT NOT NULL, " +
                    "alamat TEXT, " +
                    "memiliki_sim_a INTEGER NOT NULL DEFAULT 1" +
                    ");");

            // 4. Tabel Transaksi
            stmt.execute("CREATE TABLE IF NOT EXISTS transaksi (" +
                    "id_transaksi TEXT PRIMARY KEY, " +
                    "id_pelanggan TEXT NOT NULL, " +
                    "id_kendaraan TEXT NOT NULL, " +
                    "tanggal_pinjam TEXT NOT NULL, " +
                    "tanggal_rencana_kembali TEXT NOT NULL, " +
                    "tanggal_aktual_kembali TEXT, " +
                    "total_biaya REAL NOT NULL, " +
                    "total_denda REAL NOT NULL DEFAULT 0, " +
                    "status_transaksi TEXT NOT NULL, " +
                    "FOREIGN KEY (id_pelanggan) REFERENCES pelanggan(id_pelanggan) ON DELETE RESTRICT, " +
                    "FOREIGN KEY (id_kendaraan) REFERENCES kendaraan(id_kendaraan) ON DELETE RESTRICT" +
                    ");");

            seedInitialData(conn);
            initialized = true;

        } catch (SQLException e) {
            System.err.println("Gagal inisialisasi database: " + e.getMessage());
        }
    }

    private static void seedInitialData(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            // Seed Admin User
            stmt.execute("INSERT OR IGNORE INTO users (id, username, password, nama_lengkap, role) " +
                    "VALUES (1, 'admin@rentara.id', 'admin', 'Admin Rental', 'Admin');");

            // Seed Armada Kendaraan (Sesuai Wireframe UI)
            stmt.execute("INSERT OR IGNORE INTO kendaraan " +
                    "(id_kendaraan, kategori, plat_nomor, merk, tahun, tarif_dasar_harian, status_tersedia, jumlah_kursi, jenis_transmisi, termasuk_supir) " +
                    "VALUES ('KD-001', 'Mobil', 'B 1234 ABC', 'Toyota Avanza', 2024, 350000, 1, 7, 'Otomatis (AT)', 1);");

            stmt.execute("INSERT OR IGNORE INTO kendaraan " +
                    "(id_kendaraan, kategori, plat_nomor, merk, tahun, tarif_dasar_harian, status_tersedia, jumlah_kursi, jenis_transmisi, termasuk_supir) " +
                    "VALUES ('KD-002', 'Mobil', 'B 8912 PQR', 'Toyota Innova', 2023, 500000, 0, 8, 'Otomatis (AT)', 1);");

            stmt.execute("INSERT OR IGNORE INTO kendaraan " +
                    "(id_kendaraan, kategori, plat_nomor, merk, tahun, tarif_dasar_harian, status_tersedia, kapasitas_cc, tipe_motor, jumlah_helm) " +
                    "VALUES ('KD-003', 'Motor', 'B 5621 TMA', 'Honda Vario 160', 2024, 90000, 0, 160, 'Matic', 2);");

            stmt.execute("INSERT OR IGNORE INTO kendaraan " +
                    "(id_kendaraan, kategori, plat_nomor, merk, tahun, tarif_dasar_harian, status_tersedia, jumlah_kursi, jenis_transmisi, termasuk_supir) " +
                    "VALUES ('KD-004', 'Mobil', 'B 4221 KLM', 'Mitsubishi Xpander', 2024, 450000, 1, 7, 'Otomatis (AT)', 0);");

            stmt.execute("INSERT OR IGNORE INTO kendaraan " +
                    "(id_kendaraan, kategori, plat_nomor, merk, tahun, tarif_dasar_harian, status_tersedia, kapasitas_cc, tipe_motor, jumlah_helm) " +
                    "VALUES ('KD-005', 'Motor', 'B 7832 XYZ', 'Yamaha NMAX', 2023, 120000, 1, 155, 'Matic', 2);");

            stmt.execute("INSERT OR IGNORE INTO kendaraan " +
                    "(id_kendaraan, kategori, plat_nomor, merk, tahun, tarif_dasar_harian, status_tersedia, kapasitas_penumpang, kelas_bus, fasilitas_toilet) " +
                    "VALUES ('KD-006', 'Bus', 'B 7620 U', 'Bus Pariwisata', 2022, 1500000, 0, 35, 'Pariwisata', 1);");

            stmt.execute("INSERT OR IGNORE INTO kendaraan " +
                    "(id_kendaraan, kategori, plat_nomor, merk, tahun, tarif_dasar_harian, status_tersedia, jumlah_kursi, jenis_transmisi, termasuk_supir) " +
                    "VALUES ('KD-007', 'Mobil', 'B 3021 RFS', 'Honda Brio', 2023, 300000, 0, 5, 'Manual (MT)', 0);");

            stmt.execute("INSERT OR IGNORE INTO kendaraan " +
                    "(id_kendaraan, kategori, plat_nomor, merk, tahun, tarif_dasar_harian, status_tersedia, jumlah_kursi, jenis_transmisi, termasuk_supir) " +
                    "VALUES ('KD-008', 'Mobil', 'B 2341 SJK', 'Toyota Avanza Veloz', 2024, 350000, 0, 7, 'Otomatis (AT)', 0);");

            // Seed Data Pelanggan (Sesuai Wireframe UI)
            stmt.execute("INSERT OR IGNORE INTO pelanggan (id_pelanggan, nik_ktp, nama_lengkap, no_telepon, alamat, memiliki_sim_a) " +
                    "VALUES ('PLG-001', '3276000000000041', 'Aditya Pratama', '081234567890', 'Jl. Sudirman No. 12, Jakarta', 1);");

            stmt.execute("INSERT OR IGNORE INTO pelanggan (id_pelanggan, nik_ktp, nama_lengkap, no_telepon, alamat, memiliki_sim_a) " +
                    "VALUES ('PLG-002', '3174000000000008', 'Nabila Putri', '085712345678', 'Jl. Melati No. 45, Bandung', 1);");

            stmt.execute("INSERT OR IGNORE INTO pelanggan (id_pelanggan, nik_ktp, nama_lengkap, no_telepon, alamat, memiliki_sim_a) " +
                    "VALUES ('PLG-003', '3201000000000024', 'Rizky Ramadhan', '081356789012', 'Jl. Pahlawan No. 8, Semarang', 1);");

            stmt.execute("INSERT OR IGNORE INTO pelanggan (id_pelanggan, nik_ktp, nama_lengkap, no_telepon, alamat, memiliki_sim_a) " +
                    "VALUES ('PLG-004', '3275000000000005', 'Salsabila Zahra', '082134567890', 'Jl. Diponegoro No. 19, Semarang', 0);");

            stmt.execute("INSERT OR IGNORE INTO pelanggan (id_pelanggan, nik_ktp, nama_lengkap, no_telepon, alamat, memiliki_sim_a) " +
                    "VALUES ('PLG-005', '3171000000000019', 'Bagas Saputra', '087856781234', 'Jl. Merdeka No. 24, Bekasi', 1);");

            stmt.execute("INSERT OR IGNORE INTO pelanggan (id_pelanggan, nik_ktp, nama_lengkap, no_telepon, alamat, memiliki_sim_a) " +
                    "VALUES ('PLG-006', '3273000000000007', 'Dewi Anggraini', '081278903456', 'Jl. Riau No. 10, Bandung', 1);");

            stmt.execute("INSERT OR IGNORE INTO pelanggan (id_pelanggan, nik_ktp, nama_lengkap, no_telepon, alamat, memiliki_sim_a) " +
                    "VALUES ('PLG-007', '3271000000000099', 'CV Maju Bersama', '081198765432', 'Jl. Pemuda No. 88, Semarang', 1);");

            // Seed Transaksi Berjalan (Sesuai Wireframe UI)
            stmt.execute("INSERT OR IGNORE INTO transaksi " +
                    "(id_transaksi, id_pelanggan, id_kendaraan, tanggal_pinjam, tanggal_rencana_kembali, total_biaya, total_denda, status_transaksi) " +
                    "VALUES ('TRX-2026-038', 'PLG-005', 'KD-007', '2026-10-31', '2026-11-02', 600000, 0, 'Terlambat');");

            stmt.execute("INSERT OR IGNORE INTO transaksi " +
                    "(id_transaksi, id_pelanggan, id_kendaraan, tanggal_pinjam, tanggal_rencana_kembali, total_biaya, total_denda, status_transaksi) " +
                    "VALUES ('TRX-2026-041', 'PLG-001', 'KD-008', '2026-11-01', '2026-11-03', 700000, 0, 'Disewa');");

            stmt.execute("INSERT OR IGNORE INTO transaksi " +
                    "(id_transaksi, id_pelanggan, id_kendaraan, tanggal_pinjam, tanggal_rencana_kembali, total_biaya, total_denda, status_transaksi) " +
                    "VALUES ('TRX-2026-042', 'PLG-002', 'KD-003', '2026-11-01', '2026-11-03', 180000, 0, 'Disewa');");

            stmt.execute("INSERT OR IGNORE INTO transaksi " +
                    "(id_transaksi, id_pelanggan, id_kendaraan, tanggal_pinjam, tanggal_rencana_kembali, total_biaya, total_denda, status_transaksi) " +
                    "VALUES ('TRX-2026-043', 'PLG-003', 'KD-002', '2026-11-01', '2026-11-03', 1000000, 0, 'Disewa');");

            stmt.execute("INSERT OR IGNORE INTO transaksi " +
                    "(id_transaksi, id_pelanggan, id_kendaraan, tanggal_pinjam, tanggal_rencana_kembali, total_biaya, total_denda, status_transaksi) " +
                    "VALUES ('TRX-2026-044', 'PLG-007', 'KD-006', '2026-11-01', '2026-11-03', 3450000, 0, 'Disewa');");

        } catch (SQLException e) {
            System.err.println("Gagal seed data awal: " + e.getMessage());
        }
    }
}
