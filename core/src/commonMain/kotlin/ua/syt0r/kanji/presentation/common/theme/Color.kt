package ua.syt0r.kanji.presentation.common.theme

import androidx.compose.ui.graphics.Color

val md_theme_light_primary = Color(0xFFff5555)
val md_theme_light_onPrimary = Color(0xFFFCFCFC)
val md_theme_light_primaryContainer = Color(0xFFff5555)
val md_theme_light_onPrimaryContainer = Color(0xFFFCFCFC)
val md_theme_light_secondary = Color(0xFFff897c)
val md_theme_light_onSecondary = Color(0xFFFCFCFC)
val md_theme_light_secondaryContainer = Color(0xFFff5555)
val md_theme_light_onSecondaryContainer = Color(0xFFffffff)
val md_theme_light_tertiary = Color(0xFFff5555)
val md_theme_light_onTertiary = Color(0xFFffffff)
val md_theme_light_tertiaryContainer = Color(0xFFFCFCFC)
val md_theme_light_onTertiaryContainer = Color(0xFF410004)
val md_theme_light_error = Color(0xFFff897c)
val md_theme_light_errorContainer = Color(0xFFfeccc5)
val md_theme_light_onError = Color(0xFFffffff)
val md_theme_light_onErrorContainer = Color(0xFFc7282c)
val md_theme_light_background = Color(0xFFfcfcfc)
val md_theme_light_onBackground = Color(0xFF191c1d)
val md_theme_light_surface = Color(0xFFFCFCFC)
val lightSurfaceDim = Color(0xFFf2f2f2)
val md_theme_light_onSurface = Color(0xFF191c1d)
val md_theme_light_surfaceVariant = Color(0xFFE1E4E6)
val md_theme_light_onSurfaceVariant = Color(0xFF575b5c)
val md_theme_light_outline = Color(0xFF857372)
val md_theme_light_outline_variant = Color(0xFFe1e1e1)
val md_theme_light_inverseOnSurface = Color(0xFFfbeeed)
val md_theme_light_inverseSurface = Color(0xFF362f2e)
val md_theme_light_inversePrimary = Color(0xFFFCFCFC)
val lightThemeLinkColor = Color(0xff0054d7)
val lightThemeSuccessColor = Color(0xFF38CE3F)
val lightThemePendingColor = Color(0xFF857372)
val lightThemeDueColor = Color(0xFFFEBC2E)
val lightThemeNewColor = Color(0xFF03A9F4)

val md_theme_dark_primary = Color(0xFFff5555)
val md_theme_dark_onPrimary = Color(0xFFFCFCFC)
val md_theme_dark_primaryContainer = Color(0xFFff5555)
val md_theme_dark_onPrimaryContainer = Color(0xFFffdad7)
val md_theme_dark_secondary = Color(0xFFc00000)
val md_theme_dark_onSecondary = Color(0xFFffdad8)
val md_theme_dark_secondaryContainer = Color(0xFFff5555)
val md_theme_dark_onSecondaryContainer = Color(0xFFffdad8)
val md_theme_dark_tertiary = Color(0xFFff5555)
val md_theme_dark_onTertiary = Color(0xFFffdad8)
val md_theme_dark_tertiaryContainer = Color(0xFF930012)
val md_theme_dark_onTertiaryContainer = Color(0xFFffdad8)
val md_theme_dark_error = Color(0xFFff5555)
val md_theme_dark_errorContainer = Color(0xFFe13436)
val md_theme_dark_onError = Color(0xFF201a1a)
val md_theme_dark_onErrorContainer = Color(0xFFffdad4)
val md_theme_dark_background = Color(0xFF201a1a)
val md_theme_dark_onBackground = Color(0xFFecdfde)
val md_theme_dark_surface = Color(0xFF201a1a)
val darkSurfaceDim = Color(0xFF252222)
val md_theme_dark_onSurface = Color(0xFFFCFCFC)
val md_theme_dark_surfaceVariant = Color(0xFF313131)
val md_theme_dark_onSurfaceVariant = Color(0xFFcacaca)
val md_theme_dark_outline = Color(0xFFa08c8b)
val md_theme_dark_inverseOnSurface = Color(0xFF201a1a)
val md_theme_dark_inverseSurface = Color(0xFFecdfde)
val md_theme_dark_inversePrimary = Color(0xFFa33b3e)
val darkThemeLinkColor = Color(0xFF5C95E6)
val darkThemeSuccessColor = Color(0xFF38CE3F)
val darkThemePendingColor = Color(0xFFCCB3B2)
val darkThemeDueColor = Color(0xFFFBC02D)
val darkThemeNewColor = Color(0xFF03A9F4)

val amoled_theme_background = Color(0xFF000000)
val amoled_theme_surface = Color(0xFF000000)
val amoledSurfaceDim = Color(0xFF121212)

data class AppThemeTokens(
    val background: Color,
    val card: Color,
    val textPrimary: Color,
    val textMuted: Color,
    val accent: Color,
    val progressTrack: Color,
    val onAccent: Color,
    val isLight: Boolean,
    val useCardBorder: Boolean = isLight,
    val dialogSurface: Color = card
)

