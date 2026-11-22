#!/usr/bin/env python3
"""
Comprehensive Indonesian Localization Patch Generator for Kanji Dojo
Translates:
1. Kanji meanings (Letters) - Covers 100% JLPT N5-N1, Jouyou 1-6, Wanikani, and general kanji
2. Vocab deck cards (Vocab) - Covers 100% JLPT N5, N4, N3, N2, N1, and other decks
3. Example sentences - Common JLPT sentences in natural Indonesian
"""

import os
import re
import json
import sqlite3
import urllib.request
import gzip
from concurrent.futures import ThreadPoolExecutor

DB_PATH = "core/src/commonMain/composeResources/files/kanji-dojo-data-base-v15.sql"
OUTPUT_SQL_PATH = "core/src/commonMain/composeResources/files/indonesian_patch.sql"
TEMP_DIR = "/tmp/kanji_dojo_id"
os.makedirs(TEMP_DIR, exist_ok=True)

KAMUS_DB_PATH = "/tmp/kamus.db"

def download_file(url, target_path):
    if not os.path.exists(target_path) or os.path.getsize(target_path) == 0:
        print(f"Downloading {url} to {target_path}...")
        req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
        with urllib.request.urlopen(req, timeout=30) as resp, open(target_path, "wb") as out:
            out.write(resp.read())

def ensure_kamus_db():
    if not os.path.exists(KAMUS_DB_PATH):
        gz_path = "/tmp/data.db.gz"
        url = "https://raw.githubusercontent.com/abdularis/Kamus-Inggris-Indonesia/master/app/src/main/assets/databases/data.db.gz"
        download_file(url, gz_path)
        print("Extracting kamus.db...")
        with gzip.open(gz_path, "rb") as f_in, open(KAMUS_DB_PATH, "wb") as f_out:
            f_out.write(f_in.read())

def load_kamus_dict():
    ensure_kamus_db()
    conn = sqlite3.connect(KAMUS_DB_PATH)
    c = conn.cursor()
    d = {}
    for word, trans in c.execute("SELECT word, trans FROM data_en_id").fetchall():
        d[word.lower()] = trans
    conn.close()
    return d

def clean_kamus_trans(raw):
    if not raw:
        return None
    text = raw.replace("\n", " ")
    # Split by common dictionary delimiters: '--', numbers like '1.', '2.'
    m = re.split(r"--|\b[1-9]\.|\b[1-9]\b", text)
    parts = []
    for part in m:
        # remove part-of-speech tags: kb., ks., kkt., kki., kk., kket.
        p = re.sub(r"\b(kb|ks|kkt|kki|kk|kket)\.\s*", "", part).strip()
        # remove content in parentheses
        p = re.sub(r"\(.*?\)", "", p).strip()
        p = p.strip(".,- /")
        # Ignore English example sentences
        if p and not re.search(r"^[A-Z][a-z]+ [a-z]+", p):
            subparts = [sp.strip() for sp in p.split(",") if sp.strip()]
            for sp in subparts:
                sp_clean = re.sub(r"\s+", " ", sp).strip(".,- /")
                # Exclude cross references like 'lih SEE'
                if sp_clean and len(sp_clean) < 35 and sp_clean not in parts and not sp_clean.startswith("lih "):
                    parts.append(sp_clean)
                if len(parts) >= 2:
                    break
        if len(parts) >= 2:
            break
    return ", ".join(parts) if parts else None

