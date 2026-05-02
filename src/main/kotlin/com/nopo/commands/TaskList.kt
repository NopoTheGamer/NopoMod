package com.nopo.commands

import com.google.gson.annotations.Expose
import com.nopo.module.Module
import com.nopo.config.ModuleConfig
import com.github.stivais.commodore.Commodore
import com.github.stivais.commodore.utils.GreedyString
import com.nopo.NopoMod
import com.nopo.config.ConfigManager
import com.nopo.events.CommandRegistration
import com.nopo.utils.Utils
import com.nopo.utils.Utils.append
import com.nopo.utils.Utils.command
import com.nopo.utils.Utils.componentBuilder
import com.nopo.utils.Utils.hover
import com.nopo.utils.Utils.suggest
import com.nopo.utils.Utils.withColor
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component

object TaskList : Module("tasks", NopoMod.config.tasks, needsToggle = false), CommandRegistration {

    fun getConfig() = config as TaskConfig

    override fun createCommand(): Commodore {
        return Commodore("nopo") {
            literal("tasks") {
                literal("list").runs {
                    val taskCount = getConfig().tasks.size
                    if (taskCount == 0) {
                        sendEmptyMessage()
                        return@runs
                    }
                    Utils.sendMessageToPlayer("Tasks ($taskCount)")
                    buildTaskList(null).forEach {
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
                        if (getConfig().tasks.isEmpty()) {
                            sendEmptyMessage()
                            return@runs
                        }
                        val random = getConfig().tasks.random()
                        Utils.sendMessageToPlayer(
                            componentBuilder {
                                append("Random Task: ")
                                append(buildTaskLine(random))
                                append(" ")
                                append {
                                    append("[")
                                    append(Utils.createEmoji("repeat")) {
                                        withColor(ChatFormatting.WHITE)
                                    }
                                    append("]")
                                    withColor(ChatFormatting.GRAY)
                                    command = "/nopo tasks random"
                                    hover = Component.literal("Get a different task")
                                }
                            }
                        )
                    }
                }
                literal("deleteall") {
                    runs {
                        getConfig().tasks.clear()
                        Utils.sendMessageToPlayer(componentBuilder {
                            append("Deleted every task ")
                            append(Utils.createEmoji("frowning"))
                        })
                    }
                }
            }
        }
    }

    private fun buildTaskList(task: String?): List<Component> {
        if (task == null) {
            val list = mutableListOf<Component>()
            getConfig().tasks.forEach {
                list.add(buildTaskLine(it))
            }
            return list
        }

        return listOf(buildTaskLine(task))
    }

    private fun buildTaskLine(task: String): Component {
        return componentBuilder {
            append(task)
            append(" ")
            append {
                append("[")
                append(Utils.createEmoji("x")) {
                    withColor(ChatFormatting.WHITE)
                }
                append("]")
                withColor(ChatFormatting.GRAY)
                command = "/nopo tasks remove $task"
                hover = Component.literal("Delete this task")
            }
        }
    }

    private fun sendEmptyMessage() {
        Utils.sendMessageToPlayer(componentBuilder {
            append("No tasks created. Create some with ")
            append("/nopo tasks add") {
                withColor(ChatFormatting.YELLOW)
            }
            suggest = "/nopo tasks add "
            hover = Component.literal("Click to insert into chat bar")
        })
    }
}

class TaskConfig : ModuleConfig() {

    @Expose
    var tasks: MutableList<String> = mutableListOf()
}