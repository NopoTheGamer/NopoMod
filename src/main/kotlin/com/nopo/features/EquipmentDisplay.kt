package com.nopo.features

import com.github.stivais.commodore.Commodore
import com.google.gson.annotations.Expose
import com.nopo.NopoMod
import com.nopo.config.PositionConfig
import com.nopo.events.CommandRegistration
import com.nopo.events.GuiRendering
import com.nopo.events.ListCommandExtras
import com.nopo.events.TickEvent
import com.nopo.module.ConfigData
import com.nopo.module.FeatureModule
import com.nopo.utils.HypixelUtils
import com.nopo.utils.Utils
import com.nopo.utils.Utils.append
import com.nopo.utils.Utils.command
import com.nopo.utils.Utils.componentBuilder
import com.nopo.utils.Utils.hover
import com.nopo.utils.Utils.withColor
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.inventory.ContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

object EquipmentDisplay : FeatureModule("equipmentDisplay", NopoMod.config.equipmentDisplay,
    ConfigData(
        Component.literal("Equipment Display"),
        componentBuilder {
            append("Hud element that shows your current armour and equipment")
        }
    )), GuiRendering,
    CommandRegistration, TickEvent, ListCommandExtras {

    private fun getConfig() = config as EquipmentDisplayConfig

    var equipment = arrayOfNulls<ItemStack?>(4)
    val slotIndexes = listOf(10, 19, 28, 37)

    val loadoutRegex = Regex("\\(\\d+/\\d+\\) Loadouts")
    val equipmentRegex = Regex("\\(\\d+/\\d+\\) Equipment Sets")

    val eqActiveSwaps = 36..44

    private fun hasAnythingToRender(): Boolean {
        for (index in 0 until 4) {
            val item = Minecraft.getInstance().player?.inventory?.getItem(36 + (3 - index))
            if (item != null && item.item != Items.AIR) return true
            val item2 = equipment[index]
            if (item2 != null && item2.item != Items.AIR) return true
        }
        return false
    }

    override fun render(context: GuiGraphicsExtractor) {
        if (!config.enabled) return
        if (!HypixelUtils.onSkyblock()) return
        if (!hasAnythingToRender()) return
        getConfig().pos.render(context) {
            doRender(context)
        }
    }

    override fun doRender(context: GuiGraphicsExtractor) {
        if (!hasAnythingToRender()) {
            context.renderItem(ItemStack(Items.DIAMOND_HELMET), -16, 0)
        }
        for ((index, eq) in equipment.withIndex()) {
            if (getConfig().showArmor) {
                Minecraft.getInstance().player?.inventory?.getItem(36 + (3 - index))?.let {
                    context.item(it, -16, index * 16)
                }
            }
            if (eq == null) continue
            context.item(eq, 0, index * 16)
        }
    }

    override fun createCommand(): Commodore {
        return Commodore("nopo") {
            "feature" {
                "equipmentDisplay" {
                    "showArmour" {
                        runs {
                            Utils.sendMessageUnlessInConfig(
                                componentBuilder {
                                    append("Armor is now ")
                                    getConfig().showArmor = !getConfig().showArmor
                                    if (getConfig().showArmor) {
                                        append("shown")
                                    } else {
                                        append("hidden")
                                    }
                                }
                            )
                        }
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
        val title = screen.title.string
        if (title == "Your Equipment and Stats") statsAndLoadout()
        if (loadoutRegex.matches(title)) statsAndLoadout()
        if (equipmentRegex.matches(title)) eqWardrobe()
    }

    private fun statsAndLoadout() {
        val slots = Minecraft.getInstance().player?.containerMenu?.slots ?: return
        equipment = arrayOfNulls<ItemStack?>(4)
        for (index in slotIndexes.withIndex()) {
            val stack = slots[index.value].item
            if (stack.item != Items.LIGHT_GRAY_STAINED_GLASS_PANE) {
                equipment[index.index] = stack
            }
        }
    }

    private fun eqWardrobe() {
        val slots = Minecraft.getInstance().player?.containerMenu?.slots ?: return
        equipment = arrayOfNulls<ItemStack?>(4)
        var found = false
        for (slot in eqActiveSwaps) {
            val stack = slots[slot].item
            if (stack.item == Items.LIME_DYE) {
                found = true
                val necklace = slots[slot - 36].item
                if (necklace.item == Items.PLAYER_HEAD) equipment[0] = necklace
                val cloak = slots[slot - 27].item
                if (cloak.item == Items.PLAYER_HEAD) equipment[1] = cloak
                val belt = slots[slot - 18].item
                if (belt.item == Items.PLAYER_HEAD) equipment[2] = belt
                val bracelet = slots[slot - 9].item
                if (bracelet.item == Items.PLAYER_HEAD) equipment[3] = bracelet
            }
        }
        if (!found) equipment = arrayOfNulls<ItemStack?>(4)
    }

    override fun addListCommandData(): Component {
        return componentBuilder {
            append(" ")
            append {
                append("[")
                append(Utils.createItem("copper_chestplate")) {
                    withColor(ChatFormatting.WHITE)
                }
                command = "/nopo feature $moduleName showArmour"
                hover = Component.literal("Click to toggle showing armour in equipment display")
                append("]")
                if (getConfig().showArmor) withColor(ChatFormatting.GREEN)
                else withColor(ChatFormatting.RED)
            }
        }
    }
}

class EquipmentDisplayConfig : PositionConfig(540, 405) {
    @Expose
    var showArmor = false
}