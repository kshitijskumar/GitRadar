package org.example.project.data.github.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PullRequestResponseApiModel(
    val id: Long,
    val number: Int,
    val title: String,
    val state: String,
    @SerialName("html_url")
    val htmlUrl: String,
    val user: UserResponseApiModel? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null,
)

@Serializable
data class UserResponseApiModel(
    val login: String? = null,
)

