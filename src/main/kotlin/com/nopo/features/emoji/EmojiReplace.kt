package com.nopo.features.emoji

import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import com.nopo.Module
import com.nopo.NopoMod
import com.nopo.Utils
import com.nopo.Utils.replace
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.chat.Component
import java.lang.reflect.Type
import java.nio.file.Files

object EmojiReplace : Module("chatEmojis", NopoMod.config.chatEmojis) {

    val EMOJI_TYPE: Type? = object : TypeToken<Emojis>() {}.type
    @JvmStatic
    var emojis = listOf<Emoji>()
    @JvmStatic
    val chatList = mutableListOf<String>()

    init {
        ClientReceiveMessageEvents.MODIFY_GAME.register(::onModify)
        val path = FabricLoader.getInstance().getModContainer("nopo").get()
            .findPath("assets/nopo/emojis.json").get()
        val newInputStream = Files.newInputStream(path).reader()
        val jsonReader = JsonReader(newInputStream)
        val json = Gson().fromJson<Emojis>(jsonReader, EMOJI_TYPE)
        emojis = json.emojis
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