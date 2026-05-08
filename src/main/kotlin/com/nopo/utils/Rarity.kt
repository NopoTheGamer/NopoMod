package com.nopo.utils

import com.nopo.utils.Utils.matcher
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import java.util.Optional

enum class Rarity(val color: String) {
    COMMON("white"),
    UNCOMMON("green"),
    RARE("blue"),
    EPIC("dark_purple"),
    LEGENDARY("gold"),
    MYTHIC("light_purple"),
    DIVINE("aqua"),
    SPECIAL("red"),
    ULTIMATE("dark_red"),
    UNKNOWN(""),
    ;

    override fun toString(): String {
        return name.lowercase().capitalize()
    }

    companion object {
        fun getRarityByComponent(component: Component, match: String): Rarity {
            var rarity = UNKNOWN
            matcher(component, match)?.visit({ style: Style, value: String ->
                if (match == value) {
                    rarity = entries.firstOrNull { it.color == style.color?.name } ?: UNKNOWN
                }
                Optional.empty<Component>()
            }, Style.EMPTY)
            return rarity
        }
    }
}