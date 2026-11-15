package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Kelas Entitas TransaksiSewa
 * Menghubungkan Pelanggan dan Kendaraan, memanfaatkan polimorfisme hitungSewa()
 * dan hitungDenda() untuk kalkulasi biaya dan denda keterlambatan.
 */
public class TransaksiSewa {
    private String idTransaksi;
    private Pelanggan pelanggan;
    private Kendaraan kendaraan;
    private LocalDate tanggalPinjam;
    private LocalDate tanggalRencanaKembali;
    private LocalDate tanggalAktualKembali;
    private double totalBiaya;
    private double totalDenda;
    private String statusTransaksi; // "Disewa", "Terlambat", "Selesai"

    public TransaksiSewa(String idTransaksi, Pelanggan pelanggan, Kendaraan kendaraan,
                         LocalDate tanggalPinjam, LocalDate tanggalRencanaKembali) {
        this.idTransaksi = idTransaksi;
        this.pelanggan = pelanggan;
        this.kendaraan = kendaraan;
        this.tanggalPinjam = tanggalPinjam;
        this.tanggalRencanaKembali = tanggalRencanaKembali;
        this.tanggalAktualKembali = null;
        this.totalDenda = 0.0;
        this.statusTransaksi = "Disewa";
        prosesPerhitunganTotal();
    }

    public TransaksiSewa(String idTransaksi, Pelanggan pelanggan, Kendaraan kendaraan,
                         LocalDate tanggalPinjam, LocalDate tanggalRencanaKembali,
                         LocalDate tanggalAktualKembali, double totalBiaya, double totalDenda,
                         String statusTransaksi) {
        this.idTransaksi = idTransaksi;
        this.pelanggan = pelanggan;
        this.kendaraan = kendaraan;
        this.tanggalPinjam = tanggalPinjam;
        this.tanggalRencanaKembali = tanggalRencanaKembali;
        this.tanggalAktualKembali = tanggalAktualKembali;
        this.totalBiaya = totalBiaya;
        this.totalDenda = totalDenda;
        this.statusTransaksi = statusTransaksi;
    }

    public int getDurasiHari() {
        if (tanggalPinjam == null || tanggalRencanaKembali == null) return 1;
        long days = ChronoUnit.DAYS.between(tanggalPinjam, tanggalRencanaKembali);
        return days <= 0 ? 1 : (int) days;
    }

    /**
     * Memanfaatkan Polimorfisme: kendaraan.hitungSewa() secara dinamis memanggil
     * implementasi Mobil, Motor, atau Bus sesuai tipe objek aktual.
     */
    public double prosesPerhitunganTotal() {
        if (kendaraan != null) {
            int hari = getDurasiHari();
            this.totalBiaya = kendaraan.hitungSewa(hari);
        }
        return this.totalBiaya;
    }

    /**
     * Menghitung denda secara polimorfik jika tanggal aktual melewati tanggal rencana kembali.
     */
    public double hitungDendaAktual(LocalDate tglAktual) {
        if (kendaraan == null || tglAktual == null || tanggalRencanaKembali == null) return 0.0;
        long telatHari = ChronoUnit.DAYS.between(tanggalRencanaKembali, tglAktual);
        if (telatHari > 0) {
            return kendaraan.hitungDenda((int) telatHari);
        }
        return 0.0;
    }

    public int getHariKeterlambatan(LocalDate tglAktual) {
        if (tglAktual == null || tanggalRencanaKembali == null) return 0;
        long telatHari = ChronoUnit.DAYS.between(tanggalRencanaKembali, tglAktual);
        return telatHari > 0 ? (int) telatHari : 0;
    }

    /**
     * Menyelesaikan pengembalian kendaraan:
     * - Menghitung denda polimorfik
     * - Mengubah status transaksi menjadi "Selesai"
     * - Mengembalikan status ketersediaan armada menjadi true (Tersedia)
     */
    public void selesaikanPengembalian(LocalDate tglAktual) {
        this.tanggalAktualKembali = tglAktual;
        this.totalDenda = hitungDendaAktual(tglAktual);
        this.statusTransaksi = "Selesai";
        if (kendaraan != null) {
            kendaraan.setStatusTersedia(true);
        }
    }

