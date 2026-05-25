package org.example.project.serialization

import kotlinx.serialization.json.Json

val AppJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    isLenient = true
    encodeDefaults = true
}

inline fun <reified T> Json.decodeFromStringSafely(string: String): T? {
    return runCatching { decodeFromString<T>(string) }.getOrNull()
}
