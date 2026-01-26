package org.example.project.data.app

import kotlinx.coroutines.flow.Flow
import org.example.project.data.model.LoggedInUser
import org.example.project.data.model.PullRequest

/**
 * Single app data source:
 * - persists [LoggedInUser] locally (DataStore Preferences)
 * - uses stored user to call GitHub REST APIs
 */
interface AppLocalDataSource {
    fun observeLoggedInUser(): Flow<LoggedInUser?>
    suspend fun setLoggedInUser(user: LoggedInUser?)
}

