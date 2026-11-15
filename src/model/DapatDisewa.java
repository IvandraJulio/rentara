package model;

/**
 * Interface DapatDisewa
 * Menetapkan kontrak perilaku penyewaan armada rental sesuai prinsip Abstraksi PBO.
 */
public interface DapatDisewa {
    double hitungSewa(int hari);
    double hitungDenda(int hariTelat);
    boolean cekKetersediaan();
}
