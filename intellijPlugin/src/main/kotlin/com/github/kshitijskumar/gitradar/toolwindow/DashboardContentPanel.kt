package com.github.kshitijskumar.gitradar.toolwindow

import com.intellij.ide.BrowserUtil
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.example.project.data.pulls.PullRequestStatus
import org.example.project.screens.dashboard.DashboardPullRequestItem
import org.example.project.screens.dashboard.DashboardTab
import org.example.project.screens.dashboard.DashboardViewModel
import org.example.project.screens.dashboard.tabName
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.DefaultListModel
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JList
import javax.swing.JMenuItem
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.JProgressBar
import javax.swing.ListCellRenderer
import javax.swing.ListSelectionModel
import javax.swing.SwingUtilities

class DashboardContentPanel(
    private val viewModel: DashboardViewModel,
    coroutineScope: CoroutineScope,
    private val onOpenSettings: () -> Unit,
) {
    val component: JPanel = JPanel(BorderLayout())

    private val titleLabel = JBLabel("Dashboard")
    private val refreshButton = JButton("Refresh")

    private val errorLabel = JBLabel("").apply {
        foreground = JBColor(Color(0xE0, 0x50, 0x50), Color(0xE0, 0x50, 0x50))
        border = JBUI.Borders.empty(0, 12, 4, 12)
        isVisible = false
    }

    private val progressBar = JProgressBar().apply {
        isIndeterminate = true
        isVisible = false
    }

    private val tabButtons: Map<DashboardTab, JButton> = DashboardTab.entries.associateWith { tab ->
        JButton(tab.tabName()).apply {
            isBorderPainted = false
            isContentAreaFilled = false
            addActionListener { viewModel.handleTabSelected(tab) }
        }
    }

    private val listModel = DefaultListModel<DashboardPullRequestItem>()
    private val prList = JBList(listModel).apply {
        cellRenderer = PrCellRenderer()
        selectionMode = ListSelectionModel.SINGLE_SELECTION
        emptyText.text = "No pull requests"
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                val index = locationToIndex(e.point)
                if (index < 0) return
                val pr = model.getElementAt(index)
                if (SwingUtilities.isRightMouseButton(e)) {
                    showResolveMenu(e, pr)
                } else {
                    if (pr.browserUrl.isNotBlank()) BrowserUtil.browse(pr.browserUrl)
                }
            }
        })
    }

    init {
        refreshButton.addActionListener { viewModel.handleRefreshClicked() }
        val settingsButton = JButton("Settings").apply {
            addActionListener { onOpenSettings() }
        }

        val headerRow = JPanel(BorderLayout()).apply {
            border = JBUI.Borders.empty(6, 10, 4, 6)
            add(titleLabel, BorderLayout.WEST)
            add(JPanel(FlowLayout(FlowLayout.RIGHT, 6, 0)).apply {
                isOpaque = false
                add(refreshButton)
                add(settingsButton)
            }, BorderLayout.EAST)
        }

        val tabRow = JPanel(FlowLayout(FlowLayout.LEFT, 4, 2)).apply {
            border = JBUI.Borders.empty(0, 6)
            DashboardTab.entries.forEach { tab -> add(tabButtons[tab]) }
        }

        val topPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            add(headerRow)
            add(errorLabel)
            add(progressBar)
            add(tabRow)
        }

        component.add(topPanel, BorderLayout.NORTH)
        component.add(JBScrollPane(prList).apply { border = JBUI.Borders.empty() }, BorderLayout.CENTER)

        coroutineScope.launch {
            viewModel.state.collect { state ->
                SwingUtilities.invokeLater {
                    titleLabel.text = state.title
                    refreshButton.isEnabled = !state.isPullRequestsLoading

                    errorLabel.text = state.errorMessage?.msg ?: ""
                    errorLabel.isVisible = state.errorMessage != null

                    progressBar.isVisible = state.isPullRequestsLoading

                    DashboardTab.entries.forEach { tab ->
                        tabButtons[tab]?.font = tabButtons[tab]?.font?.deriveFont(
                            if (tab == state.selectedTab) Font.BOLD else Font.PLAIN
                        )
                    }

                    val items = when (state.selectedTab) {
                        DashboardTab.MY_PRS -> state.myPullRequests
                        DashboardTab.PR_REVIEWS -> state.pullRequestsForReview
                    }
                    listModel.clear()
                    items.forEach { listModel.addElement(it) }
                }
            }
        }
    }

    private fun showResolveMenu(e: MouseEvent, pr: DashboardPullRequestItem) {
        if (pr.status == PullRequestStatus.DRAFT) return
        val label = if (pr.status == PullRequestStatus.NEEDS_ATTENTION) "Resolve" else "Unresolve"
        JPopupMenu().apply {
            add(JMenuItem(label).apply { addActionListener { viewModel.markUnmarkResolved(pr) } })
        }.show(e.component, e.x, e.y)
    }
}

private class PrCellRenderer : JPanel(BorderLayout(8, 0)), ListCellRenderer<DashboardPullRequestItem> {
    private val dot = StatusDot()
    private val titleLabel = JBLabel()
    private val authorLabel = JBLabel().apply {
        font = font.deriveFont(11f)
        foreground = JBColor.GRAY
    }
    private val resolveHint = JBLabel().apply {
        font = font.deriveFont(11f)
        foreground = JBColor.GRAY
    }

    init {
        isOpaque = true
        border = JBUI.Borders.compound(
            JBUI.Borders.customLine(JBColor(Color(0xE0E0E0), Color(0x3C3F41)), 0, 0, 1, 0),
            JBUI.Borders.empty(8, 10),
        )

        val textCol = JPanel().apply {
            isOpaque = false
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            add(titleLabel)
            add(Box.createRigidArea(Dimension(0, 2)))
            add(authorLabel)
        }

        val left = JPanel(BorderLayout(8, 0)).apply {
            isOpaque = false
            add(dot, BorderLayout.WEST)
            add(textCol, BorderLayout.CENTER)
        }

        add(left, BorderLayout.CENTER)
        add(resolveHint, BorderLayout.EAST)
    }

    override fun getListCellRendererComponent(
        list: JList<out DashboardPullRequestItem>,
        value: DashboardPullRequestItem,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean,
    ): Component {
        titleLabel.text = value.title
        authorLabel.text = "by ${value.authorLogin}"

        dot.dotColor = when (value.status) {
            PullRequestStatus.DRAFT -> Color(0x88, 0x88, 0x88)
            PullRequestStatus.NEEDS_ATTENTION -> Color(0xE0, 0xB4, 0x54)
            PullRequestStatus.RESOLVED -> Color(0x4C, 0xAF, 0x7A)
        }

        resolveHint.isVisible = value.status != PullRequestStatus.DRAFT
        resolveHint.text = when (value.status) {
            PullRequestStatus.NEEDS_ATTENTION -> "Resolve ▸"
            PullRequestStatus.RESOLVED -> "Unresolve ▸"
            PullRequestStatus.DRAFT -> ""
        }

        background = if (isSelected) list.selectionBackground else list.background
        return this
    }
}

private class StatusDot : JComponent() {
    var dotColor: Color = Color.GRAY
        set(value) {
            field = value
            repaint()
        }

    override fun getPreferredSize() = Dimension(10, 10)
    override fun getMinimumSize() = preferredSize
    override fun getMaximumSize() = preferredSize

    override fun paintComponent(g: Graphics) {
        val g2 = g.create() as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2.color = dotColor
        g2.fillOval(0, (height - 10) / 2, 10, 10)
        g2.dispose()
    }
}
