package model;

/**
 * Subclass Motor
 * Mewarisi Kendaraan dan menerapkan perhitungan sewa polimorfik (diskon 10% jika sewa >= 7 hari)
 * serta denda keterlambatan Rp35.000/hari.
 */
public class Motor extends Kendaraan {
    private int kapasitasCC;
    private String tipeMotor; // "Matic", "Manual", "Sport"
    private int jumlahHelm;

    public Motor(String idKendaraan, String platNomor, String merk, int tahun, double tarifDasarHarian,
                 boolean statusTersedia, int kapasitasCC, String tipeMotor, int jumlahHelm) {
        super(idKendaraan, platNomor, merk, tahun, tarifDasarHarian, statusTersedia);
        setKapasitasCC(kapasitasCC);
        setTipeMotor(tipeMotor);
        setJumlahHelm(jumlahHelm);
    }

    @Override
    public double hitungSewa(int hari) {
        if (hari <= 0) hari = 1;
        double base = tarifDasarHarian * hari;
        if (hari >= 7) {
            base = base * 0.90; // Diskon 10% untuk sewa mingguan
        }
        return base;
    }

    @Override
    public double hitungDenda(int hariTelat) {
        if (hariTelat <= 0) return 0.0;
        return 35_000.0 * hariTelat;
    }

    @Override
    public String getKategori() {
        return "Motor";
    }

    @Override
    public String tampilkanInfo() {
        return String.format("Motor: %s (%s) | Thn %d | %d CC | %s | %d Helm | Rp%,.0f/hari",
                merk, platNomor, tahun, kapasitasCC, tipeMotor, jumlahHelm, tarifDasarHarian);
    }

    public int getKapasitasCC() {
        return kapasitasCC;
    }

    public void setKapasitasCC(int kapasitasCC) {
        if (kapasitasCC <= 0) {
            throw new IllegalArgumentException("Kapasitas mesin (CC) harus lebih dari 0.");
        }
        this.kapasitasCC = kapasitasCC;
    }

    public String getTipeMotor() {
        return tipeMotor;
    }

    public void setTipeMotor(String tipeMotor) {
        if (tipeMotor == null || tipeMotor.trim().isEmpty()) {
            this.tipeMotor = "Matic";
        } else {
            this.tipeMotor = tipeMotor.trim();
        }
    }

    public int getJumlahHelm() {
        return jumlahHelm;
    }

    public void setJumlahHelm(int jumlahHelm) {
        if (jumlahHelm < 0) {
            throw new IllegalArgumentException("Jumlah helm tidak boleh negatif.");
        }
        this.jumlahHelm = jumlahHelm;
    }
}
