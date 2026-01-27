package org.example.project

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.example.project.screens.app.AppManagerViewModel
import org.example.project.screens.app.AppScreenType
import org.example.project.data.app.AppLocalDataSource
import org.example.project.data.app.AppLocalDataSourceImpl
import org.example.project.screens.login.LoginScreen
import org.example.project.screens.login.LoginViewModel
import org.example.project.data.local.createUserDataStore
import org.example.project.util.PlatformContext

@Composable
fun App(
    platformContext: PlatformContext,
) {
    MaterialTheme {
        val localDataSource: AppLocalDataSource = remember(platformContext) {
            AppLocalDataSourceImpl(createUserDataStore(platformContext))
        }
        val appManagerViewModel = remember(localDataSource) { AppManagerViewModel(localDataSource) }
        val loginViewModel = remember(localDataSource) { LoginViewModel(localDataSource) }

        val state by appManagerViewModel.state.collectAsState()

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when (state.screenType) {
                null -> Text("Loading…")
                AppScreenType.LOGIN -> LoginScreen(viewModel = loginViewModel)
                AppScreenType.DASHBOARD -> Text("Dashboard")
            }
        }
    }
}