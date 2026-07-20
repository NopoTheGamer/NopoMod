package com.nopo.features.silly

import com.github.stivais.commodore.Commodore
import com.google.gson.annotations.Expose
import com.nopo.NopoMod
import com.nopo.config.ConfigManager
import com.nopo.config.ModuleConfig
import com.nopo.events.CommandRegistration
import com.nopo.events.ListCommandExtras
import com.nopo.module.ConfigData
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

object RavenousSheep : FeatureModule("ravenousSheep", NopoMod.config.ravenousSheepConfig, ConfigData(
    Component.literal("Ravenous Sheep"),
    Component.literal("Has a chance to convert Sheep to Ravagers in Dungeons")
)), ListCommandExtras, CommandRegistration {

    private fun getConfig() = config as RavenousConfig

    override fun createCommand(): Commodore {
        return Commodore("nopo") {
            "feature" {
                moduleName {
                    "always" {
                        runs {
                            Utils.sendMessageUnlessInConfig(
                                componentBuilder {
                                    getConfig().always = !getConfig().always
                                    if (getConfig().always) {
                                        append("All sheep in Dungeons are now Ravagers")
                                    } else {
                                        append("Sheep in Dungeons won't always be Ravagers now")
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

    override fun addListCommandData(): Component {
        return componentBuilder {
            append(" ")
            append {
                append("[")
                appendEmoji("cinema") {
                    withColor(ChatFormatting.WHITE)
                }
                command = "/nopo feature $moduleName always"
                hover = Component.literal("Click to toggle always turning Sheep into Ravagers in Dungeons")
                append("]")
                if (getConfig().always) withColor(ChatFormatting.GREEN)
                else withColor(ChatFormatting.RED)
            }
        }
    }
}

class RavenousConfig : ModuleConfig() {
    @Expose
    var always: Boolean = false
}