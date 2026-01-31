package org.example.project.screens.dashboard

import org.example.project.screens.base.SnackbarErrorMessage

data class DashboardState(
    val title: String = "Dashboard",
    val tabs: List<DashboardTab> = listOf(DashboardTab.PR_REVIEWS, DashboardTab.MY_PRS),
    val selectedTab: DashboardTab = DashboardTab.PR_REVIEWS,
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

fun DashboardTab.tabName(): String {
    return when(this) {
        DashboardTab.MY_PRS -> "My PRs"
        DashboardTab.PR_REVIEWS -> "PR Reviews"
    }
}

data class DashboardPullRequestItem(
    val title: String,
    val authorLogin: String,
    val browserUrl: String,
)

sealed class DashboardDialogType {
    data object LogoutConfirmationDialog : DashboardDialogType()
}

