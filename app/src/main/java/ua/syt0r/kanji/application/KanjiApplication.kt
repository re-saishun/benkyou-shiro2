package ua.syt0r.kanji.application

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import ua.syt0r.kanji.core.app_data.AppDataDatabaseProvider
import ua.syt0r.kanji.di.appComponentsModule
import ua.syt0r.kanji.di.appModules
import ua.syt0r.kanji.flavorModule

class KanjiApplication : Application() {

    companion object {
        private val modules = appModules + flavorModule + appComponentsModule
    }

    override fun onCreate() {
        super.onCreate()
        val koin = startKoin {
            androidContext(this@KanjiApplication)
            loadKoinModules(modules)
        }.koin

        registerActivityLifecycleCallbacks(koin.get<ua.syt0r.kanji.auth.CurrentActivityTracker>())

        // Dictionary database is created/patched lazily (copy from assets + language patch),
        // start it in background now so the first Search open doesn't wait for it.
        // provideAsync() starts work on its own IO scope and returns immediately.
        koin.get<AppDataDatabaseProvider>().provideAsync()
    }

}