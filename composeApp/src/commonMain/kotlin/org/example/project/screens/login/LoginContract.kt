package org.example.project.screens.login

data class LoginState(
    val githubRepositoryLink: String = "",
    val githubUsername: String = "",
    val githubToken: String = "",
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
)

