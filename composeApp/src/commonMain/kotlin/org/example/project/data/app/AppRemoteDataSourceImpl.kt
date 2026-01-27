package org.example.project.data.app

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.flow.first
import org.example.project.data.github.model.PullRequestResponseApiModel
import org.example.project.data.github.model.toDomain
import org.example.project.data.model.LoggedInUser
import org.example.project.data.model.PullRequest

class AppRemoteDataSourceImpl(
    private val githubClient: HttpClient
) : AppRemoteDataSource {

    override suspend fun listOpenPullRequests(
        user: LoggedInUser,
        page: Int,
        perPage: Int
    ): List<PullRequest> {
        val repoRef = user.githubRepoRef ?: return emptyList()

        val prs: List<PullRequestResponseApiModel> =
            githubClient.get("repos/${repoRef.owner}/${repoRef.repo}/pulls") {
                bearerAuth(user.accessToken)
                parameter("state", "open")
                parameter("sort", "created")
                parameter("direction", "desc")
                parameter("per_page", perPage.coerceIn(1, 100))
                parameter("page", page.coerceAtLeast(1))
            }.body()

        return prs.map { it.toDomain() }
    }

}

