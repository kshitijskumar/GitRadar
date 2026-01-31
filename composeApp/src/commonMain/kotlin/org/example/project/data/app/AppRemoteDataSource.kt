package org.example.project.data.app

import org.example.project.data.github.model.PullRequestResponseDocApiModel

interface AppRemoteDataSource {

    /**
     * Fetches open PRs for the repo derived from the **currently stored** logged-in user.
     *
     * Based on `docs/prd/github-apis.md`:
     * - `GET /repos/{owner}/{repo}/pulls`
     * - `per_page=100` (max allowed by GitHub)
     * - `Authorization: Bearer <token>` (from stored user)
     * - defaults in [createGithubHttpClient] add `Accept` + `X-GitHub-Api-Version` headers
     */
    suspend fun listOpenPullRequests(
        page: Int = 1,
    ): List<PullRequestResponseDocApiModel>

}