    public double getTotalAkhir() {
        return totalBiaya + totalDenda;
    }

    public String cetakStruk() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM yyyy");
        StringBuilder sb = new StringBuilder();
        sb.append("=========================================\n");
        sb.append("        RENTARA MANAGEMENT SYSTEM        \n");
        sb.append("         STRUK BUKTI PENYEWAAN           \n");
        sb.append("=========================================\n");
        sb.append(String.format("ID Transaksi   : %s\n", idTransaksi));
        sb.append(String.format("Pelanggan      : %s (%s)\n", pelanggan.getNamaLengkap(), pelanggan.getIdPelanggan()));
        sb.append(String.format("Kontak / NIK   : %s / %s\n", pelanggan.getNoTelepon(), pelanggan.getMaskedNik()));
        sb.append(String.format("Armada         : %s - %s\n", kendaraan.getMerk(), kendaraan.getPlatNomor()));
        sb.append(String.format("Kategori       : %s\n", kendaraan.getKategori()));
        sb.append(String.format("Tgl Pinjam     : %s\n", tanggalPinjam.format(dtf)));
        sb.append(String.format("Jadwal Kembali : %s (%d hari)\n", tanggalRencanaKembali.format(dtf), getDurasiHari()));
        if (tanggalAktualKembali != null) {
            sb.append(String.format("Tgl Kembali    : %s\n", tanggalAktualKembali.format(dtf)));
        }
        sb.append("-----------------------------------------\n");
        sb.append(String.format("Biaya Sewa     : Rp%,.0f\n", totalBiaya));
        if (totalDenda > 0) {
            sb.append(String.format("Denda Keterlambatan : Rp%,.0f\n", totalDenda));
        }
        sb.append("-----------------------------------------\n");
        sb.append(String.format("TOTAL PEMBAYARAN    : Rp%,.0f\n", getTotalAkhir()));
        sb.append(String.format("Status Transaksi    : %s\n", statusTransaksi));
        sb.append("=========================================\n");
        sb.append("  Terima kasih telah menyewa di Rentara! \n");
        sb.append("=========================================\n");
        return sb.toString();
    }

    // Getters and Setters
    public String getIdTransaksi() {
        return idTransaksi;
    }

    public void setIdTransaksi(String idTransaksi) {
        this.idTransaksi = idTransaksi;
    }

    public Pelanggan getPelanggan() {
        return pelanggan;
    }

    public void setPelanggan(Pelanggan pelanggan) {
        this.pelanggan = pelanggan;
    }

    public Kendaraan getKendaraan() {
        return kendaraan;
    }

    public void setKendaraan(Kendaraan kendaraan) {
        this.kendaraan = kendaraan;
    }

    public LocalDate getTanggalPinjam() {
        return tanggalPinjam;
    }

    public void setTanggalPinjam(LocalDate tanggalPinjam) {
        this.tanggalPinjam = tanggalPinjam;
    }

    public LocalDate getTanggalRencanaKembali() {
        return tanggalRencanaKembali;
    }

    public void setTanggalRencanaKembali(LocalDate tanggalRencanaKembali) {
        this.tanggalRencanaKembali = tanggalRencanaKembali;
    }

    public LocalDate getTanggalAktualKembali() {
        return tanggalAktualKembali;
    }

    public void setTanggalAktualKembali(LocalDate tanggalAktualKembali) {
        this.tanggalAktualKembali = tanggalAktualKembali;
    }

    public double getTotalBiaya() {
        return totalBiaya;
    }

    public void setTotalBiaya(double totalBiaya) {
        this.totalBiaya = totalBiaya;
    }

    public double getTotalDenda() {
        return totalDenda;
    }

    public void setTotalDenda(double totalDenda) {
        this.totalDenda = totalDenda;
    }

    public String getStatusTransaksi() {
        return statusTransaksi;
    }

    public void setStatusTransaksi(String statusTransaksi) {
        this.statusTransaksi = statusTransaksi;
    }
}
