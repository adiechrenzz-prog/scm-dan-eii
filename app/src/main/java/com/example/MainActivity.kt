package com.example

import android.content.Intent
import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.model.EiSeedDefaults
import com.example.data.repository.EiTeamRepository
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.GoogleSignInHelper
import com.example.ui.components.SinercoWatermarkBackdrop
import com.example.ui.screens.EiTeamScreen
import com.example.ui.theme.CobaltPrimary
import com.example.ui.theme.EiTeamTheme
import com.example.ui.viewmodel.ActiveModal
import com.example.ui.viewmodel.EiTeamViewModel
import com.example.util.WhatsAppReminderManager
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private var openWaReminderPending by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(
                AndroidColor.TRANSPARENT,
                AndroidColor.TRANSPARENT
            )
        )
        WhatsAppReminderManager.ensureScheduledIfEnabled(this)
        openWaReminderPending = shouldOpenWaReminder(intent)

        setContent {
            EiTeamTheme {
                AppNavigation(
                    openWaReminderPending = openWaReminderPending,
                    onConsumeOpenWaReminder = { openWaReminderPending = false }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (shouldOpenWaReminder(intent)) {
            openWaReminderPending = true
        }
    }

    private fun shouldOpenWaReminder(intent: Intent?): Boolean =
        intent?.getBooleanExtra(WhatsAppReminderManager.EXTRA_OPEN_WA_REMINDER, false) == true
}

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

@Composable
fun AppNavigation(
    auth: FirebaseAuth = Firebase.auth,
    openWaReminderPending: Boolean = false,
    onConsumeOpenWaReminder: () -> Unit = {}
) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    if (user == null) {
        AuthScreen(onAuthSuccess = {})
    } else {
        val uid = user.uid
        val context = LocalContext.current
        val appContext = context.applicationContext
        val userEmail = user.email.orEmpty()
        val resolvedAccountName = user.displayName
            ?.takeIf { it.isNotBlank() }
            ?: userEmail.substringBefore('@').takeIf { it.isNotBlank() }
            ?: EiSeedDefaults.SUPERVISOR_NAME
        val scope = rememberCoroutineScope()

        val repository by produceState<EiTeamRepository?>(initialValue = null, key1 = uid) {
            value = withContext(Dispatchers.IO) {
                val databaseId = appContext.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                EiTeamRepository(db)
            }
        }

        val readyRepository = repository
        if (readyRepository == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                SinercoWatermarkBackdrop()
                CircularProgressIndicator(color = CobaltPrimary)
            }
        } else {
            key(uid) {
                val viewModel: EiTeamViewModel = viewModel(
                    key = uid,
                    factory = viewModelFactory {
                        initializer {
                            EiTeamViewModel(
                                repository = readyRepository,
                                currentUserId = uid,
                                userDisplayName = resolvedAccountName
                            )
                        }
                    }
                )
                LaunchedEffect(openWaReminderPending) {
                    if (openWaReminderPending) {
                        viewModel.openModal(ActiveModal.WhatsAppReminder)
                        onConsumeOpenWaReminder()
                    }
                }
                EiTeamScreen(
                    viewModel = viewModel,
                    userEmail = userEmail,
                    defaultAccountName = resolvedAccountName,
                    onSignOutRequested = {
                        val credentialManager = CredentialManager.create(context)
                        GoogleSignInHelper.signOut(credentialManager, scope)
                    }
                )
            }
        }
    }
}
