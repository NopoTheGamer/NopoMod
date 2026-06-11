package com.nopo.data

import com.google.gson.annotations.Expose

data class WardrobeData(
    @Expose val map: List<String>?,
    @Expose val mode: List<String>?,
    @Expose val allowUnequip: Boolean?,
    @Expose val key1: Int?,
    @Expose val key2: Int?,
    @Expose val key3: Int?,
    @Expose val key4: Int?,
    @Expose val key5: Int?,
    @Expose val key6: Int?,
    @Expose val key7: Int?,
    @Expose val key8: Int?,
    @Expose val key9: Int?,
) {
    fun getKeys(): List<Int?> {
        return listOf(
            key1,
            key2,
            key3,
            key4,
            key5,
            key6,
            key7,
            key8,
            key9,
        )
    }
}