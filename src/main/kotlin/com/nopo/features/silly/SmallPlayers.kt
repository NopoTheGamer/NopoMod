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
import net.minecraft.client.model.geom.ModelLayerLocation
import net.minecraft.client.model.player.PlayerModel
import net.minecraft.client.renderer.entity.ArmorModelSet

object SmallPlayers : FeatureModule("small", NopoMod.config.smallConfig), CommandRegistration {

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
                            Utils.sendMessageToPlayer(
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
}

class SmallConfig(enabled: Boolean) : ModuleConfig(enabled) {
    @Expose
    var everyone = false
}