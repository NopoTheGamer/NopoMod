package com.nopo.features.chat

import com.github.stivais.commodore.Commodore
import com.github.stivais.commodore.utils.GreedyString
import com.google.gson.annotations.Expose
import com.nopo.NopoMod
import com.nopo.config.ConfigManager
import com.nopo.config.ModuleConfig
import com.nopo.events.CommandRegistration
import com.nopo.events.EntityNameEvent
import com.nopo.events.ListCommandExtras
import com.nopo.events.ModifyChat
import com.nopo.module.ConfigData
import com.nopo.module.FeatureModule
import com.nopo.utils.Utils
import com.nopo.utils.Utils.append
import com.nopo.utils.Utils.appendEmoji
import com.nopo.utils.Utils.command
import com.nopo.utils.Utils.hover
import com.nopo.utils.Utils.replace
import com.nopo.utils.Utils.withColor
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.world.entity.player.Player
import java.awt.Color
import java.util.Optional

object NewSBLevelColours : FeatureModule("newLevelColours", NopoMod.config.newSbLevelColourConfig,
    ConfigData(
        Component.literal("New SB Level Colours"),
        Component.literal("Adds new colours for levels 520+, 560+ and 600+")
    )), ModifyChat, EntityNameEvent, ListCommandExtras, CommandRegistration {

    private val regex = Regex("\\[(?<level>\\d+)\\]")
    private val legacyRegex = Regex("§8\\[§4(?<level>\\d+)§8\\]")

    fun getConfig() = config as LevelColourConfig

    override fun onModifyChat(
        message: Component,
        actionBar: Boolean
    ): Component? {
        if (actionBar) return null
        return doModification(message)
    }

    @JvmStatic
    fun doModification(message: Component): Component? {
        if (!config.enabled) return null
        val data = NopoMod.data?.levelColours ?: return null
        var message = message
        val legacy = convertLegacyColour(message)
        if (legacy != null) message = legacy

        val string = message.string
        val find = regex.find(string) ?: return null
        val level = find.groups["level"]?.value ?: return null
        val newLevelComponent = Component.empty()
        message.visit({ style: Style, string: String ->
            if (string.isEmpty()) return@visit Optional.empty()
            if (string != level || (style.color?.name != "dark_red" && style.color?.value != 11141120)) {
                newLevelComponent.append(Component.literal(string).withStyle(style))
                return@visit Optional.empty()
            }

            val level = level.toInt()
            for (levelColour in data) {
                val lowLevel = levelColour.lowLevel ?: continue
                val highLevel = levelColour.highLevel ?: continue
                if (level !in lowLevel..highLevel) continue
                if (levelColour.rainbow == true) {
                    if (!getConfig().rainbow) continue
                    newLevelComponent.append(Utils.rainbow(string, style, levelColour.rainbowSpeed ?: 5))
                } else {
                    val colour = levelColour.colour ?: continue
                    newLevelComponent.append(Component.literal(string).withStyle(style).withColor(colour))
                }
                return@visit Optional.empty()
            }
            newLevelComponent.append(Component.literal(string).withStyle(style))

            Optional.empty()
        }, Style.EMPTY)

        return newLevelComponent
    }

    /**
     * In some places they use colour codes instead of components
     * so we have to come up with our own component for them. yay!!
     */
    fun convertLegacyColour(message: Component): Component? {
        val find = legacyRegex.find(message.string) ?: return null
        val level = find.groups["level"]?.value ?: return null
        val oldStyle = Utils.matcher(message, level)?.style ?: Style.EMPTY
        val fixedLevelComponent = Utils.componentBuilder {
            withStyle(oldStyle)
            append("[")
            append(level) {
                withColor(ChatFormatting.DARK_RED)
            }
            append("]")
            withColor(ChatFormatting.DARK_GRAY)
        }
        return message.replace(find.value, fixedLevelComponent)
    }

    override fun onEntityName(
        entity: Player,
        original: Component
    ): Component? {
        return doModification(original)
    }

    override fun addListCommandData(): Component {
        return Utils.componentBuilder {
            append(" ")
            append {
                append("[")
                appendEmoji("rainbow") {
                    withColor(ChatFormatting.WHITE)
                }
                command = "/nopo feature $moduleName rainbow"
                hover = Component.literal("Click to toggle max level being rainbow coloured")
                append("]")
                if (getConfig().rainbow) withColor(ChatFormatting.GREEN)
                else withColor(ChatFormatting.RED)
            }
        }
    }

    override fun createCommand(): Commodore {
        return Commodore("nopo") {
            "colourTextTest" {
                runs { hex: GreedyString ->
                    val hexes = hex.string.split(" ")
                    for (hex in hexes) {
                        Utils.sendMessageToPlayer(
                            Utils.componentBuilder {
                                append("[")
                                append("487") {
                                    withColor(Color.decode(hex).rgb)
                                }
                                append("]")
                                withColor(ChatFormatting.DARK_GRAY)
                            }
                        )
                    }
                }
            }
            "feature" {
                moduleName {
                    "rainbow" {
                        runs {
                            Utils.sendMessageUnlessInConfig(
                                Utils.componentBuilder {
                                    getConfig().rainbow = !getConfig().rainbow
                                    if (getConfig().rainbow) {
                                        append("Max Skyblock Level will now be rainbow")
                                    } else {
                                        append("Max Skyblock Level will no longer be rainbow")
                                    }
                                    withColor(ChatFormatting.YELLOW)
                                    ConfigManager.save()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

data class CustomLevelColours(
    @Expose val lowLevel: Int?,
    @Expose val highLevel: Int?,
    @Expose val colour: Int?,
    @Expose val rainbow: Boolean? = false,
    @Expose val rainbowSpeed: Int?,
)

class LevelColourConfig : ModuleConfig() {
    @Expose
    var rainbow: Boolean = true
}