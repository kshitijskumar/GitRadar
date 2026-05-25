package org.example.project.data.pulls

import org.example.project.data.github.model.PullRequestResponseDocApiModel

data class PullRequestAppModel(
    val pr: PullRequestResponseDocApiModel,
    val status: PullRequestStatus
)
