package com.nopo.features.garden

import com.google.gson.annotations.Expose
import com.nopo.NopoMod
import com.nopo.config.ModuleConfig
import com.nopo.events.ChatEvent
import com.nopo.module.Module
import com.nopo.utils.DelayedRuns
import com.nopo.utils.HypixelUtils
import com.nopo.utils.IslandType
import com.nopo.utils.Utils
import com.nopo.utils.Utils.cleanColor
import net.minecraft.network.chat.Component

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

        if (string.matches(rareCropRegex)) {
            val crop = rareCropRegex.matchEntire(string)?.groups["crop"]?.value?.trim() ?: return
            if (NopoMod.config.debug.enabled) {
                DelayedRuns.schedule(5) {
                    Utils.sendMessageToPlayer("Found crop $crop")
                }
            }
            if (getConfig().dropTimes[crop] == null) {
                getConfig().dropTimes[crop] = mutableListOf(System.currentTimeMillis())
            } else {
                getConfig().dropTimes[crop]!!.add(System.currentTimeMillis())
            }
        }
    }


}

class RareCropConfig : ModuleConfig() {
    @Expose
    var dropTimes = mutableMapOf<String, MutableList<Long>>()
}