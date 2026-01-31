package org.example.project

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.example.project.data.app.AppRemoteDataSource
import org.example.project.data.app.AppRemoteDataSourceImpl
import org.example.project.data.github.createGithubHttpClient
import org.example.project.data.pulls.PullRequestsManager
import org.example.project.screens.app.AppManagerViewModel
import org.example.project.screens.app.AppScreenType
import org.example.project.data.app.AppLocalDataSource
import org.example.project.data.app.AppLocalDataSourceImpl
import org.example.project.screens.login.LoginScreen
import org.example.project.screens.login.LoginViewModel
import org.example.project.screens.dashboard.DashboardScreen
import org.example.project.screens.dashboard.DashboardViewModel
import org.example.project.data.local.createUserDataStore
import org.example.project.screens.app.AppManagerDialogType
import org.example.project.util.PlatformContext

@Composable
fun App(
    platformContext: PlatformContext,
) {
    MaterialTheme {
        val localDataSource: AppLocalDataSource = remember(platformContext) {
            AppLocalDataSourceImpl(createUserDataStore(platformContext))
        }
        val remoteDataSource: AppRemoteDataSource = remember(localDataSource) {
            AppRemoteDataSourceImpl(
                githubClient = createGithubHttpClient(),
                localDataSource = localDataSource,
            )
        }
        val pullRequestsManager = remember(localDataSource, remoteDataSource) {
            PullRequestsManager(
                localDataSource = localDataSource,
                remoteDataSource = remoteDataSource,
            )
        }
        val appManagerViewModel = remember(localDataSource) { AppManagerViewModel(localDataSource) }
        val loginViewModel = remember(localDataSource) { LoginViewModel(localDataSource) }
        val dashboardViewModel = remember(localDataSource, pullRequestsManager) {
            DashboardViewModel(
                localDataSource = localDataSource,
                pullRequestsManager = pullRequestsManager,
            )
        }

        val state by appManagerViewModel.state.collectAsState()

        LaunchedEffect(state.screenType) {
            when(state.screenType) {
                AppScreenType.LOGIN -> {
                    dashboardViewModel.resetViewModel()
                }
                AppScreenType.DASHBOARD -> {
                    dashboardViewModel.initialise()
                    loginViewModel.resetViewModel()
                }
                null -> {
                    loginViewModel.resetViewModel()
                    dashboardViewModel.resetViewModel()
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when (state.screenType) {
                null -> Text("Loading…")
                AppScreenType.LOGIN -> LoginScreen(viewModel = loginViewModel)
                AppScreenType.DASHBOARD -> DashboardScreen(viewModel = dashboardViewModel)
            }
        }

        when (state.dialogType) {
            AppManagerDialogType.SessionClearedDialog -> {
                AlertDialog(
                    onDismissRequest = appManagerViewModel::acknowledgeSessionClearance,
                    title = { Text("Session cleared") },
                    text = { Text("You’ve been logged out. Please login again to continue.") },
                    confirmButton = {
                        TextButton(onClick = appManagerViewModel::acknowledgeSessionClearance) {
                            Text("OK")
                        }
                    },
                )
            }
            null -> Unit
        }
    }
}