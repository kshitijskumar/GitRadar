package com.github.kshitijskumar.gitradar.toolwindow

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.github.kshitijskumar.gitradar.services.GitRadarApplicationService
import com.github.kshitijskumar.gitradar.settings.GitRadarAppSettingsConfigurable
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import kotlinx.coroutines.flow.StateFlow
import org.example.project.data.model.GithubRepoRef
import org.example.project.screens.dashboard.DashboardViewModel
import org.jetbrains.jewel.bridge.theme.SwingBridgeTheme
import javax.swing.JComponent
import org.jetbrains.jewel.bridge.JewelComposePanel

class DashboardPanel(
    private val project: Project,
    private val viewModel: DashboardViewModel,
    private val accountFlow: StateFlow<GitRadarApplicationService.AccountCredentials?>,
    private val detectedRepoFlow: StateFlow<GithubRepoRef?>,
) {
    val component: JComponent = JewelComposePanel {
        SwingBridgeTheme {
            val account by accountFlow.collectAsState()
            val repo by detectedRepoFlow.collectAsState()
            val dashboardState by viewModel.state.collectAsState()

            val emptyStateType = when {
                account == null -> EmptyStateType.NOT_LOGGED_IN
                repo == null -> EmptyStateType.NO_REPO_DETECTED
                else -> null
            }

            LaunchedEffect(emptyStateType) {
                if (emptyStateType == null) viewModel.initialise()
                else viewModel.resetViewModel()
            }

            if (emptyStateType != null) {
                GitRadarEmptyStateContent(
                    type = emptyStateType,
                    onOpenSettings = ::openSettings,
                )
            } else {
                GitRadarDashboardContent(
                    state = dashboardState,
                    onTabSelected = viewModel::handleTabSelected,
                    onRefresh = viewModel::handleRefreshClicked,
                    onOpenSettings = ::openSettings,
                    onMarkResolved = viewModel::markUnmarkResolved,
                )
            }
        }
    }

    private fun openSettings() {
        ShowSettingsUtil.getInstance()
            .showSettingsDialog(project, GitRadarAppSettingsConfigurable::class.java)
    }
}
