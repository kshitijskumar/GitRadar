package org.example.project.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import okio.Path
import okio.Path.Companion.toPath
import org.example.project.util.PlatformContext
import java.io.File

actual fun createUserDataStore(platformContext: PlatformContext): DataStore<Preferences> {
    return createDataStore {
        // NOTE: System.getProperty does NOT expand "~", so use user.home explicitly.
        val home = System.getProperty("user.home") ?: "."
        val dir = File(home, "Library/Application Support/GitRadar")
        dir.mkdirs()
        val file = File(dir, USER_DATA_STORE_NAME)
        file.absolutePath
    }
}