package io.github.ykysnk.chestdimension.client.world.inventory

import net.minecraft.client.gui.components.AbstractSliderButton
import net.minecraft.network.chat.Component

class TimeSlider(x: Int, y: Int, width: Int, height: Int, initialTime: Int, private val onChanged: (Int) -> Unit) :
    AbstractSliderButton(
        x,
        y,
        width,
        height,
        Component.literal(formatTime(initialTime)),
        (initialTime / 24000).toDouble() / 24000.0
    ) {
    override fun updateMessage() {
        message = Component.literal(formatTime((value * 24000).toInt()))
    }

    override fun applyValue() {
        onChanged((value * 24000).toInt())
    }

    companion object {
        private fun formatTime(dayTime: Int): String {
            val time = Math.floorMod(dayTime, 24000)
            val totalSeconds = (time + 6000L) * 86400L / 24000L
            val seconds = totalSeconds % 86400L

            val hours = seconds / 3600L
            val minutes = (seconds % 3600L) / 60L
            val secs = seconds % 60L

            return "%02d:%02d:%02d".format(hours, minutes, secs)
        }
    }
}