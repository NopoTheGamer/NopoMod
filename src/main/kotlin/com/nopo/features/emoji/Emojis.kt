package com.nopo.features.emoji

import com.google.gson.annotations.Expose

data class Emojis(
    @Expose val emojis: List<Emoji>,
)

data class Emoji(
    @Expose val name: String,
    @Expose val alternatives: List<String>? = null,
) {
    fun isEmoji(part: String): Boolean {
        if (part == name || ":$part:" in getColonAlternatives()) return true
        return false
    }

    fun getColonAlternatives(): List<String> {
        val newList = mutableListOf<String>()
        if (alternatives == null) return emptyList()
        for (alt in alternatives) {
            newList.add(":$alt:")
        }
        return newList
    }

    fun getAll(): List<String> {
        return getColonAlternatives() + ":$name:"
    }
}