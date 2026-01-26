package org.example.project.data.github

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

actual fun createGithubHttpClient(): HttpClient =
    HttpClient(OkHttp) {
        installGithubDefaults()
    }

