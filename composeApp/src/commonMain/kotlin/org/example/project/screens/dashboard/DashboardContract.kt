package org.example.project.screens.dashboard

import org.example.project.screens.base.SnackbarErrorMessage

data class DashboardState(
    val title: String = "Dashboard",
    val selectedTab: DashboardTab = DashboardTab.MY_PRS,
    val isPullRequestsLoading: Boolean = false,
    val myPullRequests: List<DashboardPullRequestItem> = emptyList(),
    val pullRequestsForReview: List<DashboardPullRequestItem> = emptyList(),
    val dialogType: DashboardDialogType? = null,
    val errorMessage: SnackbarErrorMessage? = null,
    val isLoading: Boolean = false,
)

enum class DashboardTab {
    MY_PRS,
    PR_REVIEWS,
}

data class DashboardPullRequestItem(
    val title: String,
    val authorLogin: String,
)

sealed class DashboardDialogType {
    data object LogoutConfirmationDialog : DashboardDialogType()
}

