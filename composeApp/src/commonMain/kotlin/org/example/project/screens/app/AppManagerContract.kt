package org.example.project.screens.app

import org.example.project.data.model.LoggedInUser

data class AppManagerState(
    val screenType: AppScreenType? = null,
    val loggedInUser: LoggedInUser? = null,
    val githubRepositoryLink: String = "",
    val githubUsername: String = "",
    val githubToken: String = "",
    val errorMessage: String? = null,
    val isLoading: Boolean = true,
    val dialogType: AppManagerDialogType? = null
)

enum class AppScreenType {
    LOGIN,
    DASHBOARD,
}

sealed class AppManagerDialogType {
    data object SessionClearedDialog : AppManagerDialogType()
}