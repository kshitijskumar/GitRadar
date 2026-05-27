package org.example.project.data.local.prefs

import kotlinx.coroutines.flow.Flow
import org.example.project.data.model.LoggedInUser

interface LoggedInUserStore {
    fun observe(): Flow<LoggedInUser?>
    suspend fun set(user: LoggedInUser?)
}
