# Rentara Management System
### Sistem Informasi Manajemen Rental Kendaraan Berbasis Desktop GUI (MVC & DAO)
**Departemen Teknik Komputer — Fakultas Teknik — Universitas Diponegoro (2026)**

---

## 📌 1. Deskripsi Proyek
Rentara Management System adalah aplikasi desktop berbasis Graphical User Interface (GUI) yang dibangun menggunakan bahasa pemrograman **Java** dengan menerapkan secara penuh **keempat pilar Pemrograman Berorientasi Objek (PBO)**, arsitektur **MVC (Model-View-Controller)**, dan lapisan persistensi **DAO (Data Access Object)** terhubung ke basis data permanen **SQLite**.

Aplikasi dirancang secara presisi mengikuti sketsa dan spesifikasi desain antarmuka (UI/UX) bertema *modern dark charcoal & warm orange*, mencakup pengelolaan armada (Mobil, Motor, Bus), data pelanggan penyewa, perhitungan tarif sewa dan denda keterlambatan secara polimorfik, serta pencatatan transaksi pengembalian armada.

---

## 🎯 2. Pemenuhan Target Capaian Proyek

### A. Minggu 2 — Progres 1: Model & Data
* **Kelas Model Lengkap:**
  - `Kendaraan` (*Abstract Class* superclass)
  - `Mobil`, `Motor`, `Bus` (*Subclasses* dengan atribut spesifik & rumus sewa/denda polimorfik)
  - `Pelanggan` (Entitas penyewa dengan validasi NIK 16 digit & SIM A)
  - `TransaksiSewa` (Entitas transaksi penyewaan & kalkulasi pengembalian)
* **Kontrak Interface:**
  - `DapatDisewa` (Kontrak perilaku `hitungSewa`, `hitungDenda`, dan `cekKetersediaan`)
  - `CrudRepository<T>` (Generic interface standarisasi operasi DAO)
* **Basis Data Permanen (SQLite):**
  - Menggunakan driver SQLite JDBC dengan konfigurasi otomatis tabel (`users`, `kendaraan`, `pelanggan`, `transaksi`) serta *initial data seeding* yang selaras dengan data mockup UI.
* **Pengujian Konsol:**
  - Tersedia kelas `ConsoleTestApp` untuk menguji dan memverifikasi seluruh lapisan Model, Enkapsulasi/Exception Handling, Polimorfisme sewa/denda, Interface, DAO, dan koneksi basis data langsung lewat terminal/konsol.

### B. Minggu 3 — Progres 2: GUI Terintegrasi
* **4 Tampilan Antarmuka GUI Modern Terintegrasi (Persis dengan UI Figma):**
  1. **Tampilan 1 — Login Petugas & Dashboard Utama (`00 · Login petugas` & `01 · Dashboard`)**:
     - Otentikasi petugas/admin (`admin@rentara.id` / `admin`).
     - Kartu Hero Banner armada, visualisasi grafik batang aktivitas mingguan (7 hari terakhir), 4 kartu statistik (*Total armada, Tersedia, Disewa, Pendapatan*), serta tabel transaksi pengembalian hari ini yang dilengkapi tombol aksi `Kembalikan →`.
  2. **Tampilan 2 — CRUD Master Kendaraan (`02 · Armada kendaraan`)**:
     - *Filter pills* armada (*Semua, Mobil, Motor, Bus*), status ketersediaan (*unit tersedia vs disewa*).
     - *Live search* berdasarkan merk atau plat nomor, filter status, dan tombol ekspor CSV.
     - JTable kustom dengan *status badge pill* (hijau untuk Tersedia, kuning/oranye untuk Disewa) serta tautan aksi Ubah dan Hapus dengan dialog konfirmasi.
     - Formulir CRUD dinamis di panel kanan yang otomatis menyesuaikan masukan spesifik Mobil (transmisi AT/MT & supir), Motor (CC, tipe motor, helm), atau Bus (kapasitas, kelas bus, toilet).
  3. **Tampilan 3 — CRUD Master Pelanggan (`03 · Data pelanggan`)**:
     - 3 Kartu statistik pelanggan (*Total pelanggan, Aktif menyewa, Pelanggan baru*).
     - Pencarian real-time Nama/NIK dan filter kepemilikan SIM A.
     - Formulir pelanggan dengan validasi NIK KTP interaktif (indikator realtime 16 digit).
  4. **Tampilan 4 — Transaksi Penyewaan & Pengembalian (`04 · Transaksi & pengembalian`)**:
     - **Panel Kiri (Penyewaan Baru):** Pemilihan Pelanggan, pemilihan Kendaraan tersedia, pemilihan tanggal pinjam/kembali, kalkulasi sewa otomatis secara polimorfik dengan rincian biaya transparan dan penyorotan total sewa (*Lime Green*).
     - **Panel Kanan (Pengembalian):** Pemilihan transaksi aktif, deteksi keterlambatan, kalkulasi denda otomatis per hari sesuai jenis kendaraan (Mobil: Rp100.000/hr, Motor: Rp35.000/hr, Bus: Rp350.000/hr), pemulihan status unit menjadi Tersedia.
     - **Panel Bawah:** Tabel transaksi berjalan dengan status badge dan fitur cetak struk penyewaan bukti transaksi.

---

## 🏛️ 3. Penerapan Empat Pilar PBO

