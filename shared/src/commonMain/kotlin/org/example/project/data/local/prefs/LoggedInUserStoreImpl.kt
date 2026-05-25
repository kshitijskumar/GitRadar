package org.example.project.data.local.prefs

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import org.example.project.data.model.LoggedInUser
import org.example.project.serialization.AppJson
import org.example.project.serialization.decodeFromStringSafely

class LoggedInUserStoreImpl(
    private val credentialStore: CredentialStore,
) : LoggedInUserStore {

    override fun observe(): Flow<LoggedInUser?> =
        credentialStore.observe(KEY_USER_DETAILS).map { raw ->
            AppJson.decodeFromStringSafely<LoggedInUser>(raw ?: "")
        }

    override suspend fun set(user: LoggedInUser?) {
        if (user == null) credentialStore.remove(KEY_USER_DETAILS)
        else credentialStore.put(KEY_USER_DETAILS, AppJson.encodeToString(user))
    }

    companion object {
        private const val KEY_USER_DETAILS = "user_details"
    }
}
