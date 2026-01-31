package org.example.project.data.pulls

import org.example.project.data.github.model.PullRequestResponseDocApiModel
import org.example.project.screens.dashboard.PullRequestStatus

data class PullRequestAppModel(
    val pr: PullRequestResponseDocApiModel,
    val status: PullRequestStatus
)