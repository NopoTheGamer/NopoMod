package com.nopo.features.combat

import com.github.stivais.commodore.Commodore
import com.github.stivais.commodore.utils.GreedyString
import com.google.gson.annotations.Expose
import com.nopo.NopoMod
import com.nopo.config.ConfigManager
import com.nopo.config.PositionConfig
import com.nopo.events.ChatEvent
import com.nopo.events.CommandRegistration
import com.nopo.events.ModifyChat
import com.nopo.module.FeatureModule
import com.nopo.screens.GuiEditor
import com.nopo.utils.HypixelUtils
import com.nopo.utils.PartyApi
import com.nopo.utils.TitleApi
import com.nopo.utils.Utils
import com.nopo.utils.Utils.append
import com.nopo.utils.Utils.appendEmoji
import com.nopo.utils.Utils.appendWithColor
import com.nopo.utils.Utils.command
import com.nopo.utils.Utils.componentBuilder
import com.nopo.utils.Utils.group
import com.nopo.utils.Utils.hover
import com.nopo.utils.Utils.suggest
import com.nopo.utils.Utils.withColor
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component

object CocoonWarning : FeatureModule("cocoonTitle", NopoMod.config.cocoonConfig), CommandRegistration, ChatEvent, ModifyChat {

    private fun getConfig() = config as CocoonConfig

    private fun getTrackedMobs(): Map<String, CocoonWarningOptions> {
        return getConfig().trackedMobs
    }

    /**
     * CAUGHT! You cocooned a Sea Archer!
     * CAUGHT! You cocooned a Zombie!
     */
    private val cocoonRegex = Regex("CAUGHT! You cocooned a (?<mobName>[^!]+)!")

