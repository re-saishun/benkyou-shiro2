package ua.syt0r.kanji.core.app_data

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.ExperimentalResourceApi
import ua.syt0r.kanji.Res
import ua.syt0r.kanji.core.CustomVersionSqlSchema
import ua.syt0r.kanji.core.app_data.db.AppDataDatabase
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.readUserVersion
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContentLanguage
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.concurrent.ConcurrentHashMap

private const val EnglishDatabaseName = "kanji_data_en"
private const val IndonesianDatabaseName = "kanji_data_id"
private const val LegacyDatabaseName = "kanji_data"
private const val CurrentIndonesianPatchVersion = 2
private const val PrefKeyIndonesianPatchVersion = "indonesian_patch_version"
private const val AppDataPrefsName = "app_data_prefs"

class AndroidAppDataDatabaseProvider(
    private val context: Context,
    private val appPreferences: PreferencesContract.AppPreferences
) : AppDataDatabaseProvider {

    private val coroutineScope = CoroutineScope(context = Dispatchers.IO)
    private val dbCache = ConcurrentHashMap<PreferencesContentLanguage, Deferred<AppDataDatabase>>()

    override fun provideAsync(): Deferred<AppDataDatabase> = coroutineScope.async {
        val language = appPreferences.contentLanguage.get()
        provideAsync(language).await()
    }

    override fun provideAsync(language: PreferencesContentLanguage): Deferred<AppDataDatabase> {
        return dbCache.getOrPut(language) {
            coroutineScope.async {
                getExistingDatabaseIfUpdated(language) ?: createNewDatabase(language)
            }
        }
    }

    private fun getDbName(language: PreferencesContentLanguage): String {
        return when (language) {
            PreferencesContentLanguage.English -> EnglishDatabaseName
            PreferencesContentLanguage.Indonesian -> IndonesianDatabaseName
        }
    }

    private suspend fun getExistingDatabaseIfUpdated(language: PreferencesContentLanguage): AppDataDatabase? {
        val dbName = getDbName(language)
        val dbFile = context.getDatabasePath(dbName)

        // If English and kanji_data_en does not exist, but legacy kanji_data exists, migrate it
        if (language == PreferencesContentLanguage.English && !dbFile.exists()) {
            val legacyFile = context.getDatabasePath(LegacyDatabaseName)
            if (legacyFile.exists()) {
                withContext(Dispatchers.IO) {
                    Files.copy(legacyFile.toPath(), dbFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                }
            }
        }

        if (!dbFile.exists()) return null

        // Check if Indonesian database has outdated patch
        if (language == PreferencesContentLanguage.Indonesian) {
            val prefs = context.getSharedPreferences(AppDataPrefsName, Context.MODE_PRIVATE)
            val patchVersion = prefs.getInt(PrefKeyIndonesianPatchVersion, 0)
            if (patchVersion < CurrentIndonesianPatchVersion) {
                Logger.d("Indonesian patch is outdated ($patchVersion < $CurrentIndonesianPatchVersion)")
                return null
            }
        }

        val result = createDriver(dbFile)

        if (result.wasUpgraded) {
            Logger.d("Database [$dbName] is outdated")
            result.driver.close()
            return null
        }

        return AppDataDatabase(result.driver)
    }

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun createNewDatabase(language: PreferencesContentLanguage): AppDataDatabase {
        val dbName = getDbName(language)
        val isDeleted = context.deleteDatabase(dbName)
        Logger.d("Creating new database for $language, isDeleted[$isDeleted]")

        val targetDbFile = context.getDatabasePath(dbName)
        val targetPath = targetDbFile.toPath()

        // If English database already exists on device, copy from it; otherwise copy from assets
        val enDbFile = context.getDatabasePath(EnglishDatabaseName)
        if (enDbFile.exists()) {
            withContext(Dispatchers.IO) {
                Files.copy(enDbFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING)
            }
        } else {
            val databaseResourcePath = Res.getUri("files/$AppDataDatabaseResourceName")
                .removePrefix("file:///android_asset/")
            Logger.d("databaseResourcePath[$databaseResourcePath]")

            val input = context.assets.open(databaseResourcePath)
            withContext(Dispatchers.IO) {
                Files.copy(input, targetPath, StandardCopyOption.REPLACE_EXISTING)
            }
        }

        // If Indonesian, apply the Indonesian patch
        if (language == PreferencesContentLanguage.Indonesian) {
            applyIndonesianPatch(targetDbFile)
            val prefs = context.getSharedPreferences(AppDataPrefsName, Context.MODE_PRIVATE)
            prefs.edit().putInt(PrefKeyIndonesianPatchVersion, CurrentIndonesianPatchVersion).apply()
        }

        val result = createDriver(targetDbFile)
        return AppDataDatabase(result.driver)
    }

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun applyIndonesianPatch(dbFile: File) {
        Logger.d("Applying Indonesian patch to ${dbFile.name}")
        withContext(Dispatchers.IO) {
            try {
                val patchResourcePath = Res.getUri("files/indonesian_patch.sql")
                    .removePrefix("file:///android_asset/")
                val input = context.assets.open(patchResourcePath)

                val sqliteDb = android.database.sqlite.SQLiteDatabase.openDatabase(
                    dbFile.path,
                    null,
                    android.database.sqlite.SQLiteDatabase.OPEN_READWRITE
                )
                try {
                    sqliteDb.beginTransaction()
                    input.bufferedReader().use { reader ->
                        val statementBuffer = StringBuilder()
                        reader.forEachLine { line ->
                            val trimmed = line.trim()
                            if (trimmed.isNotEmpty() && !trimmed.startsWith("--")) {
                                if (trimmed.equals("BEGIN TRANSACTION;", ignoreCase = true) ||
                                    trimmed.equals("COMMIT;", ignoreCase = true) ||
                                    trimmed.equals("BEGIN TRANSACTION", ignoreCase = true) ||
                                    trimmed.equals("COMMIT", ignoreCase = true)
                                ) {
                                    // Handled by sqliteDb.beginTransaction()
                                } else {
                                    statementBuffer.append(line)
                                    if (trimmed.endsWith(";")) {
                                        val stmt = statementBuffer.toString().trim()
                                        if (stmt.isNotEmpty()) {
                                            sqliteDb.execSQL(stmt)
                                        }
                                        statementBuffer.clear()
                                    } else {
                                        statementBuffer.append("\n")
                                    }
                                }
                            }
                        }
                    }
                    sqliteDb.setTransactionSuccessful()
                    Logger.d("Indonesian patch applied successfully")
                } finally {
                    sqliteDb.endTransaction()
                    sqliteDb.close()
                }
            } catch (e: Exception) {
                Logger.e("Failed to apply Indonesian patch: ${e.message}")
            }
        }
    }

    private data class DriverCreationResult(
        val driver: AndroidSqliteDriver,
        val wasUpgraded: Boolean
    )

    private suspend fun createDriver(dbFile: File): DriverCreationResult {
        val schema = CustomVersionSqlSchema(AppDataDatabaseVersion, AppDataDatabase.Schema)
        var wasUpgraded = false
        val onDatabaseOpen = CompletableDeferred<Unit>()
        val driver = AndroidSqliteDriver(
            schema = schema,
            context = context,
            name = dbFile.name,
            callback = object : AndroidSqliteDriver.Callback(schema) {
                override fun onUpgrade(
                    db: SupportSQLiteDatabase,
                    oldVersion: Int,
                    newVersion: Int
                ) {
                    super.onUpgrade(db, oldVersion, newVersion)
                    Logger.d("oldVersion[$oldVersion] newVersion[$newVersion]")
                    wasUpgraded = true
                }

                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    Logger.logMethod()
                    onDatabaseOpen.complete(Unit)
                }
            },
        )
        Logger.d("driver created for ${dbFile.name}")
        val version = driver.readUserVersion() // To trigger onOpen callback
        onDatabaseOpen.await()
        Logger.d("driver ready version[$version] wasUpgraded[$wasUpgraded]")
        return DriverCreationResult(driver, wasUpgraded)
    }
}
