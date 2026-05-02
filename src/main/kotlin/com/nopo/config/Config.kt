package com.nopo.config

import com.google.gson.annotations.Expose
import com.nopo.commands.TaskConfig
import com.nopo.features.AshwreathConfig

class Config {

    @Expose
    var firstTime = true

    @Expose
    var wokeConfig = ModuleConfig()

    @Expose
    var chatEmojis = ModuleConfig()

    @Expose
    var overflowPetLevel = ModuleConfig()

    @Expose
    var ashwreath = AshwreathConfig()

    @Expose
    var tasks = TaskConfig()

    @Expose
    var debug = ModuleConfig(false)

    // dont try saving stuff here
    var dummyConfig = ModuleConfig()

}