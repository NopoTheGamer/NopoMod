package com.nopo.config

import com.google.gson.annotations.Expose
import com.nopo.utils.Position

open class PositionConfig(x: Int = 100, y: Int = 100, default: Boolean = true) : ModuleConfig(default) {

    @Expose
    var pos = Position(x, y)
}