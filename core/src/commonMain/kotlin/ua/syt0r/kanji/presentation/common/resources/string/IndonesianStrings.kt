package ua.syt0r.kanji.presentation.common.resources.string

import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import ua.syt0r.kanji.presentation.common.CommonDateTimeFormat
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme
import ua.syt0r.kanji.presentation.common.withClickableUrl
import ua.syt0r.kanji.presentation.screen.main.screen.feedback.FeedbackScreen
import kotlin.time.Duration

object IndonesianStrings : Strings {

    override val appName: String = "勉強 Shiro!"

    override val hiragana: String = "Hiragana"
    override val katakana: String = "Katakana"

    override val kunyomi: String = "Kun"
    override val onyomi: String = "On"

    override val loading: String = "Memuat"

    override val letterPracticeTypeWriting: String = "Menulis"
    override val letterPracticeTypeReading: String = "Kartu Hafalan"
    override val letterPracticeTypeReadingPicker: String = "Pilihan Bacaan"
    override val vocabPracticeTypeFlashcard: String = "Kartu Hafalan"
    override val vocabPracticeTypeReadingPicker: String = "Pilihan Bacaan"
    override val vocabPracticeTypeWriting: String = "Menulis"

    override val reviewStateDone: String = "Selesai"
    override val reviewStateDue: String = "Perlu Diulang"
    override val reviewStateNew: String = "Baru"

    override val home: HomeStrings = IndonesianHomeStrings
    override val memorize: MemorizeStrings = IndonesianMemorizeStrings
    override val grammar: GrammarStrings = IndonesianGrammarStrings
    override val commonDashboard = IndonesianCommonDashboardStrings
    override val dailyLimit: DailyLimitStrings = IndonesianDailyLimitStrings
    override val tutorialDialog: TutorialDialogStrings = IndonesianTutorialDialogStrings

    override val stats: StatsStrings = IndonesianStatsStrings
    override val search: SearchStrings = IndonesianSearchStrings
    override val alternativeDialog: AlternativeDialogStrings = IndonesianAlternativeDialogStrings

    override val settings: SettingsStrings = IndonesianSettingsStrings
    override val reminderDialog: ReminderDialogStrings = IndonesianReminderDialogStrings
    override val about: AboutStrings = IndonesianAboutStrings
    override val backup: BackupStrings = IndonesianBackupStrings
    override val feedback: FeedbackStrings = IndonesianFeedbackStrings
    override val sponsor: SponsorStrings = IndonesianSponsorStrings

    override val account: AccountScreenStrings = IndonesianAccountScreenStrings
    override val sync: SyncScreenStrings = IndonesianSyncScreenStrings
    override val syncDialog: SyncDialogStrings = IndonesianSyncDialogStrings
    override val syncSnackbar: SyncSnackbarStrings = IndonesianSyncSnackbarStrings

    override val deckPicker: DeckPickerStrings = IndonesianDeckPickerStrings
    override val deckEdit: DeckEditStrings = IndonesianDeckEditStrings
    override val deckDetails: DeckDetailsStrings = IndonesianDeckDetailsStrings
    override val commonPractice: CommonPracticeStrings = IndonesianCommonPracticeStrings
    override val letterPractice: LetterPracticeStrings = IndonesianLetterPracticeStrings
    override val vocabPractice: VocabPracticeStrings = IndonesianVocabPracticeStrings
    override val info: InfoScreenStrings = IndonesianInfoScreenStrings

    override val urlPickerMessage: String = "Buka Dengan"
    override val urlPickerErrorMessage: String = "Peramban web tidak ditemukan"

    override val reminderNotification: ReminderNotificationStrings =
        IndonesianReminderNotificationStrings

}

object IndonesianHomeStrings : HomeStrings {
    override val screenTitle: String = "勉強 Shiro!"
    override val generalDashboardTabLabel: String = "Beranda"
    override val grammarDashboardTabLabel: String = "Tata Bahasa"
    override val memorizeDashboardTabLabel: String = "Hafalan"
    override val lettersDashboardTabLabel: String = "Karakter"
    override val vocabDashboardTabLabel: String = "Kosakata"
    override val statsTabLabel: String = "Statistik"
    override val searchTabLabel: String = "Pencarian"
    override val settingsTabLabel: String = "Pengaturan"
}

object IndonesianMemorizeStrings : MemorizeStrings {
    override val screenTitle: String = "Hafalan"
    override val lettersTitle: String = "Karakter"
    override val lettersSubtitle: String = "Kana & Kanji: menulis, kartu hafalan, pilihan bacaan"
    override val vocabTitle: String = "Kosakata"
    override val vocabSubtitle: String = "Kosakata: kartu hafalan, menulis, pilihan bacaan"
    override val decksCountLabel: (Int) -> String = { "$it dek" }
    override val dueCountLabel: (Int) -> String = { "Perlu Diulang: $it" }
    override val newCountLabel: (Int) -> String = { "Baru: $it" }
}

