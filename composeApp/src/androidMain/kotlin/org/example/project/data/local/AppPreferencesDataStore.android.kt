package org.example.project.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import okio.Path
import okio.Path.Companion.toPath
import org.example.project.util.PlatformContext


actual fun createUserDataStore(platformContext: PlatformContext): DataStore<Preferences> {
    return createDataStore(
        producePath = { platformContext.context.filesDir.resolve(USER_DATA_STORE_NAME).absolutePath }
    )
}