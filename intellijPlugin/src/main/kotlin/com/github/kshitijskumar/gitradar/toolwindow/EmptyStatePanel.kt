package com.github.kshitijskumar.gitradar.toolwindow

import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import java.awt.Component
import java.awt.Dimension
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JPanel

class EmptyStatePanel(private val onOpenSettings: () -> Unit) {
    val component: JPanel = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        border = JBUI.Borders.empty(24)
    }

    private val messageLabel = JBLabel("").apply {
        alignmentX = Component.CENTER_ALIGNMENT
    }

    init {
        val settingsButton = JButton("Open Settings").apply {
            alignmentX = Component.CENTER_ALIGNMENT
            addActionListener { onOpenSettings() }
        }

        component.add(Box.createVerticalGlue())
        component.add(messageLabel)
        component.add(Box.createRigidArea(Dimension(0, 12)))
        component.add(settingsButton)
        component.add(Box.createVerticalGlue())
    }

    fun update(type: EmptyStateType) {
        messageLabel.text = "<html><center>" + when (type) {
            EmptyStateType.NOT_LOGGED_IN -> "No account configured."
            EmptyStateType.NO_REPO_DETECTED ->
                "No GitHub repository detected in this project.<br>Only HTTPS remotes are supported."
        } + "</center></html>"
    }
}