object IndonesianGrammarStrings : GrammarStrings {
    override val practiceTypeRead: String = "Baca"
    override val practiceTypeQuiz: String = "Kuis"
    override val practiceTypeTest: String = "Tes"
    override val emptyTitle: String = "Belum Ada Paket Tata Bahasa"
    override val emptyMessage: String = "Tekan tombol + untuk memeriksa dan mengunduh materi tata bahasa secara online dari GitHub ke perangkat Anda."
    override val addPackButton: String = "Unduh Materi Tata Bahasa"
    override val catalogDialogTitle: String = "Paket Tata Bahasa (GitHub)"
    override val catalogUrlLabel: String = "URL JSON GitHub"
    override val catalogCheckButton: String = "Periksa Online"
    override val catalogCreateLocalButton: String = "Buat Dek Kosong"
    override val catalogLocalTitleHint: String = "Judul dek"
    override val catalogLoading: String = "Memeriksa GitHub..."
    override val catalogRetry: String = "Coba Lagi"
    override val catalogStatusDownloaded: String = "Terunduh"
    override val catalogStatusUpdateAvailable: String = "Pembaruan Tersedia"
    override val catalogStatusNotDownloaded: String = "Belum Diunduh"
    override val catalogDownloadButton: String = "Unduh"
    override val catalogUpdateButton: String = "Perbarui"
    override val catalogPointCount: (Int) -> String = { "$it poin" }
    override val deleteDeckDialogTitle: String = "Hapus Paket Tata Bahasa"
    override val deleteDeckDialogMessage: (String) -> String = { "Hapus \"$it\" dari perangkat ini? Progres belajar yang tersimpan tidak ikut terhapus." }
    override val deleteDeckButton: String = "Hapus"
    override val practiceMeaningLabel: String = "Arti"
    override val practiceStructureLabel: String = "Pola / Struktur"
    override val practiceNuanceLabel: String = "Catatan & Nuansa"
    override val practiceExamplesLabel: String = "Contoh Kalimat"
    override val practiceShowAnswerButton: String = "Tampilkan Jawaban"
    override val practiceNextButton: String = "Lanjutkan"
    override val practiceTestInputHint: String = "Ketik arti atau pola..."
    override val practiceTestCheckButton: String = "Periksa"
    override val practiceCorrectAnswerLabel: String = "Benar!"
    override val practiceIncorrectAnswerLabel: String = "Jawaban yang tepat"
    override val practiceEmptyQueue: String = "Tidak ada materi untuk dilatih saat ini"
    override val closeButton: String = "Tutup"
}

object IndonesianCommonDashboardStrings : CommonDashboardStrings {

    override val emptyScreenMessage: (inlineIconId: String) -> AnnotatedString = { inlineIconId ->
        buildAnnotatedString {
            append("Buat dek dengan menekan tombol ")
            appendInlineContent(inlineIconId)
            append(". Dek digunakan untuk memantau perkembangan belajar Anda")
        }
    }

    override val mergeButton: String = "Gabungkan"
    override val mergeCancelButton: String = "Batal"
    override val mergeAcceptButton: String = "Gabungkan"
    override val mergeTitle: String = "Gabungkan beberapa dek menjadi satu"
    override val mergeTitleHint: String = "Masukkan judul di sini"
    override val mergeSelectedCount: (Int) -> String = { "$it dipilih" }
    override val mergeClearSelectionButton: String = "Hapus"

    override val mergeDialogTitle: String = "Konfirmasi Penggabungan"
    override val mergeDialogMessage: (String, List<String>) -> String = { newTitle, mergedTitles ->
        "${mergedTitles.size} dek berikut akan digabungkan ke dalam dek baru \"$newTitle\": ${mergedTitles.joinToString()}"
    }
    override val mergeDialogCancelButton: String = "Batal"
    override val mergeDialogAcceptButton: String = "Gabungkan"

    override val sortButton: String = "Urutkan"
    override val sortCancelButton: String = "Batal"
    override val sortAcceptButton: String = "Terapkan"
    override val sortTitle: String = "Ubah urutan dek"
    override val sortByTimeTitle: String = "Urutkan berdasarkan waktu ulasan terakhir"

    override val itemTimeMessage: (Duration?) -> String = {
        "Ulasan terakhir: " + when {
            it == null -> "Belum pernah"
            it.inWholeDays == 1L -> "1 hari yang lalu"
            it.inWholeDays > 0 -> "${it.inWholeDays} hari yang lalu"
            else -> "< 1 hari yang lalu"
        }
    }
    override val itemTotal: String = "Total"
    override val itemDone: String = "Selesai"
    override val itemReview: String = "Perlu Diulang"
    override val itemNew: String = "Baru"
    override val dailyPracticeTitle: String = "Latihan harian"
    override val dailyPracticeNew: (Int) -> String = { "Baru ($it)" }
    override val dailyPracticeDue: (Int) -> String = { "Perlu Diulang ($it)" }
    override val itemGraphProgressTitle: String = "Penyelesaian"

    override val selectedPracticeTypeTemplate: (practiceType: String) -> String = {
        "Jenis Latihan: $it"
    }
}

object IndonesianDailyLimitStrings : DailyLimitStrings {
    override val enableSwitchTitle: String = "Batas Harian"
    override val enableSwitchDescription: String =
        "Batasi jumlah ulasan harian yang disarankan oleh aplikasi"
    override val lettersSectionTitle: String = "Karakter"
    override val vocabSectionTitle: String = "Kosakata"
    override val grammarSectionTitle: String = "Tata Bahasa"
    override val combinedLimitSwitchTitle: String = "Batas Gabungan"
    override val combinedLimitSwitchDescription: String = "Gunakan batas yang sama untuk semua jenis latihan"
    override val newLabel: String = "Baru"
    override val dueLabel: String = "Perlu Diulang"
    override val noteMessage: String =
        "Catatan: Latihan menulis dan membaca dihitung secara terpisah terhadap batas"
    override val button: String = "Simpan"
    override val changesSavedMessage: String = "Selesai"
}

