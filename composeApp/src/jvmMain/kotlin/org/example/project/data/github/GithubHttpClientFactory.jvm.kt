package org.example.project.data.github

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO

actual fun createGithubHttpClient(): HttpClient =
    HttpClient(CIO) {
        installGithubDefaults()
    }

