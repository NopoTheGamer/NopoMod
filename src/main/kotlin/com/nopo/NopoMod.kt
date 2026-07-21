package com.nopo

import com.google.gson.stream.JsonReader
import com.nopo.categories.ChatCategory
import com.nopo.categories.CombatCategory
import com.nopo.categories.DevCategory
import com.nopo.categories.DevUnlock
import com.nopo.categories.FarmingCategory
import com.nopo.categories.FishingCategory
import com.nopo.categories.FunCategory
import com.nopo.categories.InventoryCategory
import com.nopo.categories.MiningCategory
import com.nopo.categories.MiscCategory
import com.nopo.categories.RealDevUnlock
import com.nopo.categories.RiftCategory
import com.nopo.commands.ConfigSearchCommand
import com.nopo.commands.DiscordCommand
import com.nopo.commands.EmojiCommand
import com.nopo.commands.InfernoFuelCalculator
import com.nopo.commands.ListConfigCommand
import com.nopo.commands.MainCommand
import com.nopo.commands.RingCommand
import com.nopo.commands.SixSeven
import com.nopo.commands.TaskList
import com.nopo.commands.UpdateDataJsonCommand
import com.nopo.config.Config
import com.nopo.config.ConfigManager
import com.nopo.config.RareCropConfigHolder
import com.nopo.data.Data
import com.nopo.data.Version
import com.nopo.data.Version.Companion.toVersion
import com.nopo.data.WardrobeData
import com.nopo.events.FabricEvents
import com.nopo.features.AutoJoinParty
import com.nopo.features.AutoPerspective
import com.nopo.features.EquipmentDisplay
import com.nopo.features.FastAOTV
import com.nopo.features.MinionCantReachAlert
import com.nopo.features.SendOldIcons
import com.nopo.features.SkyHanniTrackerTitleTotemItem
import com.nopo.features.combat.CocoonWarning
import com.nopo.features.combat.KillCounter
import com.nopo.features.dungeons.PartyFinderKickButton
import com.nopo.features.emoji.EmojiReplace
import com.nopo.features.fishing.TrophyFishDisplay
import com.nopo.features.garden.AshwreathReminder
import com.nopo.features.garden.RareCropTracker
import com.nopo.features.inventory.ExperimentationRngDisplay
import com.nopo.features.inventory.HarpMisclick
import com.nopo.features.inventory.RaffleQuests
import com.nopo.features.inventory.WardrobeKeybinds
import com.nopo.features.meta.DebugModule
import com.nopo.features.meta.FirstTimeGreeting
import com.nopo.features.meta.UpdateNotification
import com.nopo.features.mining.PowderCoatingParticleHider
import com.nopo.features.pets.OverflowPetLevels
import com.nopo.features.pets.PetDisplay
import com.nopo.features.rift.ShensOutbidNotification
import com.nopo.features.silly.BabyDollModel
import com.nopo.features.silly.Cosmetics
import com.nopo.features.silly.ParrotCommand
import com.nopo.features.silly.RavenousSheep
import com.nopo.features.silly.SmallPlayers
import com.nopo.features.slayer.BossesSinceDrop
import com.nopo.module.BaseModule
import com.nopo.utils.DelayedRuns
import com.nopo.utils.HypixelUtils
import com.nopo.utils.PartyApi
import com.nopo.utils.TitleApi
import com.nopo.utils.Utils
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.gui.screens.Screen
import java.io.StringReader
import java.net.URI
import java.net.URL

object NopoMod : ModInitializer {

    const val MOD_ID = "nopo"
    lateinit var config: Config
    lateinit var rareCropConfig: RareCropConfigHolder
    lateinit var wardrobeDataConfig: Map<String, List<WardrobeData>>
    var modules: List<BaseModule> = emptyList()
        private set
    private const val DATA_JSON = "https://raw.githubusercontent.com/NopoTheGamer/NopoMod/refs/heads/master/src/main/resources/assets/nopo/data.json"
    var data: Data? = null
    var screenToOpen: Screen? = null

    val CURRENT_VERSION = FabricLoader.getInstance()
        .getModContainer("nopo")
        .get()
        .metadata
        .version
        .toString()
        .toVersion() ?: Version(1, 0, 0)


    override fun onInitialize() {
        config = ConfigManager.init()
        rareCropConfig = ConfigManager.initRareCrops()
        wardrobeDataConfig = ConfigManager.initWardrobeKeybinds()
        ConfigManager.save()
        ConfigManager.saveRareCrops()

        downloadDataJson(true)

        modules = listOf(
            ChatCategory,
            EmojiReplace,
            SendOldIcons,
            CocoonWarning,

            CombatCategory,
            BossesSinceDrop,
            PartyFinderKickButton,

            FarmingCategory,
            RareCropTracker,

            InventoryCategory,
            OverflowPetLevels,
            EquipmentDisplay,
            PetDisplay,
            HarpMisclick,
            ExperimentationRngDisplay,

            MiningCategory,
            PowderCoatingParticleHider,

            FunCategory,
            SmallPlayers,
            BabyDollModel,
            RavenousSheep,

            FishingCategory,
            TrophyFishDisplay,

            RiftCategory,
            ShensOutbidNotification,

            MiscCategory,
            MinionCantReachAlert,
            KillCounter,
            FastAOTV,
            AutoPerspective,
            SkyHanniTrackerTitleTotemItem,
            RaffleQuests,

            DevCategory,
            DevUnlock,
            RealDevUnlock,
            Cosmetics,
            DebugModule,
            AshwreathReminder,
            AutoJoinParty,

            // not in /nopo list
            // commands
            RingCommand,
            MainCommand,
            ConfigSearchCommand,
            DiscordCommand,
            ListConfigCommand,
            ParrotCommand,
            EmojiCommand,
            SixSeven,
            WardrobeKeybinds,
            TaskList,
            InfernoFuelCalculator,
            UpdateDataJsonCommand,

            // other
            TitleApi,
            UpdateNotification,
            FirstTimeGreeting,
            DelayedRuns,
            FabricEvents,
            HypixelUtils,
            PartyApi,
        )
    }

    fun downloadDataJson(firstLaunch: Boolean) {
        if (config.useLocalJson != true || !firstLaunch) {
            try {
                val json = URL.of(URI.create(DATA_JSON), null).readText()
                data = ConfigManager.gson.fromJson<Data>(JsonReader(StringReader(json)), Data::class.java)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (data == null) data = Utils.getJsonFromJar<Data>("data.json")
    }
}