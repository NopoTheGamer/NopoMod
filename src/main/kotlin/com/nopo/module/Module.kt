package com.nopo.module

import com.github.stivais.commodore.Commodore
import com.google.gson.annotations.Expose
import com.nopo.NopoMod
import com.nopo.config.ConfigManager
import com.nopo.config.ModuleConfig
import com.nopo.utils.Utils
import com.nopo.utils.Utils.withColor
import net.minecraft.ChatFormatting

open class Module(
    moduleName: String,
    @Expose var config: ModuleConfig = NopoMod.config.dummyConfig,
    dev: Boolean = false
) : BaseModule(moduleName, dev) {

    open fun registerToggleCommand(): Commodore? {
        if (dev && !Utils.isDevAllowed()) return null
        return Commodore("nopo") {
            literal("feature") {
                literal(moduleName) {
                    runs {
                        config.enabled = !config.enabled
                        Utils.sendMessageToPlayer(
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
                }
            }
        }
    }

}