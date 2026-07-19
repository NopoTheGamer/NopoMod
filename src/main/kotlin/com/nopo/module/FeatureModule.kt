package com.nopo.module

import com.github.stivais.commodore.Commodore
import com.google.gson.annotations.Expose
import com.nopo.NopoMod
import com.nopo.config.ConfigManager
import com.nopo.config.ModuleConfig
import com.nopo.config.PositionConfig
import com.nopo.events.GuiRendering
import com.nopo.screens.GuiEditor
import com.nopo.utils.DelayedRuns
import com.nopo.utils.Utils
import com.nopo.utils.Utils.withColor
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component

open class FeatureModule(
    moduleName: String,
    @Expose var config: ModuleConfig,
    var configData: ConfigData? = null,
    shouldBeHidden: () -> Boolean = { false },
) : BaseModule(moduleName, shouldBeHidden) {

    open fun registerToggleCommand(): Commodore? {
        if (!Utils.isDevAllowed()) {
            val disabledFeatures = NopoMod.data?.disabledFeatures ?: emptyList()
            if (moduleName in disabledFeatures) {
                if (config.enabled) {
                    DelayedRuns.schedule(100) {
                        Utils.sendMessageToPlayer("$moduleName has been remotely disabled :(")
                        ConfigManager.save()
                    }
                }
                config.enabled = false
                return null
            }
        }
        if (shouldBeHidden()) return null
        return Commodore("nopo") {
            literal("feature") {
                literal(moduleName) {
                    runs {
                        config.enabled = !config.enabled
                        Utils.sendMessageUnlessInConfig(
                            Utils.componentBuilder {
                                append("$moduleName module ")
                                if (config.enabled) {
                                    append("enabled")
                                } else {
                                    append("disabled")
                                }
                                withColor(ChatFormatting.YELLOW)
                            }
                        )
                        ConfigManager.save()
                    }
                    val posConfig = config as? PositionConfig
                    if (posConfig != null && this@FeatureModule is GuiRendering) {
                        "setPos" {
                            runs { x: Int?, y: Int? ->
                                if (x == null) {
                                    NopoMod.screenToOpen = GuiEditor(posConfig.pos) {
                                        doRender(it)
                                    }
                                } else if (y == null) {
                                    Utils.sendMessageToPlayer("Missing y argument")
                                } else {
                                    posConfig.pos.x = x
                                    posConfig.pos.y = y
                                    Utils.sendMessageToPlayer("Updated position")
                                    ConfigManager.save()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}

data class ConfigData(val name: Component, val description: Component?)