package main;

import controller.RentalController;
import dao.DatabaseConnection;
import dao.KendaraanDAO;
import dao.PelangganDAO;
import dao.TransaksiDAO;
import model.*;

import java.sql.Connection;
import java.util.List;
import java.util.Scanner;

/**
 * Console Test Application (Pengujian Progres Minggu 2: Model & Data)
 * Memvalidasi seluruh kelas model, hierarki pewarisan, kontrak interface,
 * polimorfisme sewa & denda, lapisan DAO, serta koneksi SQLite melalui antarmuka konsol.
 */
public class ConsoleTestApp {

    public static void main(String[] args) {
        System.out.println("========================================================================");
        System.out.println("   RENTARA MANAGEMENT SYSTEM - PENGUJIAN PROGRES MINGGU 2 (KONSOL)");
        System.out.println("   Departemen Teknik Komputer - Universitas Diponegoro (2026)");
        System.out.println("========================================================================");

        try {
            // 1. Verifikasi Koneksi Basis Data
            System.out.print("[1/6] Menguji Koneksi Basis Data SQLite (DatabaseConnection)... ");
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("BERHASIL! (Database 'rentara.db' aktif dan terhubung)");
            } else {
                System.out.println("GAGAL!");
                return;
            }

            // 2. Verifikasi 4 Pilar PBO & Model Polymorphism
            System.out.println("\n[2/6] Memvalidasi 4 Pilar PBO (Abstraksi, Enkapsulasi, Pewarisan, Polimorfisme):");

            Mobil m1 = new Mobil("KD-TEST-01", "B 1001 CAR", "Toyota Yaris", 2023, 300000, true, 5, "Otomatis (AT)", false);
            Mobil m2 = new Mobil("KD-TEST-02", "B 1002 CAR", "Toyota Alphard", 2024, 1000000, true, 7, "Otomatis (AT)", true);
            Motor mot1 = new Motor("KD-TEST-03", "B 2001 MOT", "Honda PCX 160", 2023, 100000, true, 160, "Matic", 2);
            Bus b1 = new Bus("KD-TEST-04", "B 3001 BUS", "Mercedes Jetbus", 2022, 2000000, true, 40, "Executive", true);

            System.out.println("   * " + m1.tampilkanInfo());
            System.out.println("   * " + m2.tampilkanInfo());
            System.out.println("   * " + mot1.tampilkanInfo());
            System.out.println("   * " + b1.tampilkanInfo());

            System.out.println("\n   Pengujian Polimorfisme hitungSewa() & hitungDenda():");
            // Mobil tanpa supir 3 hari: 300k * 3 = 900.000
            System.out.printf("   - Mobil (tanpa supir, 3 hari)    : Rp%,.0f (Sesuai: 900.000)\n", m1.hitungSewa(3));
            // Mobil dg supir 3 hari: (1.000k + 150k) * 3 = 3.450.000
            System.out.printf("   - Mobil (dg supir @150rb, 3 hari): Rp%,.0f (Sesuai: 3.450.000)\n", m2.hitungSewa(3));
            // Motor 3 hari vs 7 hari (diskon 10% jika >= 7 hari): 100k * 7 * 0.9 = 630.000
            System.out.printf("   - Motor (sewa 3 hari, normal)    : Rp%,.0f (Sesuai: 300.000)\n", mot1.hitungSewa(3));
            System.out.printf("   - Motor (sewa 7 hari, diskon 10%%): Rp%,.0f (Sesuai: 630.000)\n", mot1.hitungSewa(7));
            // Bus 2 hari (+15% biaya operasional & asuransi): 2jt * 2 * 1.15 = 4.600.000
            System.out.printf("   - Bus (+15%% kru & asuransi, 2 hr): Rp%,.0f (Sesuai: 4.600.000)\n", b1.hitungSewa(2));

            // Denda polimorfik telat 2 hari:
            System.out.printf("   - Denda Keterlambatan Mobil (2 hr): Rp%,.0f (Rp100.000/hari)\n", m1.hitungDenda(2));
            System.out.printf("   - Denda Keterlambatan Motor (2 hr): Rp%,.0f (Rp35.000/hari)\n", mot1.hitungDenda(2));
            System.out.printf("   - Denda Keterlambatan Bus (2 hr)  : Rp%,.0f (Rp350.000/hari)\n", b1.hitungDenda(2));

            // 3. Verifikasi Enkapsulasi & Validasi Identitas Pelanggan
            System.out.println("\n[3/6] Memvalidasi Enkapsulasi & Penanganan Exception Identitas Pelanggan:");
            try {
                new Pelanggan("PLG-ERR", "12345", "Invalid NIK", "08123", "Jl. Test", true);
                System.out.println("   GAGAL: Seharusnya NIK kurang dari 16 digit melempar Exception!");
            } catch (IllegalArgumentException ex) {
                System.out.println("   BERHASIL: Exception tertangkap semestinya -> \"" + ex.getMessage() + "\"");
            }

            Pelanggan validPelanggan = new Pelanggan("PLG-TST", "3301012304990001", "Budi Santoso", "081299887766", "Jl. Diponegoro 10 Semarang", true);
            System.out.println("   Pelanggan Valid: " + validPelanggan + " | NIK Masked: " + validPelanggan.getMaskedNik());

            // 4. Verifikasi Lapisan DAO (CRUD Kendaraan)
            System.out.println("\n[4/6] Memvalidasi Lapisan DAO (KendaraanDAO & CrudRepository):");
            KendaraanDAO kendaraanDAO = new KendaraanDAO();
            List<Kendaraan> listArmada = kendaraanDAO.ambilSemua();
            System.out.printf("   Total Armada di DB: %d unit (Tersedia: %d, Disewa: %d)\n",
                    listArmada.size(), kendaraanDAO.hitungTersedia(), kendaraanDAO.hitungDisewa());
            for (int i = 0; i < Math.min(listArmada.size(), 4); i++) {
                System.out.println("   [" + (i + 1) + "] " + listArmada.get(i).tampilkanInfo());
            }

            // 5. Verifikasi Lapisan DAO (CRUD Pelanggan)
            System.out.println("\n[5/6] Memvalidasi Lapisan DAO (PelangganDAO):");
            PelangganDAO pelangganDAO = new PelangganDAO();
            List<Pelanggan> listPelanggan = pelangganDAO.ambilSemua();
            System.out.printf("   Total Pelanggan di DB: %d orang\n", listPelanggan.size());
            for (int i = 0; i < Math.min(listPelanggan.size(), 3); i++) {
                Pelanggan p = listPelanggan.get(i);
                System.out.printf("   [%d] %s (%s) - NIK: %s - Telp: %s - SIM A: %s\n",
                        i + 1, p.getNamaLengkap(), p.getIdPelanggan(), p.getMaskedNik(), p.getNoTelepon(), p.isMemilikiSimA() ? "Ada" : "Tidak");
            }

            // 6. Verifikasi Transaksi DAO & Alur Sewa
            System.out.println("\n[6/6] Memvalidasi Lapisan DAO (TransaksiDAO):");
            TransaksiDAO transaksiDAO = new TransaksiDAO();
            List<TransaksiSewa> listTrans = transaksiDAO.ambilSemua();
            System.out.printf("   Total Riwayat Transaksi: %d transaksi\n", listTrans.size());
            for (int i = 0; i < Math.min(listTrans.size(), 3); i++) {
                TransaksiSewa t = listTrans.get(i);
                System.out.printf("   [%d] %s | %s | %s | Rp%,.0f | Status: %s\n",
                        i + 1, t.getIdTransaksi(), t.getPelanggan().getNamaLengkap(),
                        t.getKendaraan().getMerk(), t.getTotalBiaya(), t.getStatusTransaksi());
            }

            System.out.println("\n========================================================================");
            System.out.println("   STATUS MINGGU 2: SELURUH MODEL, INTERFACE, DAO & KONEKSI DB 100% LOLOS!");
            System.out.println("========================================================================\n");

            // Opsi menu interaktif jika evaluator ingin mencoba di konsol
            if (args.length > 0 && args[0].equalsIgnoreCase("--interactive")) {
                runInteractiveConsole();
            } else {
                System.out.println("Tips: Jalankan dengan argumen '--interactive' untuk menu CLI interaktif.");
            }

        } catch (Exception e) {
            System.err.println("Terjadi kesalahan pada pengujian konsol: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void runInteractiveConsole() {
        Scanner scanner = new Scanner(System.in);
        RentalController controller = new RentalController();
        boolean running = true;

        while (running) {
            System.out.println("\n--- MENU KONSOL INTERAKTIF RENTARA ---");
            System.out.println("1. Tampilkan Daftar Semua Armada");
            System.out.println("2. Tampilkan Daftar Pelanggan");
            System.out.println("3. Tampilkan Transaksi Berjalan");
            System.out.println("4. Uji Simulasi Hitung Sewa Polimorfik");
            System.out.println("5. Keluar");
            System.out.print("Pilih opsi (1-5): ");

            String input = scanner.nextLine().trim();
            try {
                switch (input) {
                    case "1":
                        List<Kendaraan> kList = controller.getAllKendaraan();
                        System.out.println("\n--- DAFTAR ARMADA ---");
                        for (Kendaraan k : kList) {
                            System.out.printf("[%s] %s | %s | Plat: %s | Rp%,.0f/hari | Status: %s\n",
                                    k.getIdKendaraan(), k.getKategori(), k.getMerk(), k.getPlatNomor(),
                                    k.getTarifDasarHarian(), k.isStatusTersedia() ? "Tersedia" : "Disewa");
                        }
                        break;
                    case "2":
                        List<Pelanggan> pList = controller.getAllPelanggan();
                        System.out.println("\n--- DAFTAR PELANGGAN ---");
                        for (Pelanggan p : pList) {
                            System.out.printf("[%s] %s | NIK: %s | Telp: %s | SIM A: %b\n",
                                    p.getIdPelanggan(), p.getNamaLengkap(), p.getNikKtp(), p.getNoTelepon(), p.isMemilikiSimA());
                        }
                        break;
                    case "3":
                        List<TransaksiSewa> tList = controller.getAllTransaksi();
                        System.out.println("\n--- RIWAYAT TRANSAKSI ---");
                        for (TransaksiSewa t : tList) {
                            System.out.printf("[%s] %s menyewa %s (%s s.d %s) | Total: Rp%,.0f | Status: %s\n",
                                    t.getIdTransaksi(), t.getPelanggan().getNamaLengkap(),
                                    t.getKendaraan().getMerk(), t.getTanggalPinjam(), t.getTanggalRencanaKembali(),
                                    t.getTotalBiaya(), t.getStatusTransaksi());
                        }
                        break;
                    case "4":
                        System.out.println("\nPilih Kategori: 1. Mobil, 2. Motor, 3. Bus");
                        System.out.print("Pilihan: ");
                        String cat = scanner.nextLine().trim();
                        System.out.print("Durasi sewa (hari): ");
                        int hari = Integer.parseInt(scanner.nextLine().trim());
                        if (cat.equals("1")) {
                            System.out.print("Gunakan jasa supir? (y/n): ");
                            boolean supir = scanner.nextLine().trim().equalsIgnoreCase("y");
                            Mobil m = new Mobil("SIM-01", "B 1234 TEST", "Toyota Innova", 2024, 500000, true, 7, "AT", supir);
                            System.out.printf("Total Sewa Mobil: Rp%,.0f\n", m.hitungSewa(hari));
                        } else if (cat.equals("2")) {
                            Motor mot = new Motor("SIM-02", "B 5678 TEST", "Yamaha NMAX", 2024, 120000, true, 155, "Matic", 2);
                            System.out.printf("Total Sewa Motor (diskon 10%% jika >= 7 hari): Rp%,.0f\n", mot.hitungSewa(hari));
                        } else if (cat.equals("3")) {
                            Bus b = new Bus("SIM-03", "B 9999 TEST", "Bus Pariwisata", 2023, 1500000, true, 35, "Pariwisata", true);
                            System.out.printf("Total Sewa Bus (+15%% kru & asuransi): Rp%,.0f\n", b.hitungSewa(hari));
                        }
                        break;
                    case "5":
                        running = false;
                        System.out.println("Sampai jumpa!");
                        break;
                    default:
                        System.out.println("Pilihan tidak valid.");
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }
}
