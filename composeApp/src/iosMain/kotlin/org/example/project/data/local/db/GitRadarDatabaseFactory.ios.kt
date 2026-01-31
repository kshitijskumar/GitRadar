package org.example.project.data.local.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import org.example.project.util.PlatformContext

actual fun createGitRadarSqlDriver(platformContext: PlatformContext): SqlDriver {
    // Uses the default app documents directory on Apple platforms.
    return NativeSqliteDriver(
        schema = GitRadarDatabase.Schema,
        name = "gitradar.db",
    )
}

