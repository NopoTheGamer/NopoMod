package com.nopo.features.inventory

import com.github.stivais.commodore.Commodore
import com.mojang.blaze3d.platform.InputConstants
import com.nopo.NopoMod
import com.nopo.config.ConfigManager
import com.nopo.events.CommandRegistration
import com.nopo.events.TickEvent
import com.nopo.module.BaseModule
import com.nopo.utils.HypixelUtils
import com.nopo.utils.Utils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.ContainerScreen
import net.minecraft.world.item.Items

object WardrobeKeybinds : BaseModule("wardrobeKeybinds"), TickEvent, CommandRegistration {

    val keybindData get() = NopoMod.wardrobeDataConfig

    var cooldown = -1

    const val FIRST_SLOT = 36

    override fun onTick(totalTicks: Int) {
        cooldown--
    }

    @JvmStatic
    fun onKeyPress(screen: Screen): Boolean {
        if (!HypixelUtils.onSkyblock()) return false
        if (cooldown > 0) return false
        if (screen !is ContainerScreen) return false
        if (!screen.title.string.startsWith("Wardrobe (")) return false
        val slots = Minecraft.getInstance().player?.containerMenu?.slots ?: return false
        var foundValidKeybindSet = false
        for (bind in keybindData) {
            if (foundValidKeybindSet) break
            if (bind.map != null && HypixelUtils.map !in bind.map) continue
            if (bind.mode != null && HypixelUtils.mode !in bind.mode) continue

            foundValidKeybindSet = true
            for ((index, key) in bind.getKeys().withIndex()) {
                val key = key ?: continue
                if (!InputConstants.isKeyDown(Minecraft.getInstance().window, key)) {
                    continue
                }
                val slotId = index + FIRST_SLOT
                val stack = slots[slotId]
                if (stack.item.item == Items.PINK_DYE) {
                    changeSlot(slotId)
                    return true
                }

                if (stack.item.item == Items.LIME_DYE && bind.allowUnequip != false) {
                    changeSlot(slotId)
                    return true
                }
            }
        }
        return false
    }

    private fun changeSlot(slot: Int) {
        Utils.clickSlot(slot, 0)
        cooldown = 10
    }

    override fun createCommand(): Commodore? {
        if (!Utils.isDevAllowed()) return null
        return Commodore("nopo") {
            "wardrobeKeybindsReload" {
                runs {
                    NopoMod.wardrobeDataConfig = ConfigManager.initWardrobeKeybinds()
                    Utils.sendMessageToPlayer("Reloaded wardrobe keybind data :)")
                }
            }
        }
    }
}