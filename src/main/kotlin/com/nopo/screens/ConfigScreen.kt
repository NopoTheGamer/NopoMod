package com.nopo.screens

import com.nopo.NopoMod
import com.nopo.categories.Category
import com.nopo.commands.ListConfigCommand
import com.nopo.features.meta.UpdateNotification
import com.nopo.module.FeatureModule
import com.nopo.utils.Utils
import com.nopo.utils.Utils.append
import com.nopo.utils.Utils.appendEmoji
import com.nopo.utils.Utils.hover
import com.nopo.utils.Utils.suggest
import com.nopo.utils.Utils.underlined
import com.nopo.utils.Utils.url
import com.nopo.utils.Utils.withColor
import net.minecraft.ChatFormatting
import net.minecraft.SharedConstants
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.MultiLineTextWidget
import net.minecraft.client.gui.components.StringWidget
import net.minecraft.client.gui.screens.ConfirmLinkScreen
import net.minecraft.client.gui.screens.Screen
import net.minecraft.commands.Commands
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import java.awt.Color


class ConfigScreen : Screen(Component.literal("Config")) {

    private val categoryRenderable = MultiLineTextWidget(
        getCategories(),
        Minecraft.getInstance().font
    )

    private val optionsRenderable = MultiLineTextWidget(
        getOptions(),
        Minecraft.getInstance().font
    )

    private val discordRenderable = StringWidget(
        Utils.componentBuilder {
            append("Discord") {
                url = "https://discord.com/invite/anFE6xUK6y"
                hover = Component.literal("Click to join!")
                underlined = true
                withColor(7506394)
            }
        },
        Minecraft.getInstance().font
    )

    private val donateRenderable = StringWidget(
        Utils.componentBuilder {
            append("Donate ") {
                withColor(16110348)
            }
            appendEmoji("money_mouth")
            hover = Utils.componentBuilder {
                append("Buy gems for ")
                append(
                    Utils.createGradientText(
                        Color(85, 255, 255),
                        Color.MAGENTA,
                        NopoMod.data?.devName ?: "meowgirlemily"
                    )
                )
                append(" ")
                appendEmoji("pray")
                appendEmoji("pray")
                append("\nI need community upgrades it would be very appreciated")
            }
        },
        Minecraft.getInstance().font
    )

    private val titleComponent = Utils.componentBuilder {
        append(Utils.themedGradient("Nopo Mod"))
        append(" ${NopoMod.CURRENT_VERSION}") {
            if (UpdateNotification.isOutdated()) withColor(ChatFormatting.RED)
        }
        append(" for ${SharedConstants.getCurrentVersion().name()}")
    }

    private var currentCategory: String? = null

    override fun init() {
        super.init()
        NopoMod.config.usedConfigMenu = true
        categoryRenderable.setComponentClickHandler { style ->
            val suggest = style.clickEvent as? ClickEvent.SuggestCommand
            suggest?.let {
                currentCategory = suggest.command
            }
        }
        optionsRenderable.setComponentClickHandler { style ->
            val clickEvent = style.clickEvent ?: return@setComponentClickHandler
            val url = clickEvent as? ClickEvent.OpenUrl
            url?.let {
                ConfirmLinkScreen.confirmLinkNow(this, it.uri)
                return@setComponentClickHandler
            }

            val command = clickEvent as? ClickEvent.RunCommand
            command?.let {
                Utils.sendCommandToServer(Commands.trimOptionalPrefix(it.command))
                return@setComponentClickHandler
            }

            val suggestion = clickEvent as? ClickEvent.SuggestCommand
            suggestion?.let {
                Utils.sendMessageToPlayer(
                    Utils.componentBuilder {
                        append("Run ${it.command} in chat to use this button")
                        suggest = it.command
                        hover = Component.literal("Click to suggest ${it.command}")
                    }
                )
            }
            defaultHandleGameClickEvent(clickEvent, Minecraft.getInstance(), this)
        }
        discordRenderable.setComponentClickHandler { style ->
            val clickEvent = style.clickEvent ?: return@setComponentClickHandler
            val url = clickEvent as? ClickEvent.OpenUrl
            url?.let {
                ConfirmLinkScreen.confirmLinkNow(this, it.uri)
                return@setComponentClickHandler
            }
        }
        optionsRenderable.active = true
        categoryRenderable.active = true
        discordRenderable.active = true
        addRenderableWidget(categoryRenderable)
        addRenderableWidget(optionsRenderable)
        addRenderableWidget(discordRenderable)
        addRenderableWidget(donateRenderable)
        updatePositions()
    }

    private fun getTop(): Int = height / 9 * 2
    private fun getLeft(): Int = width / 9 * 2

    override fun extractRenderState(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, f: Float) {
        context.fill(getLeft() - 5, getTop() - 5, width / 9 * 7, height / 9 * 7, -1610612736)
        context.outline(getLeft() - 5, getTop() - 5, width / 9 * 5 + 5, height / 9 * 5 + 5, -16777216)
        super.extractRenderState(context, mouseX, mouseY, f)
        Utils.drawCenteredText(
            context, titleComponent, 0, getTop()
        )
        categoryRenderable.message = getCategories()
        optionsRenderable.message = getOptions()
    }

    override fun isPauseScreen(): Boolean {
        return false
    }

    override fun repositionElements() {
        super.repositionElements()
        updatePositions()
    }

    private fun updatePositions() {
        categoryRenderable.x = width/9*2
        optionsRenderable.x = width/9*3

        categoryRenderable.y = height/9*2 + 20
        optionsRenderable.y = height/9*2 + 20

        discordRenderable.x = width / 9 * 7 - font.width(discordRenderable.message) - 5
        discordRenderable.y = getTop()

        donateRenderable.x = width / 9 * 7 - font.width(donateRenderable.message) - 5
        donateRenderable.y = getTop() + 12
    }

    private fun getCategories(): Component {
        return Utils.componentBuilder {
            for (module in NopoMod.modules) {
                if (module is Category) {
                    val categoryName = module.moduleName
                    if (currentCategory == null) currentCategory = categoryName
                    append("$categoryName\n") {
                        underlined = categoryName == currentCategory
                        suggest = categoryName
                    }
                }
            }
        }
    }

    private fun getOptions(): Component {
        return Utils.componentBuilder {
            var shouldAdd = false
            var hasAppended = false
            for (module in NopoMod.modules) {
                if (module is Category) {
                    shouldAdd = module.moduleName == currentCategory
                }
                if (!shouldAdd) continue
                if (module !is FeatureModule) continue
                if (module.shouldBeHidden()) continue
                append(ListConfigCommand.printModule(module))
                append("\n")
                hasAppended = true
            }
            if (!hasAppended) {
                append("There is nothing for you in this category ")
                appendEmoji("face_with_raised_eyebrow")
            }
        }
    }
}