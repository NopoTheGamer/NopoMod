package com.nopo.features.emoji

import com.nopo.NopoMod
import com.nopo.events.EntityNameEvent
import com.nopo.module.BaseModule
import com.nopo.utils.Utils.appendEmoji
import com.nopo.utils.Utils.componentBuilder
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Player

object EmojiName : BaseModule("emoji name"), EntityNameEvent {
    override fun onEntityName(
        entity: Player,
        original: Component
    ): Component? {
        val uuid = entity.gameProfile.id
        val emojis = NopoMod.data?.emojisName?.get(uuid) ?: return null
        return componentBuilder {
            append(original)
            for (emoji in emojis) {
                appendEmoji(emoji)
            }
        }
    }
}