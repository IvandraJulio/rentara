package model;

import java.time.Year;

/**
 * Abstract Class Kendaraan
 * Superclass untuk seluruh armada (Mobil, Motor, Bus) yang menerapkan prinsip Abstraksi,
 * Enkapsulasi, dan Pewarisan (Inheritance).
 */
public abstract class Kendaraan implements DapatDisewa {
    protected String idKendaraan;
    protected String platNomor;
    protected String merk;
    protected int tahun;
    protected double tarifDasarHarian;
    protected boolean statusTersedia;

    public Kendaraan(String idKendaraan, String platNomor, String merk, int tahun, double tarifDasarHarian, boolean statusTersedia) {
        setIdKendaraan(idKendaraan);
        setPlatNomor(platNomor);
        setMerk(merk);
        setTahun(tahun);
        setTarifDasarHarian(tarifDasarHarian);
        this.statusTersedia = statusTersedia;
    }

    // Abstract methods - wajib diimplementasikan oleh subclass secara polimorfik
    @Override
    public abstract double hitungSewa(int hari);

    @Override
    public abstract double hitungDenda(int hariTelat);

    public abstract String getKategori();

    public abstract String tampilkanInfo();

    @Override
    public boolean cekKetersediaan() {
        return this.statusTersedia;
    }

    // Getters dan Setters tervalidasi (Enkapsulasi)
    public String getIdKendaraan() {
        return idKendaraan;
    }

    public void setIdKendaraan(String idKendaraan) {
        if (idKendaraan == null || idKendaraan.trim().isEmpty()) {
            throw new IllegalArgumentException("ID Kendaraan tidak boleh kosong.");
        }
        this.idKendaraan = idKendaraan.trim();
    }

    public String getPlatNomor() {
        return platNomor;
    }

    public void setPlatNomor(String platNomor) {
        if (platNomor == null || platNomor.trim().isEmpty()) {
            throw new IllegalArgumentException("Plat nomor tidak boleh kosong.");
        }
        this.platNomor = platNomor.trim().toUpperCase();
    }

    public String getMerk() {
        return merk;
    }

    public void setMerk(String merk) {
        if (merk == null || merk.trim().isEmpty()) {
            throw new IllegalArgumentException("Merk/model kendaraan tidak boleh kosong.");
        }
        this.merk = merk.trim();
    }

    public int getTahun() {
        return tahun;
    }

    public void setTahun(int tahun) {
        int maxYear = Year.now().getValue() + 1;
        if (tahun < 1980 || tahun > maxYear) {
            throw new IllegalArgumentException("Tahun rakit tidak valid (antara 1980 - " + maxYear + ").");
        }
        this.tahun = tahun;
    }

    public double getTarifDasarHarian() {
        return tarifDasarHarian;
    }

    public void setTarifDasarHarian(double tarifDasarHarian) {
        if (tarifDasarHarian < 0) {
            throw new IllegalArgumentException("Tarif dasar harian tidak boleh negatif.");
        }
        this.tarifDasarHarian = tarifDasarHarian;
    }

    public boolean isStatusTersedia() {
        return statusTersedia;
    }

    public void setStatusTersedia(boolean statusTersedia) {
        this.statusTersedia = statusTersedia;
    }

    @Override
    public String toString() {
        return "[" + getKategori() + "] " + merk + " (" + platNomor + ") - Rp" + String.format("%,.0f", tarifDasarHarian) + "/hari";
    }
}