object IndonesianTutorialDialogStrings : TutorialDialogStrings {
    override val title: String = "Panduan"

    override val page1: String = """
        • Aplikasi ini menggunakan Spaced Repetition System (SRS) - metode belajar yang sangat efektif untuk mengoptimalkan waktu pengulangan materi berdasarkan seberapa baik daya ingat Anda
        • Ketika Anda mengulas materi, SRS akan menjadwalkan ulasan berikutnya sesuai kemampuan mengingat Anda
        • Jika Anda mengingat materi dengan mudah, jeda ulasan akan diperpanjang. Jika kesulitan, jeda ulasan akan diperpendek
    """.trimIndent()

    override val page2Top: String = """
        • Untuk memperkirakan daya ingat, aplikasi menyediakan pilihan penilaian setelah setiap ulasan
    """.trimIndent()

    override val page2Bottom: String = """
        • Pilih nilai yang paling menggambarkan kemampuan Anda mengingat materi yang diulas
        • Menilai jawaban sendiri memungkinkan aplikasi menyesuaikan kecepatan belajar dengan daya ingat Anda
    """.trimIndent()

    override val page3Top: String = """
        • Setiap hari, status materi yang diulas akan diperbarui
        • Aplikasi akan menyajikan materi baru serta materi yang telah jatuh tempo untuk diulang
    """.trimIndent()

    override val page3Bottom: String = """
        • Anda dapat mengatur batas harian agar beban belajar tetap nyaman dan teratur
    """.trimIndent()

    override val page4Top: String = """
        • Untuk memulai, buat dek baru
        • Dek digunakan untuk mengorganisasi materi yang ingin Anda kuasai. Tersedia dek karakter dan dek kosakata
    """.trimIndent()

    override val page4Bottom: String = """
        • Anda dapat membuat dek sendiri atau memilih dari dek siap pakai yang telah disediakan
    """.trimIndent()

    override val page5: String = """
        • Setelah dek dibuat, Anda bisa langsung mulai belajar
        • Tersedia beberapa mode latihan, silakan coba semuanya
        • Konsistensi adalah kunci kemajuan. Jangan ragu menurunkan batas harian agar tidak merasa jenuh
        • Semangat belajar bahasa Jepang! \(^_^)/
    """.trimIndent()
}

private val indonesianMonths = listOf(
    "Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agt",
    "Sep", "Okt", "Nov", "Des"
)

private fun formatDuration(duration: Duration): String = when {
    duration.inWholeHours > 0 -> "${duration.inWholeHours}j ${duration.inWholeMinutes % 60}m"
    duration.inWholeMinutes > 0 -> "${duration.inWholeMinutes}m ${duration.inWholeSeconds % 60}d"
    else -> "${duration.inWholeSeconds}d"
}

object IndonesianStatsStrings : StatsStrings {
    override val todayTitle: String = "Hari Ini"
    override val monthTitle: String = "Bulan Ini"
    override val monthLabel: (day: LocalDate) -> String = {
        it.run { "${indonesianMonths[monthNumber - 1]}, $year" }
    }
    override val yearTitle: String = "Tahun Ini"
    override val yearDaysPracticedLabel = { practicedDays: Int, daysInYear: Int ->
        "Hari berlatih: $practicedDays/$daysInYear"
    }
    override val totalTitle: String = "Total"
    override val newItemsTitle: String = "Baru"
    override val timeSpentTitle: String = "Waktu belajar"
    override val reviewsCountTitle: String = "Ulasan"
    override val formattedDuration: (Duration) -> String = { formatDuration(it) }
    override val uniqueLettersReviewed: String = "Karakter unik yang diulas"
    override val uniqueWordsReviewed: String = "Kata unik yang diulas"
}

object IndonesianSearchStrings : SearchStrings {
    override val inputHint: String = "Cari karakter atau kata"
    override val charactersTitle: (count: Int) -> String = { "Karakter ($it)" }
    override val wordsTitle: (count: Int) -> String = { "Kata ($it)" }
    override val radicalsSheetTitle: String = "Cari berdasarkan radikal"
    override val radicalsFoundCharacters: String = "Karakter ditemukan"
    override val radicalsEmptyFoundCharacters: String = "Tidak ada yang ditemukan"
    override val radicalSheetRadicalsSectionTitle: String = "Radikal"
}

object IndonesianAlternativeDialogStrings : AlternativeDialogStrings {
    override val title: String = "Ungkapan alternatif"
    override val readingsTitle: String = "Bacaan"
    override val meaningsTitle: String = "Arti"
    override val reportButton: String = "Laporkan"
    override val closeButton: String = "Tutup"
}

object IndonesianSettingsStrings : SettingsStrings {
    override val analyticsTitle: String = "Analitik"
    override val analyticsMessage: String = "Izinkan pengiriman data anonim penggunaan aplikasi"
    override val themeTitle: String = "Tema"
    override val themeSystem: String = "Sistem"
    override val themeLight: String = "Terang"
    override val themeDark: String = "Gelap"
    override val themeAmoled: String = "AMOLED"
    override val reminderTitle: String = "Pengingat Latihan"
    override val reminderEnabled: String = "Aktif"
    override val reminderDisabled: String = "Nonaktif"
    override val defaultTab: String = "Tab Default"
    override val contentLanguageTitle: String = "Bahasa Konten"
    override val contentLanguageEnglish: String = "Bahasa Inggris"
    override val contentLanguageIndonesian: String = "Bahasa Indonesia"
    override val feedbackTitle: String = "Masukan & Saran"
    override val account: String = "Akun"
    override val sync: String = "Sinkronisasi (Pratinjau)"
    override val backupTitle: String = "Cadangkan & Pulihkan"
    override val donateTitle: String = "Donasi"
    override val aboutTitle: String = "Tentang"
    override val pickerDialogCancel: String = "Batal"
    override val pickerDialogApply: String = "Terapkan"
}

