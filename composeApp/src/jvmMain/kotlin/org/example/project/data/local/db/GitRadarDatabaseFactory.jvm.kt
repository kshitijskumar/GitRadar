package org.example.project.data.local.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import org.example.project.util.PlatformContext
import java.io.File

actual fun createGitRadarSqlDriver(platformContext: PlatformContext): SqlDriver {
    val dbFile = File("gitradar.db")
    val isNew = !dbFile.exists()
    val driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")

    if (isNew) {
        GitRadarDatabase.Schema.create(driver)
    }

    return driver
}

