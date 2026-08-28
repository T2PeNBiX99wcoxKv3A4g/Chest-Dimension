package io.github.ykysnk.chestdimension.client.world.inventory

import io.github.ykysnk.chestdimension.Constants
import io.github.ykysnk.chestdimension.client.extensions.sendSetTime
import io.github.ykysnk.chestdimension.client.extensions.sendWeather
import io.github.ykysnk.chestdimension.world.Network
import io.github.ykysnk.chestdimension.world.WeatherType
import io.github.ykysnk.chestdimension.world.inventory.WeatherTimeControllerMenu
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory

class WeatherTimeControllerScreen(menu: WeatherTimeControllerMenu, inventory: Inventory, title: Component) :
    AbstractContainerScreen<WeatherTimeControllerMenu>(menu, inventory, title) {
    companion object {
        private const val ROWS = 6
        private val INVENTORY_TEXTURE = Constants.id("textures/gui/container/weather_time_controller.png")
    }

    init {
        imageHeight = 114 + ROWS * 18
        inventoryLabelY = imageHeight - 94
    }

    override fun init() {
        super.init()

        addRenderableWidget(TimeSlider(leftPos + 8, topPos + 20, 160, 20, 6000) { sendSetTime(it) })
        addRenderableWidget(Button.builder(Component.literal("Day")) { sendSetTime(1000) }
            .bounds(leftPos + 8, topPos + 45, 50, 20).build())
        addRenderableWidget(Button.builder(Component.literal("Noon")) { sendSetTime(6000) }
            .bounds(leftPos + 63, topPos + 45, 50, 20).build())
        addRenderableWidget(Button.builder(Component.literal("Night")) { sendSetTime(13000) }
            .bounds(leftPos + 118, topPos + 45, 50, 20).build())
        addRenderableWidget(Button.builder(Component.literal("Midnight")) { sendSetTime(18000) }
            .bounds(leftPos + 8, topPos + 70, 75, 20).build())
        addRenderableWidget(Button.builder(Component.literal("Clear")) { sendWeather(WeatherType.CLEAR) }
            .bounds(leftPos + 8, topPos + 95, 50, 20).build())
        addRenderableWidget(Button.builder(Component.literal("Rain")) { sendWeather(WeatherType.RAIN) }
            .bounds(leftPos + 63, topPos + 95, 50, 20).build())
        addRenderableWidget(Button.builder(Component.literal("Thunder")) { sendWeather(WeatherType.THUNDER) }
            .bounds(leftPos + 118, topPos + 95, 50, 20).build())
    }

    override fun renderBg(guiGraphics: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        val x = (width - imageWidth) / 2
        val y = (height - imageHeight) / 2
        guiGraphics.blit(INVENTORY_TEXTURE, x, y, 0, 0, imageWidth, ROWS * 18 + 17)
        guiGraphics.blit(INVENTORY_TEXTURE, x, y + ROWS * 18 + 17, 0, 126, imageWidth, 96)
    }

    override fun render(
        guiGraphics: GuiGraphics,
        mouseX: Int,
        mouseY: Int,
        partialTick: Float
    ) {
        renderBackground(guiGraphics)
        super.render(guiGraphics, mouseX, mouseY, partialTick)
        renderTooltip(guiGraphics, mouseX, mouseY)
    }

    private fun sendSetTime(time: Int) {
        val dayTime = Math.floorMod(Minecraft.getInstance().level?.dayTime ?: 0, 24000)
        if (dayTime == time) return
        Network.sendSetTime(menu.dimension, time)
    }

    private fun sendWeather(weather: WeatherType) {
        Network.sendWeather(menu.dimension, weather)
    }
}