object IndonesianReminderDialogStrings : ReminderDialogStrings {
    override val title: String = "Pengingat Latihan"
    override val noPermissionLabel: String = "Izin notifikasi belum diberikan"
    override val noPermissionButton: String = "Berikan Izin"
    override val enabledLabel: String = "Aktif"
    override val timeLabel: String = "Waktu"
    override val cancelButton: String = "Tutup"
    override val applyButton: String = "Terapkan"
}

object IndonesianAboutStrings : AboutStrings {
    override val title: String = "Tentang"
    override val version: (versionName: String) -> String = { "Versi: $it" }
    override val versionChangesTitle: String = "Catatan Rilis"
    override val versionChangesDescription: String = "Riwayat pembaruan aplikasi"
    override val versionChangesButton: String = "Tutup"
    override val githubTitle: String = "GitHub"
    override val githubDescription: String = "Kode sumber, laporan bug, diskusi"
    override val creditsTitle: String = "Kredit & Sumber Data"
    override val creditsDescription: String = "Pustaka dan sumber data yang digunakan"
}

object IndonesianBackupStrings : BackupStrings {
    override val title: String = "Cadangkan & Pulihkan"
    override val backupButton: String = "Buat cadangan"
    override val restoreButton: String = "Pulihkan dari cadangan"
    override val unknownError: String = "Terjadi kesalahan yang tidak diketahui"
    override val restoreVersionMessage: (Long, Long) -> String = { backupVersion, currentVersion ->
        "Versi basis data: $backupVersion (Saat ini: $currentVersion)"
    }
    override val restoreTimeMessage: (LocalDateTime) -> String = {
        "Waktu pembuatan: ${it.format(CommonDateTimeFormat)}"
    }
    override val restoreNote: String =
        "Perhatian! Semua perkembangan saat ini akan digantikan oleh data dari cadangan yang dipilih"
    override val restoreApplyButton: String = "Pulihkan"
    override val completeMessage: String = "Selesai"
}

object IndonesianFeedbackStrings : FeedbackStrings {
    override val title: String = "Masukan & Saran"
    override val topicTitle: String = "Topik"
    override val topicGeneral: String = "Umum"
    override val topicExpression: (id: Long, screen: FeedbackScreen) -> String = { id, screen ->
        val screenName: String = when (screen) {
            FeedbackScreen.WritingPractice -> "Latihan menulis"
            FeedbackScreen.ReadingPractice -> "Latihan membaca"
            FeedbackScreen.Search -> "Pencarian"
            FeedbackScreen.CharacterInfo -> "Info karakter"
            FeedbackScreen.VocabPractice -> "Latihan kosakata"
        }
        "$screenName, ungkapan $id"
    }
    override val messageLabel: String = "Tuliskan masukan Anda di sini"
    override val button: String = "Kirim"
    override val successMessage: String = "Masukan berhasil dikirim"
    override val errorMessage: (String?) -> String = { "Kesalahan: $it" }
}

object IndonesianSponsorStrings : SponsorStrings {
    override val message: String = """
        Pengembangan 勉強 Shiro! dimulai pada tahun 2021 oleh satu pengembang independen dan tetap gratis bagi semua yang ingin belajar bahasa Jepang.
        
        Jika aplikasi ini bermanfaat bagi Anda, dukung proyek ini secara finansial. Setiap kontribusi sangat berarti.
        
        Dukungan finansial membantu pengembangan fitur baru, penambahan rekaman suara, dan lokalisasi terjemahan yang lebih lengkap.
    """.trimIndent()
}

object IndonesianDeckPickerStrings : DeckPickerStrings {

    override val title: String = "Pilih Dek"

    override val customDeckButton: String = "Buat Dek Kosong"
    override val kanaTitle: String = "Kana"

    override val kanaDescription = { urlColor: Color ->
        buildAnnotatedString {
            append(
                "Karakter kana Jepang adalah kumpulan karakter suku kata yang digunakan dalam sistem penulisan bahasa Jepang. Terdapat dua jenis utama kana:\n" +
                        " • Hiragana - digunakan untuk kata asli bahasa Jepang dan unsur tata bahasa\n" +
                        " • Katakana - sering digunakan untuk kata serapan asing, nama, dan istilah teknis\n" +
                        "Karakter kana mewakili satuan bunyi, menjadikannya bagian mendasar dalam membaca dan menulis bahasa Jepang. "
            )
            withClickableUrl(
                url = "https://id.wikipedia.org/wiki/Kana",
                color = urlColor
            ) {
                append("Info selengkapnya.")
            }
        }
    }
    override val hiragana: String = "Hiragana"
    override val katakana: String = "Katakana"

    override val jltpTitle: String = "JLPT"
    override val jlptDescription: StringResolveScope<AnnotatedString> = {
        buildAnnotatedString {
            append("Japanese-Language Proficiency Test (JLPT) adalah ujian kemampuan bahasa Jepang terstandarisasi untuk penutur non-asli, mencakup pengetahuan bahasa, kemampuan membaca, dan menyimak. ")
            withClickableUrl(
                url = "https://id.wikipedia.org/wiki/Ujian_Kemampuan_Bahasa_Jepang",
                color = MaterialTheme.extraColorScheme.link
            ) {
                append("Info selengkapnya.")
            }
        }
    }
    override val jlptItem: (level: Int) -> String = { "JLPT・N$it" }

