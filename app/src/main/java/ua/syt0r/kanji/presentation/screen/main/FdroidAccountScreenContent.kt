package ua.syt0r.kanji.presentation.screen.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import ua.syt0r.kanji.core.AccountManager
import ua.syt0r.kanji.core.AccountState
import ua.syt0r.kanji.core.ApiRequestIssue
import ua.syt0r.kanji.core.SubscriptionInfo
import ua.syt0r.kanji.core.format
import ua.syt0r.kanji.core.supabase.SupabaseAuthState
import ua.syt0r.kanji.core.supabase.SupabaseGoogleLoginOutcome
import ua.syt0r.kanji.core.supabase.SupabaseService
import ua.syt0r.kanji.presentation.common.CommonDateTimeFormat
import ua.syt0r.kanji.presentation.common.MultiplatformBackHandler
import ua.syt0r.kanji.presentation.common.clickable
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.theme.appCardBorder
import ua.syt0r.kanji.presentation.common.theme.appColors
import ua.syt0r.kanji.presentation.getMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.FdroidAccountScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.account.AccountScreenContract

object FdroidAccountScreenContent : AccountScreenContract.Content {

    @Composable
    override fun invoke(
        state: MainNavigationState,
        data: AccountScreenContract.ScreenData?
    ) {
        val viewModel = getMultiplatformViewModel<FdroidAccountScreenContract.ViewModel>()
        val uriHandler = LocalUriHandler.current

        LaunchedEffect(data) {
            if (data != null) viewModel.signIn(data)
        }

        FdroidAccountScreenUI(
            state = viewModel.state.collectAsState(),
            supabaseState = viewModel.supabaseState.collectAsState(),
            isMigratingLegacy = viewModel.isMigratingLegacy.collectAsState(),
            legacyMigrationMessage = viewModel.legacyMigrationMessage.collectAsState(),
            onUpClick = { state.navigateBack() },
            onGoogleSupabaseSignIn = {
                viewModel.signInWithGoogleSupabase { url -> uriHandler.openUri(url) }
            },
            onSupabaseUpload = { viewModel.uploadSupabaseBackup() },
            onSupabaseRestore = { viewModel.restoreSupabaseBackup() },
            onSupabaseSignOut = { viewModel.signOutSupabase() },
            onLegacySignInClick = { uriHandler.openUri(AccountScreenContract.DEEP_LINK_AUTH_URL) },
            onLegacySignOutClick = { viewModel.signOut() },
            onLegacyMigrateClick = { viewModel.migrateLegacyToSupabase() },
            onLegacyRefreshClick = { viewModel.refresh() }
        )
    }

}

interface FdroidAccountScreenContract {

    interface ViewModel {
        val state: StateFlow<ScreenState>
        val supabaseState: StateFlow<SupabaseAuthState>
        val isMigratingLegacy: StateFlow<Boolean>
        val legacyMigrationMessage: StateFlow<String?>
        fun signIn(data: AccountScreenContract.ScreenData)
        fun signOut()
        fun refresh()
        fun signInWithGoogleSupabase(onOpenBrowser: (String) -> Unit)
        fun signOutSupabase()
        fun uploadSupabaseBackup()
        fun restoreSupabaseBackup()
        fun migrateLegacyToSupabase()
    }

    sealed interface ScreenState {
        data object Loading : ScreenState
        data object SignedOut : ScreenState
        data class SignedIn(
            val email: String,
            val subscriptionInfo: SubscriptionInfo,
            val issue: ApiRequestIssue?
        ) : ScreenState

