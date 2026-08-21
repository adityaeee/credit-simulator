# Credit Simulator

Console application buat ngitung simulasi cicilan bulanan kredit kendaraan
(Motor/Mobil), pake skema bunga yang naik tiap tahun dan pokok pinjaman yang
menurun (declining balance method).

Dibuat buat technical test Backend Engineer.

---

## Bahasa & Tools

- **Java 17** (plain Java, nggak pake framework eksternal)
- Library eksternal yang dipakai: **nggak ada**, kecuali `java.net.http.HttpClient`
  bawaan JDK 11+ buat fitur "Load Existing Calculation"
- Parsing JSON dilakukan manual (regex)
- Build tool: **Maven**

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

Bakal muncul menu:

```
=== Credit Simulator ===
1. New Calculation
2. Load Existing Calculation
3. Show Commands
4. Exit
Pilih menu:
```

- **New Calculation** → nanya 6 input satu-satu (jenis kendaraan, kondisi,
  tahun, jumlah pinjaman, tenor, DP), terus nampilin breakdown cicilan per tahun.
- **Load Existing Calculation** → ambil data pinjaman dari API terus otomatis
  ngitung + nampilin hasilnya. Cuma di sini, endpoint aslinya (`mocky.io`)
  ternyata udah nggak aktif lagi, jadi saya alihin ke GitHub Gist.
- **Show Commands** → nampilin daftar menu yang tersedia.
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

> Format ini saya tentuin sendiri, soalnya soal nggak kasih contoh isi
> `file_inputs.txt`.

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
yang sama (`RawLoanInput`), terus diproses lewat pipeline yang sama juga:

```
RawLoanInput -> LoanRequestFactory -> LoanValidator -> LoanCalculator -> print
```

---

## Design Pattern

- **Factory Pattern** — `VehicleFactory` dan `LoanRequestFactory` jadi satu
  pintu masuk buat bikin objek yang udah tervalidasi dari data mentah
- **Strategy Pattern** — `InputReader` sebagai interface, ada dua
  implementasi (`ConsoleInputReader`, `FileInputReader`) yang bisa saling
  gantiin tanpa `Main.java` perlu tau detail sumbernya
- **Manual Dependency Injection** — semua class nerima dependency-nya lewat
  constructor (bukan `new` sendiri di dalam), dirakit di satu tempat aja di
  `Main.java`. Ini gantiin peran DI container kayak Spring, tapi tetap nggak
  pake framework eksternal.
- **Immutable objects** — `Vehicle`, `LoanRequest`, `InstallmentResult` semua
  nggak punya setter; sekali dibuat lewat factory, datanya pasti valid dan
  nggak bisa berubah lagi di tengah jalan

---

## Formula Perhitungan

Pake metode **declining balance**: pokok pinjaman yang dipakai buat hitung
bunga tiap tahun itu sisa dari tahun sebelumnya, bukan pokok awal terus-terusan

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

Formula ini udah saya cocokin dan pas 100% sama sample di `Rumus.xlsx` (Mobil
Baru, pinjaman 100jt, DP 25%, tenor 3 tahun → cicilan bulanan 2.250.000 /
2.432.250 / 2.641.423,50 buat tahun 1/2/3).

---

## Asumsi yang Diambil

Ada beberapa bagian soal yang menurut saya kurang jelas atau agak ambigu.
Berikut asumsi-asumsi yang saya ambil selama ngerjain, plus alasannya:

1. **Pola kenaikan interest rate**
   Di soal ada dua rule: "naik 0.1% tiap 1 tahun" dan "naik 0.5% tiap 2
   tahun". Awalnya saya bingung apakah dua-duanya numpuk atau nggak. Setelah
   saya coba cocokin ke sample di `Rumus.xlsx` (8.0% → 8.1% → 8.6%), ternyata
   yang cocok itu kalau kenaikannya **gantian**, bukan numpuk: tahun ganjil
   naik 0.1%, tahun genap naik 0.5% (dan 0.5% ini gantiin yang 0.1%, bukan
   ditambah). Detailnya saya jelasin lagi di Javadoc `InterestRateCalculator`.

2. **Format `file_inputs.txt`**
   Soal nggak kasih contoh isi filenya kayak apa, jadi saya tentuin sendiri:
   6 baris, satu nilai per baris, urutannya sama persis kayak pertanyaan di
   mode interaktif.

3. **Validasi tahun kendaraan**
   Selain rule "kendaraan BARU nggak boleh tahun kurang dari currentYear-1",
   saya tambahin validasi tahun kendaraan juga nggak boleh lebih dari tahun
   sekarang (masa iya ada motor keluaran tahun depan).

