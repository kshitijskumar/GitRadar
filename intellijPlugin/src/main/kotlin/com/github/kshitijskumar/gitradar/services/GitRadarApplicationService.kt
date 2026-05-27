package com.github.kshitijskumar.gitradar.services

import com.github.kshitijskumar.gitradar.credentials.PasswordSafeCredentialStore
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@Service(Service.Level.APP)
class GitRadarApplicationService : Disposable {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val credentialStore = PasswordSafeCredentialStore()

    val accountFlow: StateFlow<AccountCredentials?> = combine(
        credentialStore.observe(KEY_PAT),
        credentialStore.observe(KEY_USERNAME),
    ) { pat, username ->
        if (pat.isNullOrBlank() || username.isNullOrBlank()) null
        else AccountCredentials(username = username, pat = pat)
    }.stateIn(
        scope = serviceScope,
        started = SharingStarted.Eagerly,
        initialValue = null,
    )

    suspend fun saveAccount(username: String, pat: String) {
        credentialStore.put(KEY_PAT, pat)
        credentialStore.put(KEY_USERNAME, username)
    }

    suspend fun clearAccount() {
        credentialStore.remove(KEY_PAT)
        credentialStore.remove(KEY_USERNAME)
    }

    override fun dispose() {
        serviceScope.cancel()
    }

    data class AccountCredentials(val username: String, val pat: String)

    companion object {
        private const val KEY_PAT = "github-pat"
        private const val KEY_USERNAME = "github-username"

        fun getInstance(): GitRadarApplicationService =
            ApplicationManager.getApplication().getService(GitRadarApplicationService::class.java)
    }
}