    override fun createCommand(): Commodore {
        return Commodore("nopo") {
            "feature" {
                moduleName {
                    "add" {
                        runs { name: GreedyString ->
                            getConfig().trackedMobs[name.string.lowercase()] = CocoonWarningOptions()
                            Utils.sendMessageToPlayer("Added ${name.string} to Cocoon Warnings")
                            ConfigManager.save()
                        }
                    }
                    literal("partyMessage").executable {
                        param("name").suggests {
                            getTrackedMobs().map { it.key }
                        }
                        runs { name: GreedyString ->
                            val options = getConfig().trackedMobs[name.string.lowercase()]
                            if (options == null) {
                                Utils.sendMessageToPlayer(
                                    componentBuilder {
                                        appendWithColor("This mob is not tracked", ChatFormatting.RED)
                                        command = "/nopo feature $moduleName add $name"
                                        hover = Component.literal("Click to track this mob")
                                    }
                                )
                                return@runs
                            }
                            options.sendToPartyChat = !options.sendToPartyChat
                            if (options.sendToPartyChat) {
                                Utils.sendMessageToPlayer("You will share ${name.string} to Party Chat")
                            } else {
                                Utils.sendMessageToPlayer("You will no longer share ${name.string} to Party Chat")
                            }
                            ConfigManager.save()
                        }
                    }
                    literal("remove").executable {
                        param("name").suggests {
                            getTrackedMobs().map { it.key }
                        }
                        runs { name: GreedyString ->
                            getConfig().trackedMobs.remove(name.string.lowercase())
                            Utils.sendMessageToPlayer("Removed ${name.string} from Cocoon Warnings")
                            ConfigManager.save()
                        }
                    }
                    "list" {
                        runs {
                            val taskCount = getConfig().trackedMobs.size
                            if (taskCount == 0) {
                                sendEmptyMessage()
                                return@runs
                            }
                            Utils.sendMessageToPlayer("Mobs ($taskCount)")
                            buildTaskList().forEach {
                                Utils.sendMessageToPlayer(it)
                            }
                        }
                    }
                    "deleteall" {
                        runs {
                            getConfig().trackedMobs.clear()
                            Utils.sendMessageToPlayer("Deleted all Cocoon Warnings!")
                            ConfigManager.save()
                        }
                    }
                    "setPos" {
                        runs { x: Int?, y: Int? ->
                            if (x == null) {
                                NopoMod.screenToOpen = GuiEditor(getConfig().pos) {
                                    it.text(
                                        Minecraft.getInstance().font,
                                        componentBuilder {
                                            appendWithColor("Cocooned Zombie", ChatFormatting.RED)
                                        },
                                        0,
                                        0,
                                        -1
                                    )
                                }
                            } else if (y == null) {
                                Utils.sendMessageToPlayer("Missing y argument")
                            } else {
                                getConfig().pos.x = x
                                getConfig().pos.y = y
                                Utils.sendMessageToPlayer("Updated position")
                                ConfigManager.save()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onChat(message: Component, actionBar: Boolean) {
        if (actionBar) return
        if (!HypixelUtils.onSkyblock()) return
        if (!getConfig().enabled) return
        val string = message.string
        val mobName = cocoonRegex.group(string, "mobName") ?: return
        val mob = getTrackedMobs()[mobName.lowercase()] ?: return
        if (!mob.enabled) return
        TitleApi.displayTitle(
            componentBuilder {
                appendWithColor("Cocooned $mobName", ChatFormatting.RED)
            },
            getConfig().pos
        )
        if (mob.sendToPartyChat) {
            PartyApi.sendPartyMessage("I cocooned $mobName!")
        }
    }

    override fun onModifyChat(
        message: Component,
        actionBar: Boolean
    ): Component? {
        if (actionBar) return null
        if (!HypixelUtils.onSkyblock()) return null
        if (!getConfig().enabled) return null
        val string = message.string
        val mobName = cocoonRegex.group(string, "mobName") ?: return null
        val inList = mobName.lowercase() in getTrackedMobs()
        return componentBuilder {
            append(message)
            if (inList) {
                hover = Component.literal("Click to stop sending titles when this mob gets cocooned")
                command = "/nopo feature $moduleName remove $mobName"
            } else {
                hover = Component.literal("Click to send a title when this mob gets cocooned")
                command = "/nopo feature $moduleName add $mobName"
            }
        }
    }

    private fun sendEmptyMessage() {
        Utils.sendMessageToPlayer(componentBuilder {
            append("No mobs tracked. Add some with ")
            appendWithColor("/nopo feature $moduleName add", ChatFormatting.YELLOW)
            appendWithColor(" (or click on the message when you cocoon a mob)", ChatFormatting.DARK_GRAY)
            suggest = "/nopo feature $moduleName add "
            hover = Component.literal("Click to insert into chat bar")
        })
    }

    private fun buildTaskList(): List<Component> {
        val list = mutableListOf<Component>()
        getConfig().trackedMobs.forEach { mob ->
            list.add(
                componentBuilder {
                    append(mob.key)
                    append(" ")
                    append {
                        append("[")
                        appendEmoji("x") {
                            withColor(ChatFormatting.WHITE)
                        }
                        append("]")
                        withColor(ChatFormatting.GRAY)
                        command = "/nopo feature $moduleName remove ${mob.key}"
                        hover = Component.literal("Delete this mob")
                    }
                    append {
                        append("[")
                        appendEmoji("speech_balloon") {
                            withColor(ChatFormatting.WHITE)
                        }
                        append("]")
                        if (mob.value.sendToPartyChat) withColor(ChatFormatting.GREEN)
                        else withColor(ChatFormatting.RED)
                        command = "/nopo feature $moduleName partyMessage ${mob.key}"
                        hover = Component.literal("Toggle sending this mob to party chat")
                    }
                }
            )
        }
        return list
    }
}

class CocoonConfig : PositionConfig(300, 240, 2.5f) {
    @Expose
    var trackedMobs: MutableMap<String, CocoonWarningOptions> = mutableMapOf("lord jawbus" to CocoonWarningOptions())
}

class CocoonWarningOptions {
    @Expose
    var enabled = true
    @Expose
    var sendToPartyChat = true
}