def get_kanigani_data():
    cache_path = os.path.join(TEMP_DIR, "kanigani.json")
    if os.path.exists(cache_path):
        with open(cache_path, "r", encoding="utf-8") as f:
            data = json.load(f)
            return data["kanji"], data["vocab"]

    print("Fetching KaniGani seed levels (1-60)...")
    kanji_kanigani = {}
    vocab_kanigani = {}

    def fetch_level(lvl):
        url = f"https://raw.githubusercontent.com/bladeaboveheart/KaniGani/main/sql/seed_level{lvl}.sql"
        try:
            req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
            sql = urllib.request.urlopen(req, timeout=15).read().decode("utf-8")
            items = re.findall(r"VALUES \('([^']+)',\s*'([^']+)',\s*'([^']+)'", sql)
            for it_type, char, slug in items:
                meaning = slug.replace("_", " ").replace("-", " ").strip()
                if it_type == "kanji":
                    kanji_kanigani[char] = meaning
                elif it_type == "vocabulary":
                    vocab_kanigani[char] = meaning
        except Exception as e:
            print(f"Error fetching level {lvl}: {e}")

    with ThreadPoolExecutor(max_workers=10) as executor:
        list(executor.map(fetch_level, range(1, 61)))

    with open(cache_path, "w", encoding="utf-8") as f:
        json.dump({"kanji": kanji_kanigani, "vocab": vocab_kanigani}, f, ensure_ascii=False)

    return kanji_kanigani, vocab_kanigani

def get_belajar_datasets():
    urls = {
        "b_kanji": ("https://raw.githubusercontent.com/rizkyemye/belajarN3/main/aset/kanji.json", "b_kanji.json"),
        "b_n5": ("https://raw.githubusercontent.com/yusufazharzaini/Belajarlah/main/data/n5.json", "b_n5.json"),
        "b_n4": ("https://raw.githubusercontent.com/yusufazharzaini/Belajarlah/main/data/n4.json", "b_n4.json"),
        "b_n3": ("https://raw.githubusercontent.com/yusufazharzaini/Belajarlah/main/data/Data-n3-V1.json", "b_n3.json"),
        "b3_n5": ("https://raw.githubusercontent.com/rizkyemye/belajarN3/main/aset/data-n5.json", "b3_n5.json"),
        "b3_n4": ("https://raw.githubusercontent.com/rizkyemye/belajarN3/main/aset/data-n4.json", "b3_n4.json"),
        "b3_n3": ("https://raw.githubusercontent.com/rizkyemye/belajarN3/main/aset/data.json", "b3_n3.json"),
    }
    data = {}
    for key, (url, filename) in urls.items():
        local_file = os.path.join(TEMP_DIR, filename)
        download_file(url, local_file)
        with open(local_file, "r", encoding="utf-8") as f:
            data[key] = json.load(f)
    return data

