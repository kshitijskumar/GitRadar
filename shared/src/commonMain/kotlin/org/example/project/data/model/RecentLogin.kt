package org.example.project.data.model

import kotlinx.serialization.Serializable

@Serializable
data class RecentLogin(
    val repositoryLink: String,
    val githubUsername: String,
)
