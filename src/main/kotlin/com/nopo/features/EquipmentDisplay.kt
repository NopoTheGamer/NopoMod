package com.nopo.features

import com.github.stivais.commodore.Commodore
import com.google.gson.annotations.Expose
import com.nopo.NopoMod
import com.nopo.config.ConfigManager
import com.nopo.config.ModuleConfig
import com.nopo.events.CommandRegistration
import com.nopo.events.GuiRendering
import com.nopo.events.TickEvent
import com.nopo.module.Module
import com.nopo.utils.HypixelUtils
import com.nopo.utils.Position
import com.nopo.utils.Utils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.ContainerScreen
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

object EquipmentDisplay : Module("equipmentDisplay", NopoMod.config.equipmentDisplay), GuiRendering, CommandRegistration, TickEvent {

    private fun getConfig() = config as EquipmentDisplayConfig

    var equipment = arrayOfNulls<ItemStack?>(4)
    val slotIndexes = listOf(10, 19, 28, 37)

    override fun render(context: GuiGraphics) {
        if (!config.enabled) return
        if (!HypixelUtils.onSkyblock()) return
        for ((index, eq) in equipment.withIndex()) {
            if (eq == null) continue
            context.renderItem(eq, getConfig().pos.x, getConfig().pos.y + index * 16)
        }
    }

    override fun createCommand(): Commodore {
        return Commodore("nopo") {
            "feature" {
                "equipmentDisplay" {
                    runs { x: Int, y: Int ->
                        getConfig().pos.x = x
                        getConfig().pos.y = y
                        Utils.sendMessageToPlayer("Updated position")
                        ConfigManager.save()
                    }
                }
            }
        }
    }

    override fun onTick(totalTicks: Int) {
        if (!config.enabled) return
        if (!HypixelUtils.onSkyblock()) return
        val screen = Minecraft.getInstance().screen
        if (screen !is ContainerScreen) return
        if (screen.title.string != "Your Equipment and Stats") return
        val slots = Minecraft.getInstance().player?.containerMenu?.slots ?: return
        equipment = arrayOfNulls<ItemStack?>(4)
        for (index in slotIndexes.withIndex()) {
            val stack = slots[index.value].item
            if (stack.item != Items.LIGHT_GRAY_STAINED_GLASS_PANE) {
                equipment[index.index] = stack
            }
        }
    }
}

class EquipmentDisplayConfig : ModuleConfig() {
    @Expose val pos = Position(520, 405)
}