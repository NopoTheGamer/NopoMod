package com.nopo.features.inventory

import com.nopo.NopoMod
import com.nopo.module.ConfigData
import com.nopo.module.FeatureModule
import com.nopo.utils.Utils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.ContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.world.inventory.ContainerInput
import net.minecraft.world.inventory.Slot
import net.minecraft.world.level.block.Blocks

object HarpMisclick : FeatureModule("preventHarpMisclicks", NopoMod.config.harpMisclickConfig,
    ConfigData(
        Component.literal("Prevent Harp Misclicks"),
        Utils.componentBuilder {
            append("Prevents you from clicking on totally empty rows in the harp")
        }
    )) {

    @JvmStatic
    fun onSlotClick(slot: Slot?, slotId: Int?, buttonNum: Int?, clickType: ContainerInput?): Boolean {
        if (!config.enabled) return false
        if (clickType != ContainerInput.CLONE) return false
        if (slotId !in 37..43 || slotId == null) return false
        val screen: Screen? = Minecraft.getInstance().screen
        if (screen !is ContainerScreen) return false
        val title = screen.getTitle().string
        if (!title.startsWith("Harp ")) return false
        if (Minecraft.getInstance().player == null) return false
        val slots = Minecraft.getInstance().player!!.containerMenu.slots
        if (slot?.item?.item == Blocks.QUARTZ_BLOCK.asItem()) return false
        for (i in 0..1) {
            val id = slotId - (9 + i * 9)
            val item = slots[id].item
            if (item.toString().contains("wool")) {
                return false
            }
        }
        return true
    }
}