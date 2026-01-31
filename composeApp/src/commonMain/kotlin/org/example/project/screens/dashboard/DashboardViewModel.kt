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
import org.example.project.data.pulls.PullRequestsManager
import org.example.project.screens.base.SnackbarErrorMessage

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

    private fun startPullRequestsCollectorsIfNeeded() {
        if (pullRequestsCollectionJob != null) return

        pullRequestsCollectionJob = viewModelScope.launch {
            launch {
                pullRequestsManager.currentUsersPullRequests().collect { prs ->
                    _state.update {
                        it.copy(
                            myPullRequests = prs.map { pr ->
                                DashboardPullRequestItem(
                                    title = pr.title,
                                    authorLogin = pr.user.login,
                                    browserUrl = pr.htmlUrl,
                                )
                            }
                        )
                    }
                }
            }

            launch {
                pullRequestsManager.pullRequestsForReview().collect { prs ->
                    _state.update {
                        it.copy(
                            pullRequestsForReview = prs.map { pr ->
                                DashboardPullRequestItem(
                                    title = pr.title,
                                    authorLogin = pr.user.login,
                                    browserUrl = pr.htmlUrl,
                                )
                            }
                        )
                    }
                }
            }
        }
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

    fun resetViewModel() {
        pullRequestsCollectionJob?.cancel()
        pullRequestsCollectionJob = null
        _state.update { DashboardState() }
    }
}

