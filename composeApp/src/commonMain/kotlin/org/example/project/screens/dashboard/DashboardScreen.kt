package org.example.project.screens.dashboard

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { snackbarHostState.showSnackbar(it.msg) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            // Adds a subtle shadow under the toolbar.
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
            ) {
                TopAppBar(
                    title = { Text(state.title) },
                    actions = {
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
                selectedTabIndex = state.selectedTab.ordinal,
            ) {
                Tab(
                    selected = state.selectedTab == DashboardTab.MY_PRS,
                    onClick = { viewModel.handleTabSelected(DashboardTab.MY_PRS) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("My PRs")
                            if (state.isPullRequestsLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.padding(start = 8.dp),
                                    strokeWidth = 2.dp,
                                )
                            }
                        }
                    },
                )
                Tab(
                    selected = state.selectedTab == DashboardTab.PR_REVIEWS,
                    onClick = { viewModel.handleTabSelected(DashboardTab.PR_REVIEWS) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("PR Reviews")
                            if (state.isPullRequestsLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.padding(start = 8.dp),
                                    strokeWidth = 2.dp,
                                )
                            }
                        }
                    },
                )
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
            ) {
                items(items) { pr ->
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Text(pr.title)
                        Text("by ${pr.authorLogin}")
                    }
                }
            }
        }
    }

    when(val dialogType = state.dialogType) {
        DashboardDialogType.LogoutConfirmationDialog -> {
            AlertDialog(
                onDismissRequest = viewModel::handleLogoutCancelled,
                title = { Text("Logout") },
                text = { Text("Are you sure you want to logout?") },
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
            )
        }
        null -> {}
    }
}

