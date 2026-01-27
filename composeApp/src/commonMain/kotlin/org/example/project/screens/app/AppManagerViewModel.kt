package org.example.project.screens.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.data.app.AppLocalDataSource
import org.example.project.data.model.LoggedInUser

class AppManagerViewModel(
    private val localDataSource: AppLocalDataSource,
) : ViewModel() {
    private val _state = MutableStateFlow(AppManagerState())
    val state: StateFlow<AppManagerState> = _state.asStateFlow()

    init {
        initialiseManager()
    }
    
    private fun initialiseManager() {
        viewModelScope.launch {
            localDataSource.observeLoggedInUser()
                .onEach { user ->
                    _state.update {
                        it.copy(
                            loggedInUser = user,
                            screenType = resolveScreenType(user),
                            dialogType = resolveDialogType(user),
                            errorMessage = null,
                            isLoading = false,
                        )
                    }
                }
                .catch { e ->
                    _state.update {
                        it.copy(
                            loggedInUser = null,
                            screenType = AppScreenType.LOGIN,
                            dialogType = null,
                            errorMessage = e.message ?: "Failed to load user",
                            isLoading = false,
                        )
                    }
                }
                .collect()
        }
    }
    
    fun acknowledgeSessionClearance() {
        _state.update { 
            it.copy(
                screenType = AppScreenType.LOGIN,
                dialogType = null,
                errorMessage = null
            )
        }
    }
    
    private fun resolveScreenType(userInfo: LoggedInUser?): AppScreenType {
        return when(val currentScreenType = state.value.screenType) {
            AppScreenType.LOGIN,
            AppScreenType.DASHBOARD -> if (userInfo == null) currentScreenType else AppScreenType.LOGIN
            null -> AppScreenType.LOGIN
        }
    }

    private fun resolveDialogType(userInfo: LoggedInUser?): AppManagerDialogType? {
        return when(state.value.screenType) {
            AppScreenType.LOGIN -> null
            AppScreenType.DASHBOARD -> if (userInfo == null) AppManagerDialogType.SessionClearedDialog else null
            null -> null
        }
    }
}

