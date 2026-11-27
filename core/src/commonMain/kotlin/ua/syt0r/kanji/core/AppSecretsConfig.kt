package ua.syt0r.kanji.core

interface AppSecretsConfig {

    val supabaseUrl: String
    val supabaseAnonKey: String
    val googleWebClientId: String
    val githubGrammarCatalogUrl: String
    val githubToken: String
    val githubRepo: String

    val isSupabaseConfigured: Boolean
        get() = supabaseUrl.isNotBlank() && supabaseAnonKey.isNotBlank()

    val resolvedGithubCatalogUrl: String?
        get() = when {
            githubGrammarCatalogUrl.isNotBlank() -> githubGrammarCatalogUrl
            githubRepo.isNotBlank() -> {
                val slug = githubRepo
                    .removePrefix("https://github.com/")
                    .removePrefix("http://github.com/")
                    .trim('/')
                if (slug.contains('/')) {
                    "https://raw.githubusercontent.com/$slug/main/catalog.json"
                } else null
            }
            else -> null
        }

    val resolvedAppGithubUrl: String
        get() {
            if (githubRepo.isNotBlank()) {
                return if (githubRepo.startsWith("http://") || githubRepo.startsWith("https://")) {
                    githubRepo
                } else {
                    "https://github.com/${githubRepo.trim('/')}"
                }
            }
            val catalog = githubGrammarCatalogUrl
            if (catalog.contains("raw.githubusercontent.com/")) {
                val parts = catalog.substringAfter("raw.githubusercontent.com/").split('/')
                if (parts.size >= 2) {
                    return "https://github.com/${parts[0]}/${parts[1]}"
                }
            }
            if (catalog.contains("github.com/")) {
                val parts = catalog.substringAfter("github.com/").split('/')
                if (parts.size >= 2) {
                    return "https://github.com/${parts[0]}/${parts[1]}"
                }
            }
            return "https://github.com/re-saishun/benkyou"
        }

}
