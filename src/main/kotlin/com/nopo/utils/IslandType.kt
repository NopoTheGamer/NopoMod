package com.nopo.utils

enum class IslandType(val map: String, val mode: String) {

    HUB("Hub", "hub"),
    GARDEN("Garden", "garden"),
    UNKNOWN("null", "null"),

    ;

    fun isActive(): Boolean {
        return this == HypixelUtils.currentIsland
    }
}