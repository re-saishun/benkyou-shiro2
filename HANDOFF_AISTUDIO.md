# Handoff ke AI Studio — Benkyou Shiro!

Status: build BELUM diuji. Lanjutkan pekerjaan di bawah, lalu build.

## Sudah ditulis (jangan ditulis ulang)
- core/.../sqldelight_user_data/migrations/12.sqm (tabel grammar, user_version 13) + insertGrammarDeck diperbaiki di UserData.sq
- core/.../user_data/database/GrammarPracticeRepository.kt + sqldelight/SqlDelightGrammarPracticeRepository.kt
- core/.../srs/GrammarSrsManager.kt (DefaultGrammarSrsManager, pola sama dengan Letter/Vocab)
- core/.../grammar/GrammarCatalogService.kt (cek katalog + unduh paket dari GitHub)
- Preferensi grammarCatalogUrl (PreferencesContract, AppPreferences, test fake)
- Koin: GrammarPracticeRepository (UserDataDatabaseDI), GrammarSrsManager & GrammarCatalogService (SrsDI)
- Contoh paket materi: grammar-packs-sample/ (unggah ke GitHub). Ubah DefaultGrammarCatalogUrl di GrammarCatalogService.kt ke URL raw catalog.json milikmu.

## Bug build yang diketahui (perbaiki dulu)
1. HomeViewModel.kt: `when` pakai PreferencesDefaultHomeTab.Letters/Vocab, padahal enum sekarang { GeneralDashboard, Grammar, Memorize }. Petakan ke tab baru.
2. DefaultHomeTabSettingItem.kt: enum DisplayableDefaultHomeTab masih Letters/Vocab -> ganti Grammar/Memorize (pakai home.grammarDashboardTabLabel / home.memorizeDashboardTabLabel).
3. MainNavigation.kt, MainDestination.LetterPractice.analyticsName: `when` belum punya ScreenLetterPracticeType.ReadingPicker.
4. GeneralDashboardScreenUI.kt (baris ~409-440) dan SubscribeOnGeneralDashboardScreenDataUseCase.kt (~116): `when` atas PracticeType (sealed) belum menangani GrammarPracticeType. Jangan tambah grammar ke enum StudyTarget; tangani dengan cabang `is GrammarPracticeType -> ...`.
5. mediaGenerator/.../ScreenshotTests.kt memakai HomeScreenTab.LettersDashboard (hanya test, sesuaikan).
6. Strings: deleteDeckDialogMessage Grammar menyebut "saved review progress" padahal progres tidak dihapus; ubah teksnya.

## Yang belum dikerjakan
A. HomeScreenTab (HomeScreenData.kt): ubah jadi GeneralDashboard, GrammarDashboard, Memorize, Search. Strings tab sudah ada (home.grammarDashboardTabLabel, home.memorizeDashboardTabLabel). Ikon Memorize: Icons.Outlined.Psychology atau sejenisnya.
B. Layar Memorize: halaman awal 2 kotak besar "Letters" dan "Vocab" (strings memorize.* sudah ada). Klik -> tampilkan LettersDashboardScreen / VocabDashboardScreen yang sudah ada, dengan state internal (Menu/Letters/Vocab) dan back handler kembali ke menu.
C. Layar Grammar (home tab): daftar deck dari GrammarSrsManager.getDecks(), tampilan mirip dashboard Letters/Vocab; tombol + membuka dialog katalog (GrammarCatalogService.getSavedCatalogUrl, fetchCatalog, downloadPack; status Downloaded/UpdateAvailable/NotDownloaded; opsi "Create Empty Deck"); hapus deck via repository.deleteDeck. Pakai strings `grammar.*` yang sudah ada. Daftarkan ViewModel di AppModule.kt (screenModules).
D. Layar latihan Grammar: MainDestination.GrammarPractice (@Serializable, daftarkan di defaultMainDestinations). Tiga mode: Read (tampilkan structure/meaning/nuance/examples lalu nilai Again/Hard/Good/Easy), Quiz (pilihan ganda dari quiz; benar=Good, salah=Again), Test (ketik jawaban; cocok dengan meaning atau structure = Good, lain = Again). Simpan hasil: SrsScheduler.answers + SrsCardRepository.update(GrammarPracticeType.X.toSrsKey(itemKey), card) dan ReviewHistoryRepository.addReview(ReviewHistoryItem(key=itemKey, practiceType=srsPracticeType.value, ...)). Lihat PracticeQueue.kt (~baris 234) sebagai contoh.
E. Dashboard Home (GeneralDashboardScreenUI.kt):
   - Study Target dengan label "TODAY" di sisinya.
   - Baris 1: New (jumlah item baru hari ini, pakai ReviewHistoryRepository.getNewReviewItemsCount) - Time spent - Review (data Today dari StatsScreen, pindahkan dari StatsSectionUI; hapus bagian Today di accordion).
   - Baris 2: Grammar Read - Quiz - Test (label kecil "Grammar" di atas, seperti kartu Letters/Vocab), data dari GrammarSrsManager.getDecks().dailyProgress/progressMap; klik mulai latihan.
   - Baris 3 dst: target Letters/Vocab seperti sekarang.
   - Di bawahnya accordion "Statistic" (kalender s/d total), terakhir "Text Analysis".
   - Hapus item Settings dari daftar di Home dan hapus "reviews" dari Header streak bila duplikat.
   - Tambahkan GrammarSrsManager.dataChangeFlow ke mergeSharedFlows di use case.
