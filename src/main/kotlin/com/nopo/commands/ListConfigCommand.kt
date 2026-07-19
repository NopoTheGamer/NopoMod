package com.nopo.commands

import com.github.stivais.commodore.Commodore
import com.nopo.NopoMod
import com.nopo.categories.Category
import com.nopo.config.PositionConfig
import com.nopo.events.CommandRegistration
import com.nopo.events.ListCommandExtras
import com.nopo.module.BaseModule
import com.nopo.module.FeatureModule
import com.nopo.utils.Utils
import com.nopo.utils.Utils.append
import com.nopo.utils.Utils.appendEmoji
import com.nopo.utils.Utils.command
import com.nopo.utils.Utils.componentBuilder
import com.nopo.utils.Utils.hover
import com.nopo.utils.Utils.withColor
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component

object ListConfigCommand : BaseModule("list config"), CommandRegistration {

    var currentCategory: String? = null

    override fun createCommand(): Commodore {
        return Commodore("nopo") {
            "list" {
                runs {
                    Utils.sendMessageToPlayer("Current Config:")
                    for (module in NopoMod.modules) {
                        if (module is Category) {
                            currentCategory = module.moduleName
                            continue
                        }
                        if (module !is FeatureModule) continue
                        if (module.shouldBeHidden()) continue
                        printCategory()
                        Utils.sendMessageToPlayer(
                            printModule(module)
                        )
                    }
                    if (!NopoMod.config.usedConfigMenu) {
                        Utils.sendMessageToPlayer(
                            componentBuilder {
                                appendEmoji("rotating_light")
                                append(" New ")
                                appendEmoji("rotating_light")
                                append(" Config screen has been added use ")
                                append("/nopo") {
                                    withColor(ChatFormatting.YELLOW)
                                }
                                append(" to open")

                                hover = Component.literal("Click to open the config menu")
                                command = "/nopo"
                            }
                        )
                    }
                }
                "modules" {
                    runs {
                        val featureModules = mutableListOf<String>()
                        val commandModules = mutableListOf<String>()
                        val hiddenModules = mutableListOf<String>()
                        val otherModules = mutableListOf<String>()
                        for (module in NopoMod.modules) {
                            if (module.shouldBeHidden()) {
                                hiddenModules.add(module.moduleName)
                            } else if (module is FeatureModule) {
                                featureModules.add(module.moduleName)
                            } else if (module is CommandRegistration) {
                                commandModules.add(module.moduleName)
                            } else {
                                otherModules.add(module.moduleName)
                            }
                        }

                        if (featureModules.isNotEmpty()) {
                            Utils.sendMessageToPlayer("Features (${featureModules.size})")
                            featureModules.forEach {
                                Utils.sendMessageToPlayer(it)
                            }
                        }
                        if (commandModules.isNotEmpty()) {
                            Utils.sendMessageToPlayer("Commands (${commandModules.size})")
                            commandModules.forEach {
                                Utils.sendMessageToPlayer(it)
                            }
                        }
                        if (otherModules.isNotEmpty()) {
                            Utils.sendMessageToPlayer("Other Modules (${otherModules.size})")
                            otherModules.forEach {
                                Utils.sendMessageToPlayer(it)
                            }
                        }
                        if (Utils.isDevAllowed() && hiddenModules.isNotEmpty()) {
                            Utils.sendMessageToPlayer("Hidden Modules (${hiddenModules.size})")
                            hiddenModules.forEach {
                                Utils.sendMessageToPlayer(it)
                            }
                        }
                    }
                }
            }
        }
    }

    fun printCategory() {
        val category = currentCategory ?: return
        Utils.sendMessageToPlayer("")
        Utils.sendMessageToPlayer(
            componentBuilder {
                append(category)
                append(":")
            }
        )
        currentCategory = null
    }

    fun printModule(module: FeatureModule): Component {
        return componentBuilder {
            append {
                module.configData?.let { data ->
                    append(data.name)
                    data.description?.let {
                        hover = it
                    }
                }
            }
            if (module.configData == null) append(module.moduleName)
            append(" ")
            if (module.config.enabled) {
                appendEmoji("white_check_mark")
            } else {
                appendEmoji("x")
            }
            if (module.config is PositionConfig) {
                append(" ")
                appendEmoji("left_right_arrow") {
                    command = "/nopo feature ${module.moduleName} setPos"
                    hover = Component.literal("Click to edit position")
                }
            }
            if (module is ListCommandExtras) append(module.addListCommandData())
            command = "/nopo feature ${module.moduleName}"
            hover = Component.literal("Click to toggle")
        }
    }
}