    override val gradeTitle: String = "Tingkat Sekolah"
    override val gradeDescription = { urlColor: Color ->
        buildAnnotatedString {
            withClickableUrl("https://en.wikipedia.org/wiki/J%C5%8Dy%C5%8D_kanji", urlColor) {
                append("Jōyō kanji")
            }
            append(" adalah daftar 2.136 karakter yang paling sering digunakan dan ditetapkan secara resmi oleh Kementerian Pendidikan Jepang. ")
            append("Karakter-karakter ini diajarkan di sekolah-sekolah di Jepang:\n")
            append(" • 1.026 kanji diajarkan di sekolah dasar (Kelas 1-6) (disebut ")
            withClickableUrl("https://en.wikipedia.org/wiki/Ky%C5%8Diku_kanji", urlColor) {
                append("kyōiku kanji")
            }
            append(")\n")
            append(" • 1.110 kanji tambahan diajarkan di sekolah menengah pertama dan atas (Kelas 7-12)")
        }
    }
    override val gradeItemNumbered: (Int) -> String = { "Kelas $it (SD)" }
    override val gradeItemSecondary: String = "Sekolah Menengah"
    override val gradeItemNames: String = "Kanji untuk nama orang (Jinmeiyō)"
    override val gradeItemNamesVariants: String = "Varian Jinmeiyō dari Jōyō"

    override val wanikaniTitle: String = "WaniKani"
    override val wanikaniDescription = { urlColor: Color ->
        buildAnnotatedString {
            append("Daftar kanji berdasarkan level di platform pembelajaran WaniKani oleh Tofugu. ")
            withClickableUrl("https://www.wanikani.com/kanji?difficulty=pleasant", urlColor) {
                append("Info selengkapnya. ")
            }
        }
    }
    override val wanikaniItem: (Int) -> String = { "WaniKani Level $it" }

    override val vocabOtherTitle: String = "Lainnya"
    override val vocabOtherDescription: AnnotatedString = buildAnnotatedString {
        append("Kumpulan dek kosakata tematik umum untuk membantu Anda memulai belajar")
    }

    override val vocabDeckItemWordsCountLabel: (words: Int) -> String = { "$it kata" }

    override val vocabDeckTitleTime: String = "Waktu"
    override val vocabDeckTitleWeek: String = "Hari dalam Seminggu"
    override val vocabDeckTitleCommonVerbs: String = "Kata Kerja Umum"
    override val vocabDeckTitleColors: String = "Warna"
    override val vocabDeckTitleRegularFood: String = "Makanan Umum"
    override val vocabDeckTitleJapaneseFood: String = "Makanan Jepang"
    override val vocabDeckTitleGrammarTerms: String = "Istilah Tata Bahasa"
    override val vocabDeckTitleAnimals: String = "Hewan"
    override val vocabDeckTitleBody: String = "Bagian Tubuh"
    override val vocabDeckTitleCommonPlaces: String = "Tempat Umum"
    override val vocabDeckTitleCities: String = "Kota-Kota"
    override val vocabDeckTitleTransport: String = "Transportasi"

}

object IndonesianDeckEditStrings : DeckEditStrings {
    override val createTitle: String = "Buat Dek"
    override val ediTitle: String = "Edit Dek"
    override val searchHint: String = "Ketik kana atau kanji"
    override val editingModeSearchTitle: String = "Cari"
    override val editingModeRemovalTitle: String = "Hapus"
    override val editingModeDetailsTitle: String = "Detail"
    override val vocabDetailsEmptyMessage: (inlineIconId: String) -> AnnotatedString = {
        buildAnnotatedString {
            append("Belum ada kartu. Untuk menambahkan kartu baru, simpan dek ini lalu gunakan ikon ")
            appendInlineContent(it)
            append(" di layar pencarian, saat latihan menulis, atau bagian lain di aplikasi")
        }
    }
    override val completeMessage: String = "Selesai"
    override val saveTitle: String = "Simpan perubahan"
    override val saveInputHint: String = "Judul Dek"
    override val saveButtonDefault: String = "Simpan"
    override val saveButtonCompleted: String = "Selesai"
    override val deleteTitle: String = "Konfirmasi Hapus"
    override val deleteMessage: (deckTitle: String) -> String = {
        "Apakah Anda yakin ingin menghapus dek \"$it\"?"
    }
    override val deleteButtonDefault: String = "Hapus"
    override val deleteButtonCompleted: String = "Selesai"

    override val unknownTitle: String = "Karakter tidak dikenal"
    override val unknownMessage: (characters: List<String>) -> String = {
        "Beberapa karakter tidak ditemukan: ${it.joinToString()}"
    }
    override val unknownButton: String = "Tutup"

    override val leaveConfirmationTitle: String = "Konfirmasi Keluar"
    override val leaveConfirmationMessage: String = "Semua perubahan yang belum disimpan akan hilang"
    override val leaveConfirmationCancel: String = "Batal"
    override val leaveConfirmationAccept: String = "Keluar"
}

