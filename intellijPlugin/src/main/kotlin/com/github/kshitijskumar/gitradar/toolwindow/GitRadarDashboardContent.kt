package com.github.kshitijskumar.gitradar.toolwindow

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.intellij.ide.BrowserUtil
import org.example.project.data.pulls.PullRequestStatus
import org.example.project.screens.dashboard.DashboardPullRequestItem
import org.example.project.screens.dashboard.DashboardState
import org.example.project.screens.dashboard.DashboardTab
import org.example.project.screens.dashboard.tabName
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.TabData
import org.jetbrains.jewel.ui.component.TabStrip
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.theme.defaultTabStyle

@Composable
fun GitRadarDashboardContent(
    state: DashboardState,
    onTabSelected: (DashboardTab) -> Unit,
    onRefresh: () -> Unit,
    onOpenSettings: () -> Unit,
    onMarkResolved: (DashboardPullRequestItem) -> Unit,
) {
    val background = JewelTheme.globalColors.panelBackground
    val cardBackground = JewelTheme.globalColors.panelBackground

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background),
    ) {
        // Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = state.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            OutlinedButton(
                onClick = onRefresh,
                enabled = !state.isPullRequestsLoading,
            ) { Text("Refresh") }
            Spacer(Modifier.width(6.dp))
            OutlinedButton(onClick = onOpenSettings) { Text("Settings") }
        }

        // Error banner
        state.errorMessage?.let { err ->
            Text(
                text = err.msg,
                color = Color(0xFFE05050),
                fontSize = 11.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp),
            )
        }

        // Loading indicator
        if (state.isPullRequestsLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(Color(0xFF3574F0)),
            )
        }

        // Tab strip
        val tabs = remember(state.tabs, state.selectedTab) {
            state.tabs.map { tab ->
                TabData.Default(
                    selected = tab == state.selectedTab,
                    content = { Text(tab.tabName()) },
                    onClick = { onTabSelected(tab) },
                )
            }
        }
        TabStrip(tabs = tabs, style = JewelTheme.defaultTabStyle)

        // PR list
        val items = when (state.selectedTab) {
            DashboardTab.MY_PRS -> state.myPullRequests
            DashboardTab.PR_REVIEWS -> state.pullRequestsForReview
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(items = items, key = { it.prId }) { pr ->
                PrCard(
                    pr = pr,
                    cardBackground = cardBackground,
                    onMarkResolved = onMarkResolved,
                )
            }
        }
    }
}

@Composable
private fun PrCard(
    pr: DashboardPullRequestItem,
    cardBackground: Color,
    onMarkResolved: (DashboardPullRequestItem) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(cardBackground)
            .clickable(enabled = pr.browserUrl.isNotBlank()) {
                BrowserUtil.browse(pr.browserUrl)
            }
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(pr.status.indicatorColor()),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = pr.title,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "by ${pr.authorLogin}",
                fontSize = 11.sp,
                color = JewelTheme.globalColors.text.normal.copy(alpha = 0.65f),
            )
            Spacer(Modifier.weight(1f))
            if (pr.status != PullRequestStatus.DRAFT) {
                val resolveLabel = when (pr.status) {
                    PullRequestStatus.NEEDS_ATTENTION -> "Resolve"
                    PullRequestStatus.RESOLVED -> "Unresolve"
                    PullRequestStatus.DRAFT -> ""
                }
                Text(
                    text = resolveLabel,
                    fontSize = 11.sp,
                    color = JewelTheme.globalColors.text.normal.copy(alpha = 0.65f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onMarkResolved(pr) }
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
        }
    }
}

private fun PullRequestStatus.indicatorColor(): Color = when (this) {
    PullRequestStatus.DRAFT -> Color(0xFF888888)
    PullRequestStatus.NEEDS_ATTENTION -> Color(0xFFE0B454)
    PullRequestStatus.RESOLVED -> Color(0xFF4CAF7A)
}
