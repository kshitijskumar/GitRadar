package org.example.project.data.local.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.example.project.util.PlatformContext

actual fun createGitRadarSqlDriver(platformContext: PlatformContext): SqlDriver {
    return AndroidSqliteDriver(
        schema = GitRadarDatabase.Schema,
        context = platformContext.context,
        name = "gitradar.db",
    )
}

