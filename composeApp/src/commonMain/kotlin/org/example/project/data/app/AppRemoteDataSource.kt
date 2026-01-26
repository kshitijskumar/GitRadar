package org.example.project.data.app

import org.example.project.data.model.LoggedInUser
import org.example.project.data.model.PullRequest

interface AppRemoteDataSource {

    /**
     * Fetches open PRs for the repo derived from the stored [LoggedInUser.repositoryLink].
     * Uses GitHub REST `GET /repos/{owner}/{repo}/pulls` with `state=open&sort=created&per_page=100`.
     */
    suspend fun listOpenPullRequests(
        user: LoggedInUser,
        page: Int = 1,
        perPage: Int = 100,
    ): List<PullRequest>

}