F. HomeScreenUI.kt: ganti ikon donasi (Handshake) di header kanan atas (portrait dan landscape) dengan ikon Settings -> navigate MainDestination.Settings. Tambahkan item Donate (MainDestination.Sponsor) di SettingsScreenUI.kt.
G. Optimasi Search (home/screen/search/SearchViewModel.kt): periksa kenapa loading awal lambat (database kamus dimuat saat pertama dibuka); muat di background saat aplikasi mulai / cache hasil, debounce input, jalankan query di Dispatchers.IO.
H. Ikon launcher: res/drawable/ic_launcher_background.xml jadi putih (solid #FFFFFF), ic_launcher_foreground.xml perkecil logo (~48dp), dan buat ulang PNG mipmap-* (bulat putih + logo kecil) dari public/assets/aistudio/Logo_BenkyouShiro__g31_*.png. Nama aplikasi sudah "勉強 Shiro!".
I. Setelah selesai: periksa semua `when` atas ScreenLetterPracticeType, GrammarPracticeType, HomeScreenTab, PreferencesDefaultHomeTab agar exhaustive.

## Format GitHub (sudah diimplementasikan di GrammarCatalogService)
catalog.json: {"packs":[{"id","title","level","description","version","url","pointCount"}]} (url relatif terhadap catalog.json atau absolut).
Paket: {"id","title","level","description","version","points":[{"key","title","structure","meaning","nuance","examples":[{"japanese","reading","translation"}],"quiz":{"question","options":[...],"correctIndex","explanation"}}]}
Contoh: grammar-packs-sample/

---
## UPDATE (sesi Claude) — status terbaru
Build BELUM diuji (tidak ada Gradle/jaringan di sandbox).

### Selesai
- Bug 1-6 (HomeViewModel, DefaultHomeTabSettingItem, MainNavigation ReadingPicker, use case dashboard + GrammarPracticeType, ScreenshotTests, teks hapus deck)
- A: HomeScreenTab = GeneralDashboard, GrammarDashboard, Memorize, Search
- B: home/screen/memorize/MemorizeScreen.kt
- C: home/screen/grammar_dashboard/* (+ grammarDashboardScreenModule di AppModule)
- E: GeneralDashboardScreenUI (TODAY, baris New/Time/Review, baris Grammar, accordion Statistic, tanpa Settings)
- F: ikon Settings di header, item Donate di SettingsScreenUI
- LocalHomeNavigationState (HomeNavigation.kt) untuk pindah tab dari dashboard

### BELUM (build akan GAGAL sampai D ditulis)
- D: practice_grammar/GrammarPracticeScreen.kt (+ ViewModel) dan `grammarPracticeScreenModule` (sudah di-import di AppModule.kt dan dipakai MainDestination.GrammarPractice). Config sudah ada: practice_grammar/data/GrammarPracticeScreenConfiguration.kt
- G: optimasi SearchViewModel
- H: ikon launcher
- I: cek exhaustive `when` (ScreenLetterPracticeType, GrammarPracticeType, HomeScreenTab, PreferencesDefaultHomeTab)

---
## UPDATE 2 (sesi Claude, lanjutan) — status terbaru
Build tetap BELUM diuji (tidak ada Gradle/jaringan di sandbox). Semua perubahan di bawah hasil review statis saja.

### Selesai
- D: practice_grammar/ — GrammarPracticeScreenContract.kt, GrammarPracticeViewModel.kt, GrammarPracticeScreenUI.kt, GrammarPracticeScreen.kt, GrammarPracticeScreenModule.kt (`grammarPracticeScreenModule` sudah terdaftar di AppModule).
  - Read: judul -> "Show answer" -> structure/meaning/nuance/examples -> Again/Hard/Good/Easy (FlashcardPracticeAnswerButtonsRow).
  - Quiz: pilihan ganda; benar = Good, salah = Again; tombol Continue. Poin tanpa `quiz` dilewati di mode Quiz.
  - Test: ketik jawaban; cocok (abaikan huruf besar/kecil, spasi, tanda baca; alternatif dipisah `; / , 、`) dengan meaning atau structure = Good, lain = Again.
  - Item dengan interval < 1 hari diulang di sesi yang sama (posisi ke-3). Hasil disimpan via srsCardRepository.update + reviewHistoryRepository.addReview.
  - Ada layar ringkasan di akhir sesi (PracticeSummaryContainer) dan dialog konfirmasi saat keluar di tengah sesi.
- G: Search — database kamus dipanaskan saat aplikasi start (KanjiApplication memanggil AppDataDatabaseProvider.provideAsync()), prewarm tabel words di SearchViewModel, debounce 200 -> 150 ms, SearchScreenProcessInputUseCase menjalankan cek karakter + query kata secara paralel.
- H: ikon launcher — background putih solid, foreground 35x48dp (drawable/shiro_logo_foreground.png, background logo sudah transparan), mipmap-* dibuat ulang (bulat putih + logo kecil).
- I: semua `when` atas ScreenLetterPracticeType, GrammarPracticeType/PracticeType, HomeScreenTab, PreferencesDefaultHomeTab sudah diperiksa, exhaustive.

### Catatan / risiko
- Titik kemungkinan error kompilasi pertama: GrammarPracticeScreenUI.kt (pemakaian komponen practice_common) dan import di GrammarPracticeViewModel.kt. Jalankan `./gradlew :core:compileDebugKotlinAndroid` dulu.
- Poin grammar tanpa `quiz` tetap dihitung "new/due" untuk Quiz di dashboard walau tidak muncul saat latihan. Sebaiknya paket materi selalu menyertakan quiz, atau filter di GrammarDashboardViewModel.
- Opsi kuis tidak diacak (urutan sesuai paket).
- Mode Test memakai pencocokan persis (bukan fuzzy); jawaban panjang berbahasa Indonesia sulit cocok persis.