        data class Error(
            val issue: ApiRequestIssue
        ) : ScreenState
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FdroidAccountScreenUI(
    state: State<ScreenState>,
    supabaseState: State<SupabaseAuthState>,
    isMigratingLegacy: State<Boolean>,
    legacyMigrationMessage: State<String?>,
    onUpClick: () -> Unit,
    onGoogleSupabaseSignIn: () -> Unit,
    onSupabaseUpload: () -> Unit,
    onSupabaseRestore: () -> Unit,
    onSupabaseSignOut: () -> Unit,
    onLegacySignInClick: () -> Unit,
    onLegacySignOutClick: () -> Unit,
    onLegacyMigrateClick: () -> Unit,
    onLegacyRefreshClick: () -> Unit
) {
    MultiplatformBackHandler(onBack = onUpClick)

    val appColors = MaterialTheme.appColors
    val cardBorder = appCardBorder()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(resolveString { account.title }) },
                navigationIcon = {
                    IconButton(
                        onClick = onUpClick,
                        modifier = Modifier.testTag("account_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .wrapContentWidth()
                .widthIn(max = 460.dp)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Primary Google + Supabase Section
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = appColors.card,
                border = cardBorder,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = resolveString { account.supabaseSectionTitle },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = appColors.textPrimary
                    )

                    Text(
                        text = resolveString { account.supabaseSectionDescription },
                        style = MaterialTheme.typography.bodySmall,
                        color = appColors.textMuted
                    )

                    when (val sState = supabaseState.value) {
                        SupabaseAuthState.Loading -> {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(color = appColors.accent)
                            }
                        }

                        SupabaseAuthState.NotConfigured,
                        is SupabaseAuthState.SignedOut -> {
                            val isConfigured = sState !is SupabaseAuthState.NotConfigured
                            val errorMsg = (sState as? SupabaseAuthState.SignedOut)?.lastError

                            Button(
                                onClick = onGoogleSupabaseSignIn,
                                enabled = isConfigured,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = appColors.accent,
                                    contentColor = appColors.onAccent
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 56.dp)
                                    .testTag("supabase_google_login_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.VerifiedUser,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.size(10.dp))
                                Text(
                                    text = resolveString { account.continueWithGoogleSupabase },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            if (!isConfigured) {
                                Text(
                                    text = resolveString { account.supabaseNotConfiguredHint },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = appColors.textMuted
                                )
                            } else if (!errorMsg.isNullOrBlank()) {
                                Text(
                                    text = errorMsg,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }

                            // Subtle small clickable text directly beneath the big login button for migrating old accounts
                            Text(
                                text = resolveString { account.legacyMigrationPrompt },
                                style = MaterialTheme.typography.labelMedium.copy(
                                    textDecoration = TextDecoration.Underline
                                ),
                                color = appColors.textMuted,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                                    .clickable(action = onLegacySignInClick)
                                    .padding(vertical = 6.dp)
                                    .testTag("legacy_migration_login_link")
                            )
                        }

                        is SupabaseAuthState.SignedIn -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Email,
                                    contentDescription = null,
                                    tint = appColors.accent
                                )
                                Column {
                                    Text(
                                        text = resolveString { account.supabaseSignedInLabel },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = appColors.textMuted
                                    )
                                    Text(
                                        text = sState.email,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = appColors.textPrimary
                                    )
                                }
                            }

                            sState.lastSyncTimestamp?.let { ts ->
                                val formatted = Instant.fromEpochMilliseconds(ts)
                                    .toLocalDateTime(TimeZone.currentSystemDefault())
                                    .format(CommonDateTimeFormat)
                                Text(
                                    text = resolveString { account.supabaseLastSyncTemplate }.format(formatted),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = appColors.textMuted
                                )
                            }

                            sState.syncMessage?.let { msg ->
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = appColors.accent
                                )
                            }
                            sState.syncError?.let { err ->
                                Text(
                                    text = err,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }

                            HorizontalDivider(color = appColors.progressTrack)

                            Button(
                                onClick = onSupabaseUpload,
                                enabled = !sState.isSyncing,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = appColors.accent,
                                    contentColor = appColors.onAccent
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .testTag("supabase_upload_button")
                            ) {
                                if (sState.isSyncing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = appColors.onAccent,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Outlined.CloudUpload, contentDescription = null)
                                }
                                Spacer(Modifier.size(8.dp))
                                Text(resolveString { account.supabaseUploadBackupButton })
                            }

                            OutlinedButton(
                                onClick = onSupabaseRestore,
                                enabled = !sState.isSyncing,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .testTag("supabase_restore_button")
                            ) {
                                Icon(Icons.Outlined.CloudDownload, contentDescription = null)
                                Spacer(Modifier.size(8.dp))
                                Text(resolveString { account.supabaseRestoreBackupButton })
                            }

                            TextButton(
                                onClick = onSupabaseSignOut,
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.Logout,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.size(6.dp))
                                Text(resolveString { account.signOutButton })
                            }

                            if (state.value is ScreenState.SignedOut) {
                                Text(
                                    text = resolveString { account.legacyMigrationPrompt },
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        textDecoration = TextDecoration.Underline
                                    ),
                                    color = appColors.textMuted,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(action = onLegacySignInClick)
                                        .padding(vertical = 6.dp)
                                        .testTag("legacy_migration_login_link_signed_in")
                                )
                            }
                        }
                    }
                }
            }

            // Legacy Account Migration Panel (shown when logged in to legacy account or in loading/error)
            when (val legacyState = state.value) {
                ScreenState.SignedOut -> Unit

                ScreenState.Loading -> {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = appColors.card,
                        border = cardBorder,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = appColors.accent,
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = resolveString { loading },
                                style = MaterialTheme.typography.bodyMedium,
                                color = appColors.textPrimary
                            )
                        }
                    }
                }

                is ScreenState.SignedIn -> {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = appColors.card,
                        border = cardBorder,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = resolveString { account.legacyMigrationBannerTitle },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = appColors.textPrimary
                            )
                            Text(
                                text = legacyState.email,
                                style = MaterialTheme.typography.bodyMedium,
                                color = appColors.textMuted
                            )

                            legacyState.issue?.let {
                                Text(
                                    text = it.toString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }

                            legacyMigrationMessage.value?.let { message ->
                                Text(
                                    text = message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = appColors.accent
                                )
                            }

                            Button(
                                onClick = onLegacyMigrateClick,
                                enabled = !isMigratingLegacy.value,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = appColors.progressTrack,
                                    contentColor = appColors.textPrimary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .testTag("legacy_migrate_to_supabase_button")
                            ) {
                                if (isMigratingLegacy.value) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = appColors.accent,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Outlined.Sync, contentDescription = null)
                                }
                                Spacer(Modifier.size(8.dp))
                                Text(resolveString { account.legacyMigrationPullAndSyncButton })
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                TextButton(onClick = onLegacyRefreshClick) {
                                    Text("Refresh")
                                }
                                TextButton(onClick = onLegacySignOutClick) {
                                    Text(resolveString { account.legacySignOutButton })
                                }
                            }
                        }
                    }
                }

                is ScreenState.Error -> {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = appColors.card,
                        border = cardBorder,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = resolveString { account.issueOtherTitle },
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = legacyState.issue.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                color = appColors.textMuted
                            )
                            TextButton(onClick = onLegacySignInClick) {
                                Text(resolveString { account.signInButton })
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }

}

