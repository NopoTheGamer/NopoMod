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

    val loadoutRegex = Regex("\\(\\d+/\\d+\\) Loadouts")
    val equipmentRegex = Regex("\\(\\d+/\\d+\\) Equipment Sets")
    val wardrobeRegex = Regex("\\(\\d+/\\d+\\) Armor Sets")

    @JvmStatic
    fun onKeyPress(screen: Screen): Boolean {
        if (!HypixelUtils.onSkyblock()) return false
        if (keybindData.isEmpty()) return false
        if (cooldown > 0) return false
        if (screen !is ContainerScreen) return false
        val title = screen.title.string
        if (wardrobeRegex.matches(title) || title.startsWith("Wardrobe (")) return wardrobeAndEquipment("wardrobe")
        if (equipmentRegex.matches(title)) return wardrobeAndEquipment("equipment")
        if (loadoutRegex.matches(title)) return loadout()
        return false
    }

    fun wardrobeAndEquipment(menu: String): Boolean {
        if (keybindData[menu].isNullOrEmpty()) return false
        val keybinds = keybindData[menu]!!
        val slots = Minecraft.getInstance().player?.containerMenu?.slots ?: return false
        var foundValidKeybindSet = false
        for (bind in keybinds) {
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
                val selectorButton = stack.item.item
                if (selectorButton == Items.PINK_DYE || selectorButton == Items.GRAY_DYE) {
                    changeSlot(slotId)
                    return true
                }

                if (selectorButton == Items.LIME_DYE && bind.allowUnequip != false) {
                    changeSlot(slotId)
                    return true
                }
            }

            if (bind.keyPrevPage != null) {
                if (InputConstants.isKeyDown(Minecraft.getInstance().window, bind.keyPrevPage)) {
                    val prevPage = 45
                    if (slots[prevPage].item.item == Items.ARROW) {
                        changeSlot(prevPage)
                        return true
                    }
                }
            }
            if (bind.keyNextPage != null) {
                if (InputConstants.isKeyDown(Minecraft.getInstance().window, bind.keyNextPage)) {
                    val nextPage = 53
                    if (slots[nextPage].item.item == Items.ARROW) {
                        changeSlot(nextPage)
                        return true
                    }
                }
            }
        }
        return false
    }

    fun loadout(): Boolean {
        if (keybindData["loadout"].isNullOrEmpty()) return false
        val keybinds = keybindData["loadout"]!!
        val slots = Minecraft.getInstance().player?.containerMenu?.slots ?: return false
        var foundValidKeybindSet = false
        for (bind in keybinds) {
            if (foundValidKeybindSet) break
            if (bind.map != null && HypixelUtils.map !in bind.map) continue
            if (bind.mode != null && HypixelUtils.mode !in bind.mode) continue

            foundValidKeybindSet = true
            for ((index, key) in bind.getLoadoutKeys().withIndex()) {
                val key = key ?: continue
                if (!InputConstants.isKeyDown(Minecraft.getInstance().window, key)) {
                    continue
                }
                val slotId = (index / 3 * 9) + (index % 3) + 14
                val stack = slots[slotId]
                if (stack.item.item != Items.GRAY_DYE) {
                    changeSlot(slotId)
                    return true
                }
            }

            if (bind.keyPrevPage != null) {
                if (InputConstants.isKeyDown(Minecraft.getInstance().window, bind.keyPrevPage)) {
                    val prevPage = 17
                    if (slots[prevPage].item.item == Items.ARROW) {
                        changeSlot(prevPage)
                        return true
                    }
                }
            }
            if (bind.keyNextPage != null) {
                if (InputConstants.isKeyDown(Minecraft.getInstance().window, bind.keyNextPage)) {
                    val nextPage = 44
                    if (slots[nextPage].item.item == Items.ARROW) {
                        changeSlot(nextPage)
                        return true
                    }
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
                    if (NopoMod.wardrobeDataConfig.isEmpty()) {
                        Utils.sendMessageToPlayer("Reloaded keybinds but none were found :(")
                        return@runs
                    }
                    Utils.sendMessageToPlayer("Reloaded wardrobe keybind data :)")
                }
            }
        }
    }
}