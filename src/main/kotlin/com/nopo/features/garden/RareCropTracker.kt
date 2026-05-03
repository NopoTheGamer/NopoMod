package com.nopo.features.garden

import com.google.gson.annotations.Expose
import com.nopo.NopoMod
import com.nopo.config.ConfigManager
import com.nopo.config.ModuleConfig
import com.nopo.events.ChatEvent
import com.nopo.module.Module
import com.nopo.utils.DelayedRuns
import com.nopo.utils.HypixelUtils
import com.nopo.utils.IslandType
import com.nopo.utils.Utils
import com.nopo.utils.Utils.cleanColor
import com.nopo.utils.Utils.componentBuilder
import com.nopo.utils.Utils.format
import net.minecraft.network.chat.Component
import kotlin.time.Duration.Companion.milliseconds

object RareCropTracker : Module("rareCropTracker", NopoMod.config.rareCrop), ChatEvent {

    fun getConfig() = config as RareCropConfig

    /*
    RARE CROP! Crystalized Moonlight
    RARE CROP! Seasoning (automatically donated)
    RARE CROP! Rarefinder Chip
    RARE CROP! Salted Sunflower Seeds
    VERY RARE CROP! Burrowing Spores
     */
    val rareCropRegex = Regex("(?:VERY )?RARE CROP! (?<crop>[a-zA-Z ]+)(\\(automatically donated\\))?")

    override fun onChat(message: Component, actionBar: Boolean) {
        if (!HypixelUtils.onSkyblock() || !getConfig().enabled) return
        if (!IslandType.GARDEN.isActive()) return
        val string = message.string.cleanColor()

        if (!string.matches(rareCropRegex)) return
        val crop = rareCropRegex.matchEntire(string)?.groups["crop"]?.value?.trim() ?: return
        if (NopoMod.config.debug.enabled) {
            DelayedRuns.schedule(5) {
                Utils.sendMessageToPlayer("Found crop $crop")
            }
        }
        val currentTime = System.currentTimeMillis()
        if (getConfig().dropTimes[crop] == null) {
            getConfig().dropTimes[crop] = mutableListOf(currentTime)
        } else {
            val lastDrop = getConfig().dropTimes[crop]?.maxOrNull() ?: currentTime
            getConfig().dropTimes[crop]!!.add(currentTime)
            if (lastDrop != currentTime) {
                val timeSince = (currentTime - lastDrop).milliseconds
                Utils.sendMessageToPlayer(
                    componentBuilder {
                        append("Took ")
                        append(timeSince.format())
                        append("to drop ")
                        append(Utils.matcherOrString(message, crop))
                    }
                )
            }
        }
        ConfigManager.save()
    }


}

class RareCropConfig : ModuleConfig() {
    @Expose
    var dropTimes = mutableMapOf<String, MutableList<Long>>()
}