object IndonesianDeckDetailsStrings : DeckDetailsStrings {
    override val emptyListMessage: String = "Tidak ada item di sini"
    override val detailsGroupTitle: (index: Int) -> String = { "Grup $it" }
    override val firstTimeReviewMessage: (LocalDateTime?) -> String = {
        "Waktu ulasan pertama: " + when (it) {
            null -> "Belum pernah"
            else -> groupDetailsDateTimeFormatter(it)
        }
    }
    override val lastTimeReviewMessage: (LocalDateTime?) -> String = {
        "Waktu ulasan terakhir: " + when (it) {
            null -> "Belum pernah"
            else -> groupDetailsDateTimeFormatter(it)
        }
    }
    override val groupDetailsButton: String = "Mulai"

    override val expectedReviewDate: (LocalDate?) -> String =
        { "Jadwal Ulasan: ${it ?: "-"}" }
    override val lastReviewDate: (LocalDateTime?) -> String = {
        "Ulasan Terakhir: ${it?.date ?: "-"}"
    }
    override val repetitions: (Int) -> String = { "Pengulangan: $it" }
    override val lapses: (Int) -> String = { "Lupa: $it" }

    override val dialogCommon: LetterDeckDetailDialogCommonStrings =
        IndonesianLetterDeckDetailDialogCommonStrings
    override val filterDialog: FilterDialogStrings = IndonesianFilterDialogStrings
    override val sortDialog: SortDialogStrings = IndonesianSortDialogStrings
    override val layoutDialog: PracticePreviewLayoutDialogStrings =
        IndonesianPracticePreviewLayoutDialogStrings

    override val multiselectTitle: (selectedCount: Int) -> String = { "$it Dipilih" }
    override val multiselectDataNotLoaded: String = "Memuat, mohon tunggu…"
    override val multiselectNoSelected: String = "Pilih setidaknya satu grup"

    override val filterAllLabel: String = "Semua"
    override val filterNoneLabel: String = "Tidak Ada"
    override val kanaGroupsModeActivatedLabel: String = "Mode Grup Kana"
    override val shareLetterDeckClipboardMessage: String =
        "Karakter dari dek berhasil disalin ke papan klip"

}

object IndonesianLetterDeckDetailDialogCommonStrings : LetterDeckDetailDialogCommonStrings {
    override val buttonCancel: String = "Batal"
    override val buttonApply: String = "Terapkan"
}

object IndonesianFilterDialogStrings : FilterDialogStrings {
    override val title: String = "Filter"
}

object IndonesianSortDialogStrings : SortDialogStrings {
    override val title: String = "Urutkan"
    override val sortOptionAddOrder: String = "Urutan penambahan"
    override val sortOptionAddOrderHint: String = "↑ Item baru di akhir\n↓ Item baru di awal"
    override val sortOptionFrequency: String = "Frekuensi"
    override val sortOptionFrequencyHint: String =
        "Frekuensi kemunculan karakter di surat kabar\n↑ Sering muncul duluan\n↓ Jarang muncul duluan"
    override val sortOptionName: String = "Nama"
    override val sortOptionNameHint: String = "↑ Dari kecil ke besar\n↓ Dari besar ke kecil"
    override val sortOptionReviewTime: String = "Waktu Ulasan"
    override val sortOptionReviewTimeHint: String =
        "↑ Belum pernah diulas duluan\n↓ Jadwal terjauh duluan"
}

object IndonesianPracticePreviewLayoutDialogStrings : PracticePreviewLayoutDialogStrings {
    override val title: String = "Tata Letak"
    override val singleCharacterOptionLabel: String = "Per Karakter"
    override val groupsOptionLabel: String = "Per Grup"
    override val kanaGroupsTitle: String = "Grup Kana"
    override val kanaGroupsSubtitle: String =
        "Sesuaikan ukuran grup dengan tabel kana jika latihan berisi seluruh karakter kana"
}

object IndonesianCommonPracticeStrings : CommonPracticeStrings {
    override val configurationTitle: String = "Konfigurasi"
    override val configurationSelectedItemsLabel: String = "Dipilih:"
    override val configurationCharactersPreview: String = "Pratinjau karakter"
    override val shuffleConfigurationTitle: String = "Acak"
    override val shuffleConfigurationMessage: String = "Acak urutan ulasan materi"
    override val configurationCompleteButton: String = "Mulai"

    override val additionalKanaReadingsNote: (List<String>) -> String = {
        "Catatan: juga dapat ditulis sebagai ${it.joinToString()}"
    }

    override val formattedSrsInterval: (Duration) -> String = { formattedSrsDuration(it) }
    override val flashcardRevealButton: String = "Tampilkan Jawaban"
    override val againButton: String = "Ulangi"
    override val hardButton: String = "Sulit"
    override val goodButton: String = "Bagus"
    override val easyButton: String = "Mudah"

    override val summaryTimeSpentValue: (Duration) -> String = { formatDuration(it) }

    override val earlyFinishDialogTitle: String = "Akhiri latihan?"
    override val earlyFinishDialogMessage: String =
        "Pindah ke ringkasan, perkembangan Anda saat ini sudah tersimpan"
    override val earlyFinishDialogCancelButton: String = "Batal"
    override val earlyFinishDialogAcceptButton: String = "Selesai"
}

