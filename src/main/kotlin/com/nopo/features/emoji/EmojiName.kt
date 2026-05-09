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
        val emoji = NopoMod.data?.emojiName?.get(uuid) ?: return null
        return componentBuilder {
            append(original)
            appendEmoji(emoji)
        }
    }
}