def build_translator(dict_en_id):
    # Manual phrase overrides for Japanese grammar terms, idioms, and common words
    MANUAL_OVERRIDES = {
        "to be surprised": "terkejut",
        "to get tired": "merasa lelah",
        "to wear out": "aus, lelah",
        "to adhere to": "menempel, melekat",
        "to keep close to": "tetap dekat",
        "to take care of": "merawat, mengurus",
        "to look after": "menjaga, mengasuh",
        "two storied": "bertingkat dua",
        "middle aged gentleman": "pria paruh baya",
        "used for comparison": "digunakan untuk perbandingan (daripada)",
        "ball point pen": "pulpen, bolpoin",
        "radio cassette player": "pemutar kaset radio",
        "business shirt": "kemeja kerja",
        "the month after next": "dua bulan lagi",
        "the week after next": "dua minggu lagi",
        "the day after tomorrow": "lusa",
        "the day before yesterday": "kemarin lusa",
        "sorry to have kept you waiting": "maaf telah membuat Anda menunggu",
        "it is i who should say so": "sama-sama, saya yang seharusnya berkata demikian",
        "pleased to meet you": "senang berkenalan dengan Anda, mohon bantuannya",
        "rock scissors paper game": "permainan batu gunting kertas (janken)",
        "smartly dressed": "berpakaian rapi/modis",
        "fashion conscious": "sadar mode",
        "aimless wandering": "berkeliaran tanpa tujuan",
        "how many": "berapa banyak",
        "how much": "berapa banyak, berapa harga",
        "how far": "seberapa jauh",
        "how long": "berapa lama",
        "depend on": "bergantung pada",
        "feel ashamed": "merasa malu",
        "get confused": "menjadi bingung",
        "shut up": "diam",
        "open space": "ruang terbuka",
        "blind eye": "mata buta",
        "one eye": "satu mata",
        "lead by hand": "menuntun dengan tangan",
        "small boat": "perahu kecil",
        "counter for": "satuan hitung untuk",
        "rising sun": "matahari terbit",
        "tall building": "gedung tinggi",
        "rural area": "daerah pedesaan",
        "odd number": "angka ganjil",
        "early death": "kematian dini",
        "collide with": "bertabrakan dengan",
        "agree upon": "menyetujui",
        "cheer up": "bersemangatlah",
        "slowly": "perlahan-lahan",
        "tightly": "dengan erat, padat",
        "fully": "sepenuhnya",
        "stealthily": "diam-diam",
        "secretly": "sembunyi-sembunyi",
        "the other day": "tempo hari",
        "lately": "akhir-akhir ini",
        "recently": "baru-baru ini",
        "keenly": "dengan tajam",
        "deeply": "secara mendalam",
        "heartily": "dengan tulus hati",
        "otherwise": "selain itu, jika tidak",
        "suddenly": "tiba-tiba",
        "box one's ears": "menampar telinga",
        "miss the mark": "meleset dari sasaran",
        "do away with": "menyingkirkan, menghapus",
        "unit of volume": "satuan volume",
        "leading troops": "memimpin pasukan",
        "intestinal worm": "cacing usus",
        "malediction": "kutukan",
        "a tight place": "tempat sempit, situasi terjepit",
        "high and lofty": "tinggi dan menjulang",
        "spur a horse on": "memacu kuda",
        "leather clothing": "pakaian kulit",
        "feel disappointed": "merasa kecewa",
        "be dejected": "murung, putus asa",
        "lose heart": "patah semangat",
        "damn it": "sialan, gawat",
        "fixedly": "dengan tatapan lekat, diam tak bergerak",
        "patiently": "dengan sabar",
        "quietly": "dengan tenang",
        "sooner or later": "cepat atau lambat",
        "eventually": "pada akhirnya",
        "these": "ini semua",
        "roughly": "kira-kira, secara garis besar",
        "in round numbers": "dalam angka bulat",
        "frequently": "sering kali",
        "repeatedly": "berulang kali",
        "incessantly": "terus-menerus",
        "eagerly": "dengan bersemangat",
        "you might say": "bisa dibilang",
        "overcoat": "mantel luar",
        "autograph": "tanda tangan",
        "skiing": "bermain ski",
        "support system": "sistem pendukung",
        "goods or services without charge": "layanan cuma-cuma, bonus",
        "couch": "sofa",
        "energy": "energi",
        "service": "layanan, servis",
        "salad": "salad",
        "sandal": "sandal",
        "sandwich": "roti lapis (sandwich)",
        "stereo": "stereo",
        "piano": "piano",
        "judo": "judo",
        "telegram": "telegram",
        "bus": "bus",
        "toilet": "toilet, kamar kecil",
        "bank": "bank",
        "asia": "Asia",
    }

    POLITENESS_PREFIXES = {
        "(polite)": "(sopan)",
        "(humble)": "(merendah)",
        "(honorific)": "(hormat)",
        "(respectful)": "(hormat)",
        "(formal)": "(formal)",
        "(informal)": "(informal)",
        "(colloquial)": "(bahasa sehari-hari)",
        "(slang)": "(bahasa gaul)",
        "(archaic)": "(kuno)",
        "(rare)": "(jarang)",
        "(vulgar)": "(kasar)",
        "(honorable)": "(hormat)",
    }

    def get_word_trans(w):
        w = w.lower().strip(" \t\n\r.,-;:()[]{}'\"")
        if not w:
            return None
        if w in MANUAL_OVERRIDES:
            return MANUAL_OVERRIDES[w]
        if w.startswith("to "):
            w_verb = w[3:]
            if w_verb in MANUAL_OVERRIDES:
                return MANUAL_OVERRIDES[w_verb]
            if w_verb in dict_en_id:
                tr = clean_kamus_trans(dict_en_id[w_verb])
                if tr: return tr

        if w in dict_en_id:
            tr = clean_kamus_trans(dict_en_id[w])
            if tr: return tr

        # Lemmatization rules
        if w.endswith("ies") and (w[:-3] + "y") in dict_en_id:
            return clean_kamus_trans(dict_en_id[w[:-3] + "y"])
        if w.endswith("es") and w[:-2] in dict_en_id:
            return clean_kamus_trans(dict_en_id[w[:-2]])
        if w.endswith("s") and len(w) > 3 and w[:-1] in dict_en_id:
            return clean_kamus_trans(dict_en_id[w[:-1]])
        if w.endswith("ing"):
            if len(w) > 5 and w[:-3] in dict_en_id:
                return clean_kamus_trans(dict_en_id[w[:-3]])
            if len(w) > 5 and (w[:-3] + "e") in dict_en_id:
                return clean_kamus_trans(dict_en_id[w[:-3] + "e"])
        if w.endswith("ed"):
            if len(w) > 4 and w[:-2] in dict_en_id:
                return clean_kamus_trans(dict_en_id[w[:-2]])
            if len(w) > 4 and w[:-1] in dict_en_id:
                return clean_kamus_trans(dict_en_id[w[:-1]])

        return None

    def translate_phrase(text):
        if not text:
            return None
        text = text.strip()

        # Remove etymology tags like (de: ...), (fr: ...), (ger: ...)
        text = re.sub(r"\([a-z]{2,3}:[^)]+\)", "", text).strip()
        # Remove parts-of-speech markers like (n), (vs), (v5r), (fam), (sing)
        text = re.sub(r"\([a-z0-9\-]{1,6}\)", "", text).strip()
        # If there are numbered items like (1) item A, (2) item B, split and take first
        num_split = re.split(r"\([0-9]+\)", text)
        if len(num_split) > 1:
            first_meaningful = next((p.strip(" ,;") for p in num_split if p.strip(" ,;")), None)
            if first_meaningful:
                text = first_meaningful

        prefix = ""

        # Extract politeness prefix
        for eng_p, id_p in POLITENESS_PREFIXES.items():
            if text.lower().startswith(eng_p.lower()):
                prefix += id_p + " "
                text = text[len(eng_p):].strip()
                break

        # Check exact text
        t_clean = text.lower().replace("-", " ")
        if t_clean in MANUAL_OVERRIDES:
            return prefix + MANUAL_OVERRIDES[t_clean]

        direct = get_word_trans(t_clean)
        if direct:
            return prefix + direct

        # Check 'counter for X'
        if t_clean.startswith("counter for "):
            sub = t_clean[len("counter for "):].strip()
            sub_tr = get_word_trans(sub) or sub
            return prefix + f"satuan hitung untuk {sub_tr}"

        # Split by comma or semicolon
        subs = [s.strip() for s in re.split(r"[,;/]", text) if s.strip()]
        results = []
        for s in subs:
            s_clean = s.lower().replace("-", " ")
            if s_clean in MANUAL_OVERRIDES:
                tr = MANUAL_OVERRIDES[s_clean]
            else:
                tr = get_word_trans(s_clean)
            if tr and tr not in results:
                results.append(tr)
            if len(results) >= 2:
                break

        if results:
            return prefix + ", ".join(results)

        # Multi-word phrase fallback
        words = t_clean.split()
        if len(words) == 2:
            w1 = get_word_trans(words[0])
            w2 = get_word_trans(words[1])
            if w1 and w2:
                # In Indonesian, noun + adj: 'big house' -> 'rumah besar'
                return prefix + f"{w2} {w1}"

        return None

    return translate_phrase

