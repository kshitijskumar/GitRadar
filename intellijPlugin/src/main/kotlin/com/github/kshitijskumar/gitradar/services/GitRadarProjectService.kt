package com.github.kshitijskumar.gitradar.services

import com.github.kshitijskumar.gitradar.git.RepoDetector
import com.github.kshitijskumar.gitradar.store.PluginLoggedInUserStore
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.example.project.data.app.AppLocalDataSource
import org.example.project.data.app.AppLocalDataSourceImpl
import org.example.project.data.app.AppRemoteDataSourceImpl
import org.example.project.data.local.db.GitRadarDatabaseFactory
import org.example.project.data.local.prefs.CredentialStore
import org.example.project.data.pulls.PullRequestsManager
import org.example.project.util.PlatformContext

@Service(Service.Level.PROJECT)
class GitRadarProjectService(project: Project) : Disposable {

    val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val repoDetector = RepoDetector(project, this)

    private val appService = GitRadarApplicationService.getInstance()

    private val pluginUserStore = PluginLoggedInUserStore(
        accountFlow = appService.accountFlow,
        detectedRepoFlow = repoDetector.detectedRepo,
    )

    private val database = GitRadarDatabaseFactory.getGitRadarDatabase(PlatformContext())

    val localDataSource: AppLocalDataSource = AppLocalDataSourceImpl(
        loggedInUserStore = pluginUserStore,
        credentialStore = NoOpCredentialStore,
        database = database,
    )

    private val remoteDataSource = AppRemoteDataSourceImpl.create(localDataSource)

    val pullRequestsManager = PullRequestsManager(
        localDataSource = localDataSource,
        remoteDataSource = remoteDataSource,
    )

    override fun dispose() {
        pullRequestsManager.clear()
        serviceScope.cancel()
    }

    companion object {
        fun getInstance(project: Project): GitRadarProjectService =
            project.getService(GitRadarProjectService::class.java)
    }
}

private object NoOpCredentialStore : CredentialStore {
    override fun observe(key: String): Flow<String?> = flowOf(null)
    override suspend fun get(key: String): String? = null
    override suspend fun put(key: String, value: String) = Unit
    override suspend fun remove(key: String) = Unit
    override suspend fun clear() = Unit
}
