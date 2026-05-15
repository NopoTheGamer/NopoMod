package com.nopo.features.silly

import com.github.stivais.commodore.Commodore
import com.google.gson.annotations.Expose
import com.nopo.NopoMod
import com.nopo.config.ConfigManager
import com.nopo.config.ModuleConfig
import com.nopo.events.CommandRegistration
import com.nopo.module.FeatureModule
import com.nopo.utils.Utils
import com.nopo.utils.Utils.componentBuilder
import com.nopo.utils.Utils.withColor
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey
import net.minecraft.ChatFormatting

object BabyDollModel : FeatureModule("babyDoll", NopoMod.config.babyDollConfig), CommandRegistration {

    private fun getConfig() = config as ShoulderConfig

    @JvmStatic val key: RenderStateDataKey<Boolean> = RenderStateDataKey.create<Boolean> { "doll" }

    override fun createCommand(): Commodore {
        return Commodore("nopo") {
            "feature" {
                moduleName {
                    "everyone" {
                        runs {
                            Utils.sendMessageToPlayer(
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
}

class ShoulderConfig(enabled: Boolean) : ModuleConfig(enabled) {
    @Expose
    var everyone = false

    @Expose
    var left = true
}