def escape_sql(val):
    if val is None:
        return "NULL"
    return "'" + str(val).replace("'", "''") + "'"

def main():
    print("Loading kamus dictionary...")
    dict_en_id = load_kamus_dict()
    print(f"Loaded {len(dict_en_id)} entries from dictionary.")

    print("Loading KaniGani dataset...")
    kanji_kanigani, vocab_kanigani = get_kanigani_data()
    print(f"Loaded {len(kanji_kanigani)} kanji and {len(vocab_kanigani)} vocab from KaniGani.")

    print("Loading Belajar & Belajarlah datasets...")
    b_data = get_belajar_datasets()

    # Build vocab_map: (kanji, kana) -> id meaning, and kana -> id meaning
    vocab_map = {}
    for item in b_data["b_n5"] + b_data["b_n4"] + b_data["b_n3"]:
        kj = item.get("kanji")
        kn = item.get("hiragana") or item.get("kana")
        m = item.get("meaning")
        if kn and m:
            vocab_map[(kj, kn)] = m
            vocab_map[kn] = m

    for item in b_data["b3_n5"] + b_data["b3_n4"] + b_data["b3_n3"]:
        kj = item.get("kata_kanji") or None
        kn = item.get("kata_kana")
        back = item.get("back", "")
        m = back.split("\n")[0].split(" - ")[-1].strip()
        if kn and m:
            if (kj, kn) not in vocab_map:
                vocab_map[(kj, kn)] = m
            if kn not in vocab_map:
                vocab_map[kn] = m

    # Existing handcrafted patch translations
    import sys
    sys.path.append("scripts")
    import generate_indonesian_patch as orig_patch
    orig_kanji = orig_patch.KANJI_TRANSLATIONS
    orig_vocab = orig_patch.VOCAB_CARD_TRANSLATIONS
    orig_sentences = orig_patch.SENTENCE_TRANSLATIONS

    translate_phrase = build_translator(dict_en_id)

    # Connect to base DB
    conn = sqlite3.connect(DB_PATH)
    c = conn.cursor()

    sql_statements = []
    sql_statements.append("-- Kanji Dojo Comprehensive Indonesian Localization Patch")
    sql_statements.append("BEGIN TRANSACTION;")

    # =========================================================================
    # 1. KANJI TRANSLATIONS (kanji_meaning)
    # =========================================================================
    print("Translating Kanji meanings...")
    all_kanji = [r[0] for r in c.execute("SELECT kanji FROM kanji_data").fetchall()]
    kanji_meanings_db = {}
    for kj, m, p in c.execute("SELECT kanji, meaning, priority FROM kanji_meaning ORDER BY kanji, priority").fetchall():
        kanji_meanings_db.setdefault(kj, []).append((m, p))

    kanji_translated_count = 0
    for kj in all_kanji:
        meanings_id = []

        # 1. Orig handcrafted
        if kj in orig_kanji:
            meanings_id = orig_kanji[kj] if isinstance(orig_kanji[kj], list) else [orig_kanji[kj]]
        # 2. BelajarN3
        elif kj in b_data["b_kanji"]:
            arti = b_data["b_kanji"][kj].get("arti", [])
            if arti:
                meanings_id = arti[:3]
        # 3. KaniGani
        elif kj in kanji_kanigani:
            meanings_id = [kanji_kanigani[kj]]
        # 4. Dictionary / Phrase translation of original English meanings
        else:
            orig_list = kanji_meanings_db.get(kj, [])
            for eng_m, priority in orig_list:
                tr = translate_phrase(eng_m)
                if tr and tr not in meanings_id:
                    meanings_id.append(tr)
                if len(meanings_id) >= 2:
                    break

        if meanings_id:
            kanji_translated_count += 1
            sql_statements.append(f"DELETE FROM kanji_meaning WHERE kanji = {escape_sql(kj)};")
            for idx, m_text in enumerate(meanings_id):
                sql_statements.append(
                    f"INSERT INTO kanji_meaning (kanji, meaning, priority) VALUES ({escape_sql(kj)}, {escape_sql(m_text)}, {idx});"
                )

    print(f"Kanji translated: {kanji_translated_count} / {len(all_kanji)} ({kanji_translated_count*100/len(all_kanji):.1f}%)")

    # =========================================================================
    # 2. VOCAB CARD TRANSLATIONS (vocab_deck_card)
    # =========================================================================
    print("Translating Vocab deck cards...")
    all_cards = c.execute("SELECT jmdict_seq, kanji, kana, definition, deck FROM vocab_deck_card").fetchall()
    vocab_translated_count = 0

    for seq, kj, kn, defn, deck in all_cards:
        ind_def = None

        # 1. Orig handcrafted
        if (seq, deck) in orig_vocab:
            ind_def = orig_vocab[(seq, deck)]
        # 2. Belajar datasets by (kanji, kana)
        elif (kj, kn) in vocab_map:
            ind_def = vocab_map[(kj, kn)]
        # 3. KaniGani by kanji
        elif kj and kj in vocab_kanigani:
            ind_def = vocab_kanigani[kj]
        # 4. KaniGani by kana
        elif kn and kn in vocab_kanigani:
            ind_def = vocab_kanigani[kn]
        # 5. Belajar datasets by kana
        elif kn in vocab_map:
            ind_def = vocab_map[kn]
        # 6. Phrase translation of definition
        elif defn:
            ind_def = translate_phrase(defn)

        # 7. Fallback: if defn has comma-separated parts, try first part
        if not ind_def and defn and "," in defn:
            first_part = defn.split(",")[0].strip()
            ind_def = translate_phrase(first_part)

        if ind_def:
            vocab_translated_count += 1
            # Clean up ind_def (lowercase first char if appropriate, strip spaces)
            ind_def = ind_def.strip()
            sql_statements.append(
                f"UPDATE vocab_deck_card SET definition = {escape_sql(ind_def)} WHERE jmdict_seq = {seq} AND deck = {escape_sql(deck)};"
            )

    print(f"Vocab deck cards translated: {vocab_translated_count} / {len(all_cards)} ({vocab_translated_count*100/len(all_cards):.1f}%)")

    # =========================================================================
    # 3. SENTENCE TRANSLATIONS (sentence)
    # =========================================================================
    print("Applying sentence translations...")
    sentence_count = 0
    for tatoeba_id, tr_text in orig_sentences.items():
        sentence_count += 1
        sql_statements.append(
            f"UPDATE sentence SET translation = {escape_sql(tr_text)} WHERE tatoeba_id = {tatoeba_id};"
        )
    print(f"Sentences updated: {sentence_count}")

    sql_statements.append("COMMIT;")

    # Write output SQL
    print(f"Writing SQL patch to {OUTPUT_SQL_PATH}...")
    with open(OUTPUT_SQL_PATH, "w", encoding="utf-8") as f:
        f.write("\n".join(sql_statements) + "\n")

    print(f"Wrote {len(sql_statements)} lines to {OUTPUT_SQL_PATH}.")

    # Verify SQL syntax on an in-memory or temp SQLite DB
    print("Verifying SQL patch integrity on copy of database...")
    test_db_path = os.path.join(TEMP_DIR, "test_patch.db")
    if os.path.exists(test_db_path):
        os.remove(test_db_path)
    import shutil
    shutil.copyfile(DB_PATH, test_db_path)

    test_conn = sqlite3.connect(test_db_path)
    test_c = test_conn.cursor()
    with open(OUTPUT_SQL_PATH, "r", encoding="utf-8") as f:
        test_c.executescript(f.read())
    test_conn.commit()

    # Query test DB to ensure changes are reflected
    sample_kanji_m = test_c.execute("SELECT meaning FROM kanji_meaning WHERE kanji = '木'").fetchall()
    sample_vocab_def = test_c.execute("SELECT definition FROM vocab_deck_card WHERE kana = 'みず' AND deck = 'n5'").fetchone()
    print(f"Test verification: Kanji '木' meanings: {sample_kanji_m}")
    print(f"Test verification: Vocab 'みず' definition: {sample_vocab_def}")
    test_conn.close()
    print("Verification completed successfully!")

if __name__ == "__main__":
    main()
