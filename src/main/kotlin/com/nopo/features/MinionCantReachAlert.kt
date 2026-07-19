package com.nopo.features

import com.nopo.NopoMod
import com.nopo.config.PositionConfig
import com.nopo.events.GuiRendering
import com.nopo.events.TickEvent
import com.nopo.module.ConfigData
import com.nopo.module.FeatureModule
import com.nopo.utils.IslandType
import com.nopo.utils.Utils
import com.nopo.utils.Utils.cleanColor
import com.nopo.utils.Utils.group
import com.nopo.utils.Utils.withColor
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.decoration.ArmorStand

object MinionCantReachAlert : FeatureModule("minionCantReachAlert", NopoMod.config.minionCantReachConfig,
    ConfigData(
        Component.literal("Minion Can't Reach Alert"),
        Utils.componentBuilder {
            append("Sends a title if your minion can't reach its mobs\n")
            append("Useful for bone dye grinding to know when to kill the mobs")
        }
    )), TickEvent, GuiRendering {

    // I can't reach any Skeletons
    val cantReachRegex = Regex("I can't reach any (?<mob>.*)")

    var currentMob: String? = null

    private fun getConfig() = config as PositionConfig

    override fun onTick(totalTicks: Int) {
        currentMob = null
        if (!IslandType.PRIVATE_ISLAND.isActive()) return
        val entities = Minecraft.getInstance().level?.entitiesForRendering()?.filterIsInstance<ArmorStand>()?.toMutableList() ?: return
        for (entity in entities) {
            val name = entity.displayName.string.cleanColor()
            if (name.matches(cantReachRegex)) {
                currentMob = cantReachRegex.group(name, "mob")
                return
            }
        }
    }

    override fun render(context: GuiGraphicsExtractor) {
        if (!config.enabled) return
        if (!IslandType.PRIVATE_ISLAND.isActive()) return
        if (currentMob == null) return
        getConfig().pos.render(context) {
            doRender(context)
        }
    }

    override fun doRender(context: GuiGraphicsExtractor) {
        context.text(
            Minecraft.getInstance().font, Utils.componentBuilder {
                append("Minions have stopped generating ")
                append(currentMob ?: "mobs")
                withColor(ChatFormatting.RED)
            }, 0, 0, -1
        )
    }
}