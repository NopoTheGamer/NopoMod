package com.nopo.config

import com.google.gson.annotations.Expose
import com.nopo.commands.TaskConfig
import com.nopo.features.FastAOTVConfig
import com.nopo.features.PerspectiveConfig
import com.nopo.features.chat.DiceConfig
import com.nopo.features.chat.LevelColourConfig
import com.nopo.features.combat.CocoonConfig
import com.nopo.features.garden.AshwreathConfig
import com.nopo.features.inventory.EquipmentDisplayConfig
import com.nopo.features.pets.PetConfig
import com.nopo.features.silly.ParrotConfig
import com.nopo.features.silly.RavenousConfig
import com.nopo.features.silly.ShoulderConfig
import com.nopo.features.silly.SmallConfig
import com.nopo.features.slayer.BossesSinceDropConfig

class Config {

    @Expose
    var firstTime = true

    @Expose
    var usedConfigMenu = false

    @Expose
    var fakeDevUnlock = ModuleConfig(false)

    @Expose
    var realDevUnlock = ModuleConfig(false)

    @Expose
    var useLocalJson: Boolean? = null

    @Expose
    var cosmeticConfig = ModuleConfig()

    @Expose
    var chatEmojis = ModuleConfig()

    @Expose
    var overflowPetLevel = ModuleConfig()

    @Expose
    var ashwreath = AshwreathConfig(false)

    @Expose
    var rareCrop = ModuleConfig()

    @Expose
    var tasks = TaskConfig()

    @Expose
    var bossesSinceDrop = BossesSinceDropConfig()

    @Expose
    var partyFinderKickButton = ModuleConfig()

    @Expose
    var equipmentDisplay = EquipmentDisplayConfig()

    @Expose
    var petDisplay = PetConfig()

    @Expose
    var powderCoatingHider = ModuleConfig()

    @Expose
    var skyhanniTrackerTotem = ModuleConfig(false)

    @Expose
    var riftShenOutbid = ModuleConfig()

    @Expose
    var parrotConfig = ParrotConfig()

    @Expose
    var smallConfig = SmallConfig(false)

    @Expose
    var babyDollConfig = ShoulderConfig(false)

    @Expose
    var cocoonConfig = CocoonConfig()

    @Expose
    var trophyFishConfig = PositionConfig()

    @Expose
    var perspectiveConfig = PerspectiveConfig(false)

    @Expose
    var fastAotvConfig = FastAOTVConfig(false)

    @Expose
    var raffleQuestConfig = PositionConfig()

    @Expose
    var minionCantReachConfig = PositionConfig(200, 200, 2f, default = false)

    @Expose
    var killCounterConfig = PositionConfig(default = false)

    @Expose
    var harpMisclickConfig = ModuleConfig()

    @Expose
    var autoJoinPartyConfig = ModuleConfig(false)

    @Expose
    var sendOldIconConfig = ModuleConfig()

    @Expose
    var experimentRngConfig = PositionConfig(387, 170)

    @Expose
    var ravenousSheepConfig = RavenousConfig()

    @Expose
    var callPartyCommandConfig = ModuleConfig()

    @Expose
    var newSbLevelColourConfig = LevelColourConfig()

    @Expose
    var diceTrackerConfig = DiceConfig()

    @Expose
    var itemPartyCommandConfig = ModuleConfig()

    @Expose
    var debug = ModuleConfig(false)

}