# Credit Simulator

Console application untuk menghitung simulasi cicilan bulanan kredit kendaraan
(Motor/Mobil), dengan skema bunga yang naik tiap tahun dan pokok pinjaman yang
menurun (declining balance method).

Dibuat untuk technical test Backend Engineer.

---

## Bahasa & Tools

- **Java 17** (plain Java, tanpa framework eksternal seperti Spring)
- Library eksternal yang dipakai: **tidak ada**, kecuali `java.net.http.HttpClient`
  bawaan JDK 11+ untuk fitur "Load Existing Calculation" (sesuai requirement yang
  mengizinkan library khusus web service).
- Parsing JSON dilakukan manual (regex), tanpa Gson/Jackson.
- Build tool: **Maven**.

---

## Cara Menjalankan

### 1. Build project

```bash
mvn clean package
```

### 2. Mode Interaktif (input manual lewat terminal)

```bash
./credit_simulator
# atau
bin/credit_simulator
```

Akan muncul menu:

```
=== Credit Simulator ===
1. New Calculation
2. Load Existing Calculation
3. Show Commands
4. Exit
Pilih menu:
```

- **New Calculation** → menanyakan 6 input satu-satu (jenis kendaraan, kondisi,
  tahun, jumlah pinjaman, tenor, DP), lalu menampilkan breakdown cicilan per tahun.
- **Load Existing Calculation** → mengambil data pinjaman dari API
  (`run.mocky.io`) dan otomatis menghitung + menampilkan hasilnya.
- **Show Commands** → menampilkan daftar menu yang tersedia.
- **Exit** → keluar dari aplikasi.

### 3. Mode File Input (non-interaktif)

```bash
./credit_simulator file_inputs.txt
```

Format file `file_inputs.txt`: **6 baris, satu nilai per baris**, urutan tetap:

```
Mobil
Baru
2025
100000000
3
25000000
```

| Baris | Isi |
|---|---|
| 1 | Jenis Kendaraan (`Motor` / `Mobil`) |
| 2 | Kondisi Kendaraan (`Bekas` / `Baru`) |
| 3 | Tahun Kendaraan (4 digit) |
| 4 | Jumlah Pinjaman Total |
| 5 | Tenor Pinjaman (tahun) |
| 6 | Jumlah DP |

> Format ini **ditentukan sendiri** karena soal tidak menyertakan contoh isi
> `file_inputs.txt` secara eksplisit — lihat bagian **Asumsi** di bawah.

---

## Struktur Project

```
src/main/java/com/aditya/creditsimulator/
├── Main.java                    # Entry point, menu, composition root
│
├── vehicle/                     # Data & Factory kendaraan
│   ├── VehicleType.java         # enum: MOBIL (8%), MOTOR (9%)
│   ├── VehicleCondition.java    # enum: BARU (DP min 35%), BEKAS (DP min 25%)
│   ├── Vehicle.java             # model gabungan, immutable
│   └── VehicleFactory.java      # validasi & bikin objek Vehicle
│
├── model/
│   ├── LoanRequest.java         # data input pinjaman (typed)
│   ├── LoanRequestFactory.java  # parsing RawLoanInput -> LoanRequest
│   └── InstallmentResult.java   # hasil kalkulasi per tahun
│
├── calculator/
│   ├── InterestRateCalculator.java  # rate naik per tahun
│   └── LoanCalculator.java          # loop per tahun, declining balance
│
├── validator/
│   └── LoanValidator.java       # rule bisnis: tenor, DP minimum, max pinjaman
│
├── io/
│   ├── InputReader.java         # interface (Strategy Pattern)
│   ├── RawLoanInput.java        # data mentah (String semua)
│   ├── ConsoleInputReader.java  # baca dari Scanner
│   └── FileInputReader.java     # baca dari file .txt
│
└── service/
    ├── LoanApiClient.java           # panggil API via HttpClient
    └── JsonLoanResponseMapper.java  # parse JSON manual -> RawLoanInput
```

Ada 3 sumber input (console, file, API) yang semuanya bermuara jadi bentuk data
yang sama (`RawLoanInput`), lalu diproses lewat pipeline yang identik:

```
RawLoanInput -> LoanRequestFactory -> LoanValidator -> LoanCalculator -> print
```

---

## Design Pattern

- **Factory Pattern** — `VehicleFactory` dan `LoanRequestFactory` jadi satu
  pintu masuk untuk membuat objek yang sudah tervalidasi dari data mentah.
- **Strategy Pattern** — `InputReader` sebagai interface, dengan dua
  implementasi (`ConsoleInputReader`, `FileInputReader`) yang bisa saling
  gantiin tanpa `Main.java` perlu tau detail sumbernya.
- **Manual Dependency Injection** — semua class menerima dependency-nya lewat
  constructor (bukan `new` sendiri di dalam), dirakit satu tempat di
  `Main.java` (composition root). Ini menggantikan peran DI container seperti
  Spring, sambil tetap tidak memakai framework eksternal.
- **Immutable objects** — `Vehicle`, `LoanRequest`, `InstallmentResult` semua
  tidak punya setter; sekali dibuat lewat factory, datanya pasti valid dan
  tidak bisa berubah di tengah jalan.

---

