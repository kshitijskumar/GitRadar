package org.example.project.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.data.app.AppLocalDataSource
import org.example.project.data.github.model.PullRequestResponseDocApiModel
import org.example.project.data.pulls.PullRequestAppModel
import org.example.project.data.pulls.PullRequestsManager
import org.example.project.screens.base.SnackbarErrorMessage
import kotlin.time.Instant

class DashboardViewModel(
    private val localDataSource: AppLocalDataSource,
    private val pullRequestsManager: PullRequestsManager,
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    private var pullRequestsCollectionJob: Job? = null

    fun initialise() {
        startPullRequestsCollectorsIfNeeded()
        fetchPullRequests()

        viewModelScope.launch {
            val user = localDataSource.observeLoggedInUser().firstOrNull()
            val repoRef = user?.githubRepoRef
            val username = user?.githubUsername

            val title =
                when {
                    username != null && repoRef != null -> "$username • ${repoRef.owner}/${repoRef.repo}"
                    repoRef != null -> "${repoRef.owner}/${repoRef.repo}"
                    username != null -> username
                    else -> "Dashboard"
                }

            _state.update { it.copy(title = title) }
        }
    }

    fun handleTabSelected(tab: DashboardTab) {
        _state.update { it.copy(selectedTab = tab) }
    }

    fun handleRefreshClicked() {
        // UI should disable the button, but keep a guard here too.
        if (state.value.isPullRequestsLoading) return
        fetchPullRequests()
    }

    private fun startPullRequestsCollectorsIfNeeded() {
        if (pullRequestsCollectionJob != null) return

        pullRequestsCollectionJob = viewModelScope.launch {
            launch {
                pullRequestsManager.currentUsersPullRequests().collect { prs ->
                    _state.update {
                        it.copy(
                            myPullRequests = prs.map { pr -> pr.toDashboardItem() }
                        )
                    }
                }
            }

            launch {
                pullRequestsManager.pullRequestsForReview().collect { prs ->
                    _state.update {
                        it.copy(
                            pullRequestsForReview = prs.map { pr -> pr.toDashboardItem() }
                        )
                    }
                }
            }
        }
    }

    private fun PullRequestAppModel.toDashboardItem(): DashboardPullRequestItem {
        return DashboardPullRequestItem(
            prId = this.pr.id,
            title = this.pr.title,
            authorLogin = this.pr.user.login,
            browserUrl = this.pr.htmlUrl,
            updatedAt = this.pr.updatedAt,
            status = this.status,
        )
    }

    private fun fetchPullRequests() {
        if (state.value.isPullRequestsLoading) return

        viewModelScope.launch {
            _state.update { it.copy(isPullRequestsLoading = true, errorMessage = null) }
            val error = pullRequestsManager.fetchPullRequests()
            _state.update {
                it.copy(
                    isPullRequestsLoading = false,
                    errorMessage = error?.let { SnackbarErrorMessage(it) },
                )
            }
        }
    }

    fun handleLogoutClicked() {
        _state.update {
            it.copy(
                dialogType = DashboardDialogType.LogoutConfirmationDialog,
                errorMessage = null
            )
        }
    }

    fun handleLogoutCancelled() {
        _state.update { it.copy(dialogType = null) }
    }

    fun handleLogoutConfirmed() {
        if (state.value.isLoading) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, dialogType = null, errorMessage = null) }
            runCatching { localDataSource.setLoggedInUser(null) }
                .onSuccess {
                    _state.update { it.copy(isLoading = false) }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = SnackbarErrorMessage(e.message ?: "Failed to logout"),
                        )
                    }
                }
        }
    }

    fun markUnmarkResolved(pr: DashboardPullRequestItem) {
        viewModelScope.launch {
            when(pr.status) {
                PullRequestStatus.DRAFT -> {
                    // no concept of resolved/unresolved from drafts
                    return@launch
                }
                PullRequestStatus.NEEDS_ATTENTION -> {
                    val instant = Instant.parseOrNull(pr.updatedAt) ?: return@launch
                    val repo = localDataSource.observeLoggedInUser().firstOrNull()?.githubRepoRef ?: return@launch
                    localDataSource.markResolvedAt(
                        prId = pr.prId,
                        timeInMillis = instant.toEpochMilliseconds(),
                        repoName = repo.repo,
                        repoOwner = repo.owner
                    )
                }
                PullRequestStatus.RESOLVED -> {
                    localDataSource.delete(pr.prId)
                }
            }
        }
    }

    fun resetViewModel() {
        pullRequestsCollectionJob?.cancel()
        pullRequestsCollectionJob = null
        _state.update { DashboardState() }
        pullRequestsManager.clear()
    }

    companion object {
        private const val MINUTES_5_IN_MILLIS = 5 * 60 * 1000
    }
}

