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
import net.minecraft.network.chat.Style
import java.util.Optional
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
    private val rareCropRegex = Regex("(?:VERY )?RARE CROP! (?<crop>[a-zA-Z ]+)(\\(automatically donated\\))?")

    // PET DROP! Slug (+2,668☘)
    private val petDropRegex = Regex("PET DROP! (?<pet>\\w+) \\(\\+[0-9,]+☘\\)")

    override fun onChat(message: Component, actionBar: Boolean) {
        if (!HypixelUtils.onSkyblock() || !getConfig().enabled) return
        if (!IslandType.GARDEN.isActive()) return
        val string = message.string.cleanColor()

        if (string.matches(petDropRegex)) {
            val pet = petDropRegex.matchEntire(string)?.groups["pet"]?.value?.trim() ?: return
            // lf hypixel using components...
            val rarity = if (message.string.contains("§5$pet")) {
                "Epic"
            } else if (message.string.contains("§6$pet")) {
                "Legendary"
            } else {
                "Unknown"
            }
            addRareDrop("$rarity $pet", message)
            return
        }

        if (!string.matches(rareCropRegex)) return
        val crop = rareCropRegex.matchEntire(string)?.groups["crop"]?.value?.trim() ?: return
        if (NopoMod.config.debug.enabled) {
            DelayedRuns.schedule(5) {
                Utils.sendMessageToPlayer("Found crop $crop")
            }
        }
        addRareDrop(crop, message)
    }

    fun addRareDrop(drop: String, message: Component) {
        val currentTime = System.currentTimeMillis()
        if (getConfig().dropTimes[drop] == null) {
            getConfig().dropTimes[drop] = mutableListOf(currentTime)
        } else {
            val lastDrop = getConfig().dropTimes[drop]?.maxOrNull() ?: currentTime
            getConfig().dropTimes[drop]!!.add(currentTime)
            if (lastDrop != currentTime) {
                val timeSince = (currentTime - lastDrop).milliseconds
                DelayedRuns.schedule(5) {
                    Utils.sendMessageToPlayer(
                        componentBuilder {
                            append("Took ")
                            append(timeSince.format())
                            append(" to drop ")
                            append(Utils.matcherOrString(message, drop))
                        }
                    )
                }
            }
        }
        ConfigManager.save()

    }


}

class RareCropConfig : ModuleConfig() {
    @Expose
    var dropTimes = mutableMapOf<String, MutableList<Long>>()
}