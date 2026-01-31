package org.example.project.data.pulls

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.example.project.data.app.AppLocalDataSource
import org.example.project.data.app.AppRemoteDataSource
import org.example.project.data.github.model.PullRequestResponseDocApiModel
import org.example.project.screens.dashboard.PullRequestStatus
import kotlin.time.Instant

class PullRequestsManager(
    private val localDataSource: AppLocalDataSource,
    private val remoteDataSource: AppRemoteDataSource,
) {
    private val _pullRequests = MutableStateFlow<List<PullRequestResponseDocApiModel>>(emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val pullResolutions = localDataSource
        .observeLoggedInUser()
        .flatMapLatest { user ->
            val repoInfo = user?.githubRepoRef
            if (user == null || user.githubUsername.trim().isEmpty() || repoInfo == null) {
                flowOf(listOf())
            } else {
                localDataSource.getAllPullRequestsData(
                    repoName = repoInfo.repo,
                    repoOwner = repoInfo.owner
                )
            }
        }

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
    fun currentUsersPullRequests(): Flow<List<PullRequestAppModel>> {
        return combine(
            localDataSource.observeLoggedInUser(),
            _pullRequests.asStateFlow(),
        ) { user, prs ->
            val me = user?.githubUsername
            if (me.isNullOrBlank()) return@combine emptyList()
            prs.filter { it.user.login == me }
        }
            .resolveStatus()

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
            .resolveStatus()

    private fun Flow<List<PullRequestResponseDocApiModel>>.resolveStatus(): Flow<List<PullRequestAppModel>> {
        return combine(
            this,
            flow2 = pullResolutions
        ) { prs, resolutions ->
            val resolutionMap = resolutions.associateBy { it.prId }
            prs.map { pr ->
                if (pr.draft) {
                    return@map PullRequestAppModel(
                        pr = pr,
                        status = PullRequestStatus.DRAFT
                    )
                }

                val lastUpdatedAt = Instant.parseOrNull(pr.updatedAt) ?: Instant.parseOrNull(pr.createdAt)
                if (lastUpdatedAt == null) {
                    return@map PullRequestAppModel(
                        pr = pr,
                        status = PullRequestStatus.NEEDS_ATTENTION
                    )
                }

                val resolutionInfo = resolutionMap[pr.id]

                if (resolutionInfo == null) {
                    return@map PullRequestAppModel(
                        pr = pr,
                        status = PullRequestStatus.NEEDS_ATTENTION
                    )
                }

                val lastResolvedAt = Instant.fromEpochMilliseconds(resolutionInfo.lastResolvedAtMillis)

                if (lastUpdatedAt > lastResolvedAt) {
                    PullRequestAppModel(
                        pr = pr,
                        status = PullRequestStatus.NEEDS_ATTENTION
                    )
                } else {
                    PullRequestAppModel(
                        pr = pr,
                        status = PullRequestStatus.RESOLVED
                    )
                }
            }
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