class FdroidAccountScreenViewModel(
    private val coroutineScope: CoroutineScope,
    private val accountManager: AccountManager,
    private val supabaseService: SupabaseService
) : FdroidAccountScreenContract.ViewModel {

    private val _state = MutableStateFlow<ScreenState>(ScreenState.Loading)
    override val state: StateFlow<ScreenState> = _state

    override val supabaseState: StateFlow<SupabaseAuthState> = supabaseService.state

    private val _isMigratingLegacy = MutableStateFlow(false)
    override val isMigratingLegacy: StateFlow<Boolean> = _isMigratingLegacy

    private val _legacyMigrationMessage = MutableStateFlow<String?>(null)
    override val legacyMigrationMessage: StateFlow<String?> = _legacyMigrationMessage

    init {
        accountManager.state
            .onEach {
                _state.value = when (it) {
                    AccountState.Loading -> ScreenState.Loading
                    AccountState.LoggedOut -> ScreenState.SignedOut
                    is AccountState.LoggedIn -> ScreenState.SignedIn(
                        email = it.email,
                        subscriptionInfo = it.subscriptionInfo,
                        issue = it.issue
                    )

                    is AccountState.Error -> ScreenState.Error(it.issue)
                }
            }
            .launchIn(coroutineScope)
    }

    override fun signIn(data: AccountScreenContract.ScreenData) {
        accountManager.signIn(data.refreshToken, data.idToken)
    }

    override fun signOut() {
        accountManager.signOut()
    }

    override fun refresh() {
        accountManager.refreshUserData()
    }

    override fun signInWithGoogleSupabase(onOpenBrowser: (String) -> Unit) {
        coroutineScope.launch {
            when (supabaseService.signInWithGoogle()) {
                SupabaseGoogleLoginOutcome.OpenBrowserOAuth -> {
                    supabaseService.buildGoogleOAuthUrl()?.let(onOpenBrowser)
                }

                else -> Unit
            }
        }
    }

    override fun signOutSupabase() {
        coroutineScope.launch {
            supabaseService.signOut()
        }
    }

    override fun uploadSupabaseBackup() {
        coroutineScope.launch {
            supabaseService.uploadBackup()
        }
    }

    override fun restoreSupabaseBackup() {
        coroutineScope.launch {
            supabaseService.restoreBackup()
        }
    }

    override fun migrateLegacyToSupabase() {
        if (_isMigratingLegacy.value) return
        coroutineScope.launch {
            _isMigratingLegacy.value = true
            _legacyMigrationMessage.value = null
            val result = supabaseService.migrateFromLegacyAccount()
            _legacyMigrationMessage.value = result.getOrElse { it.message ?: "Migration failed" }
            _isMigratingLegacy.value = false
        }
    }

}
