package com.github.kshitijskumar.gitradar.toolwindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.SwingConstants

class GitRadarToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = JPanel(BorderLayout()).apply {
            border = JBUI.Borders.empty(16)
            add(
                JBLabel("GitRadar — coming soon", SwingConstants.CENTER),
                BorderLayout.CENTER
            )
        }
        val content = toolWindow.contentManager.factory
            .createContent(panel, null, false)
        toolWindow.contentManager.addContent(content)
    }
}
