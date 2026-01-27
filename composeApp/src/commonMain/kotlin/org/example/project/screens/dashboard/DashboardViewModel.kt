package org.example.project.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.data.app.AppLocalDataSource
import org.example.project.screens.base.SnackbarErrorMessage

class DashboardViewModel(
    private val localDataSource: AppLocalDataSource,
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    fun initialise() {
        viewModelScope.launch {
            val user = localDataSource.observeLoggedInUser().firstOrNull()
            val repoRef = user?.githubRepoRef
            val username = user?.githubUsername

            val title =
                when {
                    username != null && repoRef != null -> "$username • ${repoRef.owner}/${repoRef.repo}"
                    repoRef != null -> "${repoRef.owner}/${repoRef.repo}"
                    username != null -> username
                    else -> "Dashboard"
                }

            _state.update { it.copy(title = title) }
        }
    }

    fun handleLogoutClicked() {
        _state.update {
            it.copy(
                dialogType = DashboardDialogType.LogoutConfirmationDialog,
                errorMessage = null
            )
        }
    }

    fun handleLogoutCancelled() {
        _state.update { it.copy(dialogType = null) }
    }

    fun handleLogoutConfirmed() {
        if (state.value.isLoading) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, dialogType = null, errorMessage = null) }
            runCatching { localDataSource.setLoggedInUser(null) }
                .onSuccess {
                    _state.update { it.copy(isLoading = false) }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = SnackbarErrorMessage(e.message ?: "Failed to logout"),
                        )
                    }
                }
        }
    }

    fun resetViewModel() {
        _state.update { DashboardState() }
    }
}

