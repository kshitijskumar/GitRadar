package org.example.project.data.local.db

import app.cash.sqldelight.db.SqlDriver
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import org.example.project.util.PlatformContext
import kotlin.concurrent.Volatile

expect fun createGitRadarSqlDriver(platformContext: PlatformContext): SqlDriver

object GitRadarDatabaseFactory: SynchronizedObject() {

    @Volatile
    private var db: GitRadarDatabase? = null

    fun getGitRadarDatabase(platformContext: PlatformContext) : GitRadarDatabase {
        if (db != null) {
            return db!!
        }

        return synchronized(this) {
            var db = this.db
            if (db != null) {
                return@synchronized db
            }

            db = createGitRadarDatabase(platformContext)
            this.db = db
            return@synchronized db
        }
    }

}

private fun createGitRadarDatabase(platformContext: PlatformContext): GitRadarDatabase {
    return GitRadarDatabase(createGitRadarSqlDriver(platformContext))
}

