package ua.syt0r.kanji.core.user_data.database.migration

import app.cash.sqldelight.db.SqlDriver
import ua.syt0r.kanji.core.user_data.database.UserDataDatabaseContract

object UserDataDatabaseMigrationAfter12 : UserDataDatabaseContract.Migration {

    override val version: Long = 12

    override suspend fun execute(driver: SqlDriver) {
        driver.execute(
            identifier = null,
            sql = """
                CREATE TABLE IF NOT EXISTS grammar_deck (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    remote_id TEXT NOT NULL UNIQUE,
                    title TEXT NOT NULL,
                    level TEXT NOT NULL DEFAULT '',
                    description TEXT NOT NULL DEFAULT '',
                    source_url TEXT NOT NULL DEFAULT '',
                    version INTEGER NOT NULL DEFAULT 1,
                    position INTEGER NOT NULL DEFAULT 0
                );
            """.trimIndent(),
            parameters = 0
        )
        driver.execute(
            identifier = null,
            sql = """
                CREATE TABLE IF NOT EXISTS grammar_point (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    deck_id INTEGER NOT NULL,
                    point_key TEXT NOT NULL,
                    title TEXT NOT NULL,
                    structure TEXT NOT NULL,
                    meaning TEXT NOT NULL,
                    nuance TEXT NOT NULL DEFAULT '',
                    examples_json TEXT NOT NULL,
                    quiz_question TEXT NOT NULL DEFAULT '',
                    quiz_options_json TEXT NOT NULL,
                    quiz_correct_index INTEGER NOT NULL DEFAULT 0,
                    quiz_explanation TEXT NOT NULL DEFAULT '',
                    UNIQUE(deck_id, point_key),
                    FOREIGN KEY(deck_id) REFERENCES grammar_deck(id) ON UPDATE NO ACTION ON DELETE CASCADE
                );
            """.trimIndent(),
            parameters = 0
        )
    }

}
