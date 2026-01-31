package org.example.project.screens.dashboard

import androidx.compose.ui.graphics.Color
import org.example.project.data.theme.AppTheme
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

enum class PullRequestStatus {
    DRAFT,
    NEEDS_ATTENTION,
    RESOLVED,
}

fun PullRequestStatus.indicatorColor(): Color {
    return when (this) {
        PullRequestStatus.DRAFT -> AppTheme.color.draftIndicator
        PullRequestStatus.NEEDS_ATTENTION -> AppTheme.color.actionRequiredIndicator
        PullRequestStatus.RESOLVED -> AppTheme.color.caughtUpIndicator
    }
}

fun DashboardTab.tabName(): String {
    return when(this) {
        DashboardTab.MY_PRS -> "My PRs"
        DashboardTab.PR_REVIEWS -> "PR Reviews"
    }
}

data class DashboardPullRequestItem(
    val prId: Long,
    val title: String,
    val authorLogin: String,
    val browserUrl: String,
    val updatedAt: String,
    val status: PullRequestStatus,
)

sealed class DashboardDialogType {
    data object LogoutConfirmationDialog : DashboardDialogType()
}

