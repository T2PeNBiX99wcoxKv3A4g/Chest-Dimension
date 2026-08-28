package io.github.ykysnk.chestdimension.client.world.inventory

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.AbstractSliderButton
import net.minecraft.network.chat.Component

class TimeSlider(x: Int, y: Int, width: Int, height: Int, private val onChanged: (Int) -> Unit) :
    AbstractSliderButton(
        x,
        y,
        width,
        height,
        Component.literal(formatTime(0)),
        0.0
    ) {
    private var isDragging = false
    private val realDayTime
        get() = Math.floorMod(Minecraft.getInstance().level?.dayTime ?: 0L, 24000)
    private val dayTime
        get() = Math.floorMod(realDayTime + OFFSET, 24000).toDouble() / 24000.0
    private val realValueTime
        get() = Math.floorMod(valueTime - OFFSET, 24000)
    private val valueTime
        get() = (value * 24000.0).toInt()

    override fun updateMessage() {
        message = Component.literal(formatTime(valueTime))
    }

    override fun applyValue() {
        onChanged(realValueTime)
    }

    override fun onRelease(mouseX: Double, mouseY: Double) {
        super.onRelease(mouseX, mouseY)
        isDragging = false
    }

    override fun onClick(mouseX: Double, mouseY: Double) {
        isDragging = true
        super.onClick(mouseX, mouseY)
    }

    fun updateValue() {
        if (isDragging) return
        value = dayTime
        updateMessage()
    }

    companion object {
        private const val OFFSET = 6000
        private fun formatTime(time: Int): String {
            val totalSeconds = time * 86400L / 24000L
            val hours = totalSeconds / 3600L
            val minutes = (totalSeconds % 3600L) / 60L
            val secs = totalSeconds % 60L

            return "%02d:%02d:%02d".format(hours, minutes, secs)
        }
    }
}