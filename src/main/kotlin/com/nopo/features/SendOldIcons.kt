package com.nopo.features

import com.google.gson.annotations.Expose
import com.google.gson.reflect.TypeToken
import com.nopo.NopoMod
import com.nopo.events.ModifyChat
import com.nopo.events.ModifyOutgoingMessages
import com.nopo.module.ConfigData
import com.nopo.module.FeatureModule
import com.nopo.utils.HypixelUtils
import com.nopo.utils.Utils
import com.nopo.utils.Utils.appendWithColor
import com.nopo.utils.Utils.replace
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import java.lang.reflect.Type

object SendOldIcons : FeatureModule("sendOldIcons", NopoMod.config.sendOldIconConfig, ConfigData(
    Component.literal("Send Stat Icons In Chat"),
    Utils.componentBuilder {
        append("Converts stat icons ")
        appendWithColor("eg ", ChatFormatting.RED)
        appendWithColor("(+398 \uE01A Magic Find) ", ChatFormatting.AQUA)
        append("to their old versions when you send them in chat on Hypixel")
    }
)), ModifyOutgoingMessages, ModifyChat {

    var iconMap: Map<String, Icons>? = null

    private val EMOJI_TYPE: Type? = object : TypeToken<Map<String, Icons>>() {}.type

    init {
        val json = Utils.getJsonFromJar<Map<String, Icons>>("sbicons.json", EMOJI_TYPE)
        iconMap = json
    }

    override fun onChatSent(message: String): String {
        if (!config.enabled) return message
        if (!HypixelUtils.onSkyblock()) return message
        if (iconMap == null) return message
        var newMessage = message
        for ((_, icon) in iconMap) {
            newMessage = newMessage.replace(icon.to, icon.from)
        }
        return newMessage
    }

    override fun onModifyChat(message: Component, actionBar: Boolean): Component? {
        if (!config.enabled) return null
        if (!HypixelUtils.onSkyblock()) return null
        if (iconMap == null) return null
        var hasReplaced = false
        var newMessage = message.copy()
        for ((name, icon) in iconMap) {
            // hypixel uses a bunch of old stat symbols as icons in other places so this kinda cooks some stuff
            // magic find is the only stat that is sent enough to be worthwhile to replace
            if (name != "MAGIC_FIND") continue
            newMessage = newMessage.replace(icon.from, icon.to) ?: continue
            hasReplaced = true
        }
        if (hasReplaced) return newMessage
        return message
    }
}

data class Icons(
    @Expose val from: String,
    @Expose val to: String
)