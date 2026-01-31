package org.example.project.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.data.app.AppLocalDataSource
import org.example.project.data.model.LoggedInUser
import org.example.project.data.model.RecentLogin
import org.example.project.screens.base.SnackbarErrorMessage

class LoginViewModel(
    private val localDataSource: AppLocalDataSource,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            localDataSource.observeRecentLogins().collect { logins ->
                _state.update {
                    it.copy(
                        recentLogins = logins.map { login ->
                            LoginQuickLoginItem(
                                repositoryLink = login.repositoryLink,
                                githubUsername = login.githubUsername,
                                repoDisplayText = buildRepoDisplayText(login),
                            )
                        }
                    )
                }
            }
        }
    }

    fun handleGithubRepositoryEntered(value: String) {
        _state.update { it.copy(githubRepositoryLink = value, errorMessage = null) }
    }

    fun handleGithubUsernameEntered(value: String) {
        _state.update { it.copy(githubUsername = value, errorMessage = null) }
    }

    fun handleGithubTokenEntered(value: String) {
        _state.update { it.copy(githubToken = value, errorMessage = null) }
    }

    fun handleQuickLoginClicked(item: LoginQuickLoginItem) {
        _state.update {
            it.copy(
                githubRepositoryLink = item.repositoryLink,
                githubUsername = item.githubUsername,
                githubToken = "",
                errorMessage = null,
            )
        }
    }

    fun handleProceedClicked() {
        if (state.value.isLoading) {
            return
        }
        _state.update { it.copy(isLoading = true) }
        val repoLink = state.value.githubRepositoryLink.trim()
        val username = state.value.githubUsername.trim()
        val token = state.value.githubToken.trim()

        if (repoLink.isEmpty() || username.isEmpty() || token.isEmpty()) {
            _state.update {
                it.copy(
                    errorMessage = SnackbarErrorMessage("Please fill repository, username and token."),
                    isLoading = false
                )
            }
            return
        }

        val user = LoggedInUser(
            repositoryLink = repoLink,
            accessToken = token,
            githubUsername = username,
        )

        if (user.githubRepoRef == null) {
            _state.update {
                it.copy(
                    errorMessage = SnackbarErrorMessage("Invalid repository link. Expected a GitHub repo URL like github.com/<owner>/<repo>."),
                    isLoading = false
                )
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { localDataSource.setLoggedInUser(user) }
                .onSuccess {
                    runCatching {
                        localDataSource.addRecentLogin(
                            RecentLogin(
                                repositoryLink = repoLink,
                                githubUsername = username,
                            )
                        )
                    }
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                        )
                    }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = SnackbarErrorMessage(e.message ?: "Failed to save user"),
                        )
                    }
                }
        }
    }

    fun resetViewModel() {
        _state.update { LoginState() }
    }
}

private fun buildRepoDisplayText(login: RecentLogin): String {
    val parsed = LoggedInUser(
        repositoryLink = login.repositoryLink,
        accessToken = "",
        githubUsername = login.githubUsername,
    ).githubRepoRef

    return if (parsed != null) {
        "${parsed.owner}/${parsed.repo}"
    } else {
        login.repositoryLink
    }
}

