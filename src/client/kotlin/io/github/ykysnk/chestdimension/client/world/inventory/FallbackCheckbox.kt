package io.github.ykysnk.chestdimension.client.world.inventory

import net.minecraft.client.gui.components.Checkbox
import net.minecraft.network.chat.Component

class FallbackCheckbox(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    message: Component,
    selected: Boolean,
    showLabel: Boolean,
    private val fallback: (Boolean) -> Unit,
) : Checkbox(x, y, width, height, message, selected, showLabel) {
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
}