object IndonesianLetterPracticeStrings : LetterPracticeStrings {
    override val configurationTitle: (practiceType: String) -> String = { "Latihan Karakter・$it" }
    override val hintStrokesTitle: String = "Petunjuk Goresan"
    override val hintStrokesMessage: String = "Atur kapan petunjuk goresan karakter ditampilkan"
    override val hintStrokeNewOnlyMode: String = "Hanya materi baru"
    override val hintStrokeAllMode: String = "Untuk semua"
    override val hintStrokeNoneMode: String = "Tidak pernah"
    override val inputModeTitle: String = "Mode Masukan"
    override val inputModeMessage: String =
        "Pilih apakah memvalidasi setiap goresan atau seluruh karakter sekaligus"
    override val inputModeStroke: String = "Per Goresan"
    override val inputModeCharacter: String = "Seluruh Karakter"
    override val kanaRomajiTitle: String = "Tampilkan romaji pada latihan kana"
    override val kanaRomajiMessage: String =
        "Saat mengulas kana, tampilkan romaji alih-alih karakter kana"
    override val noTranslationLayoutTitle: String = "Sembunyikan arti karakter"
    override val noTranslationLayoutMessage: String =
        "Sembunyikan terjemahan karakter saat latihan menulis"
    override val leftHandedModeTitle: String = "Mode Kidal"
    override val leftHandedModeMessage: String =
        "Sesuaikan posisi area tulis saat layar dalam posisi horizontal (landscape)"

    override val headerWordsMessage: (count: Int) -> String = {
        "Contoh Kosakata ($it)"
    }
    override val studyFinishedButton: String = "Ulas"
    override val noKanjiTranslationsLabel: String = "[Tidak ada terjemahan]"

    override val altStrokeEvaluatorTitle: String = "Evaluator Goresan Ketat"
    override val altStrokeEvaluatorMessage: String = "Algoritma alternatif untuk penilaian ketepatan goresan"

    override val variantsTitle: String = "Bentuk Varian: "
    override val variantsHint: String = "Klik untuk menampilkan"
    override val unicodeTitle: (String) -> String = { "Unicode: $it" }
    override val strokeCountTitle: (count: Int) -> String = { "Jumlah goresan: $it" }
}

object IndonesianVocabPracticeStrings : VocabPracticeStrings {
    override val configurationTitle: (practiceType: String) -> String = {
        "Latihan Kosakata・$it"
    }
    override val readingMeaningConfigurationTitle: String = "Selalu Tampilkan Arti"
    override val readingMeaningConfigurationMessage: String =
        "Pilih visibilitas arti saat jawaban belum dipilih"
    override val translationInFrontConfigurationTitle: String = "Arti di Sisi Depan"
    override val translationInFrontConfigurationMessage: String =
        "Tampilkan terjemahan arti alih-alih kata saat kartu hafalan tertutup"
    override val writingKanaReadingConfigurationTitle: String = "Tampilkan Bacaan Kana"
    override val writingKanaReadingConfigurationMessage: String =
        "Tampilkan bacaan kana bahkan sebelum jawaban dituliskan"
    override val writingWordsPreviewTitle: String = "Pratinjau kosakata"
    override val writingHintStrokesMessage: String =
        "Atur kapan petunjuk goresan karakter ditampilkan"
    override val writingInputModeMessage: String =
        "Pilih apakah memvalidasi setiap goresan atau seluruh karakter sekaligus"
    override val writingNoTranslationLayoutMessage: String =
        "Sembunyikan arti kata saat latihan menulis"
    override val writingKanjiInfoTitle: String = "Informasi Kanji"
    override val writingCharacterDetailsButton: String = "Detail Karakter"
    override val detailsButton: String = "Detail"
}

object IndonesianInfoScreenStrings : InfoScreenStrings {
    override val strokesMessage: (count: Int) -> AnnotatedString = {
        buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(it.toString()) }
            append(" goresan")
        }
    }
    override val clipboardCopyMessage: String = "Berhasil disalin"
    override val radicalsSectionTitle: (count: Int) -> String = { "Radikal ($it)" }
    override val noRadicalsMessage: String = "Tidak ada radikal"
    override val wordsSectionTitle: (count: Int) -> String = { "Kosakata ($it)" }
    override val romajiMessage: (romaji: List<String>) -> String = {
        "Bacaan romaji: ${it.joinToString()}"
    }
    override val gradeMessage: (grade: Int) -> String = {
        when {
            it <= 6 -> "Jōyō kanji, diajarkan di kelas $it SD"
            it == 8 -> "Jōyō kanji, diajarkan di SMP"
            it >= 9 -> "Jinmeiyō kanji, digunakan untuk nama orang"
            else -> throw IllegalStateException("Tingkat tidak dikenal $it")
        }
    }
    override val jlptMessage: (level: Int) -> String = { "JLPT level $it" }
    override val frequencyMessage: (frequency: Int) -> String = {
        "Peringkat ke-$it dari 2500 kanji paling sering digunakan di surat kabar"
    }
}

object IndonesianReminderNotificationStrings : ReminderNotificationStrings {
    override val channelName: String = "Notifikasi Pengingat"
    override val title: String = "Waktunya mengulang latihan!"
    override val noDetailsMessage: String = "Lanjutkan belajar bahasa Jepang sekarang"
    override val newOnlyMessage: (Int) -> String = {
        "Ada $it kartu baru untuk dipelajari hari ini"
    }
    override val dueOnlyMessage: (Int) -> String = {
        "Ada $it kartu yang perlu diulang hari ini"
    }
    override val message: (Int, Int) -> String = { new, due ->
        "Ada $new kartu baru dan $due kartu yang perlu diulang hari ini"
    }
}

