package org.example.project.data.app

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.example.project.data.local.db.GitRadarDatabase
import org.example.project.data.local.db.Pr_resolution
import org.example.project.data.model.LoggedInUser
import org.example.project.data.model.RecentLogin
import org.example.project.serialization.AppJson
import org.example.project.serialization.decodeFromStringSafely

class AppLocalDataSourceImpl(
    private val preferencesDataStore: DataStore<Preferences>,
    private val database: GitRadarDatabase,
) : AppLocalDataSource {
    private val userDetailKey = stringPreferencesKey("user_details")
    private val recentLoginsKey = stringPreferencesKey("recent_logins")

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

    override fun observeRecentLogins(): Flow<List<RecentLogin>> =
        preferencesDataStore.data.map { prefs ->
            val raw = prefs[recentLoginsKey] ?: ""
            AppJson.decodeFromStringSafely<List<RecentLogin>>(raw) ?: emptyList()
        }

    override suspend fun addRecentLogin(login: RecentLogin) {
        preferencesDataStore.edit { prefs ->
            val existing = AppJson.decodeFromStringSafely<List<RecentLogin>>(prefs[recentLoginsKey] ?: "").orEmpty()

            // Deduplicate by repoLink + username and keep most recent first.
            val updated =
                buildList {
                    add(login)
                    existing
                        .asSequence()
                        .filterNot { it.repositoryLink == login.repositoryLink && it.githubUsername == login.githubUsername }
                        .take(10)
                        .forEach { add(it) }
                }

            prefs[recentLoginsKey] = AppJson.encodeToString(updated)
        }
    }

    override fun getAllPullRequestsData(
        repoOwner: String,
        repoName: String,
    ): Flow<List<Pr_resolution>> {
        return database.prResolutionQueries
            .selectAllData(
                repoOwner = repoOwner,
                repoName = repoName,
            )
            .asFlow()
            .mapToList(Dispatchers.IO)
    }

    override suspend fun markResolvedAt(
        prId: Long,
        timeInMillis: Long,
        repoName: String,
        repoOwner: String
    ) {
        withContext(Dispatchers.IO) {
            database.prResolutionQueries.upsertLastResolvedAtMillis(
                Pr_resolution(
                    prId = prId,
                    lastResolvedAtMillis = timeInMillis,
                    repoName = repoName,
                    repoOwner = repoOwner
                )
            )
        }
    }

    override suspend fun delete(prId: Long) {
        withContext(Dispatchers.IO) {
            database.prResolutionQueries.delete(prId)
        }
    }
}

