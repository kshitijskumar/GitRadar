package org.example.project.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.example.project.data.theme.AppColors
import org.example.project.data.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { snackbarHostState.showSnackbar(it.msg) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = AppTheme.color.surfaceColor,
        topBar = {
            // Adds a subtle shadow under the toolbar.
            Surface(
                color = AppTheme.color.surfaceColor,
                shadowElevation = 0.dp,
            ) {
                TopAppBar(
                    title = { Text(
                        text = state.title,
                        color = AppTheme.color.primaryText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    ) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = AppTheme.color.surfaceColor,
                        titleContentColor = AppTheme.color.surfaceColor
                    ),
                    actions = {
                        TextButton(
                            onClick = viewModel::handleRefreshClicked,
                            enabled = !state.isPullRequestsLoading,
                        ) {
                            Text("Refresh")
                        }
                        TextButton(
                            onClick = viewModel::handleLogoutClicked,
                            enabled = !state.isLoading,
                        ) {
                            Text("Logout")
                        }
                    },
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            PrimaryTabRow(
                selectedTabIndex = state.tabs.indexOf(state.selectedTab).takeIf { it >= 0 } ?: 0,
                containerColor = AppTheme.color.surfaceColor
            ) {
                state.tabs.forEach {
                    Tab(
                        selected = state.selectedTab == it,
                        onClick = { viewModel.handleTabSelected(it) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = it.tabName(),
                                    color = if (state.selectedTab == it) AppTheme.color.primaryButton else AppTheme.color.disabledText
                                )
                                if (state.isPullRequestsLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.padding(start = 8.dp),
                                        strokeWidth = 2.dp,
                                    )
                                }
                            }
                        },
                        selectedContentColor = AppTheme.color.primaryButton,
                        unselectedContentColor = AppTheme.color.disabledText
                    )
                }
            }

            val items =
                when (state.selectedTab) {
                    DashboardTab.MY_PRS -> state.myPullRequests
                    DashboardTab.PR_REVIEWS -> state.pullRequestsForReview
                }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items) { pr ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = AppTheme.color.cardOnSurface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AppTheme.color.cardOnSurface)
                                .clickable(enabled = pr.browserUrl.isNotBlank()) {
                                    uriHandler.openUri(pr.browserUrl)
                                }
                                .padding(
                                    vertical = 16.dp,
                                    horizontal = 16.dp
                                )
                        ) {
                            Text(
                                text = pr.title,
                                color = AppTheme.color.primaryText,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "by ${pr.authorLogin}",
                                color = AppTheme.color.secondaryText
                            )
                        }
                    }
                }
            }
        }
    }

    when(val dialogType = state.dialogType) {
        DashboardDialogType.LogoutConfirmationDialog -> {
            AlertDialog(
                onDismissRequest = viewModel::handleLogoutCancelled,
                title = {
                    Text(
                        text = "Logout",
                        color = AppTheme.color.primaryText
                    )
                        },
                text = {
                    Text(
                        text = "Are you sure you want to logout?",
                        color = AppTheme.color.secondaryText
                    )
                       },
                confirmButton = {
                    Button(onClick = viewModel::handleLogoutConfirmed) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    Button(onClick = viewModel::handleLogoutCancelled) {
                        Text("Cancel")
                    }
                },
                containerColor = AppTheme.color.cardOnSurface
            )
        }
        null -> {}
    }
}

