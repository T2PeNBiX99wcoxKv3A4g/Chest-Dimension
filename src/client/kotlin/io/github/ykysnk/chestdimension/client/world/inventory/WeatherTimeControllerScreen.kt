package io.github.ykysnk.chestdimension.client.world.inventory

import io.github.ykysnk.chestdimension.client.extensions.freezeTime
import io.github.ykysnk.chestdimension.client.extensions.freezeWeather
import io.github.ykysnk.chestdimension.client.extensions.sendSetTime
import io.github.ykysnk.chestdimension.client.extensions.sendWeather
import io.github.ykysnk.chestdimension.tags.NameSpaces
import io.github.ykysnk.chestdimension.world.Network
import io.github.ykysnk.chestdimension.world.WeatherType
import io.github.ykysnk.chestdimension.world.inventory.WeatherTimeControllerMenu
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory

class WeatherTimeControllerScreen(menu: WeatherTimeControllerMenu, inventory: Inventory, title: Component) :
    AbstractContainerScreen<WeatherTimeControllerMenu>(menu, inventory, title) {
    companion object {
        private const val ROWS = 6
        private val INVENTORY_TEXTURE = NameSpaces.MOD("textures/gui/container/weather_time_controller.png")
    }

    private lateinit var timeSlider: TimeSlider

    init {
        imageHeight = 114 + ROWS * 18
        inventoryLabelY = imageHeight - 94
    }

    override fun init() {
        super.init()

        PosSpaceOffsetHelper(8, 23).let { xOffsetHelper ->
            timeSlider = TimeSlider(leftPos + xOffsetHelper.get(), topPos + 20, 160, 20) { sendSetTime(it) }
            timeSlider.updateValue()

            addRenderableWidget(timeSlider)

            PosSpaceOffsetHelper(45, 23).let { yOffsetHelper ->
                addRenderableWidget(
                    IconButton(
                        leftPos + xOffsetHelper.get(),
                        topPos + yOffsetHelper.get(),
                        20,
                        20,
                        INVENTORY_TEXTURE,
                        180,
                        0
                    ) { sendSetTime(1000) })

                addRenderableWidget(
                    IconButton(
                        leftPos + xOffsetHelper.next(),
                        topPos + yOffsetHelper.get(),
                        20,
                        20,
                        INVENTORY_TEXTURE,
                        196,
                        0
                    ) { sendSetTime(6000) })

                addRenderableWidget(
                    IconButton(
                        leftPos + xOffsetHelper.next(),
                        topPos + yOffsetHelper.get(),
                        20,
                        20,
                        INVENTORY_TEXTURE,
                        212,
                        0
                    ) { sendSetTime(13000) })

                addRenderableWidget(
                    IconButton(
                        leftPos + xOffsetHelper.next(),
                        topPos + yOffsetHelper.get(),
                        20,
                        20,
                        INVENTORY_TEXTURE,
                        228,
                        0
                    ) { sendSetTime(18000) })

                addRenderableWidget(
                    IconButton(
                        leftPos + xOffsetHelper.next(),
                        topPos + yOffsetHelper.get(),
                        20,
                        20,
                        INVENTORY_TEXTURE,
                        180,
                        16
                    ) { sendWeather(WeatherType.CLEAR) })

                addRenderableWidget(
                    IconButton(
                        leftPos + xOffsetHelper.next(),
                        topPos + yOffsetHelper.get(),
                        20,
                        20,
                        INVENTORY_TEXTURE,
                        196,
                        16
                    ) { sendWeather(WeatherType.RAIN) })

                addRenderableWidget(
                    IconButton(
                        leftPos + xOffsetHelper.next(),
                        topPos + yOffsetHelper.get(),
                        20,
                        20,
                        INVENTORY_TEXTURE,
                        212,
                        16
                    ) { sendWeather(WeatherType.THUNDER) })

                addRenderableWidget(
                    FallbackCheckbox(
                        leftPos + 8,
                        topPos + yOffsetHelper.next(),
                        100,
                        20,
                        Component.literal("Freeze Time"),
                        menu.freezeTime
                    ) { freezeTime(it) }
                )

                addRenderableWidget(
                    FallbackCheckbox(
                        leftPos + 8,
                        topPos + yOffsetHelper.next(),
                        100,
                        20,
                        Component.literal("Freeze Weather"),
                        menu.freezeWeather
                    ) { freezeWeather(it) }
                )
            }
        }
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

    override fun containerTick() {
        timeSlider.updateValue()
    }

    private fun sendSetTime(time: Int) {
        val dayTime = Math.floorMod(Minecraft.getInstance().level?.dayTime ?: 0, 24000)
        if (dayTime == time) return
        Network.sendSetTime(menu.dimension, time)
    }

    private fun sendWeather(weather: WeatherType) {
        Network.sendWeather(menu.dimension, weather)
    }

    private fun freezeTime(value: Boolean) {
        Network.freezeTime(menu.dimension, value)
    }

    private fun freezeWeather(value: Boolean) {
        Network.freezeWeather(menu.dimension, value)
    }
}