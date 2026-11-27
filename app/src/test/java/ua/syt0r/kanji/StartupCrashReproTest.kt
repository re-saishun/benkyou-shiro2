package ua.syt0r.kanji

import android.app.Application
import android.graphics.drawable.AdaptiveIconDrawable
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import ua.syt0r.kanji.application.KanjiApplication
import ua.syt0r.kanji.core.app_data.AppDataDatabaseProvider
import ua.syt0r.kanji.core.user_data.database.UserDataDatabaseContract
import ua.syt0r.kanji.presentation.screen.main.MainActivity

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = KanjiApplication::class)
class StartupCrashReproTest : KoinComponent {

    @Test
    fun verifyLauncherIconAndThemeResources() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val icon = ContextCompat.getDrawable(app, R.mipmap.ic_launcher)
        assertNotNull("ic_launcher drawable should inflate", icon)
        val roundIcon = ContextCompat.getDrawable(app, R.mipmap.ic_launcher_round)
        assertNotNull("ic_launcher_round drawable should inflate", roundIcon)
        if (icon is AdaptiveIconDrawable) {
            assertNotNull("Adaptive background", icon.background)
            assertNotNull("Adaptive foreground", icon.foreground)
        }
    }

    @Test
    fun verifyMainActivityLaunchAndComposeFrame() {
        val controller = Robolectric.buildActivity(MainActivity::class.java)
            .create()
            .start()
            .resume()
            .visible()

        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()

        // Also await AppDataDatabaseProvider and UserDataDatabase read transaction
        val appDataProvider = get<AppDataDatabaseProvider>()
        val appDb = runBlocking { appDataProvider.provideAsync().await() }
        assertNotNull(appDb)

        val userDbManager = get<UserDataDatabaseContract.Manager>()
        runBlocking {
            userDbManager.readTransaction {
                getLetterDecks().executeAsList()
                getVocabDecks().executeAsList()
                getGrammarDecks().executeAsList()
            }
        }

        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        assertNotNull(controller.get())
    }

}
