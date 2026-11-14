package com.nopo.features.mining

import com.nopo.NopoMod
import com.nopo.events.ItemOverlayEvent
import com.nopo.module.ConfigData
import com.nopo.module.FeatureModule
import com.nopo.utils.Utils
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import kotlin.jvm.optionals.getOrNull

object DrillGemstoneDisplay: FeatureModule("drillGemstoneDisplay", NopoMod.config.drillGemstoneDisplayConfig,
    ConfigData(
        Component.literal("Drill Gemstone Display"),
        Component.literal("Displays a small §a● §ficon on drills to indicate which gemstone is in the universal slot")
    )), ItemOverlayEvent {

    override fun onItemOverlay(drawContext: GuiGraphicsExtractor, font: Font, stack: ItemStack, x: Int, y: Int) {
        if (!config.enabled) return
        val itemId = Utils.getItemId(stack)
        if (itemId == "TITANIUM_DRILL_4" || itemId == "DIVAN_DRILL") {
            val nbt = stack.get(DataComponents.CUSTOM_DATA)?.copyTag() ?: return
            val gemstone = nbt.getCompound("gems").getOrNull()?.getString("MINING_0_gem")?.getOrNull() ?: return
            val overlay: String
            when (gemstone) {
                "JADE" -> overlay = "§a●"
                "TOPAZ" -> overlay = "§e●"
                "AMBER" -> overlay = "§6●"
                else -> overlay = ""
            }
            drawContext.pose().pushMatrix()
            drawContext.text(font, overlay, x + 10, y, -1)
            drawContext.pose().popMatrix()
        }
    }
}