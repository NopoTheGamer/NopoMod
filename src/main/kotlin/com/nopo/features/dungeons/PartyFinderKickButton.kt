package com.nopo.features.dungeons

import com.nopo.NopoMod
import com.nopo.events.ModifyChat
import com.nopo.module.FeatureModule
import com.nopo.utils.HypixelUtils
import com.nopo.utils.Utils
import com.nopo.utils.Utils.append
import com.nopo.utils.Utils.appendEmoji
import com.nopo.utils.Utils.cleanColor
import com.nopo.utils.Utils.command
import com.nopo.utils.Utils.hover
import com.nopo.utils.Utils.withColor
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component

object PartyFinderKickButton : FeatureModule("partyFinderKickButton", NopoMod.config.partyFinderKickButton),
    ModifyChat {

    private val pfRegex = Regex("Party Finder > (?<name>[a-zA-Z0-9_]+) joined the (?:dungeon )?group! \\((?<class>[a-zA-Z]+) Level \\d+\\)")

    override fun onModifyChat(
        message: Component,
        actionBar: Boolean
    ): Component? {
        if (!HypixelUtils.onSkyblock()) return null
        if (!config.enabled) return null
        val string = message.string.cleanColor()
        if (!string.matches(pfRegex)) return null
        val name = pfRegex.matchEntire(string)?.groups["name"]?.value?.trim() ?: return null

        return Utils.componentBuilder {
            append(message)
            append {
                append(" [")
                appendEmoji("x") {
                    withColor(ChatFormatting.WHITE)
                }
                append("]")
                withColor(ChatFormatting.GRAY)
                command = "/party kick $name"
                hover = Component.literal("Kick $name from the party")
            }
        }
    }
}