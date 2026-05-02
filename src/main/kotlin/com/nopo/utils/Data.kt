package com.nopo.utils

import com.google.gson.annotations.Expose

data class Data(
    @Expose val devs: List<String>? = emptyList()
)