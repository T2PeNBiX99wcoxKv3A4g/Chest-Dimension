package io.github.ykysnk.chestdimension.client.world.inventory

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

class IconButton(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    private val texture: ResourceLocation,
    private val u: Int,
    private val v: Int,
    onPress: OnPress
) : Button(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION) {
    override fun renderWidget(
        guiGraphics: GuiGraphics,
        mouseX: Int,
        mouseY: Int,
        partialTick: Float
    ) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick)
        guiGraphics.blit(texture, x + 2, y + 2, u, v, 16, 16)
    }
}