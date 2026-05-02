package com.nopo.utils

import com.nopo.NopoMod
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer
import net.minecraft.client.gui.components.debug.DebugScreenEntries
import net.minecraft.client.gui.components.debug.DebugScreenEntry
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor
import net.minecraft.network.chat.contents.objects.AtlasSprite
import net.minecraft.resources.Identifier
import net.minecraft.world.level.Level
import net.minecraft.world.level.chunk.LevelChunk
import java.awt.Color
import java.util.Optional
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.text.replace

object Utils {

    private val chatPrefix = componentBuilder {
        append("[") {
            withColor(1279794)
        }
        // idk if i like this yet
        append(createGradientText(Color(24, 199, 146), Color(28, 173, 122), "Nopo"))
        append("] ") {
            withColor(1279794)
        }
    }

    fun sendMessageToPlayer(message: String, prefix: Boolean = true) {
        var finalMessage: Component = Component.literal(message)
        if (prefix) {
            finalMessage = componentBuilder {
                append(chatPrefix)
                append(message)
            }
        }
        Minecraft.getInstance()?.player?.displayClientMessage(finalMessage, false)
    }

    fun sendMessageToPlayer(message: Component, prefix: Boolean = true) {
        var finalMessage: Component = message
        if (prefix) {
            finalMessage = componentBuilder {
                append(chatPrefix)
                append(message)
            }
        }
        Minecraft.getInstance()?.player?.displayClientMessage(finalMessage, false)
    }

    fun componentBuilder(init: MutableComponent.() -> Unit): Component {
        return Component.empty().also(init)
    }

    val ALWAYS get(): (Style?) -> Boolean = { true }

    /**
     * Replace a string within a Component with another string
     * The strings have to exist within 1 sibling
     * AKA they have to have the same Style
     */
    fun Component.replace(
        oldValue: String,
        newValue: String,
        onlyReplaceFirst: Boolean = false,
        predicate: (Style?) -> Boolean = ALWAYS,
    ): MutableComponent? {
        return replace(this, oldValue, newValue, onlyReplaceFirst, predicate)
    }

    fun Component.replace(
        oldValue: Regex,
        newValue: String,
        onlyReplaceFirst: Boolean = false,
        predicate: (Style?) -> Boolean = ALWAYS,
    ): MutableComponent? {
        return replace(this, oldValue, newValue, onlyReplaceFirst, predicate)
    }

    private fun replace(
        component: Component,
        oldValue: Any,
        newValue: String,
        onlyReplaceFirst: Boolean,
        predicate: (Style?) -> Boolean = ALWAYS,
    ): MutableComponent? {
        val newComp = Component.empty()
        var hasEdited = false

        component.visit(
            { style: Style?, string: String? ->
                var edit = string
                if ((!onlyReplaceFirst || !hasEdited) && predicate(style)) {
                    edit = when (oldValue) {
                        is String -> string?.replace(oldValue, newValue)
                        is Regex -> string?.replace(oldValue, newValue)
                        else -> {
                            sendMessageToPlayer("fucked it up")
                            return@visit Optional.empty<Component>()
                        }
                    }
                }
                if (edit != string) hasEdited = true

                val safeStyle = style ?: Style.EMPTY
                newComp.append(Component.literal(edit.orEmpty()).withStyle(safeStyle))
                Optional.empty<Component>()
            },
            Style.EMPTY,
        )

        if (!hasEdited) return null
        return newComp
    }

    @OptIn(ExperimentalAtomicApi::class)
    fun Component.replace(
        oldValue: String,
        newValue: Component,
        onlyReplaceFirst: Boolean = false,
        predicate: (Style?) -> Boolean = ALWAYS,
    ): MutableComponent? {
        val newComp = Component.empty()
        val hasEdited = AtomicBoolean(false)

        this.visit(
            { currentStyle: Style?, string: String? ->
                val safeCurrentStyle = currentStyle ?: Style.EMPTY
                if (string?.contains(oldValue) == true && (!onlyReplaceFirst || !hasEdited.load()) && predicate(style)) {
                    val split = string.split(oldValue)
                    newComp.append(
                        componentBuilder {
                            for ((index, str) in split.withIndex()) {
                                append(Component.literal(str).withStyle(safeCurrentStyle))
                                if (index < split.size - 1) {
                                    if (!onlyReplaceFirst || !hasEdited.load()) {
                                        append(newValue)
                                        hasEdited.store(true)
                                    } else {
                                        append(oldValue) {
                                            style = safeCurrentStyle
                                        }
                                    }
                                }
                            }
                        },
                    )
                } else {
                    newComp.append(Component.literal(string.orEmpty()).withStyle(safeCurrentStyle))
                }
                Optional.empty<Component>()
            },
            Style.EMPTY,
        )

        if (!hasEdited.load()) return null
        return newComp
    }

