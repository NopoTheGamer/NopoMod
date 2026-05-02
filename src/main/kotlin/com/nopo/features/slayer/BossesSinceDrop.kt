package com.nopo.features.slayer

import com.google.gson.annotations.Expose
import com.nopo.NopoMod
import com.nopo.config.ConfigManager
import com.nopo.config.ModuleConfig
import com.nopo.events.AllowChat
import com.nopo.events.WorldChange
import com.nopo.module.Module
import com.nopo.utils.DelayedRuns
import com.nopo.utils.HypixelUtils
import com.nopo.utils.Utils
import com.nopo.utils.Utils.cleanColor
import com.nopo.utils.Utils.componentBuilder
import net.minecraft.network.chat.Component

object BossesSinceDrop : Module("killsSinceSlayerDrop", NopoMod.config.bossesSinceDrop), AllowChat, WorldChange {

    private fun getConfig() = config as BossesSinceDropConfig

    /*
    * "   Wolf Slayer LVL 8 - Next LVL in 260,232 XP!"
    * "   Zombie Slayer LVL 9 - LVL MAXED OUT!"
     */
    private val bossTypeRegex = Regex(" +(?<slayer>Wolf|Zombie|Blaze|Vampire|Spider|Enderman) Slayer LVL \\d.*")

    /*
    * VERY RARE DROP! (Critical VI) (+218% ✯ Magic Find)
    * RARE DROP! (4x Hamster Wheel) (+218% ✯ Magic Find)
    * INSANE DROP! (Shard of the Shredded) (+170% ✯ Magic Find)
    * INSANE DROP! (Warden Heart)
    * CRAZY RARE DROP! (Judgement Core) (+236% ✯ Magic Find)
    * VERY RARE DROP! (◆ Spirit Rune I) (+218% ✯ Magic Find)
     */
    private val dropRegex = Regex("(?:VERY RARE|RARE|INSANE|CRAZY RARE) DROP! \\((?<amount>\\d+x )?(?<item>[^)]+)\\)(?: .+)?")
    private var currentBoss: SlayerType? = null

    init {
        for (type in SlayerType.entries) {
            if (!getConfig().bossMap.contains(type)) {
                getConfig().bossMap[type] = BossesSinceDropConfig.SlayerData()
            }
        }
    }

    override fun onChat(message: Component, actionBar: Boolean): Boolean {
        if (actionBar) return true
        if (!HypixelUtils.onSkyblock()) return true
        val string = message.string.cleanColor()
        if (bossTypeRegex.matches(string)) {
            val group = bossTypeRegex.matchEntire(string)?.groups["slayer"]?.value
            for (type in SlayerType.entries) {
                if (type.display == group) {
                    currentBoss = type
                    break
                }
            }
            getConfig().bossMap[currentBoss]?.kills++
            ConfigManager.save()
            return true
        }

        if (dropRegex.matches(string)) {
            DelayedRuns.schedule(30) {
                // should only happen if the first boss is a cocoon
                // maybe if the first boss is a t5 spider that takes ages to play its anim
                if (currentBoss == null) {
                    Utils.debug("dropped $string but no current boss")
                    return@schedule
                }
                val drop = dropRegex.matchEntire(string)?.groups["item"]?.value ?: return@schedule
                val bossData = getConfig().bossMap[currentBoss]
                val currentKills = bossData?.kills ?: 0
                val lastDropped = bossData?.drops[drop] ?: 0
                bossData?.drops[drop] = currentKills
                val dropComponent = Utils.matcher(message, drop) ?: Component.literal(drop)
                val sinceLast = currentKills - lastDropped
                if (!config.enabled) return@schedule
                Utils.sendMessageToPlayer(
                    componentBuilder {
                        append("Took $sinceLast boss")
                        if (sinceLast != 1) append("es")
                        append(" to drop ")
                        append(dropComponent)
                    }
                )

                ConfigManager.save()
            }
        }
        return true
    }

    override fun onWorldChange() {
        currentBoss = null
    }
}

class BossesSinceDropConfig : ModuleConfig() {

    @Expose
    val bossMap = mutableMapOf<SlayerType, SlayerData>()

    data class SlayerData(
        @Expose var kills: Int = 0,
        // This stores what number kill was the last time you got the drop
        // NOT how many you have dropped
        @Expose var drops: MutableMap<String, Int> = mutableMapOf()
    )
}

enum class SlayerType(val display: String) {
    ZOMBIE("Zombie"),
    SPIDER("Spider"),
    WOLF("Wolf"),
    ENDERMAN("Enderman"),
    VAMPIRE("Vampire"),
    BLAZE("Blaze")
}