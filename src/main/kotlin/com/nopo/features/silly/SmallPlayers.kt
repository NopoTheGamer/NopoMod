package com.nopo.features.silly

import com.github.stivais.commodore.Commodore
import com.google.gson.annotations.Expose
import com.nopo.NopoMod
import com.nopo.config.ConfigManager
import com.nopo.config.ModuleConfig
import com.nopo.events.CommandRegistration
import com.nopo.events.ListCommandExtras
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
import net.minecraft.client.model.geom.ModelLayerLocation
import net.minecraft.client.model.player.PlayerModel
import net.minecraft.client.renderer.entity.ArmorModelSet
import net.minecraft.network.chat.Component

object SmallPlayers : FeatureModule("small", NopoMod.config.smallConfig), CommandRegistration, ListCommandExtras {

    private fun getConfig() = config as SmallConfig

    @JvmStatic var PLAYER_BABY_ARMOR: ArmorModelSet<ModelLayerLocation>? = null
    @JvmStatic var PLAYER_BABY: ModelLayerLocation? = null
    @JvmStatic var PLAYER_BABY_SLIM_ARMOR: ArmorModelSet<ModelLayerLocation>? = null
    @JvmStatic var PLAYER_BABY_SLIM: ModelLayerLocation? = null
    @JvmStatic var PLAYER_MODEL: PlayerModel? = null
    @JvmStatic var PLAYER_MODEL_SLIM: PlayerModel? = null
    @JvmStatic val key: RenderStateDataKey<Boolean> = RenderStateDataKey.create<Boolean> { "baby" }


    override fun createCommand(): Commodore {
        return Commodore("nopo") {
            "feature" {
                moduleName {
                    "everyone" {
                        runs {
                            Utils.sendMessageUnlessInConfig(
                                componentBuilder {
                                    append("Everyone is now ")
                                    getConfig().everyone = !getConfig().everyone
                                    if (getConfig().everyone) {
                                        append("small")
                                    } else {
                                        append("big")
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
                appendEmoji("family_wwgg") {
                    withColor(ChatFormatting.WHITE)
                }
                command = "/nopo feature $moduleName everyone"
                hover = Component.literal("Click to toggle showing everyone being small")
                append("]")
                if (getConfig().everyone) withColor(ChatFormatting.GREEN)
                else withColor(ChatFormatting.RED)
            }
        }
    }
}

class SmallConfig(enabled: Boolean) : ModuleConfig(enabled) {
    @Expose
    var everyone = false
}