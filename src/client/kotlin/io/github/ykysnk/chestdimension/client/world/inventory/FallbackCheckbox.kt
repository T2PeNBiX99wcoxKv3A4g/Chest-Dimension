package io.github.ykysnk.chestdimension.client.world.inventory

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Checkbox
import net.minecraft.network.chat.Component


class FallbackCheckbox(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    message: Component,
    selected: Boolean,
    private val showLabel: Boolean,
    private val fallback: (Boolean) -> Unit,
) : Checkbox(x, y, width, height, message, selected, false) {
    constructor(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        message: Component,
        selected: Boolean,
        fallback: (Boolean) -> Unit
    ) : this(
        x,
        y,
        width,
        height,
        message,
        selected,
        true,
        fallback,
    )

    private var oldSelected: Boolean = selected

    override fun onPress() {
        super.onPress()
        if (oldSelected == selected()) return
        oldSelected = selected()
        fallback(selected())
    }

    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick)

        val minecraft = Minecraft.getInstance()
        val font = minecraft.font

        if (showLabel) {
            guiGraphics.drawString(
                font,
                message,
                x + 24,
                y + (height - 8) / 2,
                0x404040,
                false
            )
        }
    }
}