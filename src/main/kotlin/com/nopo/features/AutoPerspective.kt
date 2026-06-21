package com.nopo.features

import com.github.stivais.commodore.Commodore
import com.google.gson.annotations.Expose
import com.nopo.NopoMod
import com.nopo.config.ConfigManager
import com.nopo.config.ModuleConfig
import com.nopo.events.CommandRegistration
import com.nopo.events.ListCommandExtras
import com.nopo.events.TickEvent
import com.nopo.module.FeatureModule
import com.nopo.utils.Utils
import com.nopo.utils.Utils.append
import com.nopo.utils.Utils.appendEmoji
import com.nopo.utils.Utils.componentBuilder
import com.nopo.utils.Utils.hover
import com.nopo.utils.Utils.suggest
import com.nopo.utils.Utils.withColor
import net.minecraft.ChatFormatting
import net.minecraft.client.CameraType
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.util.Mth

object AutoPerspective : FeatureModule("autoPerspective", NopoMod.config.perspectiveConfig), TickEvent, CommandRegistration, ListCommandExtras {

    fun getConfig() = config as PerspectiveConfig

    fun getDegreesNeeded() = 90 - (getConfig().degreesFromBottom ?: 10)

    var settingF5 = false

    override fun onTick(totalTicks: Int) {
        if (!getConfig().enabled) return
        val player = Minecraft.getInstance().player ?: return
        val yaw = Mth.wrapDegrees(player.xRot)

        if (yaw > getDegreesNeeded()) {
            if (Minecraft.getInstance().options.cameraType == CameraType.FIRST_PERSON && !settingF5) {
                Minecraft.getInstance().options.cameraType = CameraType.THIRD_PERSON_BACK
                settingF5 = true
            }
        } else if (yaw <= getDegreesNeeded()) {
            if (Minecraft.getInstance().options.cameraType == CameraType.THIRD_PERSON_BACK && settingF5) {
                Minecraft.getInstance().options.cameraType = CameraType.FIRST_PERSON
            }
            settingF5 = false

        } else {
            Utils.debug("secret 3rd yaw option")
        }
    }

    override fun createCommand(): Commodore {
        return Commodore("nopo") {
            "feature" {
                moduleName {
                    "setDegrees" {
                        runs { degrees: Int? ->
                            if (degrees == null) {
                                getConfig().degreesFromBottom = 10
                                Utils.sendMessageToPlayer("Reset degrees to 10")
                                ConfigManager.save()
                                return@runs
                            }
                            getConfig().degreesFromBottom = degrees
                            Utils.sendMessageToPlayer("Set degrees to trigger third person to $degrees")
                            ConfigManager.save()
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
                appendEmoji("triangular_ruler") {
                    withColor(ChatFormatting.WHITE)
                }
                suggest = "/nopo feature $moduleName setDegrees "
                hover = Component.literal("Click to change degrees needed to toggle f5")
                append("]")
                withColor(ChatFormatting.YELLOW)
            }
        }
    }

}

class PerspectiveConfig(enabled: Boolean) : ModuleConfig(enabled) {
    @Expose
    var degreesFromBottom: Int? = 20
}