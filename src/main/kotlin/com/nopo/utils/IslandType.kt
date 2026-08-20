package com.nopo.utils

enum class IslandType(val map: String, val mode: String) {

    HUB("Hub", "hub"),
    GARDEN("Garden", "garden"),
    RIFT("The Rift", "rift"),
    DUNGEON("Dungeon", "dungeon"),
    PRIVATE_ISLAND("Private Island", "dynamic"),
    SAFARI("Safari", "safari"),
    UNKNOWN("null", "null"),

    ;

    fun isActive(): Boolean {
        return this == HypixelUtils.currentIsland
    }
}