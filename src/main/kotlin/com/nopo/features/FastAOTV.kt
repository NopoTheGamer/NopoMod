package com.nopo.features

import com.github.stivais.commodore.Commodore
import com.google.gson.annotations.Expose
import com.nopo.NopoMod
import com.nopo.config.ConfigManager
import com.nopo.config.ModuleConfig
import com.nopo.events.CommandRegistration
import com.nopo.module.ConfigData
import com.nopo.module.FeatureModule
import com.nopo.utils.Utils
import com.nopo.utils.Utils.appendEmoji
import com.nopo.utils.Utils.appendWithColor
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component

object FastAOTV : FeatureModule("fastAotv", NopoMod.config.fastAotvConfig,
    ConfigData(
        Component.literal("Fast AOTV"),
        Utils.componentBuilder {
            append("Replicates the Minecraft quirk where binding \"Use Item\" to a keyboard key triggers it without delay, unlike what normally happens when holding right click\n")
            appendWithColor("Probably not bannable but idk ", ChatFormatting.RED)
            appendEmoji("woman_shrugging")
        }
    )
), CommandRegistration {

    private fun getConfig() = config as FastAOTVConfig

    override fun createCommand(): Commodore {
        return Commodore("nopo") {
            "feature" {
                moduleName {
                    "setDelay" {
                        runs { ticks: Int? ->
                            if (!getConfig().enabled) {
                                Utils.sendMessageToPlayer("Fast AOTV is not enabled. Do /nopo feature $moduleName to enable")
                                return@runs
                            }
                            if (ticks == null) {
                                getConfig().tickDelay = 3
                                Utils.sendMessageToPlayer("Reset Tick Delay to 3")
                                ConfigManager.save()
                                return@runs
                            }
                            if (ticks !in 1..4) {
                                Utils.sendMessageToPlayer("Ticks should be between 1 and 4. (4 is vanilla, 3 is default)")
                                return@runs
                            }
                            getConfig().tickDelay = ticks
                            Utils.sendMessageToPlayer("Set AOTV Tick Delay to $ticks")
                            ConfigManager.save()
                        }
                    }
                }
            }
        }
    }
}

class FastAOTVConfig(enabled: Boolean) : ModuleConfig(enabled) {
    @Expose
    var tickDelay: Int? = 3

    fun getTickDelayAmount(): Int {
        if (tickDelay in 1..4) return tickDelay ?: 4
        else return 4
    }
}