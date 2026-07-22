package com.nopo.features.garden

import com.github.stivais.commodore.Commodore
import com.google.gson.annotations.Expose
import com.nopo.NopoMod
import com.nopo.config.ConfigManager
import com.nopo.events.ChatEvent
import com.nopo.events.CommandRegistration
import com.nopo.events.ListCommandExtras
import com.nopo.module.ConfigData
import com.nopo.module.FeatureModule
import com.nopo.utils.DelayedRuns
import com.nopo.utils.HypixelUtils
import com.nopo.utils.IslandType
import com.nopo.utils.PartyApi
import com.nopo.utils.Utils
import com.nopo.utils.Utils.append
import com.nopo.utils.Utils.appendEmoji
import com.nopo.utils.Utils.cleanColor
import com.nopo.utils.Utils.command
import com.nopo.utils.Utils.componentBuilder
import com.nopo.utils.Utils.format
import com.nopo.utils.Utils.hover
import com.nopo.utils.Utils.withColor
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import kotlin.time.Duration.Companion.milliseconds

object RareCropTracker : FeatureModule("rareCropTracker", NopoMod.config.rareCrop,
    ConfigData(
        Component.literal("Rare Crop Tracker"),
        componentBuilder {
            append("Tells you how long it took to drop that Rare Crop")
        }
    )), ChatEvent, ListCommandExtras, CommandRegistration {

    private fun getConfig() = NopoMod.rareCropConfig.cropConfig

    /*
    RARE CROP! Seasoning (+131) (automatically donated)
    RARE CROP! Melon Juice (+162)
     */
    private val rareCropRegex = Regex("(?:VERY )?RARE CROP! (?<crop>[a-zA-Z ]+) \\(\\+[0-9,.]+\\)( \\(automatically donated\\))?")

    // PET DROP! LEGENDARY Slug (+136)
    private val petDropRegex = Regex("PET DROP! (?<pet>\\w+) \\(\\+[0-9,.]+\\)")

    private const val CROP_FEVER = "WOAH! You caught a case of the CROP FEVER for 60 seconds!"

    override fun onChat(message: Component, actionBar: Boolean) {
        if (actionBar) return
        if (!HypixelUtils.onSkyblock()) return
        doCropMessage(message)
        doPartyCommand(message)
    }

    fun doCropMessage(message: Component) {
        if (!config.enabled) return
        if (!IslandType.GARDEN.isActive()) return
        val string = message.string.cleanColor()

        if (string.matches(petDropRegex)) {
            val pet = petDropRegex.matchEntire(string)?.groups["pet"]?.value?.trim() ?: return
            // lf hypixel using components...
            val rarity = if (message.string.contains("§5$pet")) {
                "Epic"
            } else if (message.string.contains("§6$pet")) {
                "Legendary"
            } else {
                "Unknown"
            }
            addRareDrop("$rarity $pet", message)
            return
        }

        if (string == CROP_FEVER) {
            addRareDrop("CROP FEVER", message)
        }

        if (!string.matches(rareCropRegex)) return
        val crop = rareCropRegex.matchEntire(string)?.groups["crop"]?.value?.trim() ?: return
        if (NopoMod.config.debug.enabled) {
            DelayedRuns.schedule(5) {
                Utils.sendMessageToPlayer("Found crop $crop")
            }
        }
        addRareDrop(crop, message)
    }

    fun doPartyCommand(message: Component) {
        if (!getConfig().partyCommands) return
        val crop = Utils.getPartyCommand(message, "!since") ?: return
        val currentTime = System.currentTimeMillis()
        val drop = getConfig().dropTimes.entries.firstOrNull { it.key.equals(crop, ignoreCase = true) } ?: return
        val lastDropTime = drop.value.maxOrNull() ?: return
        val timeSince = (currentTime - lastDropTime).milliseconds
        Utils.sendCommandToServer("pc ${timeSince.format()} since last ${drop.key}")
    }

    fun addRareDrop(drop: String, message: Component) {
        if (drop.lowercase() == "ethereal vine" && getConfig().ignoreVines) return
        val currentTime = System.currentTimeMillis()
        if (getConfig().dropTimes[drop] == null) {
            getConfig().dropTimes[drop] = mutableListOf(currentTime)
        } else {
            val lastDrop = getConfig().dropTimes[drop]?.maxOrNull() ?: currentTime
            getConfig().dropTimes[drop]!!.add(currentTime)
            if (lastDrop != currentTime) {
                val timeSince = (currentTime - lastDrop).milliseconds
                val message = componentBuilder {
                    append("Took ")
                    append(timeSince.format())
                    append(" to drop ")
                    append(Utils.matcherOrString(message, drop))
                }
                DelayedRuns.schedule(5) {
                    Utils.sendMessageToPlayer(message)
                    if (getConfig().sendToPartyChat) PartyApi.sendPartyMessage(message.string)
                }
            }
        }
        ConfigManager.saveRareCrops()

    }

    override fun addListCommandData(): Component {
        return componentBuilder {
            append(" ")
            append {
                append("[")
                appendEmoji("speech_balloon") {
                    withColor(ChatFormatting.WHITE)
                }
                command = "/nopo feature $moduleName partyMessage"
                hover = componentBuilder {
                    append("Click to toggle sending rare crops to party chat")
                }
                append("]")
                if (getConfig().sendToPartyChat) withColor(ChatFormatting.GREEN)
                else withColor(ChatFormatting.RED)
            }
            append {
                append("[")
                appendEmoji("herb") {
                    withColor(ChatFormatting.WHITE)
                }
                command = "/nopo feature $moduleName etherealVine"
                hover = componentBuilder {
                    append("Click to toggle tracking Ethereal Vines")
                }
                append("]")
                if (getConfig().ignoreVines) withColor(ChatFormatting.RED)
                else withColor(ChatFormatting.GREEN)
            }
            append {
                append("[")
                appendEmoji("singer") {
                    withColor(ChatFormatting.WHITE)
                }
                command = "/nopo feature $moduleName partyCommand"
                hover = componentBuilder {
                    append("Click to toggle !since (crop) party command")
                }
                append("]")
                if (getConfig().partyCommands) withColor(ChatFormatting.GREEN)
                else withColor(ChatFormatting.RED)
            }
        }
    }

    override fun createCommand(): Commodore {
        return Commodore("nopo") {
            "feature" {
                moduleName {
                    "partyMessage" {
                        runs {
                            Utils.sendMessageUnlessInConfig(
                                componentBuilder {
                                    append("Sending rare crops to party chat is now ")
                                    getConfig().sendToPartyChat = !getConfig().sendToPartyChat
                                    if (getConfig().sendToPartyChat) {
                                        append("enabled")
                                    } else {
                                        append("disabled")
                                    }
                                    withColor(ChatFormatting.YELLOW)
                                    ConfigManager.save()
                                }
                            )
                        }
                    }
                    "etherealVine" {
                        runs {
                            Utils.sendMessageUnlessInConfig(
                                componentBuilder {
                                    append("Tracking ethereal vines is now ")
                                    getConfig().ignoreVines = !getConfig().ignoreVines
                                    if (getConfig().ignoreVines) {
                                        append("disabled")
                                    } else {
                                        append("enabled")
                                    }
                                    withColor(ChatFormatting.YELLOW)
                                    ConfigManager.save()
                                }
                            )
                        }
                    }
                    "partyCommand" {
                        runs {
                            Utils.sendMessageUnlessInConfig(
                                componentBuilder {
                                    append("!since party command for crops is now ")
                                    getConfig().partyCommands = !getConfig().partyCommands
                                    if (getConfig().partyCommands) {
                                        append("enabled")
                                    } else {
                                        append("disabled")
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

class RareCropConfig {
    @Expose
    var dropTimes = mutableMapOf<String, MutableList<Long>>()
    @Expose
    var sendToPartyChat = false
    @Expose
    var ignoreVines = true
    @Expose
    var partyCommands = true
}