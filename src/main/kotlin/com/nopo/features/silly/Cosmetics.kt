package com.nopo.features.silly

import com.google.gson.annotations.Expose
import com.nopo.NopoMod
import com.nopo.events.EntityNameEvent
import com.nopo.events.ModifyChat
import com.nopo.module.ConfigData
import com.nopo.module.FeatureModule
import com.nopo.utils.Utils
import com.nopo.utils.Utils.appendEmoji
import com.nopo.utils.Utils.replace
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.world.entity.player.Player
import java.awt.Color
import java.util.Optional
import java.util.UUID

object Cosmetics : FeatureModule(
    "cosmetics", NopoMod.config.cosmeticConfig, ConfigData(
        Component.literal("Cosmetics"),
        Component.literal("Custom name colours + emojis after name")
    ), shouldBeHidden = { !Utils.isDevAllowed() && !Utils.hasCosmetics() }),
    ModifyChat, EntityNameEvent {

    override fun onModifyChat(
        message: Component,
        actionBar: Boolean
    ): Component? {
        if (actionBar) return null
        if (!message.string.contains("Throwpo")) return null
        val comp = getCosmeticData("meowgirlemily")?.getComponent(message) ?: return null
        return comp
    }

    override fun onEntityName(
        entity: Player,
        original: Component
    ): Component? {
        val data = getCosmeticData(entity.gameProfile.id) ?: return null
        return getNameFromCosmeticData(original, data)
    }

    @JvmStatic
    fun getCosmeticData(name: String?): CosmeticData? {
        if (name == null) return null
        val cosmetics = NopoMod.data?.cosmetics ?: return null
        for (data in cosmetics) {
            if (name != data.name) continue
            return data
        }
        return null
    }

    @JvmStatic
    fun getCosmeticData(uuid: UUID?): CosmeticData? {
        if (uuid == null) return null
        val cosmetics = NopoMod.data?.cosmetics ?: return null
        for (data in cosmetics) {
            if (uuid != data.uuid) continue
            return data
        }
        return null
    }

    @JvmStatic
    fun getCosmeticDataFromTab(component: Component?): CosmeticData? {
        if (component == null) return null
        var result: CosmeticData? = null
        val cosmetics = NopoMod.data?.cosmetics ?: return null
            component.visit({ style: Style, string: String ->
                if (string.isEmpty()) return@visit Optional.empty()
                for (data in cosmetics) {
                    if (string.trim() == data.name) {
                        result = data
                        return@visit Optional.empty()
                    }
                }
                Optional.empty()
            }, Style.EMPTY)
        return result
    }

    fun CosmeticData.getComponent(original: Component): Component? {
        return getNameFromCosmeticData(original, this)
    }

    @JvmStatic
    fun getNameFromCosmeticData(original: Component, data: CosmeticData?): Component? {
        if (!config.enabled) return null
        if (data == null) return null
        if (data.name == null) return null
        var customName: Component? = null
        if (data.startColour != null && data.endColour != null) {
            val animated = Utils.createGradientText(
                Color(data.startColour), Color(data.endColour), data.name, data.animatedColour != false
            )
            customName = original.replace(
                data.name, animated
            )
        }
        if (customName == null && data.emojis.isNullOrEmpty()) return null
        return Utils.componentBuilder {
            if (customName != null) append(customName)
            else append(original)
            if (!data.emojis.isNullOrEmpty()) {
                append(" ")
                for (emoji in data.emojis) {
                    appendEmoji(emoji)
                }
            }
        }
    }

}

data class CosmeticData(
    @Expose val name: String?,
    @Expose val uuid: UUID?,
    @Expose val startColour: Int?,
    @Expose val endColour: Int?,
    @Expose val animatedColour: Boolean? = true,
    @Expose val emojis: List<String>?,
)