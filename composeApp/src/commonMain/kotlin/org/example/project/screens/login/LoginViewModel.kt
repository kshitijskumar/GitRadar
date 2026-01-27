package org.example.project.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.data.app.AppLocalDataSource
import org.example.project.data.model.LoggedInUser

class LoginViewModel(
    private val localDataSource: AppLocalDataSource,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun handleGithubRepositoryEntered(value: String) {
        _state.update { it.copy(githubRepositoryLink = value, errorMessage = null) }
    }

    fun handleGithubUsernameEntered(value: String) {
        _state.update { it.copy(githubUsername = value, errorMessage = null) }
    }

    fun handleGithubTokenEntered(value: String) {
        _state.update { it.copy(githubToken = value, errorMessage = null) }
    }

    fun handleProceedClicked() {
        val repoLink = state.value.githubRepositoryLink.trim()
        val username = state.value.githubUsername.trim()
        val token = state.value.githubToken.trim()

        if (repoLink.isEmpty() || username.isEmpty() || token.isEmpty()) {
            _state.update { it.copy(errorMessage = "Please fill repository, username and token.") }
            return
        }

        val user = LoggedInUser(
            repositoryLink = repoLink,
            accessToken = token,
            githubUsername = username,
        )

        if (user.githubRepoRef == null) {
            _state.update {
                it.copy(errorMessage = "Invalid repository link. Expected a GitHub repo URL like github.com/<owner>/<repo>.")
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { localDataSource.setLoggedInUser(user) }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "Failed to save user",
                        )
                    }
                }
        }
    }

    fun resetViewModel() {
        _state.update { LoginState() }
    }
}