val shuLightThemeTokens = AppThemeTokens(
    background = md_theme_light_background,
    card = md_theme_light_surfaceVariant.copy(alpha = 0.55f),
    textPrimary = md_theme_light_onSurface,
    textMuted = md_theme_light_onSurfaceVariant,
    accent = md_theme_light_primary,
    progressTrack = md_theme_light_surfaceVariant,
    onAccent = md_theme_light_onPrimary,
    isLight = true,
    useCardBorder = false,
    dialogSurface = md_theme_light_surface
)

val shuDarkThemeTokens = AppThemeTokens(
    background = md_theme_dark_background,
    card = md_theme_dark_surfaceVariant.copy(alpha = 0.55f),
    textPrimary = md_theme_dark_onSurface,
    textMuted = md_theme_dark_onSurfaceVariant,
    accent = md_theme_dark_primary,
    progressTrack = md_theme_dark_surfaceVariant,
    onAccent = md_theme_dark_onPrimary,
    isLight = false,
    useCardBorder = false,
    dialogSurface = md_theme_dark_surface
)

val sumiDarkThemeTokens = AppThemeTokens(
    background = Color(0xFF141414),
    card = Color(0xFF1F1F1F),
    textPrimary = Color(0xFFEDEDED),
    textMuted = Color(0xFF9A9A9A),
    accent = Color(0xFFF2F2F2),
    progressTrack = Color(0xFF333333),
    onAccent = Color(0xFF141414),
    isLight = false
)

val sumiLightThemeTokens = AppThemeTokens(
    background = Color(0xFFF6F6F4),
    card = Color(0xFFFFFFFF),
    textPrimary = Color(0xFF1A1A1A),
    textMuted = Color(0xFF6F6F6F),
    accent = Color(0xFF1A1A1A),
    progressTrack = Color(0xFFDCDCD8),
    onAccent = Color(0xFFFFFFFF),
    isLight = true
)

val matchaDarkThemeTokens = AppThemeTokens(
    background = Color(0xFF131A16),
    card = Color(0xFF1D2621),
    textPrimary = Color(0xFFE8EFE9),
    textMuted = Color(0xFF92A498),
    accent = Color(0xFF8FCF9F),
    progressTrack = Color(0xFF2F3D35),
    onAccent = Color(0xFF10231A),
    isLight = false
)

val matchaLightThemeTokens = AppThemeTokens(
    background = Color(0xFFF3F6F1),
    card = Color(0xFFFFFFFF),
    textPrimary = Color(0xFF1B2A20),
    textMuted = Color(0xFF66776B),
    accent = Color(0xFF3D8B57),
    progressTrack = Color(0xFFDBE5DC),
    onAccent = Color(0xFFFFFFFF),
    isLight = true
)

val sakuraDarkThemeTokens = AppThemeTokens(
    background = Color(0xFF1C1618),
    card = Color(0xFF282024),
    textPrimary = Color(0xFFF3E9EC),
    textMuted = Color(0xFFA8949A),
    accent = Color(0xFFF0A3B8),
    progressTrack = Color(0xFF3A2F33),
    onAccent = Color(0xFF2A1219),
    isLight = false
)

val sakuraLightThemeTokens = AppThemeTokens(
    background = Color(0xFFFCF5F6),
    card = Color(0xFFFFFFFF),
    textPrimary = Color(0xFF2B1D21),
    textMuted = Color(0xFF8A6F77),
    accent = Color(0xFFCF5A7B),
    progressTrack = Color(0xFFEFDFE3),
    onAccent = Color(0xFFFFFFFF),
    isLight = true
)

val aiDarkThemeTokens = AppThemeTokens(
    background = Color(0xFF12151F),
    card = Color(0xFF1B2030),
    textPrimary = Color(0xFFE8ECF8),
    textMuted = Color(0xFF96A0BD),
    accent = Color(0xFF8CA8FF),
    progressTrack = Color(0xFF2B3350),
    onAccent = Color(0xFF0E1430),
    isLight = false
)

val aiLightThemeTokens = AppThemeTokens(
    background = Color(0xFFF4F6FC),
    card = Color(0xFFFFFFFF),
    textPrimary = Color(0xFF18203A),
    textMuted = Color(0xFF66708F),
    accent = Color(0xFF3A5BD9),
    progressTrack = Color(0xFFDDE3F3),
    onAccent = Color(0xFFFFFFFF),
    isLight = true
)

val sunaDarkThemeTokens = AppThemeTokens(
    background = Color(0xFF1B1814),
    card = Color(0xFF26221D),
    textPrimary = Color(0xFFF1EBE0),
    textMuted = Color(0xFFA39A8A),
    accent = Color(0xFFE3A95C),
    progressTrack = Color(0xFF3A342B),
    onAccent = Color(0xFF2A1B08),
    isLight = false
)

val sunaLightThemeTokens = AppThemeTokens(
    background = Color(0xFFFAF6EE),
    card = Color(0xFFFFFFFF),
    textPrimary = Color(0xFF2A2418),
    textMuted = Color(0xFF85795F),
    accent = Color(0xFFB5721A),
    progressTrack = Color(0xFFE8DFCC),
    onAccent = Color(0xFFFFFFFF),
    isLight = true
)

