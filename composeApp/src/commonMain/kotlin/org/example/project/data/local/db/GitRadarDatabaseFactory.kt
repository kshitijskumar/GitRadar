package org.example.project.data.local.db

import app.cash.sqldelight.db.SqlDriver
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.example.project.util.PlatformContext
import kotlin.concurrent.Volatile

expect fun createGitRadarSqlDriver(platformContext: PlatformContext): SqlDriver

object GitRadarDatabaseFactory {

    @Volatile
    private var db: GitRadarDatabase? = null
    private val mutex = Mutex()

    fun getGitRadarDatabase(platformContext: PlatformContext) : GitRadarDatabase = runBlocking {
        if (db != null) {
            return@runBlocking db!!
        }

        return@runBlocking mutex.withLock {
            var db = this@GitRadarDatabaseFactory.db
            if (db != null) {
                return@withLock db
            }

            db = createGitRadarDatabase(platformContext)
            this@GitRadarDatabaseFactory.db = db
            return@withLock db
        }
    }

}

private fun createGitRadarDatabase(platformContext: PlatformContext): GitRadarDatabase {
    return GitRadarDatabase(createGitRadarSqlDriver(platformContext))
}

