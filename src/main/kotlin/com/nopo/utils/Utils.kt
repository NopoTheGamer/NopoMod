package com.nopo.utils

import com.google.gson.stream.JsonReader
import com.nopo.NopoMod
import com.nopo.config.ConfigManager
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer
import net.minecraft.client.gui.components.debug.DebugScreenEntries
import net.minecraft.client.gui.components.debug.DebugScreenEntry
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor
import net.minecraft.network.chat.contents.objects.AtlasSprite
import net.minecraft.resources.Identifier
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.chunk.LevelChunk
import java.awt.Color
import java.lang.reflect.Type
import java.net.URI
import java.nio.file.Files
import java.text.NumberFormat
import java.util.Optional
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.math.absoluteValue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

object Utils {

    private val chatPrefix = componentBuilder {
        append("[") {
            withColor(1279794)
        }
        append(themedGradient("Nopo"))
        append("] ") {
            withColor(1279794)
        }
    }

    fun debug(message: String) {
        if (!isDevAllowed()) return
        sendMessageToPlayer(message)
    }

    fun debug(message: Component) {
        if (!isDevAllowed()) return
        sendMessageToPlayer(message)
    }

    fun sendMessageToPlayer(message: String, prefix: Boolean = true) {
        sendMessageToPlayer(Component.literal(message), prefix)
    }

