package com.nopo.data

import com.google.gson.annotations.Expose
import com.nopo.NopoMod
import com.nopo.features.UpdateNotificationData
import java.util.UUID

data class Data(
    @Expose val devs: List<UUID>? = emptyList(),
    @Expose val emojiName: Map<UUID, String>? = emptyMap(),
    @Expose val updateNotification: UpdateNotificationData? = UpdateNotificationData(NopoMod.CURRENT_VERSION, ""),
)