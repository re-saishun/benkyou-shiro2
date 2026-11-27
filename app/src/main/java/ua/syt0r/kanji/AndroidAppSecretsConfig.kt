package ua.syt0r.kanji

import ua.syt0r.kanji.core.AppSecretsConfig

class AndroidAppSecretsConfig : AppSecretsConfig {

    private fun sanitize(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return ""
        if (trimmed.startsWith("your_") && trimmed.endsWith("_here")) return ""
        return trimmed
    }

    override val supabaseUrl: String = sanitize(BuildConfig.SUPABASE_URL).trimEnd('/')
    override val supabaseAnonKey: String = sanitize(BuildConfig.SUPABASE_ANON_KEY)
    override val googleWebClientId: String = sanitize(BuildConfig.GOOGLE_WEB_CLIENT_ID)
    override val githubGrammarCatalogUrl: String = sanitize(BuildConfig.GITHUB_GRAMMAR_CATALOG_URL)
    override val githubToken: String = sanitize(BuildConfig.GITHUB_TOKEN)
    override val githubRepo: String = sanitize(BuildConfig.GITHUB_REPO)

}