## Formula Perhitungan

Menggunakan metode **declining balance**: pokok pinjaman yang dipakai hitung
bunga tiap tahun adalah sisa dari tahun sebelumnya, bukan pokok awal terus-menerus.

Untuk tahun ke-n (n = 1..tenorYears):

```
rate(n)               = base_rate + kenaikan_kumulatif(n)
totalWithInterest(n)  = pokok(n) * (1 + rate(n))
remainingMonths(n)    = (tenorYears - n + 1) * 12
installmentMonthly(n) = totalWithInterest(n) / remainingMonths(n)
installmentYearly(n)  = installmentMonthly(n) * 12
pokok(n+1)            = totalWithInterest(n) - installmentYearly(n)
```

dengan `pokok(1) = totalLoanAmount - downPayment`.

Formula ini diverifikasi cocok 100% dengan sample di `Rumus.xlsx` (Mobil Baru,
pinjaman 100jt, DP 25%, tenor 3 tahun → cicilan bulanan 2.250.000 / 2.432.250 /
2.641.423,50 untuk tahun 1/2/3).

---

## Asumsi yang Diambil

Beberapa bagian soal tidak dispesifikasikan secara eksplisit atau berpotensi
ambigu. Berikut asumsi yang diambil, beserta alasannya:

1. **Pola kenaikan interest rate**
   Soal menyebut dua rule terpisah ("naik 0.1% tiap 1 tahun" dan "naik 0.5%
   tiap 2 tahun") yang secara literal ambigu apakah keduanya numpuk. Setelah
   dicocokkan ke sample `Rumus.xlsx` (8.0% → 8.1% → 8.6%), yang cocok adalah
   interpretasi **bergantian** (bukan numpuk): transisi tahun ganjil naik
   0.1%, transisi tahun genap naik 0.5% (menggantikan, bukan menambah, 0.1%
   di tahun itu). Detail ada di Javadoc `InterestRateCalculator`.

2. **Format `file_inputs.txt`**
   Tidak ada contoh format file yang diberikan di soal. Format yang dipakai:
   6 baris teks polos, satu nilai per baris, urutan sama seperti pertanyaan
   di mode interaktif.

3. **Validasi tahun kendaraan**
   Selain rule "kendaraan BARU tidak boleh tahun kurang dari currentYear-1",
   ditambahkan validasi bahwa tahun kendaraan tidak boleh lebih besar dari
   tahun sekarang (mencegah input tahun kendaraan di masa depan).

4. **Validasi jumlah pinjaman & DP**
   Ditambahkan validasi bahwa jumlah pinjaman harus lebih besar dari 0, dan
   DP tidak boleh lebih besar atau sama dengan jumlah pinjaman total (kalau
   DP >= pinjaman, tidak ada yang perlu dicicil).

5. **Mapping field API**
   Response API pakai key `loanTenure`, sementara field internal aplikasi ini
   namanya `tenorYears` — secara makna sama (tenor pinjaman dalam tahun),
   sudah dipetakan di `JsonLoanResponseMapper`.

6. **Instruksi "Tambahin menu save sheet, jadi bisa switch sheet"**
   Instruksi ini ada di bagian "Dynamic Code Test" di awal dokumen soal,
   tapi tidak nyambung dengan konteks soal kredit simulator (tidak ada
   konsep "sheet" dalam aplikasi ini). Instruksi ini **tidak diimplementasikan**
   karena dianggap tidak relevan/kemungkinan template generik dari jenis
   soal lain. Instruksi "Tambahin menu (show)" pada bagian yang sama
   diimplementasikan sebagai menu **"Show Commands"**.

7. **Endpoint "Load Existing Calculation" diganti dari yang diberikan di soal**
   URL asli dari soal (`https://run.mocky.io/v3/9108b1da-beec-409e-ae14-e212003666c`)
   sudah tidak bisa diakses saat development (timeout / tidak ditemukan), dan
   `designer.mocky.io` (tempat membuat mock baru) juga mengembalikan 404 saat
   dicoba — mengindikasikan layanan mocky.io sedang bermasalah secara
   keseluruhan, bukan cuma mock individual yang expired. Sebagai gantinya,
   JSON yang sama persis di-host di GitHub Gist (lebih stabil dan tidak
   memiliki masa expired): `[URL_GIST_DI_SINI]`. Struktur/isi JSON identik
   dengan contoh di soal, hanya sumber hosting-nya yang berbeda. Logic
   `LoanApiClient` dan `JsonLoanResponseMapper` tidak berubah sama sekali -
   keduanya generik untuk endpoint JSON manapun.

8. **Field "Monthly Rata2" di `Rumus.xlsx`**
   Nilai ini tidak berhasil dicocokkan dengan kombinasi perhitungan apa pun
   dari data yang tersedia (bukan rata-rata sederhana dari installmentMonthly
   3 tahun, bukan juga total dibagi total bulan). Karena requirement soal
   hanya meminta output "Jumlah Cicilan Perbulan" (bukan rata-rata), field
   ini tidak diimplementasikan.

---

## Unit Testing

_(TODO — akan ditambahkan menyusul)_

## Build & Deployment (Docker, CI/CD)

_(TODO — akan ditambahkan menyusul)_