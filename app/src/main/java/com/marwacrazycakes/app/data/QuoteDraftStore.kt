package com.marwacrazycakes.app.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Holds quote details gathered by the assistant until the contact form is opened.
 * Mirrors the React Native `src/assistant/quoteDraft.ts` module store.
 */
object QuoteDraftStore {

    val fields = listOf(
        "name",
        "email",
        "phone",
        "eventType",
        "eventDate",
        "servings",
        "location",
        "theme",
    )

    private val _draft = MutableStateFlow<Map<String, String>>(emptyMap())
    val draft: StateFlow<Map<String, String>> = _draft.asStateFlow()

    fun read(): Map<String, String> = _draft.value

    fun write(patch: Map<String, String>) {
        val next = _draft.value.toMutableMap()
        for (field in fields) {
            val value = patch[field]?.trim().orEmpty()
            if (value.isNotEmpty()) next[field] = value
        }
        _draft.value = next
    }

    fun clear() {
        _draft.value = emptyMap()
    }
}
