import sqlite3
import json
import re

# Comprehensive English to Indonesian vocabulary and kanji dictionary
# Designed for Japanese learning contexts (JLPT N5-N1, Jouyou Kanji)

KANJI_TRANSLATIONS = {
    # Numbers
    "一": ["satu"], "二": ["dua"], "三": ["tiga"], "四": ["empat"], "五": ["lima"],
    "六": ["enam"], "七": ["tujuh"], "八": ["delapan"], "九": ["sembilan"], "十": ["sepuluh"],
    "百": ["ratus"], "千": ["ribu"], "万": ["sepuluh ribu", "banyak"], "億": ["ratus juta"],
    
    # Days, nature, elements
    "日": ["hari", "matahari", "Jepang"], "月": ["bulan"], "火": ["api"], "水": ["air"],
    "木": ["pohon", "kayu"], "金": ["emas", "uang", "logam"], "土": ["tanah", "bumi"],
    "山": ["gunung"], "川": ["sungai"], "田": ["sawah"], "雨": ["hujan"], "雪": ["salju"],
    "風": ["angin"], "空": ["langit", "kosong"], "天": ["surga", "langit", "cuaca"],
    "気": ["energi", "semangat", "suasana hati", "udara"], "電": ["listrik"],
    "海": ["laut"], "池": ["kolam"], "森": ["hutan lebat"], "林": ["hutan"], "花": ["bunga"],
    "草": ["rumput"], "竹": ["bambu"], "石": ["batu"], "岩": ["batu besar"], "光": ["cahaya"],
    
    # People, body, family
    "人": ["orang", "manusia"], "男": ["pria", "laki-laki"], "女": ["wanita", "perempuan"],
    "子": ["anak"], "母": ["ibu"], "父": ["ayah"], "友": ["teman", "sahabat"],
    "兄": ["kakak laki-laki"], "弟": ["adik laki-laki"], "姉": ["kakak perempuan"],
    "妹": ["adik perempuan"], "私": ["saya", "pribadi"], "自": ["diri sendiri"],
    "身": ["tubuh", "diri"], "体": ["tubuh", "badan"], "頭": ["kepala"],
    "顔": ["wajah"], "目": ["mata", "butir"], "耳": ["telinga"], "口": ["mulut", "pintu masuk"],
    "手": ["tangan", "orang ahli"], "足": ["kaki", "cukup"], "指": ["jari", "menunjuk"],
    "心": ["hati", "pikiran", "jiwa"], "声": ["suara"],
    
    # Time, direction, position
    "年": ["tahun"], "時": ["waktu", "jam"], "分": ["menit", "bagian", "memahami"],
    "秒": ["detik"], "半": ["setengah", "separuh"], "今": ["sekarang"],
    "朝": ["pagi"], "昼": ["siang", "tengah hari"], "夜": ["malam"], "夕": ["sore", "senja"],
    "週": ["minggu", "pekan"], "毎": ["setiap"], "前": ["depan", "sebelum"],
    "後": ["belakang", "sesudah", "nanti"], "先": ["sebelumnya", "depan", "ujung"],
    "上": ["atas", "naik"], "下": ["bawah", "turun"], "中": ["tengah", "dalam"],
    "外": ["luar", "asing"], "左": ["kiri"], "右": ["kanan"], "間": ["antara", "ruang", "selang waktu"],
    "東": ["timur"], "西": ["barat"], "南": ["selatan"], "北": ["utara"],
    "方": ["arah", "sisi", "orang (hormat)", "cara"], "近": ["dekat"], "遠": ["jauh"],
    
    # School, learning, society
    "学": ["belajar", "ilmu"], "校": ["sekolah"], "生": ["hidup", "lahir", "mentah"],
    "先": ["terdahulu", "sebelum"], "師": ["guru", "ahli"], "教": ["mengajar", "agama"],
    "室": ["ruangan", "kamar"], "図": ["gambar", "rencana", "peta"], "書": ["menulis", "buku"],
    "本": ["buku", "asal", "pokok", "asli"], "文": ["kalimat", "tulisan", "sastra"],
    "字": ["karakter", "huruf"], "語": ["bahasa", "kata"], "話": ["berbicara", "cerita"],
    "会": ["bertemu", "perkumpulan", "rapat"], "社": ["perusahaan", "kuil", "masyarakat"],
    "店": ["toko", "kedai"], "屋": ["toko", "atap", "rumah"], "場": ["tempat", "lokasi"],
    "所": ["tempat"], "国": ["negara"], "町": ["kota", "wilayah kota"], "市": ["kota besar", "pasar"],
    "村": ["desa"], "京": ["ibu kota"], "都": ["ibu kota", "metropolis"],
    "道": ["jalan", "cara", "ajaran"], "駅": ["stasiun"], "車": ["mobil", "kendaraan", "roda"],
    "門": ["gerbang", "pintu gerbang"], "家": ["rumah", "keluarga", "ahli"],
    "銀": ["perak"], "病": ["sakit", "penyakit"], "院": ["institusi", "lembaga"],
    
    # Common actions / verbs
    "行": ["pergi", "melakukan", "baris"], "来": ["datang", "berikutnya"],
    "帰": ["pulang", "kembali"], "食": ["makan", "makanan"], "飲": ["minum"],
    "見": ["melihat", "tampak"], "聞": ["mendengar", "bertanya"],
    "言": ["berkata", "mengatakan"], "読": ["membaca"], "立": ["berdiri"],
    "休": ["istirahat", "libur"], "買": ["membeli"], "売": ["menjual"],
    "入": ["masuk", "memasukkan"], "出": ["keluar", "mengeluarkan"],
    "開": ["membuka"], "閉": ["menutup"], "止": ["berhenti"],
    "作": ["membuat", "menciptakan"], "使": ["menggunakan", "memakai"],
    "持": ["membawa", "memiliki"], "待": ["menunggu"], "知": ["mengetahui", "tahu"],
    "思": ["berpikir", "merasa"], "考": ["memikirkan", "merenungkan"],
    "歩": ["berjalan kaki"], "走": ["berlari"], "起": ["bangun", "terjadi"],
    "寝": ["tidur"], "始": ["mulai"], "終": ["selesai", "berakhir"],
    "住": ["tinggal", "menetap"], "働": ["bekerja"], "動": ["bergerak"],
    "切": ["memotong", "putus"], "貸": ["meminjamkan"], "借": ["meminjam"],
    "送": ["mengirim"], "返": ["mengembalikan"], "洗": ["mencuci"],
    "答": ["menjawab", "jawaban"], "着": ["memakai", "tiba"],
    "脱": ["melepas (pakaian)"], "登": ["mendaki", "naik"],
    
    # Adjectives, states
    "大": ["besar"], "小": ["kecil"], "多": ["banyak"], "少": ["sedikit"],
    "高": ["tinggi", "mahal"], "安": ["murah", "tenang", "aman"],
    "新": ["baru"], "古": ["lama", "tua", "kuno"],
    "長": ["panjang", "pemimpin"], "短": ["pendek", "singkat"],
    "重": ["berat", "tumpuk"], "軽": ["ringan"],
    "早": ["cepat (waktu)", "pagi-pagi"], "速": ["cepat (kecepatan)"],
    "遅": ["lambat", "terlambat"], "広": ["luas", "lebar"], "狭": ["sempit"],
    "明": ["terang", "jelas"], "暗": ["gelap"],
    "白": ["putih"], "黒": ["hitam"], "赤": ["merah"], "青": ["biru", "hijau"],
    "黄": ["kuning"], "茶": ["teh", "cokelat"],
    "強": ["kuat"], "弱": ["lemah"], "正": ["benar", "tepat"],
    "好": ["suka", "gemar"], "同": ["sama", "serupa"], "異": ["berbeda"],
    "楽": ["menyenangkan", "nyaman", "musik"],
    "苦": ["pahit", "menderita"], "痛": ["sakit", "nyeri"],
    "美": ["indah", "cantik"], "熱": ["panas", "demam"], "温": ["hangat"],
    "冷": ["dingin (benda)"], "寒": ["dingin (cuaca)"], "暑": ["panas (cuaca)"],
    "難": ["sulit", "sukar"], "易": ["mudah", "sederhana"],
    
    # Objects, food, nature, society
    "魚": ["ikan"], "鳥": ["burung"], "肉": ["daging"], "牛": ["sapi"],
    "馬": ["kuda"], "犬": ["anjing"], "猫": ["kucing"], "虫": ["serangga"],
    "米": ["beras", "nasi", "Amerika"], "茶": ["teh"], "酒": ["arak", "alkohol"],
    "物": ["benda", "barang", "hal"], "事": ["hal", "perkara", "urusan"],
    "紙": ["kertas"], "服": ["pakaian", "baju"], "皿": ["piring"],
    "門": ["gerbang"], "戸": ["pintu"], "窓": ["jendela"],
    "色": ["warna"], "音": ["bunyi", "suara"], "歌": ["lagu", "menyanyi"],
    "画": ["gambar", "lukisan", "guratan"], "映": ["memantul", "proyeksi"],
    "真": ["sejati", "nyata", "kebenaran"], "写": ["menyalin", "memotret"],
    "親": ["orang tua", "akrab"], "切": ["memotong", "ramah (shinsetsu)"],
    "便": ["nyaman", "pos", "kotoran"], "利": ["keuntungan", "bermanfaat"],
    "運": ["keberuntungan", "mengangkut"], "転": ["berguling", "berputar"],
    "員": ["anggota"], "工": ["pabrik", "konstruksi"],
    "野": ["lapangan", "liar"], "菜": ["sayuran"], "理": ["alasan", "logika"],
    "料": ["biaya", "bahan"], "飯": ["nasi", "makanan"],
    
    # Abstract, question, grammar
    "何": ["apa"], "誰": ["siapa"], "幾": ["berapa banyak"],
    "不": ["tidak", "bukan"], "無": ["tanpa", "tidak ada"],
    "有": ["ada", "memiliki"], "的": ["sasaran", "bersifat"],
    "性": ["sifat", "gender"], "化": ["berubah", "-isasi"],
    "度": ["derajat", "kali", "suhu"], "回": ["kali", "berputar"],
    "番": ["nomor", "giliran"], "号": ["nomor"], "部": ["bagian", "klub"],
    "界": ["dunia"], "世": ["dunia", "zaman", "generasi"]
}

