package com.nopo.data

import com.google.gson.annotations.Expose
import com.nopo.NopoMod
import com.nopo.features.meta.UpdateNotificationData
import java.util.UUID

data class Data(
    @Expose val devs: List<UUID>? = emptyList(),
    @Expose val emojisName: Map<UUID, List<String>>? = emptyMap(),
    @Expose val updateNotification: UpdateNotificationData? = UpdateNotificationData(NopoMod.CURRENT_VERSION, ""),
    @Expose val disabledFeatures: List<String>? = emptyList(),
    @Expose val devName: String? = "",
)