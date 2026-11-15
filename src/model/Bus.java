package model;

/**
 * Subclass Bus
 * Mewarisi Kendaraan dan menerapkan perhitungan sewa polimorfik (+15% asuransi & kru)
 * serta denda keterlambatan Rp350.000/hari.
 */
public class Bus extends Kendaraan {
    private int kapasitasPenumpang;
    private String kelasBus; // "Pariwisata", "Executive", "Ekonomi"
    private boolean fasilitasToilet;

    public Bus(String idKendaraan, String platNomor, String merk, int tahun, double tarifDasarHarian,
               boolean statusTersedia, int kapasitasPenumpang, String kelasBus, boolean fasilitasToilet) {
        super(idKendaraan, platNomor, merk, tahun, tarifDasarHarian, statusTersedia);
        setKapasitasPenumpang(kapasitasPenumpang);
        setKelasBus(kelasBus);
        this.fasilitasToilet = fasilitasToilet;
    }

    @Override
    public double hitungSewa(int hari) {
        if (hari <= 0) hari = 1;
        double base = tarifDasarHarian * hari;
        double biayaKruAsuransi = base * 0.15; // 15% kru & asuransi operasional
        return base + biayaKruAsuransi;
    }

    @Override
    public double hitungDenda(int hariTelat) {
        if (hariTelat <= 0) return 0.0;
        return 350_000.0 * hariTelat;
    }

    @Override
    public String getKategori() {
        return "Bus";
    }

    @Override
    public String tampilkanInfo() {
        return String.format("Bus: %s (%s) | Thn %d | %d Kursi | %s | Toilet: %s | Rp%,.0f/hari",
                merk, platNomor, tahun, kapasitasPenumpang, kelasBus, (fasilitasToilet ? "Ada" : "Tidak"), tarifDasarHarian);
    }

    public int getKapasitasPenumpang() {
        return kapasitasPenumpang;
    }

    public void setKapasitasPenumpang(int kapasitasPenumpang) {
        if (kapasitasPenumpang <= 0) {
            throw new IllegalArgumentException("Kapasitas penumpang harus lebih dari 0.");
        }
        this.kapasitasPenumpang = kapasitasPenumpang;
    }

    public String getKelasBus() {
        return kelasBus;
    }

    public void setKelasBus(String kelasBus) {
        if (kelasBus == null || kelasBus.trim().isEmpty()) {
            this.kelasBus = "Pariwisata";
        } else {
            this.kelasBus = kelasBus.trim();
        }
    }

    public boolean isFasilitasToilet() {
        return fasilitasToilet;
    }

    public void setFasilitasToilet(boolean fasilitasToilet) {
        this.fasilitasToilet = fasilitasToilet;
    }
}