# Vocabulary Deck Card translations (N5, N4, N3 common words)
VOCAB_CARD_TRANSLATIONS = {
    # N5
    "人": "orang", "二人": "dua orang", "私": "saya, aku",
    "お父さん": "ayah", "お母さん": "ibu", "お兄さん": "kakak laki-laki",
    "お姉さん": "kakak perempuan", "弟": "adik laki-laki", "妹": "adik perempuan",
    "おじいさん": "kakek, pria lansia", "おばあさん": "nenek, wanita lansia",
    "先生": "guru, pengajar", "学生": "murid, mahasiswa", "生徒": "siswa, murid",
    "友達": "teman, sahabat", "子供": "anak-anak", "大人": "orang dewasa",
    "男の人": "pria, laki-laki", "女の人": "wanita, perempuan",
    "男の子": "anak laki-laki", "女の子": "anak perempuan",
    "犬": "anjing", "猫": "kucing", "鳥": "burung", "魚": "ikan",
    "本": "buku", "辞書": "kamus", "雑誌": "majalah", "新聞": "koran, surat kabar",
    "ノート": "buku catatan", "手紙": "surat", "ペン": "pena, pulpen",
    "鉛筆": "pensil", "机": "meja belajar", "椅子": "kursi",
    "部屋": "kamar, ruangan", "家": "rumah", "学校": "sekolah",
    "教室": "ruang kelas", "大学": "universitas", "病院": "rumah sakit",
    "駅": "stasiun", "銀行": "bank", "郵便局": "kantor pos",
    "交番": "pos polisi", "店": "toko", "レストラン": "restoran",
    "デパート": "toserba, department store", "スーパー": "supermarket",
    "車": "mobil, kendaraan", "電車": "kereta api listrik", "地下鉄": "kereta bawah tanah",
    "バス": "bus", "タクシー": "taksi", "自転車": "sepeda", "飛行機": "pesawat terbang",
    "道": "jalan", "橋": "jembatan", "川": "sungai", "山": "gunung",
    "海": "laut", "空": "langit", "雨": "hujan", "雪": "salju", "風": "angin",
    "今日": "hari ini", "明日": "besok", "昨日": "kemarin",
    "明後日": "lusa", "一昨日": "kemarin lusa",
    "今週": "minggu ini", "来週": "minggu depan", "先週": "minggu lalu",
    "今月": "bulan ini", "来月": "bulan depan", "先月": "bulan lalu",
    "今年": "tahun ini", "来年": "tahun depan", "去年": "tahun lalu",
    "今": "sekarang", "朝": "pagi", "昼": "siang", "晩": "malam", "夜": "malam",
    "毎日": "setiap hari", "毎朝": "setiap pagi", "毎晩": "setiap malam",
    "時間": "waktu, jam", "時計": "jam tangan, lonceng",
    "水": "air", "お茶": "teh Jepang", "紅茶": "teh", "牛乳": "susu sapi",
    "ジュース": "jus", "コーヒー": "kopi", "お酒": "arak, sake", "ビール": "bir",
    "ご飯": "nasi, makanan", "パン": "roti", "肉": "daging", "野菜": "sayuran",
    "果物": "buah-buahan", "卵": "telur", "料理": "masakan, kuliner",
    "朝ご飯": "sarapan", "昼ご飯": "makan siang", "晩ご飯": "makan malam",
    "食べる": "makan", "飲む": "minum", "吸う": "menghisap, merokok",
    "行く": "pergi", "来る": "datang", "帰る": "pulang, kembali",
    "見る": "melihat, menonton", "聞く": "mendengar, bertanya",
    "読む": "membaca", "書く": "menulis", "話す": "berbicara",
    "買う": "membeli", "売る": "menjual", "待つ": "menunggu",
    "会う": "bertemu", "遊ぶ": "bermain", "泳ぐ": "berenang",
    "起きる": "bangun tidur", "寝る": "tidur", "立つ": "berdiri", "座る": "duduk",
    "入る": "masuk", "出る": "keluar", "開ける": "membuka", "閉める": "menutup",
    "つける": "menyalakan", "消す": "mematikan, menghapus",
    "教える": "mengajar, memberitahu", "習う": "mempelajari", "勉強する": "belajar",
    "働く": "bekerja", "休む": "beristirahat, libur",
    "大きい": "besar", "小さい": "kecil", "長い": "panjang", "短い": "pendek",
    "高い": "tinggi, mahal", "安い": "murah", "低い": "rendah",
    "新しい": "baru", "古い": "lama, tua", "良い": "bagus, baik", "悪い": "buruk, jelek",
    "暑い": "panas (cuaca)", "熱い": "panas (benda/suhu)", "寒い": "dingin (cuaca)",
    "冷たい": "dingin (benda)", "温かい": "hangat", "暖かい": "hangat (cuaca)",
    "美味しい": "enak, lezat", "甘い": "manis", "辛い": "pedas, asin",
    "広い": "luas", "狭い": "sempit", "遠い": "jauh", "近い": "dekat",
    "速い": "cepat", "遅い": "lambat, terlambat", "重い": "berat", "軽い": "ringan",
    "明るい": "terang, ceria", "暗い": "gelap",
    "静か": "tenang, sunyi", "賑やか": "ramai, meriah", "便利": "praktis, nyaman",
    "有名": "terkenal", "親切": "ramah, baik hati", "元気": "sehat, bersemangat",
    "暇": "waktu luang", "好き": "suka, gemar", "嫌い": "tidak suka, benci",
    "上手": "pandai, mahir", "下手": "kurang pandai, canggung",
    
    # Common expressions
    "ありがとう": "terima kasih", "ありがとうございます": "terima kasih banyak",
    "どういたしまして": "sama-sama", "おはようございます": "selamat pagi",
    "こんにちは": "halo, selamat siang", "こんばんは": "selamat malam",
    "おやすみなさい": "selamat tidur", "さようなら": "selamat tinggal",
    "すみません": "permisi, maaf", "ごめんなさい": "mohon maaf",
    "いただきます": "selamat makan", "ごちそうさまでした": "terima kasih atas makanannya",
    "初めまして": "senang berkenalan dengan Anda",
    "よろしくお願いします": "mohon bantuannya, salam kenal",
    "はい": "ya", "いいえ": "tidak, bukan"
}

