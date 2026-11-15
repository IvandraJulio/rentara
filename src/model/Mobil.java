package model;

/**
 * Subclass Mobil
 * Mewarisi Kendaraan dan menerapkan perhitungan sewa polimorfik (opsi supir Rp150.000/hari)
 * serta denda keterlambatan Rp100.000/hari.
 */
public class Mobil extends Kendaraan {
    private int jumlahKursi;
    private String jenisTransmisi; // "Otomatis (AT)" atau "Manual (MT)"
    private boolean termasukSupir;

    public Mobil(String idKendaraan, String platNomor, String merk, int tahun, double tarifDasarHarian,
                 boolean statusTersedia, int jumlahKursi, String jenisTransmisi, boolean termasukSupir) {
        super(idKendaraan, platNomor, merk, tahun, tarifDasarHarian, statusTersedia);
        setJumlahKursi(jumlahKursi);
        setJenisTransmisi(jenisTransmisi);
        this.termasukSupir = termasukSupir;
    }

    @Override
    public double hitungSewa(int hari) {
        if (hari <= 0) hari = 1;
        double total = tarifDasarHarian * hari;
        if (termasukSupir) {
            total += 150_000.0 * hari;
        }
        return total;
    }

    @Override
    public double hitungDenda(int hariTelat) {
        if (hariTelat <= 0) return 0.0;
        return 100_000.0 * hariTelat;
    }

    @Override
    public String getKategori() {
        return "Mobil";
    }

    @Override
    public String tampilkanInfo() {
        return String.format("Mobil: %s (%s) | Thn %d | %d Kursi | %s | Supir: %s | Rp%,.0f/hari",
                merk, platNomor, tahun, jumlahKursi, jenisTransmisi, (termasukSupir ? "Ya" : "Tidak"), tarifDasarHarian);
    }

    public int getJumlahKursi() {
        return jumlahKursi;
    }

    public void setJumlahKursi(int jumlahKursi) {
        if (jumlahKursi <= 0) {
            throw new IllegalArgumentException("Jumlah kursi harus lebih dari 0.");
        }
        this.jumlahKursi = jumlahKursi;
    }

    public String getJenisTransmisi() {
        return jenisTransmisi;
    }

    public void setJenisTransmisi(String jenisTransmisi) {
        if (jenisTransmisi == null || jenisTransmisi.trim().isEmpty()) {
            this.jenisTransmisi = "Otomatis (AT)";
        } else {
            this.jenisTransmisi = jenisTransmisi.trim();
        }
    }

    public boolean isTermasukSupir() {
        return termasukSupir;
    }

    public void setTermasukSupir(boolean termasukSupir) {
        this.termasukSupir = termasukSupir;
    }
}
