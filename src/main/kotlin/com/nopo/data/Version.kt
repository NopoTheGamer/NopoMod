package com.nopo.data

import com.google.gson.TypeAdapter
import com.google.gson.annotations.Expose
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.nopo.data.Version.Companion.toVersion

data class Version(
    @Expose val major: Int,
    @Expose val minor: Int,
    @Expose val patch: Int,
) : Comparable<Version> {
    override fun toString(): String {
        return "$major.$minor.$patch"
    }

    override fun compareTo(other: Version): Int {
        if (this.major != other.major) return this.major.compareTo(other.major)
        if (this.minor != other.minor) return this.minor.compareTo(other.minor)
        return this.patch.compareTo(other.patch)
    }

    companion object {
        fun String.toVersion(): Version? {
            val split = this.split(".")
            if (split.size != 3) return null
            return Version(split[0].toInt(), split[1].toInt(), split[2].toInt())
        }
    }
}

class VersionTypeAdapter : TypeAdapter<Version>() {
    override fun write(out: JsonWriter, version: Version?) {
        out.value(version.toString())
    }

    override fun read(reader: JsonReader): Version? {
        if (reader.hasNext()) {
            return reader.nextString().toVersion()
        }
        return null
    }

}