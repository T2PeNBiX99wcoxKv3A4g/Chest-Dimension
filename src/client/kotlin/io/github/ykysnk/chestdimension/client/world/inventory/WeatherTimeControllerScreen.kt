package io.github.ykysnk.chestdimension.client.world.inventory

import io.github.ykysnk.chestdimension.client.extensions.sendSetTime
import io.github.ykysnk.chestdimension.client.extensions.sendWeather
import io.github.ykysnk.chestdimension.world.Network
import io.github.ykysnk.chestdimension.world.WeatherType
import io.github.ykysnk.chestdimension.world.inventory.WeatherTimeControllerMenu
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory

class WeatherTimeControllerScreen(menu: WeatherTimeControllerMenu, inventory: Inventory, title: Component) :
    AbstractContainerScreen<WeatherTimeControllerMenu>(menu, inventory, title) {
    override fun init() {
        super.init()

        addRenderableWidget(Button.builder(Component.literal("Day")) { sendSetTime(1000) }
            .bounds(leftPos + 10, topPos + 30, 70, 20).build())
        addRenderableWidget(Button.builder(Component.literal("Noon")) { sendSetTime(6000) }
            .bounds(leftPos + 90, topPos + 30, 70, 20).build())
        addRenderableWidget(Button.builder(Component.literal("Night")) { sendSetTime(13000) }
            .bounds(leftPos + 170, topPos + 30, 70, 20).build())
        addRenderableWidget(Button.builder(Component.literal("Midnight")) { sendSetTime(18000) }
            .bounds(leftPos + 10, topPos + 55, 70, 20).build())
        addRenderableWidget(Button.builder(Component.literal("Clear")) { sendWeather(WeatherType.CLEAR) }
            .bounds(leftPos + 10, topPos + 90, 70, 20).build())
        addRenderableWidget(Button.builder(Component.literal("Rain")) { sendWeather(WeatherType.RAIN) }
            .bounds(leftPos + 90, topPos + 90, 70, 20).build())
        addRenderableWidget(Button.builder(Component.literal("Thunder")) { sendWeather(WeatherType.THUNDER) }
            .bounds(leftPos + 170, topPos + 90, 70, 20).build())
    }

    override fun renderBg(guiGraphics: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        guiGraphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF202020.toInt())
    }

    private fun sendSetTime(time: Int) {
        Network.sendSetTime(menu.dimension, time)
    }

    private fun sendWeather(weather: WeatherType) {
        Network.sendWeather(menu.dimension, weather)
    }
}