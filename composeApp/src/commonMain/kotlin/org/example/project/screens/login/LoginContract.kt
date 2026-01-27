package org.example.project.screens.login

import org.example.project.screens.base.SnackbarErrorMessage

data class LoginState(
    val githubRepositoryLink: String = "",
    val githubUsername: String = "",
    val githubToken: String = "",
    val errorMessage: SnackbarErrorMessage? = null,
    val isLoading: Boolean = false,
)

