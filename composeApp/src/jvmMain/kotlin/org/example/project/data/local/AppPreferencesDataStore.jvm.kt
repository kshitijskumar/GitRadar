package org.example.project.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import okio.Path
import okio.Path.Companion.toPath
import org.example.project.util.PlatformContext
import java.io.File

actual fun createUserDataStore(platformContext: PlatformContext): DataStore<Preferences> {
    return createDataStore {
        val file = File(System.getProperty("~/Library/Application Support/GitRadar"), USER_DATA_STORE_NAME)
        file.absolutePath
    }
}