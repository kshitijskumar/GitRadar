package org.example.project.data.app

import org.example.project.data.github.model.PullRequestResponseDocApiModel

interface AppRemoteDataSource {

    suspend fun listOpenPullRequests(
        page: Int = 1,
    ): List<PullRequestResponseDocApiModel>

}
