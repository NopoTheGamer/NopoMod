package com.nopo.features.emoji

import com.google.common.reflect.TypeToken
import com.nopo.NopoMod
import com.nopo.module.Module
import com.nopo.utils.Utils
import com.nopo.utils.Utils.replace
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.network.chat.Component
import java.lang.reflect.Type

object EmojiReplace : Module("chatEmojis", NopoMod.config.chatEmojis) {

    val EMOJI_TYPE: Type? = object : TypeToken<Emojis>() {}.type
    @JvmStatic
    var emojis = listOf<Emoji>()
    @JvmStatic
    val chatList = mutableListOf<String>()

    init {
        ClientReceiveMessageEvents.MODIFY_GAME.register(::onModify)
        val json = Utils.getJsonFromJar<Emojis>("emojis.json", EMOJI_TYPE)
        emojis = json?.emojis ?: emptyList()
        for (emoji in emojis) {
            for (part in emoji.getAll()) {
                chatList.add(part)
            }
        }
    }

    private fun onModify(message: Component, actionBar: Boolean): Component {
        if (actionBar || !config.enabled) return message
        try {
            var component = message.copy()
            val text = message.string
            var hasDone = false
            val split = text.split(":")
            if (split.size < 3) return message

            for (part in split) {
                for (emoji in emojis) {
                    if (emoji.isEmoji(part)) {
                        component = component.replace(":$part:", Utils.createEmoji(emoji.name)) ?: continue
                        hasDone = true
                    }
                }
            }


            if (!hasDone) return message
            return component
        } catch (_: Exception) {
            return message
        }
    }
}