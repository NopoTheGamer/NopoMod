package com.nopo.data

import com.google.gson.annotations.Expose
import com.nopo.NopoMod
import com.nopo.features.meta.UpdateNotificationData
import com.nopo.features.silly.CosmeticData
import java.util.UUID

data class Data(
    @Expose val devs: List<UUID>? = emptyList(),
    @Expose val updateNotification: UpdateNotificationData? = UpdateNotificationData(NopoMod.CURRENT_VERSION, ""),
    @Expose val disabledFeatures: List<String>? = emptyList(),
    @Expose val devName: String? = "",
    @Expose val cosmetics: List<CosmeticData>? = emptyList(),
)