package com.github.kshitijskumar.gitradar.toolwindow

import com.github.kshitijskumar.gitradar.services.GitRadarApplicationService
import com.github.kshitijskumar.gitradar.services.GitRadarProjectService
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory

class GitRadarToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val service = GitRadarProjectService.getInstance(project)
        val appService = GitRadarApplicationService.getInstance()
        val panel = DashboardPanel(
            project = project,
            viewModel = service.dashboardViewModel,
            accountFlow = appService.accountFlow,
            detectedRepoFlow = service.repoDetector.detectedRepo,
        )
        val content = toolWindow.contentManager.factory
            .createContent(panel.component, null, false)
        toolWindow.contentManager.addContent(content)
    }
}