| Pilar PBO | Komponen / Kelas | Implementasi Nyata pada Aplikasi Rentara |
| :--- | :--- | :--- |
| **1. Abstraksi** | `Kendaraan`, `DapatDisewa`, `CrudRepository<T>` | Superclass abstrak `Kendaraan` menetapkan kontrak method abstrak `hitungSewa(hari)` dan `hitungDenda(hariTelat)`. Interface `DapatDisewa` dan `CrudRepository<T>` memisahkan kontrak perilaku dari detail implementasi. |
| **2. Enkapsulasi** | Seluruh kelas model (`Kendaraan`, `Mobil`, `Motor`, `Bus`, `Pelanggan`, `TransaksiSewa`) | Seluruh variabel bertipe `private`/`protected`. Mutasi nilai melalui setter dilengkapi validasi ketat (tarif tidak boleh negatif, NIK wajib 16 digit, nama/plat tidak boleh kosong). |
| **3. Pewarisan** | `Kendaraan` $\rightarrow$ `Mobil`, `Motor`, `Bus` | Subclass mewarisi atribut umum (`idKendaraan`, `platNomor`, `merk`, `tahun`, `tarifDasarHarian`, `statusTersedia`) serta memperluas atribut spesifik per jenis armada. |
| **4. Polimorfisme** | Override method `hitungSewa()` & `hitungDenda()` | Kalkulasi biaya otomatis berubah sesuai objek instansi aktual:<br>• **Mobil:** $(\text{tarif} \times \text{hari}) + \text{biaya supir Rp150.000/hari}$ jika dipilih.<br>• **Motor:** Diskon 10% jika durasi sewa $\ge 7$ hari.<br>• **Bus:** Penambahan biaya operasional kru & asuransi $15\%$.<br>• **Denda:** Mobil Rp100.000/hr, Motor Rp35.000/hr, Bus Rp350.000/hr. |

---

## 📁 4. Struktur Direktori Proyek

```text
rentalpbo/
├── assets/                          # Aset visual resmi yang diekstrak dari Figma UI
│   ├── 00_img_0.jpeg               # Gambar mobil hero login
│   ├── 01_img_0.jpeg               # Gambar mobil banner dashboard
│   └── 02_img_0.png                # Avatar admin rental
├── lib/                             # Dependensi JAR pihak ketiga
│   ├── flatlaf.jar                 # FlatLaf Look and Feel Modern Dark
│   ├── slf4j-api.jar               # Logging abstraction
│   ├── slf4j-simple.jar            # Simple logger
│   └── sqlite-jdbc.jar             # SQLite JDBC driver
├── src/
│   ├── model/                      # Lapisan Model & Logika PBO
│   │   ├── DapatDisewa.java        # Interface kontrak sewa armada
│   │   ├── Kendaraan.java          # Superclass abstrak armada
│   │   ├── Mobil.java              # Subclass mobil
│   │   ├── Motor.java              # Subclass motor
│   │   ├── Bus.java                # Subclass bus
│   │   ├── Pelanggan.java          # Entitas pelanggan
│   │   └── TransaksiSewa.java      # Entitas transaksi sewa & pengembalian
│   ├── dao/                        # Lapisan Akses Basis Data (DAO)
│   │   ├── CrudRepository.java     # Generic DAO interface
│   │   ├── DatabaseConnection.java # Pengelola koneksi SQLite & seed data
│   │   ├── KendaraanDAO.java       # DAO tabel kendaraan
│   │   ├── PelangganDAO.java       # DAO tabel pelanggan
│   │   └── TransaksiDAO.java       # DAO tabel transaksi
│   ├── controller/                 # Lapisan Controller (MVC)
│   │   └── RentalController.java   # Jembatan logika bisnis antara View & Model/DAO
│   ├── view/                       # Lapisan Antarmuka Grafis Desktop GUI
│   │   ├── Theme.java              # Palet warna, tipografi & token UI
│   │   ├── UIComponents.java       # Komponen visual kustom (Card, Button, Table, Badge)
│   │   ├── LoginPanel.java         # Tampilan 1: Login petugas
│   │   ├── DashboardPanel.java     # Tampilan 1: Dashboard overview
│   │   ├── KendaraanPanel.java     # Tampilan 2: CRUD armada kendaraan
│   │   ├── PelangganPanel.java     # Tampilan 3: CRUD data pelanggan
│   │   ├── TransaksiPanel.java     # Tampilan 4: Transaksi & pengembalian
│   │   └── MainFrame.java          # Frame utama aplikasi dengan sidebar navigasi
│   └── main/                       # Titik Masuk Eksekusi Aplikasi
│       ├── Main.java               # Peluncur aplikasi Desktop GUI (Minggu 3)
│       └── ConsoleTestApp.java     # Peluncur pengujian konsol (Minggu 2)
├── build.bat / build.ps1           # Skrip kompilasi otomatis
├── run.bat / run.ps1               # Skrip menjalankan GUI (Minggu 3)
├── run-console.bat / .ps1          # Skrip menjalankan pengujian konsol (Minggu 2)
├── rentara.db                      # Basis data SQLite lokal
└── README.md                       # Dokumentasi lengkap proyek
```

---

## 🚀 5. Cara Menjalankan Aplikasi

### Persyaratan Sistem:
* Java Development Kit (JDK) versi 17 atau yang lebih baru (Teruji pada Java 22).

### Menjalankan Desktop GUI (Target Minggu 3):
Cukup klik ganda berkas `run.bat` atau jalankan via PowerShell:
```powershell
./run.ps1
```
Atau melalui baris perintah:
```bash
java -cp "bin;lib/*" main.Main
```

### Menjalankan Pengujian Konsol (Target Minggu 2):
Klik ganda berkas `run-console.bat` atau jalankan via PowerShell:
```powershell
./run-console.ps1
```
Untuk mengaktifkan menu interaktif pada konsol:
```powershell
./run-console.ps1 --interactive
```

### Kredensial Masuk (Login):
* **Email / Username:** `admin@rentara.id`
* **Kata Sandi:** `admin`
