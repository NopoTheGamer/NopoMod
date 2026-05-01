package com.nopo.config

import com.google.gson.GsonBuilder
import com.google.gson.stream.JsonReader
import com.nopo.NopoMod
import com.nopo.utils.Utils
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.Minecraft
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.nio.file.AccessDeniedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

object ConfigManager {

    val configFolder = File(FabricLoader.getInstance().configDir.toFile(), "nopo")
    val configFile = File(configFolder, "nopo.json")
    val gson = GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create()

    fun init(): Config {
        configFolder.mkdirs()
        configFile.createNewFile()
        val reader = JsonReader(FileReader(configFile))
        var data = gson.fromJson<Config>(reader, Config::class.java)
        if (data == null) data = Config()
        return data
    }

    fun save() {
        val unit = configFolder.resolve("configField.json.temp")
        unit.createNewFile()
        BufferedWriter(OutputStreamWriter(FileOutputStream(unit), StandardCharsets.UTF_8)).use { writer ->
            writer.write(gson.toJson(NopoMod.config))
        }
        move(unit, configFile)
    }

    private fun move(source: File, target: File, count: Int = 0) {
        if (count == 6) {
            Utils.sendMessageToPlayer("Config error !")
            return
        }
        try {
            Files.move(
                source.toPath(),
                target.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE,
            )
        } catch (e: AccessDeniedException) {
            Minecraft.getInstance().schedule { move(source, target, count + 1) }
        }
    }
}