    fun MutableComponent.append(string: String = "", init: MutableComponent.() -> Unit): MutableComponent {
        return this.append(Component.literal(string).also(init))
    }

    fun MutableComponent.append(comp: Component, init: MutableComponent.() -> Unit): MutableComponent {
        return this.append(comp.copyIfNeeded().also(init))
    }

    fun MutableComponent.appendWithColor(string: String = "", color: Int, init: MutableComponent.() -> Unit = {}): MutableComponent {
        return this.append(Component.literal(string).withColor(color).also(init))
    }

    fun MutableComponent.appendWithColor(comp: Component, color: Int, init: MutableComponent.() -> Unit = {}): MutableComponent {
        return this.append(comp.copyIfNeeded().withColor(color).also(init))
    }

    fun MutableComponent.appendWithColor(string: String = "", color: ChatFormatting, init: MutableComponent.() -> Unit = {}): MutableComponent {
        return this.append(Component.literal(string).withColor(color).also(init))
    }

    fun MutableComponent.appendWithColor(comp: Component, color: ChatFormatting, init: MutableComponent.() -> Unit = {}): MutableComponent {
        return this.append(comp.copyIfNeeded().withColor(color).also(init))
    }

    fun MutableComponent.appendWithColor(string: String = "", color: TextColor, init: MutableComponent.() -> Unit = {}): MutableComponent {
        return this.append(Component.literal(string).withColor(color).also(init))
    }

    fun MutableComponent.appendWithColor(comp: Component, color: TextColor, init: MutableComponent.() -> Unit = {}): MutableComponent {
        return this.append(comp.copyIfNeeded().withColor(color).also(init))
    }

    fun Component.copyIfNeeded(): MutableComponent = this as? MutableComponent ?: this.copy()

    fun MutableComponent.withColor(formatting: ChatFormatting): MutableComponent {
        return this.withStyle { it.withColor(formatting) }
    }

    fun MutableComponent.withColor(color: TextColor): MutableComponent {
        return this.withStyle { it.withColor(color) }
    }

    fun blendRGB(start: Color, end: Color, progress: Int, max: Int): Color {
        val percent = (progress.toDouble() / max.toDouble()).coerceAtMost(1.0)
        return blendRGB(start, end, percent)
    }

    fun blendRGB(start: Color, end: Color, percent: Double) = Color(
        (start.red * (1 - percent) + end.red * percent).toInt(),
        (start.green * (1 - percent) + end.green * percent).toInt(),
        (start.blue * (1 - percent) + end.blue * percent).toInt(),
    )

    fun createGradientText(start: Color, end: Color, string: String): Component {
        val length = string.length
        val text = componentBuilder {
            for ((index, char) in string.withIndex()) {
                val color = blendRGB(start, end, index, length).rgb
                append(char.toString()) {
                    withColor(color)
                }
            }
        }
        return text
    }

    val guiIdentifier = Identifier.withDefaultNamespace("gui")

    fun createEmoji(name: String): Component {
        val emoji = Identifier.fromNamespaceAndPath(NopoMod.MOD_ID, name)
        return Component.`object`(AtlasSprite(guiIdentifier, emoji))
    }

    fun isDevAllowed(): Boolean {
        return FabricLoader.getInstance().isDevelopmentEnvironment || Minecraft.getInstance().player?.name?.string == "Throwpo"
    }

    fun registerDebugScreenEntry(
        name: String,
        condition: () -> Boolean = { true },
        lineBuilder: MutableList<String>.() -> Unit,
    ) {
        val id = Identifier.fromNamespaceAndPath("nopo", name)
        DebugScreenEntries.register(
            id,
            object : DebugScreenEntry {
                override fun display(
                    displayer: DebugScreenDisplayer,
                    level: Level?,
                    clientChunk: LevelChunk?,
                    serverChunk: LevelChunk?,
                ) {
                    if (level == null || !condition()) return
                    displayer.addToGroup(id, buildList(lineBuilder))
                }

                override fun isAllowed(reducedDebugInfo: Boolean) = true
            },
        )
    }
}