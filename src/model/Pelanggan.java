package model;

/**
 * Kelas Entitas Pelanggan
 * Mengelola data penyewa armada beserta validasi identitas (NIK 16 digit).
 */
public class Pelanggan {
    private String idPelanggan;
    private String nikKtp;
    private String namaLengkap;
    private String noTelepon;
    private String alamat;
    private boolean memilikiSimA;

    public Pelanggan(String idPelanggan, String nikKtp, String namaLengkap, String noTelepon, String alamat, boolean memilikiSimA) {
        setIdPelanggan(idPelanggan);
        setNikKtp(nikKtp);
        setNamaLengkap(namaLengkap);
        setNoTelepon(noTelepon);
        setAlamat(alamat);
        this.memilikiSimA = memilikiSimA;
    }

    public boolean validasiIdentitas() {
        return nikKtp != null && nikKtp.matches("\\d{16}") &&
                namaLengkap != null && !namaLengkap.trim().isEmpty() &&
                noTelepon != null && !noTelepon.trim().isEmpty();
    }

    // Getters and Setters
    public String getIdPelanggan() {
        return idPelanggan;
    }

    public void setIdPelanggan(String idPelanggan) {
        if (idPelanggan == null || idPelanggan.trim().isEmpty()) {
            throw new IllegalArgumentException("ID Pelanggan tidak boleh kosong.");
        }
        this.idPelanggan = idPelanggan.trim();
    }

    public String getNikKtp() {
        return nikKtp;
    }

    public void setNikKtp(String nikKtp) {
        if (nikKtp == null || !nikKtp.trim().matches("\\d{16}")) {
            throw new IllegalArgumentException("NIK harus terdiri dari 16 digit angka.");
        }
        this.nikKtp = nikKtp.trim();
    }

    public String getNamaLengkap() {
        return namaLengkap;
    }

    public void setNamaLengkap(String namaLengkap) {
        if (namaLengkap == null || namaLengkap.trim().isEmpty()) {
            throw new IllegalArgumentException("Nama lengkap tidak boleh kosong.");
        }
        this.namaLengkap = namaLengkap.trim();
    }

    public String getNoTelepon() {
        return noTelepon;
    }

    public void setNoTelepon(String noTelepon) {
        if (noTelepon == null || noTelepon.trim().isEmpty()) {
            throw new IllegalArgumentException("Nomor telepon tidak boleh kosong.");
        }
        this.noTelepon = noTelepon.trim();
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = (alamat != null) ? alamat.trim() : "";
    }

    public boolean isMemilikiSimA() {
        return memilikiSimA;
    }

    public void setMemilikiSimA(boolean memilikiSimA) {
        this.memilikiSimA = memilikiSimA;
    }

    public String getMaskedNik() {
        if (nikKtp != null && nikKtp.length() == 16) {
            return nikKtp.substring(0, 4) + " •••• •••• " + nikKtp.substring(12);
        }
        return nikKtp;
    }

    @Override
    public String toString() {
        return namaLengkap + " (" + idPelanggan + ")";
    }
}