# Example sentences translations
SENTENCE_TRANSLATIONS = {
    4711: "Saya tidak tahu harus berkata apa.",
    4726: "Saya menghasilkan 100 euro per hari.",
    4914: "\"Siapa itu?\" \"Ini ibumu.\"",
    4922: "\"Sejujurnya, aku takut ketinggian.\" \"Dasar penakut!\"",
    4925: "\"Ini terlihat sangat menarik,\" kata Hiroshi.",
    4928: "Mari kita makan.",
    4930: "Saya sedang belajar bahasa Jepang.",
    4933: "Cuaca hari ini sangat bagus.",
    4935: "Apakah kamu mengerti?",
    4937: "Tolong bantu saya.",
    4940: "Berapa harganya ini?",
    4942: "Saya ingin pergi ke Jepang.",
    4945: "Terima kasih banyak.",
    4950: "Di mana stasiun kereta api?",
}

def generate_patch(db_path, output_sql_path):
    conn = sqlite3.connect(db_path)
    cur = conn.cursor()
    
    statements = []
    statements.append("BEGIN TRANSACTION;")
    
    # 1. Update kanji meanings
    kanji_updated = 0
    for kanji, meanings in KANJI_TRANSLATIONS.items():
        # Check if kanji exists
        cur.execute("SELECT count(*) FROM kanji_meaning WHERE kanji = ?", (kanji,))
        count = cur.fetchone()[0]
        if count > 0:
            escaped_kanji = kanji.replace("'", "''")
            statements.append(f"DELETE FROM kanji_meaning WHERE kanji = '{escaped_kanji}';")
            for priority, meaning in enumerate(meanings):
                escaped_meaning = meaning.replace("'", "''")
                statements.append(f"INSERT INTO kanji_meaning (kanji, meaning, priority) VALUES ('{escaped_kanji}', '{escaped_meaning}', {priority});")
            kanji_updated += 1
            
    print(f"Prepared Indonesian translations for {kanji_updated} kanji.")
    
    # 2. Update vocab deck cards
    cards_updated = 0
    for key, id_def in VOCAB_CARD_TRANSLATIONS.items():
        # Match by kanji or kana
        cur.execute("SELECT jmdict_seq, kanji, kana, deck FROM vocab_deck_card WHERE kanji = ? OR (kanji IS NULL AND kana = ?)", (key, key))
        rows = cur.fetchall()
        for jmdict_seq, kanji, kana, deck in rows:
            escaped_def = id_def.replace("'", "''")
            escaped_deck = deck.replace("'", "''")
            if kanji:
                escaped_kanji = kanji.replace("'", "''")
                statements.append(f"UPDATE vocab_deck_card SET definition = '{escaped_def}' WHERE jmdict_seq = {jmdict_seq} AND kanji = '{escaped_kanji}' AND deck = '{escaped_deck}';")
            else:
                escaped_kana = kana.replace("'", "''")
                statements.append(f"UPDATE vocab_deck_card SET definition = '{escaped_def}' WHERE jmdict_seq = {jmdict_seq} AND kanji IS NULL AND kana = '{escaped_kana}' AND deck = '{escaped_deck}';")
            cards_updated += 1
            
    print(f"Prepared Indonesian translations for {cards_updated} vocab deck cards.")
    
    # 3. Update sample sentences
    sentences_updated = 0
    for tatoeba_id, id_trans in SENTENCE_TRANSLATIONS.items():
        escaped_trans = id_trans.replace("'", "''")
        statements.append(f"UPDATE sentence SET translation = '{escaped_trans}' WHERE tatoeba_id = {tatoeba_id};")
        sentences_updated += 1
        
    print(f"Prepared Indonesian translations for {sentences_updated} sentences.")
    
    statements.append("COMMIT;")
    
    with open(output_sql_path, "w", encoding="utf-8") as f:
        f.write("\n".join(statements) + "\n")
        
    print(f"Wrote {len(statements)} SQL statements to {output_sql_path}")

if __name__ == "__main__":
    import os
    build_script = os.path.join(os.path.dirname(__file__), "build_full_indonesian_patch.py")
    if os.path.exists(build_script):
        import build_full_indonesian_patch
        build_full_indonesian_patch.main()
    else:
        generate_patch(
            "core/src/commonMain/composeResources/files/kanji-dojo-data-base-v15.sql",
            "core/src/commonMain/composeResources/files/indonesian_patch.sql"
        )
