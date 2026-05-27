package com.github.kshitijskumar.gitradar.git

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import git4idea.repo.GitRepository
import git4idea.repo.GitRepositoryChangeListener
import git4idea.repo.GitRepositoryManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.example.project.data.model.GithubRepoRef

class RepoDetector(project: Project, parentDisposable: Disposable) {

    private val _detectedRepo = MutableStateFlow<GithubRepoRef?>(null)
    val detectedRepo: StateFlow<GithubRepoRef?> = _detectedRepo.asStateFlow()

    init {
        detectRepo(project)
        project.messageBus.connect(parentDisposable).subscribe(
            GitRepository.GIT_REPO_CHANGE,
            GitRepositoryChangeListener { _ -> detectRepo(project) },
        )
    }

    private fun detectRepo(project: Project) {
        val url = GitRepositoryManager.getInstance(project)
            .repositories
            .flatMap { it.remotes }
            .flatMap { it.urls }
            .firstOrNull { it.startsWith("https://github.com/") }
        _detectedRepo.value = url?.let { parseHttpsGithubUrl(it) }
    }

    private fun parseHttpsGithubUrl(url: String): GithubRepoRef? {
        val withoutProtocol = url.removePrefix("https://")
        if (!withoutProtocol.startsWith("github.com/")) return null
        val segments = withoutProtocol
            .removePrefix("github.com/")
            .split('/')
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
        private val validSegmentRegex = Regex("""^[A-Za-z0-9_.-]+$""")
    }
}
