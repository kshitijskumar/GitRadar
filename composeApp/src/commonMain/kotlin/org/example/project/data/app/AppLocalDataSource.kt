package org.example.project.data.app

import kotlinx.coroutines.flow.Flow
import org.example.project.data.local.db.Pr_resolution
import org.example.project.data.model.LoggedInUser
import org.example.project.data.model.RecentLogin

/**
 * Single app data source:
 * - persists [LoggedInUser] locally (DataStore Preferences)
 * - uses stored user to call GitHub REST APIs
 */
interface AppLocalDataSource {
    fun observeLoggedInUser(): Flow<LoggedInUser?>
    suspend fun setLoggedInUser(user: LoggedInUser?)

    fun observeRecentLogins(): Flow<List<RecentLogin>>
    suspend fun addRecentLogin(login: RecentLogin)

    fun getAllPullRequestsData(
        repoOwner: String,
        repoName: String,
    ): Flow<List<Pr_resolution>>

    suspend fun markResolvedAt(
        prId: Long,
        timeInMillis: Long,
        repoName: String,
        repoOwner: String
    )

    suspend fun delete(prId: Long)
}

