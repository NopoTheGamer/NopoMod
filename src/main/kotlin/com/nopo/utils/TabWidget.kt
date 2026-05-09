package com.nopo.utils

import net.minecraft.network.chat.Component

enum class TabWidget(title: String) {
    PET("Pet:"),
    SKILLS("Skills:"),
    ;

    val regex = Regex(title)
    val lines: MutableList<Component> = mutableListOf()


    companion object {

        fun updateWidgets(tab: List<Component>) {
            reset()
            var currentWidget: TabWidget? = null

            for (line in tab) {
                val string = line.string
                if (string.isEmpty() || string.toCharArray()[0] != ' ') {
                    currentWidget = null
                }
                if (currentWidget == null) {
                    currentWidget = entries.firstOrNull { it.regex.matches(string) } ?: continue
                    currentWidget.lines.clear()
                    currentWidget.lines.add(line)
                } else {
                    if (string.toCharArray()[0] == ' ') {
                        currentWidget.lines.add(line)
                    }
                }
            }
        }

        fun reset() {
            entries.forEach { it.lines.clear() }
        }

    }

}

