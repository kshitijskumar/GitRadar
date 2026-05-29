package com.github.kshitijskumar.gitradar.toolwindow

import com.github.kshitijskumar.gitradar.services.GitRadarApplicationService
import com.github.kshitijskumar.gitradar.settings.GitRadarAppSettingsConfigurable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.example.project.data.model.GithubRepoRef
import org.example.project.screens.dashboard.DashboardViewModel
import java.awt.CardLayout
import javax.swing.JPanel
import javax.swing.SwingUtilities
//
class DashboardPanel(
    private val project: Project,
    private val viewModel: DashboardViewModel,
    private val coroutineScope: CoroutineScope,
    private val accountFlow: StateFlow<GitRadarApplicationService.AccountCredentials?>,
    private val detectedRepoFlow: StateFlow<GithubRepoRef?>,
) {
    private val cardLayout = CardLayout()
    val component: JPanel = JPanel(cardLayout)

    private val emptyState = EmptyStatePanel(::openSettings)
    private val dashboard = DashboardContentPanel(viewModel, coroutineScope, ::openSettings)

    init {
        component.add(emptyState.component, CARD_EMPTY)
        component.add(dashboard.component, CARD_CONTENT)

        coroutineScope.launch {
            combine(accountFlow, detectedRepoFlow) { account, repo ->
                log.info("[GitKshitij1] DashboardPanel combine: account=${if (account == null) "null" else "present"} repo=$repo")
                when {
                    account == null -> EmptyStateType.NOT_LOGGED_IN
                    repo == null -> EmptyStateType.NO_REPO_DETECTED
                    else -> null
                }
            }.collect { emptyStateType ->
                log.info("[GitKshitij1] DashboardPanel state -> ${emptyStateType ?: "DASHBOARD"}")
                if (emptyStateType != null) viewModel.resetViewModel() else viewModel.initialise()
                SwingUtilities.invokeLater {
                    if (emptyStateType != null) {
                        emptyState.update(emptyStateType)
                        cardLayout.show(component, CARD_EMPTY)
                    } else {
                        cardLayout.show(component, CARD_CONTENT)
                    }
                }
            }
        }
    }

    private fun openSettings() {
        ShowSettingsUtil.getInstance().showSettingsDialog(project, "GitRadar")
    }

    companion object {
        private val log = Logger.getInstance(DashboardPanel::class.java)
        private const val CARD_EMPTY = "empty"
        private const val CARD_CONTENT = "content"
    }
}
