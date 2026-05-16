package com.nopo.features.combat

import com.github.stivais.commodore.Commodore
import com.github.stivais.commodore.utils.GreedyString
import com.google.gson.annotations.Expose
import com.nopo.NopoMod
import com.nopo.config.ConfigManager
import com.nopo.config.ModuleConfig
import com.nopo.events.ChatEvent
import com.nopo.events.CommandRegistration
import com.nopo.events.ModifyChat
import com.nopo.module.FeatureModule
import com.nopo.utils.Utils
import com.nopo.utils.Utils.append
import com.nopo.utils.Utils.appendEmoji
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

    private fun getTrackedMobs(): List<String> {
        return getConfig().trackedMobs.map { it.lowercase() }
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
                            getConfig().trackedMobs.add(name.string.lowercase())
                            Utils.sendMessageToPlayer("Added ${name.string} to Cocoon Warnings")
                            ConfigManager.save()
                        }
                    }
                    "remove" {
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
                }
            }
        }
    }

    override fun onChat(message: Component, actionBar: Boolean) {
        if (actionBar) return
        if (!getConfig().enabled) return
        val string = message.string
        val mobName = cocoonRegex.group(string, "mobName") ?: return

        if (mobName.lowercase() in getTrackedMobs()) {
            Minecraft.getInstance().gui.setTitle(
                componentBuilder {
                    append("Cocooned $mobName") {
                        withColor(ChatFormatting.RED)
                    }
                }
            )
        }
    }

    override fun onModifyChat(
        message: Component,
        actionBar: Boolean
    ): Component? {
        if (actionBar) return null
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
            append("/nopo feature $moduleName add") {
                withColor(ChatFormatting.YELLOW)
            }
            append(" (or click on the message when you cocoon a mob)") {
                withColor(ChatFormatting.DARK_GRAY)
            }
            suggest = "/nopo tasks add "
            hover = Component.literal("Click to insert into chat bar")
        })
    }

    private fun buildTaskList(): List<Component> {
        val list = mutableListOf<Component>()
        getConfig().trackedMobs.forEach { mobs ->
            list.add(
                componentBuilder {
                    append(mobs)
                    append(" ")
                    append {
                        append("[")
                        appendEmoji("x") {
                            withColor(ChatFormatting.WHITE)
                        }
                        append("]")
                        withColor(ChatFormatting.GRAY)
                        command = "/nopo feature $moduleName remove $mobs"
                        hover = Component.literal("Delete this mob")
                    }
                }
            )
        }
        return list
    }
}

class CocoonConfig : ModuleConfig() {
    @Expose
    var trackedMobs: MutableList<String> = mutableListOf("lord jawbus")
}