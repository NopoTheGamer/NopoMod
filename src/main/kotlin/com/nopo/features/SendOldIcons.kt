package com.nopo.features

import com.google.gson.annotations.Expose
import com.google.gson.reflect.TypeToken
import com.nopo.NopoMod
import com.nopo.events.ModifyOutgoingMessages
import com.nopo.module.FeatureModule
import com.nopo.utils.Utils
import java.lang.reflect.Type

object SendOldIcons : FeatureModule("sendOldIcons", NopoMod.config.sendOldIconConfig), ModifyOutgoingMessages {

    var iconMap: Map<String, Icons>? = null

    private val EMOJI_TYPE: Type? = object : TypeToken<Map<String, Icons>>() {}.type

    init {
        val json = Utils.getJsonFromJar<Map<String, Icons>>("sbicons.json", EMOJI_TYPE)
        iconMap = json
    }

    override fun onChatSent(message: String): String {
        if (!config.enabled) return message
        if (iconMap == null) return message
        var newMessage = message
        for ((name, icon) in iconMap) {
            newMessage = newMessage.replace(icon.to, icon.from)
        }
        return newMessage
    }
}

data class Icons(
    @Expose val from: String,
    @Expose val to: String
)