object IndonesianAccountScreenStrings : AccountScreenStrings {
    override val title = "Akun"
    override val loggedOutMessage = "Belum masuk"
    override val signInButton = "Masuk"
    override val signOutButton = "Keluar"
    override val emailTitle = "Email"
    override val subscriptionTitle = "Langganan"
    override val subscriptionStatusActive = "Aktif"
    override val subscriptionStatusExpired = "Kedaluwarsa"
    override val subscriptionStatusInactive = "Tidak Aktif"
    override val subscriptionValidUntilTemplate = "Berlaku hingga %s"
    override val issueNoConnectionTitle = "Tidak Ada Koneksi"
    override val issueNoConnectionMessage = "Menampilkan data tembolok"
    override val issueSessionExpiredTitle = "Sesi Berakhir"
    override val issueSessionExpiredMessage = "Klik untuk masuk kembali"
    override val issueSubscriptionOutdatedTitle = "Status langganan usang"
    override val issueSubscriptionOutdatedMessage = "Klik untuk memuat ulang"
    override val issueOtherTitle = "Kesalahan"
    override val issueOtherMessageFallback = "Kesalahan tidak diketahui"
}

object IndonesianSyncScreenStrings : SyncScreenStrings {
    override val title = "Sinkronisasi (Pratinjau)"
    override val guideTitle: String = "Sinkronkan Perkembangan Anda Antar Perangkat"
    override val guideMessage =
        "Unggah data Anda secara otomatis ke cloud, simpan sebagai cadangan, dan sinkronkan di semua perangkat Anda"
    override val guideStepAccountTitle = "Buat akun dan masuk"
    override val guideStepAccountMessage: String = "Buka akun"
    override val guideStepSubscriptionTitle =
        "Mungkin memerlukan langganan berbayar di masa mendatang"
    override val guideStepSubscriptionMessage: String =
        "Gratis selama masa pratinjau hingga pemberitahuan lebih lanjut"
    override val accountErrorMessage: String = "Terdapat masalah pada akun Anda"
    override val syncButton = "Sinkronkan sekarang"
    override val statusTitle = "Status"
    override val statusMessageLoading = "Memeriksa pembaruan di server..."
    override val statusMessageDataDiffer = "Data lokal dan server berbeda"
    override val statusMessageLocalNewer = "Dapat mengunggah data terbaru"
    override val statusMessageUpToDate = "Sudah sinkron dengan server"
    override val statusMessageError = "Kesalahan"
    override val statusMessageUploading = "Mengunggah"
    override val statusMessageDownloading = "Mengunduh"
    override val statusMessageCanceled = "Dibatalkan, klik tombol sinkronisasi untuk mengulang"
    override val localDataTitle = "Data Lokal"
    override val localDataIdTemplate = "ID: %s"
    override val localDataTimestampTemplate = "Waktu: %s"

    override val errorNoConnectionTitle = "Tidak Ada Koneksi"
    override val errorNoConnectionMessage = "Tidak dapat terhubung ke server"
    override val errorSessionExpiredTitle = "Sesi Berakhir"
    override val errorSessionExpiredMessage = "Klik untuk masuk kembali"
    override val errorNoSubscriptionTitle = "Status langganan usang"
    override val errorNoSubscriptionMessage = "Perbarui status langganan di layar akun"
    override val errorOtherTitle = "Kesalahan"
    override val errorOtherMessageFallback = "Kesalahan tidak diketahui"
}

object IndonesianSyncDialogStrings : SyncDialogStrings {
    override val title = "Sinkronisasi"
    override val buttonCancel = "Batal"
    override val buttonUpload = "Unggah"
    override val buttonDownload = "Unduh"
    override val buttonAccount = "Akun"
    override val uploadingMessage = "Mengunggah..."
    override val downloadingMessage = "Mengunduh..."
    override val conflictRemoteNewerTitle = "Data Baru Ditemukan"
    override val conflictRemoteNewerMessage = "Data di server lebih baru daripada salinan lokal Anda"
    override val conflictIncompatibleTitle = "Konflik Data"
    override val conflictIncompatibleMessage =
        "Data lokal dan server sama-sama telah diubah sejak sinkronisasi terakhir, hasil tidak dapat digabungkan secara otomatis"
    override val errorNoNetworkTitle = "Tidak Ada Jaringan"
    override val errorNoNetworkMessage = "Gagal membuat koneksi jaringan"
    override val errorNoSubscriptionTitle = "Langganan Kedaluwarsa"
    override val errorNoSubscriptionMessage =
        "Langganan Anda telah berakhir, sinkronisasi dinonaktifkan, perbarui status langganan Anda di layar Akun"
    override val errorNotAuthenticatedTitle = "Sesi Berakhir"
    override val errorNotAuthenticatedMessage = "Masuk kembali ke akun Anda untuk melanjutkan"
    override val errorUnexpectedErrorTitle = "Kesalahan Tak Terduga"
    override val errorUnexpectedErrorMessage = "Masalah tidak diketahui"
    override val errorUnsupportedDataTitle = "Data di server tidak didukung"
    override val errorUnsupportedDataMessage =
        "Data di server dibuat menggunakan versi aplikasi yang lebih baru dan tidak kompatibel dengan versi yang saat ini terpasang. Perbarui aplikasi untuk mengambil data Anda"
}

object IndonesianSyncSnackbarStrings : SyncSnackbarStrings {
    override val errorNoConnection = "Tidak Ada Koneksi"
    override val errorNoSubscription = "Langganan kedaluwarsa"
    override val errorNotAuthenticated = "Sesi masuk berakhir"
    override val errorDataNotSupported = "Data server tidak didukung"
    override val errorMessageTemplate = "Kesalahan Sinkronisasi: %s"
    override val errorMessageNoReason = "Kesalahan Sinkronisasi"
    override val actionButton = "Detail"
}
