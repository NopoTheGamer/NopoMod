package com.nopo.features.slayer

import com.github.stivais.commodore.Commodore
import com.google.gson.annotations.Expose
import com.nopo.NopoMod
import com.nopo.config.ConfigManager
import com.nopo.config.ModuleConfig
import com.nopo.events.ChatEvent
import com.nopo.events.CommandRegistration
import com.nopo.events.WorldChange
import com.nopo.module.ConfigData
import com.nopo.module.FeatureModule
import com.nopo.utils.DelayedRuns
import com.nopo.utils.HypixelUtils
import com.nopo.utils.IslandType
import com.nopo.utils.Utils
import com.nopo.utils.Utils.cleanColor
import com.nopo.utils.Utils.componentBuilder
import net.minecraft.network.chat.Component

object BossesSinceDrop : FeatureModule("killsSinceSlayerDrop", NopoMod.config.bossesSinceDrop,
    ConfigData(
        Component.literal("Kills Since Slayer Drop"),
        componentBuilder {
            append("Tells you how many Slayer kills it took since last time you got that drop")
        }
    )), ChatEvent, WorldChange, CommandRegistration {

    private fun getConfig() = config as BossesSinceDropConfig

    /*
    * "   Wolf Slayer LVL 8 - Next LVL in 260,232 XP!"
    * "   Zombie Slayer LVL 9 - LVL MAXED OUT!"
     */
    private val bossTypeRegex = Regex(" +(?<slayer>Wolf|Zombie|Blaze|Vampire|Spider|Enderman|Guardian) Slayer LVL \\d.*")

    /*
    * VERY RARE DROP! (Critical VI) (+218%  Magic Find)
    * RARE DROP! (4x Hamster Wheel) (+218%  Magic Find)
    * INSANE DROP! (Shard of the Shredded) (+170%  Magic Find)
    * INSANE DROP! (Warden Heart)
    * CRAZY RARE DROP! (Judgement Core) (+236%  Magic Find)
    * VERY RARE DROP! (◆ Spirit Rune I) (+218%  Magic Find)
     */
    private val dropRegex = Regex("(?:VERY RARE|RARE|INSANE|CRAZY RARE) DROP! \\((?<amount>\\d+x )?(?<item>[^)]+)\\)(?: .+)?")
    private var currentBoss: SlayerType? = null
    private var hasWorldChanged = false

    init {
        for (type in SlayerType.entries) {
            if (!getConfig().bossMap.contains(type)) {
                getConfig().bossMap[type] = BossesSinceDropConfig.SlayerData()
            }
        }
    }

    override fun onChat(message: Component, actionBar: Boolean) {
        if (actionBar) return
        if (!HypixelUtils.onSkyblock()) return
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
            hasWorldChanged = false
            return
        }

        if (dropRegex.matches(string)) {
            DelayedRuns.schedule(30) {
                var currentBoss = currentBoss
                if (IslandType.RIFT.isActive()) currentBoss = SlayerType.VAMPIRE
                if (currentBoss == null) {
                    Utils.debug("dropped $string but no current boss")
                    return@schedule
                }
                val drop = dropRegex.matchEntire(string)?.groups["item"]?.value ?: return@schedule
                val bossData = getConfig().bossMap[currentBoss]
                if (hasWorldChanged && bossData?.drops[drop] == null) {
                    Utils.debug("dropped $string but world swap and boss hasn't dropped this")
                    return@schedule
                }
                val currentKills = bossData?.kills ?: 0
                val lastDropped = bossData?.drops[drop] ?: 0
                bossData?.drops[drop] = currentKills
                val dropComponent = Utils.matcherOrString(message, drop)
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
        return
    }

    override fun onWorldChange() {
        hasWorldChanged = true
    }

    override fun createCommand(): Commodore {
        return Commodore("nopo") {
            "slayer" {
                runs {
                    for (slayer in SlayerType.entries) {
                        val data = getConfig().bossMap[slayer]
                        if (slayer == SlayerType.GUARDIAN && (data == null || data.kills == 0)) {
                            continue
                        }
                        val kills = data?.kills ?: 0
                        Utils.sendMessageToPlayer(
                            componentBuilder {
                                append(slayer.display)
                                append(": $kills kills")
                            }
                        )
                    }
                }
            }
        }
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
    BLAZE("Blaze"),
    GUARDIAN("Guardian"),
}