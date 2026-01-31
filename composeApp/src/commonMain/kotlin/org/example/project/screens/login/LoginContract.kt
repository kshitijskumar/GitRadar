package org.example.project.screens.login

import org.example.project.screens.base.SnackbarErrorMessage

data class LoginState(
    val githubRepositoryLink: String = "",
    val githubUsername: String = "",
    val githubToken: String = "",
    val recentLogins: List<LoginQuickLoginItem> = emptyList(),
    val errorMessage: SnackbarErrorMessage? = null,
    val isLoading: Boolean = false,
)

data class LoginQuickLoginItem(
    val repositoryLink: String,
    val githubUsername: String,
    val repoDisplayText: String,
)

