package org.example.project.data.app

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import org.example.project.data.github.model.PullRequestResponseApiModel
import org.example.project.data.github.model.toDomain
import org.example.project.data.model.LoggedInUser
import org.example.project.data.model.PullRequest
import org.example.project.serialization.AppJson
import org.example.project.serialization.decodeFromStringSafely

class AppLocalDataSourceImpl(
    private val preferencesDataStore: DataStore<Preferences>
) : AppLocalDataSource {
    private val userDetailKey = stringPreferencesKey("user_details")

    override fun observeLoggedInUser(): Flow<LoggedInUser?> =
        preferencesDataStore.data.map { prefs ->
            val userString = prefs[userDetailKey] ?: ""
            AppJson.decodeFromStringSafely<LoggedInUser>(userString)
        }

    override suspend fun setLoggedInUser(user: LoggedInUser?) {
        preferencesDataStore.edit { prefs ->
            if (user == null) {
                prefs.remove(userDetailKey)
            } else {
                prefs[userDetailKey] = AppJson.encodeToString(user)
            }
        }
    }
}

