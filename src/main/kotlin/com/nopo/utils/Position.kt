package com.nopo.utils

import com.google.gson.annotations.Expose

data class Position(
    @Expose var x: Int = 100,
    @Expose var y: Int = 100,
)