    fun sendMessageToPlayer(message: Component, prefix: Boolean = true) {
        var finalMessage: Component = message
        if (prefix) {
            finalMessage = componentBuilder {
                append(chatPrefix)
                append(message)
            }
        }
        Minecraft.getInstance().player?.displayClientMessage(finalMessage, false)
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

    fun themedGradient(string: String): Component {
        // idk if i like this yet
        val start = Color(24, 199, 146)
        val end = Color(22, 166, 149)
        return createGradientText(start, end, string)
    }

    val guiIdentifier = Identifier.withDefaultNamespace("gui")

    fun createEmoji(name: String): Component {
        val emoji = Identifier.fromNamespaceAndPath(NopoMod.MOD_ID, name)
        return Component.`object`(AtlasSprite(guiIdentifier, emoji))
    }

    fun isDevAllowed(): Boolean {
        if (NopoMod.config.dev == true) return true
        return FabricLoader.getInstance().isDevelopmentEnvironment || Minecraft.getInstance().player?.stringUUID == NopoMod.data?.devs?.first()
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

    var Component.hover: Component?
        get() = this.style.hoverEvent?.takeIf {
            it.action() == HoverEvent.Action.SHOW_TEXT
        }?.let { (it as HoverEvent.ShowText).value }
        set(value) {
            value?.let { new -> this.copyIfNeeded().withStyle { it.withHoverEvent(HoverEvent.ShowText(new)) } }
        }

    var Component.stackHover: ItemStack?
        get() = this.style.hoverEvent?.takeIf {
            it.action() == HoverEvent.Action.SHOW_ITEM
        }?.let { (it as HoverEvent.ShowItem).item }
        set(value) {
            value?.let { new -> this.copyIfNeeded().withStyle { it.withHoverEvent(HoverEvent.ShowItem(new)) } }
        }

    var Component.command: String?
        get() = this.style.clickEvent?.takeIf {
            it.action() == ClickEvent.Action.RUN_COMMAND
        }?.let { (it as ClickEvent.RunCommand).command }
        set(value) {
            this.copyIfNeeded().withStyle { (it.withClickEvent(ClickEvent.RunCommand(value.orEmpty()))) }
        }

    var Component.suggest: String?
        get() = this.style.clickEvent?.takeIf {
            it.action() == ClickEvent.Action.SUGGEST_COMMAND
        }?.let { (it as ClickEvent.SuggestCommand).command }
        set(value) {
            this.copyIfNeeded().withStyle { (it.withClickEvent(ClickEvent.SuggestCommand(value.orEmpty()))) }
        }

    var Component.url: String?
        get() = this.style.clickEvent?.takeIf {
            it.action() == ClickEvent.Action.OPEN_URL
        }?.let { (it as ClickEvent.OpenUrl).uri.toString() }
        set(value) {
            this.copyIfNeeded().withStyle { (it.withClickEvent(ClickEvent.OpenUrl(URI.create(value.orEmpty())))) }
        }

    var MutableComponent.underlined: Boolean
        get() = this.style.isUnderlined
        set(value) {
            this.withStyle { it.withUnderlined(value) }
        }

    var MutableComponent.bold: Boolean
        get() = this.style.isBold
        set(value) {
            this.withStyle { it.withBold(value) }
        }

    var MutableComponent.strikethrough: Boolean
        get() = this.style.isStrikethrough
        set(value) {
            this.withStyle { it.withStrikethrough(value) }
        }

    var MutableComponent.italic: Boolean
        get() = this.style.isItalic
        set(value) {
            this.withStyle { it.withItalic(value) }
        }

    var MutableComponent.obfuscated: Boolean
        get() = this.style.isObfuscated
        set(value) {
            this.withStyle { it.withObfuscated(value) }
        }

    fun String.cleanColor(): String {
        return this.replace(Regex("§."), "")
    }

    fun Component.append(newText: Component): MutableComponent {
        return this.copyIfNeeded().append(newText)
    }

    fun matcherOrString(component: Component, match: String): Component {
        return matcher(component, match) ?: Component.literal(match)
    }

    fun matcher(component: Component, match: String): Component? {
        var index = 0
        var newComponent: Component = Component.empty()
        var currentString = ""

        component.visit({ style: Style, string: String ->
            if (string.isEmpty()) return@visit Optional.empty()
            for (c in string) {
                if (index >= match.length) {
                    if (!currentString.isEmpty()) {
                        newComponent.append(Component.literal(currentString).withStyle(style))
                    }
                    currentString = ""
                    return@visit Optional.of(newComponent)
                }
                if (c == match[index]) {
                    currentString += c
                    index++
                } else {
                    currentString = ""
                    newComponent = Component.empty()
                    index = 0
                }
            }
            if (!currentString.isEmpty()) {
                newComponent.append(Component.literal(currentString).withStyle(style))
            }
            currentString = ""

            Optional.empty()
        }, Style.EMPTY)
        if (newComponent.string.isEmpty()) return null
        return newComponent
    }

    inline fun <reified T> getJsonFromJar(fileName: String, token: Type? = null): T? {
        try {
            val path = FabricLoader.getInstance().getModContainer("nopo").get()
                .findPath("assets/nopo/$fileName").get()
            val newInputStream = Files.newInputStream(path).reader()
            val jsonReader = JsonReader(newInputStream)
            if (token == null) {
                return ConfigManager.gson.fromJson(jsonReader, T::class.java)
            } else {
                return ConfigManager.gson.fromJson(jsonReader, token)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private const val FACTOR_SECONDS = 1000L
    private const val FACTOR_MINUTES = FACTOR_SECONDS * 60
    private const val FACTOR_HOURS = FACTOR_MINUTES * 60
    private const val FACTOR_DAYS = FACTOR_HOURS * 24
    private const val FACTOR_YEARS = (FACTOR_DAYS * 365.25).toLong()

    enum class TimeUnit(val factor: Long, private val shortName: String, private val longName: String) {
        YEAR(FACTOR_YEARS, "y", "Year"),
        DAY(FACTOR_DAYS, "d", "Day"),
        HOUR(FACTOR_HOURS, "h", "Hour"),
        MINUTE(FACTOR_MINUTES, "m", "Minute"),
        SECOND(FACTOR_SECONDS, "s", "Second"),
        ;

        fun getName(value: Int, longFormat: Boolean) = if (longFormat) {
            " $longName" + if (value == 1) "" else "s"
        } else shortName
    }

    fun Duration.format(
        biggestUnit: TimeUnit = TimeUnit.YEAR,
        showMilliSeconds: Boolean = this.absoluteValue < 1.seconds,
        longName: Boolean = false,
        maxUnits: Int = -1,
        showSmallerUnits: Boolean = false,
        showNegativeAsSoon: Boolean = true,
    ): String {
        var millis = inWholeMilliseconds.absoluteValue
        val prefix = if (isNegative()) {
            if (showNegativeAsSoon) return "Soon"
            "-"
        } else ""
        val parts = mutableMapOf<TimeUnit, Int>()

        for (unit in TimeUnit.entries) {
            if (unit.ordinal >= biggestUnit.ordinal) {
                val factor = unit.factor
                parts[unit] = (millis / factor).toInt()
                millis %= factor
            }
        }

        val largestNonZeroUnit = parts.firstNotNullOfOrNull { if (it.value != 0) it.key else null } ?: TimeUnit.SECOND

        if (absoluteValue < 1.seconds) {
            val formattedMillis = (millis / 100).toInt()
            return "${prefix}0.${formattedMillis}${TimeUnit.SECOND.getName(formattedMillis, longName)}"
        }

        var currentUnits = 0
        val result = buildString {
            for ((unit, value) in parts) {
                val showUnit = value != 0 || (showSmallerUnits && unit.factor <= largestNonZeroUnit.factor)

                if (showUnit) {
                    val formatted = value.addSeparators()
                    val text = if (unit == TimeUnit.SECOND && showMilliSeconds) {
                        val formattedMillis = (millis / 100).toInt()
                        "$formatted.$formattedMillis"
                    } else formatted

                    val name = unit.getName(value, longName)
                    append("$text$name ")
                    if (maxUnits != -1 && ++currentUnits == maxUnits) break
                }
            }
        }
        return prefix + result.trim()
    }

    fun Number.addSeparators(): String {
        return NumberFormat.getNumberInstance().format(this)
    }

    fun getRarityByComponent(component: Component, match: String): String {
        var rarity = "Unknown"
        matcher(component, match)?.visit({ style: Style, value: String ->
            if (match == value) {
                rarity = when (style.color?.name) {
                    "dark_red" -> "Ultimate"
                    "red" -> "Special" // special and very special are the same name
                    "aqua" -> "Divine"
                    "light_purple" -> "Mythic"
                    "gold" -> "Legendary"
                    "dark_purple" -> "Epic"
                    "blue" -> "Rare"
                    "green" -> "Uncommon"
                    "white" -> "Common"
                    else -> "Unknown"
                }
            }
            Optional.empty<Component>()
        }, Style.EMPTY)
        return rarity
    }
}