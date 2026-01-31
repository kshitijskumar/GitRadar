package org.example.project.data.pulls

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.example.project.data.app.AppLocalDataSource
import org.example.project.data.app.AppRemoteDataSource
import org.example.project.data.github.model.PullRequestResponseDocApiModel
import org.example.project.screens.dashboard.PullRequestStatus

class PullRequestsManager(
    private val localDataSource: AppLocalDataSource,
    private val remoteDataSource: AppRemoteDataSource,
) {
    private val _pullRequests = MutableStateFlow<List<PullRequestResponseDocApiModel>>(emptyList())

    /**
     * Fetches PRs once and updates the cached list, which drives the derived flows.
     *
     * @return nullable error message if the fetch fails, else null.
     */
    suspend fun fetchPullRequests(): String? {
        return runCatching { remoteDataSource.listOpenPullRequests(page = 1) }
            .onSuccess { prs -> _pullRequests.update { prs } }
            .exceptionOrNull()
            ?.message
            ?: run {
                // Success
                null
            }
    }

    /**
     * PRs authored by the currently logged-in user (`LoggedInUser.githubUsername`).
     */
    fun currentUsersPullRequests(): Flow<List<PullRequestAppModel>> =
        combine(
            localDataSource.observeLoggedInUser(),
            _pullRequests.asStateFlow(),
        ) { user, prs ->
            val me = user?.githubUsername
            if (me.isNullOrBlank()) return@combine emptyList()
            prs.filter { it.user.login == me }
        }
            .map { list ->
                list.map {
                    PullRequestAppModel(
                        pr = it,
                        status = resolvePRStatus(it)
                    )
                }
            }

    /**
     * PRs where the currently logged-in user is either:
     * - in `assignees`, OR
     * - in `requested_reviewers`
     */
    fun pullRequestsForReview(): Flow<List<PullRequestAppModel>> =
        combine(
            localDataSource.observeLoggedInUser(),
            _pullRequests.asStateFlow(),
        ) { user, prs ->
            val me = user?.githubUsername
            if (me.isNullOrBlank()) return@combine emptyList()

            prs.filter { pr ->
                pr.assignees.any { it.login == me } ||
                    pr.requestedReviewers.any { it.login == me }
            }
        }
            .map { list ->
                list.map {
                    PullRequestAppModel(
                        pr = it,
                        status = resolvePRStatus(it)
                    )
                }
            }

    private fun resolvePRStatus(
        pr: PullRequestResponseDocApiModel
    ): PullRequestStatus {
        return if (pr.draft) PullRequestStatus.DRAFT else PullRequestStatus.NEEDS_ATTENTION
    }

    fun clear() {
        _pullRequests.update { listOf() }
    }
}

