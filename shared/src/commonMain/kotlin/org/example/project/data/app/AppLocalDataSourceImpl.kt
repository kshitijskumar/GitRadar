package org.example.project.data.app

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import org.example.project.data.local.db.GitRadarDatabase
import org.example.project.data.local.db.Pr_resolution
import org.example.project.data.local.prefs.CredentialStore
import org.example.project.data.local.prefs.LoggedInUserStore
import org.example.project.data.model.LoggedInUser
import org.example.project.data.model.RecentLogin
import org.example.project.serialization.AppJson
import org.example.project.serialization.decodeFromStringSafely

class AppLocalDataSourceImpl(
    private val loggedInUserStore: LoggedInUserStore,
    private val credentialStore: CredentialStore,
    private val database: GitRadarDatabase,
) : AppLocalDataSource {

    override fun observeLoggedInUser(): Flow<LoggedInUser?> =
        loggedInUserStore.observe()

    override suspend fun setLoggedInUser(user: LoggedInUser?) =
        loggedInUserStore.set(user)

    override fun observeRecentLogins(): Flow<List<RecentLogin>> =
        credentialStore.observe(KEY_RECENT_LOGINS).map { raw ->
            AppJson.decodeFromStringSafely<List<RecentLogin>>(raw ?: "") ?: emptyList()
        }

    override suspend fun addRecentLogin(login: RecentLogin) {
        val existing = AppJson
            .decodeFromStringSafely<List<RecentLogin>>(credentialStore.get(KEY_RECENT_LOGINS) ?: "")
            .orEmpty()
        val updated = buildList {
            add(login)
            existing
                .asSequence()
                .filterNot { it.repositoryLink == login.repositoryLink && it.githubUsername == login.githubUsername }
                .take(10)
                .forEach { add(it) }
        }
        credentialStore.put(KEY_RECENT_LOGINS, AppJson.encodeToString(updated))
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

    companion object {
        private const val KEY_RECENT_LOGINS = "recent_logins"
    }
}
