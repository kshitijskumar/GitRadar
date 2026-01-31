package org.example.project.data.local.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import org.example.project.util.PlatformContext
import java.io.File

actual fun createGitRadarSqlDriver(platformContext: PlatformContext): SqlDriver {
    val home = System.getProperty("user.home")
    val fileName = "gitradar.db"
    val filePath = "$home/Library/Application Support/$fileName"
    val dbFile = File(filePath)
    val isNew = !dbFile.exists()
    val driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")

    if (isNew) {
        GitRadarDatabase.Schema.create(driver)
    }

    return driver
}

