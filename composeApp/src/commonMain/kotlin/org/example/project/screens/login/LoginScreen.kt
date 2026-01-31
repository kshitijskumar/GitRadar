package org.example.project.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import org.example.project.data.theme.AppTheme

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { snackbarHostState.showSnackbar(it.msg) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = AppTheme.color.surfaceColor
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .horizontalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = state.githubRepositoryLink,
                onValueChange = viewModel::handleGithubRepositoryEntered,
                label = { Text("Repository link") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = AppTheme.color.primaryText,
                    unfocusedTextColor = AppTheme.color.primaryText,
                    focusedLabelColor = AppTheme.color.secondaryText,
                    unfocusedLabelColor = AppTheme.color.secondaryText,
                    focusedContainerColor = AppTheme.color.cardOnSurface,
                    unfocusedContainerColor = AppTheme.color.cardOnSurface,
                )
            )

            OutlinedTextField(
                value = state.githubUsername,
                onValueChange = viewModel::handleGithubUsernameEntered,
                label = { Text("GitHub username") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .padding(bottom = 12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = AppTheme.color.primaryText,
                    unfocusedTextColor = AppTheme.color.primaryText,
                    focusedLabelColor = AppTheme.color.secondaryText,
                    unfocusedLabelColor = AppTheme.color.secondaryText,
                    focusedContainerColor = AppTheme.color.cardOnSurface,
                    unfocusedContainerColor = AppTheme.color.cardOnSurface
                )
            )

            OutlinedTextField(
                value = state.githubToken,
                onValueChange = viewModel::handleGithubTokenEntered,
                label = { Text("Access token") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .padding(bottom = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = AppTheme.color.primaryText,
                    unfocusedTextColor = AppTheme.color.primaryText,
                    focusedLabelColor = AppTheme.color.secondaryText,
                    unfocusedLabelColor = AppTheme.color.secondaryText,
                    focusedContainerColor = AppTheme.color.cardOnSurface,
                    unfocusedContainerColor = AppTheme.color.cardOnSurface
                )
            )

            Button(
                onClick = viewModel::handleProceedClicked,
                enabled = !state.isLoading,
            ) {
                Text(if (state.isLoading) "Please wait…" else "Proceed")
            }

            if (state.recentLogins.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    state.recentLogins.forEach { item ->
                        AssistChip(
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = AppTheme.color.cardOnSurface
                            ),
                            onClick = { viewModel.handleQuickLoginClicked(item) },
                            label = {
                                Column(
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    Text(
                                        text = item.repoDisplayText,
                                        color = AppTheme.color.secondaryText
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = item.githubUsername,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AppTheme.color.primaryText
                                    )
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

