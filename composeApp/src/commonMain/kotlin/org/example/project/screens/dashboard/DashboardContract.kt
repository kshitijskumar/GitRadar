package org.example.project.screens.dashboard

import org.example.project.screens.base.SnackbarErrorMessage

data class DashboardState(
    val title: String = "Dashboard",
    val dialogType: DashboardDialogType? = null,
    val errorMessage: SnackbarErrorMessage? = null,
    val isLoading: Boolean = false,
)

sealed class DashboardDialogType {
    data object LogoutConfirmationDialog : DashboardDialogType()
}

