package org.example.project.data.model

data class LoggedInUser(
    val repositoryLink: String,
    val accessToken: String,
    val githubUsername: String,
) {
    val githubRepoRef: GithubRepoRef? by lazy {
        parseGithubRepoRefOrNull(repositoryLink)
    }

    private fun parseGithubRepoRefOrNull(repositoryLink: String): GithubRepoRef? {
        val raw = repositoryLink.trim()
        if (raw.isEmpty()) return null

        val normalized = raw
            .removePrefix("https://")
            .removePrefix("http://")
            .removePrefix("www.")

        val afterHost = if (normalized.startsWith("github.com/")) {
            normalized.removePrefix("github.com/")
        } else {
            normalized
        }

        val segments = afterHost
            .split('/')
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        if (segments.size < 2) return null

        val owner = segments[0]
        val repo = segments[1].removeSuffix(".git")

        if (!ownerRepoRegex.matches(owner)) return null
        if (!ownerRepoRegex.matches(repo)) return null

        return GithubRepoRef(owner = owner, repo = repo)
    }

    private companion object {
        private val ownerRepoRegex = Regex("""^[A-Za-z0-9_.-]+$""")
    }
}

