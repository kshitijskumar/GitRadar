package com.github.kshitijskumar.gitradar.store

import com.github.kshitijskumar.gitradar.services.GitRadarApplicationService.AccountCredentials
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import org.example.project.data.local.prefs.LoggedInUserStore
import org.example.project.data.model.GithubRepoRef
import org.example.project.data.model.LoggedInUser

class PluginLoggedInUserStore(
    private val accountFlow: Flow<AccountCredentials?>,
    private val detectedRepoFlow: StateFlow<GithubRepoRef?>,
) : LoggedInUserStore {

    override fun observe(): Flow<LoggedInUser?> = combine(
        accountFlow,
        detectedRepoFlow,
    ) { creds, repo ->
        if (creds == null || repo == null) null
        else LoggedInUser(
            repositoryLink = "https://github.com/${repo.owner}/${repo.repo}",
            accessToken = creds.pat,
            githubUsername = creds.username,
        )
    }

    // Credentials are managed via settings — writes are intentionally no-ops here.
    override suspend fun set(user: LoggedInUser?) = Unit
}
