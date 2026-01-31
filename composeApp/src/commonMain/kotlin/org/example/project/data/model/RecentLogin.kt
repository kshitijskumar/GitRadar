package org.example.project.data.model

import kotlinx.serialization.Serializable

/**
 * Stored for "quick login" suggestions.
 *
 * IMPORTANT: does NOT include access token.
 */
@Serializable
data class RecentLogin(
    val repositoryLink: String,
    val githubUsername: String,
)

