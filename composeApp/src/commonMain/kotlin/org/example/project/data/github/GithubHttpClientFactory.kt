package org.example.project.data.github

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import org.example.project.serialization.AppJson

internal fun HttpClientConfig<*>.installGithubDefaults() {
    install(ContentNegotiation) {
        json(AppJson.instance)
    }

    defaultRequest {
        url {
            protocol = URLProtocol.HTTPS
            host = "api.github.com"
        }
        headers.append("Accept", "application/vnd.github+json")
        headers.append("X-GitHub-Api-Version", "2022-11-28")
    }
}

expect fun createGithubHttpClient(): HttpClient

