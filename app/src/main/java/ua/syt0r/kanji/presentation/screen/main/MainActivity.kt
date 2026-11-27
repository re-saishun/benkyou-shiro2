package ua.syt0r.kanji.presentation.screen.main

import ua.syt0r.kanji.configureAppCheck
import ua.syt0r.kanji.presentation.KanjiDojoActivity

class MainActivity : KanjiDojoActivity() {

    override fun onPreContentCreate() {
        configureAppCheck(this, intent)
    }

}
