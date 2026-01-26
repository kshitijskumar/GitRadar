package org.example.project.data.github.model

import org.example.project.data.model.PullRequest

internal fun PullRequestResponseApiModel.toDomain(): PullRequest =
    PullRequest(
        id = id,
        number = number,
        title = title,
        state = state,
        htmlUrl = htmlUrl,
        authorLogin = user?.login,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

