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

class AppLocalDataSourceImpl(
    private val preferencesDataStore: DataStore<Preferences>
) : AppLocalDataSource {
    private val repositoryLinkKey = stringPreferencesKey("repository_link")
    private val accessTokenKey = stringPreferencesKey("access_token")
    private val githubUsernameKey = stringPreferencesKey("github_username")

    override fun observeLoggedInUser(): Flow<LoggedInUser?> =
        preferencesDataStore.data.map { prefs ->
            val repo = prefs[repositoryLinkKey]
            val token = prefs[accessTokenKey]
            val username = prefs[githubUsernameKey]
            if (repo.isNullOrBlank() || token.isNullOrBlank() || username.isNullOrBlank()) {
                null
            } else {
                LoggedInUser(
                    repositoryLink = repo,
                    accessToken = token,
                    githubUsername = username,
                )
            }
        }

    override suspend fun setLoggedInUser(user: LoggedInUser?) {
        preferencesDataStore.edit { prefs ->
            if (user == null) {
                prefs.remove(repositoryLinkKey)
                prefs.remove(accessTokenKey)
                prefs.remove(githubUsernameKey)
            } else {
                prefs[repositoryLinkKey] = user.repositoryLink
                prefs[accessTokenKey] = user.accessToken
                prefs[githubUsernameKey] = user.githubUsername
            }
        }
    }
}

