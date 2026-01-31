package org.example.project.data.app

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.flow.firstOrNull
import org.example.project.data.github.model.PullRequestResponseDocApiModel

class AppRemoteDataSourceImpl(
    private val githubClient: HttpClient,
    private val localDataSource: AppLocalDataSource,
) : AppRemoteDataSource {

    override suspend fun listOpenPullRequests(
        page: Int,
    ): List<PullRequestResponseDocApiModel> {
        val user = localDataSource.observeLoggedInUser().firstOrNull() ?: return emptyList()
        val repoRef = user.githubRepoRef ?: return emptyList()

        val prs: List<PullRequestResponseDocApiModel> =
            githubClient.get("repos/${repoRef.owner}/${repoRef.repo}/pulls") {
                bearerAuth(user.accessToken)
                parameter("state", "open")
                parameter("sort", "created")
                parameter("direction", "desc")
                parameter("per_page", 100)
                parameter("page", page.coerceAtLeast(1))
            }.body()

        return prs
    }

}

