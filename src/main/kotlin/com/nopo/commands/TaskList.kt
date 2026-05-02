package com.nopo.commands

import com.google.gson.annotations.Expose
import com.nopo.Module
import com.nopo.config.ModuleConfig
import com.github.stivais.commodore.Commodore
import com.github.stivais.commodore.utils.GreedyString
import com.nopo.NopoMod
import com.nopo.config.ConfigManager
import com.nopo.events.CommandRegistration
import com.nopo.utils.Utils

object TaskList : Module("tasks", NopoMod.config.tasks, needsToggle = false), CommandRegistration {

    fun getConfig() = config as TaskConfig

    override fun createCommand(): Commodore {
        return Commodore("nopo") {
            literal("tasks") {
                literal("list").runs {
                    Utils.sendMessageToPlayer("Tasks (${getConfig().tasks.size})")
                    getConfig().tasks.forEach {
                        Utils.sendMessageToPlayer(it)
                    }
                }
                literal("add").runs { task: GreedyString ->
                    getConfig().tasks.add(task.string)
                    Utils.sendMessageToPlayer("Added task \"${task.string}\"")
                    ConfigManager.save()
                }
                literal("remove").executable {
                    param("task") {
                        suggests {
                            getConfig().tasks
                        }
                    }
                    runs { task: GreedyString ->
                        getConfig().tasks.remove(task.string)
                        Utils.sendMessageToPlayer("Removed task \"${task.string}\"")
                        ConfigManager.save()
                    }
                }
                literal("random") {
                    runs {
                        val random = getConfig().tasks.random()
                        Utils.sendMessageToPlayer("Random Task: $random")
                    }
                }
            }
        }
    }
}

class TaskConfig : ModuleConfig() {

    @Expose
    var tasks: MutableList<String> = mutableListOf()
}