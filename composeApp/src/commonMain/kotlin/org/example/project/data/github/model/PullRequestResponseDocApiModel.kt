package org.example.project.data.github.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Matches the pull request JSON structure captured in `docs/prd/github-apis.md`.
 *
 * Note: This is intentionally "doc-shaped" and only contains fields present in that document.
 */
@Serializable
data class PullRequestResponseDocApiModel(
    val url: String,
    val id: Long,
    @SerialName("html_url") val htmlUrl: String,
    @SerialName("issue_url")
    val issueUrl: String?,
    val number: Int,
    val state: String,
    val locked: Boolean,
    val title: String,
    val user: PullRequestUserSummaryDocApiModel,
    val body: String? = null,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    @SerialName("closed_at")
    val closedAt: String? = null,
    @SerialName("merged_at")
    val mergedAt: String? = null,
    @SerialName("merge_commit_sha")
    val mergeCommitSha: String? = null,
    val assignees: List<PullRequestUserSummaryDocApiModel> = emptyList(),
    @SerialName("requested_reviewers")
    val requestedReviewers: List<PullRequestUserSummaryDocApiModel> = emptyList(),
    val draft: Boolean = false,
    @SerialName("commits_url")
    val commitsUrl: String?,
    @SerialName("review_comments_url")
    val reviewCommentsUrl: String?,
    @SerialName("review_comment_url")
    val reviewCommentUrl: String?,
    @SerialName("comments_url")
    val commentsUrl: String?,
    @SerialName("statuses_url")
    val statusesUrl: String?,
)

@Serializable
data class PullRequestUserSummaryDocApiModel(
    val login: String,
    val id: Long,
    val type: String,
)

///**
// * `github-apis.md` shows `"labels": []`. Keep a model to match the field shape exactly.
// */
//@Serializable
//data class PullRequestLabelDocApiModel(
//    // Intentionally empty: `github-apis.md` currently shows `"labels": []`
//)

///**
// * `github-apis.md` shows `"milestone": null`. Keep a model to match the field shape exactly.
// */
//@Serializable
//data class PullRequestMilestoneDocApiModel(
//    // Intentionally empty: `github-apis.md` currently shows `"milestone": null`
//)