4. **Validasi jumlah pinjaman & DP**
   Saya tambahin validasi jumlah pinjaman harus lebih dari 0, dan DP nggak
   boleh lebih besar atau sama dengan jumlah pinjaman total — soalnya kalau
   DP-nya segitu, nggak ada yang perlu dicicil lagi, jadi nggak masuk akal.

5. **Mapping field API**
   Response dari API pakai key `loanTenure`, sedangkan di aplikasi saya
   namanya `tenorYears` — beda nama doang, maksudnya sama (tenor pinjaman
   dalam tahun). Udah saya mapping di `JsonLoanResponseMapper`.

6. **Soal instruksi "Tambahin menu save sheet, jadi bisa switch sheet"**
   Ini ada di bagian awal soal ("Dynamic Code Test"), tapi menurut saya nggak
   nyambung sama konteks kredit simulator ini (nggak ada konsep "sheet" di
   aplikasi ini). Jadi saya skip, kemungkinan ini instruksi generik dari
   template soal lain. Instruksi satunya lagi di bagian yang sama, "Tambahin
   menu (show)", tetap saya implementasikan sebagai menu **"Show Commands"**.

7. **Endpoint "Load Existing Calculation" saya ganti dari yang dikasih di soal**
   URL asli dari soal (`https://run.mocky.io/v3/9108b1da-beec-409e-ae14-e212003666c`)
   udah nggak bisa diakses pas saya development (timeout terus / not found).
   Saya coba bikin mock baru di `designer.mocky.io` juga ternyata 404 —
   kelihatannya layanan mocky.io ini emang lagi bermasalah, bukan cuma mock
   saya doang yang expired. Jadi saya pindahin JSON-nya ke GitHub Gist
   (lebih stabil, nggak ada masa kadaluarsa):
   ```
   https://gist.githubusercontent.com/adityaeee/35126a8035f369e5685ff9eb327b8ef3/raw/62fef1f77a9396a0081df823f9f85591db2b29ed/loan-data.json
   ```
   Isi JSON-nya persis sama kayak contoh di soal, cuma beda tempat hosting
   doang. Logic di `LoanApiClient` dan `JsonLoanResponseMapper` nggak saya
   ubah sama sekali — keduanya emang generik, bisa dipakai buat endpoint
   JSON manapun.

8. **Field "Monthly Rata2" di `Rumus.xlsx`**
   Saya udah coba cocokin angka ini ke beberapa kombinasi perhitungan (rata-
   rata dari 3 installmentMonthly, total dibagi total bulan, dll), tapi
   nggak ada yang pas. Karena requirement cuma minta output "Jumlah Cicilan
   Perbulan" (bukan rata-rata), field ini akhirnya nggak saya implementasikan.

---

## Unit Testing

Karena waktu pengerjaan mepet (48 jam) dan unit test sifatnya "huge plus"
(bukan mandatory) di requirement, saya belum sempat bikin unit test buat
submission ini.

Struktur kode udah saya desain biar gampang di-unit-test kalau ada waktu
lebih lanjut — tiap class (`InterestRateCalculator`, `LoanCalculator`,
`LoanValidator`, dll) nerima dependency-nya lewat constructor, jadi bisa
di-test terisolasi tanpa perlu mock framework tambahan.

---

## Build & Deployment

### Docker

Build image:
```bash
docker build -t credit-simulator .
```

Jalankan mode interaktif (wajib pakai `-it` biar bisa input keyboard):
```bash
docker run -it credit-simulator
```

Jalankan mode file (pakai sample `file_inputs.txt` yang udah di-bundle ke image):
```bash
docker run credit-simulator file_inputs.txt
```

Jalankan dengan file input custom dari luar container:
```bash
docker run -v $(pwd)/my_file.txt:/app/my_file.txt credit-simulator my_file.txt
```

Image-nya pake **multi-stage build**: tahap pertama compile pakai Maven+JDK
17, tahap kedua (image final) cuma isinya JRE + jar hasil compile, jadi
ukuran image-nya jauh lebih kecil.

### CI/CD

Pake **GitHub Actions** (`.github/workflows/ci.yml`), otomatis jalan tiap
`git push` ke branch `master`. Alur pipeline-nya:

1. Checkout kode
2. Setup JDK 17
3. `mvn clean package` (validasi build dulu sebelum masuk tahap Docker)
4. Build Docker image (pakai `Dockerfile` multi-stage di atas)
5. Push image ke Docker Hub: `adityae/credit-simulator:latest`

Image publiknya bisa diambil dengan:
```bash
docker pull adityae/credit-simulator:latest
```

---

## Creator

Dibuat oleh **Rifky Aditya** untuk keperluan technical test Backend Engineer.