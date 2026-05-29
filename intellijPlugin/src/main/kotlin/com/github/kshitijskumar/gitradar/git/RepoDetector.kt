package com.github.kshitijskumar.gitradar.git

import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import git4idea.repo.GitRepository
import git4idea.repo.GitRepositoryChangeListener
import git4idea.repo.GitRepositoryManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.data.model.GithubRepoRef

class RepoDetector(project: Project, parentDisposable: Disposable, private val scope: CoroutineScope) {

    private val _detectedRepo = MutableStateFlow<GithubRepoRef?>(null)
    val detectedRepo: StateFlow<GithubRepoRef?> = _detectedRepo.asStateFlow()

    private var debounceJob: Job? = null

    init {
        detectRepo(project)
        project.messageBus.connect(parentDisposable).subscribe(
            GitRepository.GIT_REPO_CHANGE,
            GitRepositoryChangeListener { _ ->
                log.info("[GitKshitij1] GIT_REPO_CHANGE received, debouncing detectRepo")
                debounceJob?.cancel()
                debounceJob = scope.launch {
                    delay(500)
                    detectRepo(project)
                }
            },
        )
    }

    private fun detectRepo(project: Project) {
        val repos = GitRepositoryManager.getInstance(project).repositories
        log.info("[GitKshitij1] detectRepo: found ${repos.size} git repositories")

        if (repos.isEmpty()) {
            // Transient state during VCS refresh — keep the current value rather than clearing it.
            log.info("[GitKshitij1] detectRepo: skipping update, empty repo list is transient")
            return
        }

        repos.forEach { repo ->
            repo.remotes.forEach { remote ->
                log.info("[GitKshitij1] remote '${remote.name}': ${remote.urls}")
            }
        }
        val ref = repos
            .flatMap { it.remotes }
            .flatMap { it.urls }
            .firstNotNullOfOrNull { parseGithubUrl(it) }
        log.info("[GitKshitij1] detected repo: $ref")
        _detectedRepo.value = ref
    }

    private fun parseGithubUrl(url: String): GithubRepoRef? =
        parseHttpsGithubUrl(url) ?: parseSshGithubUrl(url)

    private fun parseHttpsGithubUrl(url: String): GithubRepoRef? {
        if (!url.startsWith("https://")) return null
        // Strip optional embedded credentials: https://user@github.com/ -> https://github.com/
        val normalized = url.replaceFirst(Regex("^https://[^@/]+@"), "https://")
        if (!normalized.startsWith("https://github.com/")) return null
        return extractOwnerRepo(normalized.removePrefix("https://github.com/"))
    }

    // Handles both git@github.com:owner/repo.git and ssh://git@github.com/owner/repo.git
    private fun parseSshGithubUrl(url: String): GithubRepoRef? {
        val path = when {
            url.startsWith("git@github.com:") -> url.removePrefix("git@github.com:")
            url.startsWith("ssh://git@github.com/") -> url.removePrefix("ssh://git@github.com/")
            else -> return null
        }
        return extractOwnerRepo(path)
    }

    private fun extractOwnerRepo(path: String): GithubRepoRef? {
        val segments = path.split('/')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        if (segments.size < 2) return null
        val owner = segments[0]
        val repo = segments[1].removeSuffix(".git")
        if (!validSegmentRegex.matches(owner)) return null
        if (!validSegmentRegex.matches(repo)) return null
        return GithubRepoRef(owner = owner, repo = repo)
    }

    companion object {
        private val log = Logger.getInstance(RepoDetector::class.java)
        private val validSegmentRegex = Regex("""^[A-Za-z0-9_.-]+$""")
    }
}
