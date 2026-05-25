package org.example.project.data.local.prefs

import kotlinx.coroutines.flow.Flow

interface CredentialStore {
    fun observe(key: String): Flow<String?>
    suspend fun get(key: String): String?
    suspend fun put(key: String, value: String)
    suspend fun remove(key: String)
    suspend fun clear()
}
