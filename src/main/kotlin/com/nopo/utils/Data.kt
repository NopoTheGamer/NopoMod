package com.nopo.utils

import com.google.gson.annotations.Expose
import java.util.UUID

data class Data(
    @Expose val devs: List<UUID>? = emptyList(),
    @Expose val emojiName: Map<UUID, String>? = emptyMap()
)