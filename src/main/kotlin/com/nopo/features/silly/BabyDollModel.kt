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
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component

object BabyDollModel : FeatureModule("babyDoll", NopoMod.config.babyDollConfig,
    ConfigData(
        Component.literal("Baby Doll"),
        componentBuilder {
            append("Gives people a baby doll model of themself that renders sitting on their shoulder")
        }
    )), CommandRegistration, ListCommandExtras {

    private fun getConfig() = config as ShoulderConfig

    @JvmStatic val key: RenderStateDataKey<Boolean> = RenderStateDataKey.create<Boolean> { "doll" }

    override fun createCommand(): Commodore {
        return Commodore("nopo") {
            "feature" {
                moduleName {
                    "everyone" {
                        runs {
                            Utils.sendMessageUnlessInConfig(
                                componentBuilder {
                                    append("Everyone ")
                                    getConfig().everyone = !getConfig().everyone
                                    if (getConfig().everyone) {
                                        append("now has a baby doll!")
                                    } else {
                                        append("lost their baby doll :(")
                                    }
                                    withColor(ChatFormatting.YELLOW)
                                    ConfigManager.save()
                                }
                            )
                        }
                    }
                    "side" {
                        runs {
                            Utils.sendMessageToPlayer(
                                componentBuilder {
                                    append("Doll Model now shows on the ")
                                    getConfig().left = !getConfig().left
                                    if (getConfig().left) {
                                        append("left")
                                    } else {
                                        append("right")
                                    }
                                    append(" side!")
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
                appendEmoji("family_wwgg") {
                    withColor(ChatFormatting.WHITE)
                }
                command = "/nopo feature $moduleName everyone"
                hover = Component.literal("Click to toggle showing baby doll on everyone")
                append("]")
                if (getConfig().everyone) withColor(ChatFormatting.GREEN)
                else withColor(ChatFormatting.RED)
            }
        }
    }
}

class ShoulderConfig(enabled: Boolean) : ModuleConfig(enabled) {
    @Expose
    var everyone = false

    @Expose
    var left = true
}