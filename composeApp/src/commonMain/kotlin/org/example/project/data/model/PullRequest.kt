package org.example.project.data.model

data class PullRequest(
    val id: Long,
    val number: Int,
    val title: String,
    val state: String,
    val htmlUrl: String,
    val authorLogin: String?,
    val createdAt: String?,
    val updatedAt: String?,
)

