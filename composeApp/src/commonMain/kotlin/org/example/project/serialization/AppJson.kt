package org.example.project.serialization

import kotlinx.serialization.json.Json

/**
 * Shared Json configuration for the whole app (network + persistence).
 */
val AppJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    isLenient = true
    encodeDefaults = true
}

