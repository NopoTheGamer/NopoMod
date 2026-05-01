package com.nopo.config

import com.google.gson.annotations.Expose
import com.nopo.features.WokeConfig

class Config {

    @Expose
    var wokeConfig = ModuleConfig()

    @Expose
    var chatEmojis = ModuleConfig()

    // dont try saving stuff here
    var dummyConfig = ModuleConfig()

}