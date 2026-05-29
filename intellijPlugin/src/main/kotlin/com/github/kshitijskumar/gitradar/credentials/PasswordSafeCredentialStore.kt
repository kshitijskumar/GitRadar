package com.github.kshitijskumar.gitradar.credentials

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.diagnostic.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext
import org.example.project.data.local.prefs.CredentialStore
import java.util.concurrent.ConcurrentHashMap

class PasswordSafeCredentialStore : CredentialStore {

    private val log = Logger.getInstance(PasswordSafeCredentialStore::class.java)
    private val cache = ConcurrentHashMap<String, MutableStateFlow<String?>>()

    private fun attrs(key: String) = CredentialAttributes("GitRadar", key)

    private fun cacheFlowFor(key: String): MutableStateFlow<String?> =
        cache.computeIfAbsent(key) { MutableStateFlow(null) }

    override fun observe(key: String): Flow<String?> = callbackFlow {
        val initial = withContext(Dispatchers.IO) {
            PasswordSafe.instance.get(attrs(key))?.getPasswordAsString()
        }
        log.info("[GitKshitij1] observe($key): initial=${if (initial == null) "null" else "present"}")
        val flow = cacheFlowFor(key)
        flow.value = initial
        flow.collectLatest { send(it) }
        awaitClose()
    }

    override suspend fun get(key: String): String? = withContext(Dispatchers.IO) {
        val cached = cache[key]
        if (cached != null) {
            log.info("[GitKshitij1] get($key): returning cached=${if (cached.value == null) "null" else "present"}")
            return@withContext cached.value
        }
        val value = PasswordSafe.instance.get(attrs(key))?.getPasswordAsString()
        log.info("[GitKshitij1] get($key): loaded from PasswordSafe=${if (value == null) "null" else "present"}")
        cacheFlowFor(key).value = value
        value
    }

    override suspend fun put(key: String, value: String): Unit = withContext(Dispatchers.IO) {
        log.info("[GitKshitij1] put($key): saving to PasswordSafe")
        PasswordSafe.instance.set(attrs(key), Credentials(key, value))
        cacheFlowFor(key).value = value
        log.info("[GitKshitij1] put($key): done, cache updated")
    }

    override suspend fun remove(key: String): Unit = withContext(Dispatchers.IO) {
        log.info("[GitKshitij1] remove($key)")
        PasswordSafe.instance.set(attrs(key), null)
        cacheFlowFor(key).value = null
    }

    override suspend fun clear(): Unit = withContext(Dispatchers.IO) {
        cache.keys.toList().forEach { key ->
            PasswordSafe.instance.set(attrs(key), null)
            cache[key]?.value = null
        }
    }
}
