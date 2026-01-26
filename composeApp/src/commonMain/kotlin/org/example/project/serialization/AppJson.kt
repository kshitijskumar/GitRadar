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

inline fun <reified T>Json.decodeFromStringSafely(data: String): T? {
    return try {
        this.decodeFromString<T>(data)
    } catch (e: Exception) {
        null
    }
}

