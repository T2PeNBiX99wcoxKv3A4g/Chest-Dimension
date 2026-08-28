package io.github.ykysnk.chestdimension.world.inventory

import net.minecraft.core.registries.Registries
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceKey
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

class WeatherTimeControllerMenu(containerId: Int, inventory: Inventory, val dimension: ResourceKey<Level>) :
    AbstractContainerMenu(MenuTypes.WEATHER_TIME_CONTROLLER, containerId) {
    constructor(containerId: Int, inventory: Inventory, buf: FriendlyByteBuf) : this(
        containerId,
        inventory,
        ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation())
    )

    companion object {
        private const val ROWS = 6
    }

    init {
        val y: Int = (ROWS - 4) * 18

        for (row in 0..2) {
            for (col in 0..8) {
                addSlot(Slot(inventory, col + row * 9 + 9, 8 + col * 18, 103 + row * 18 + y))
            }
        }

        for (col in 0..8) {
            addSlot(Slot(inventory, col, 8 + col * 18, 161 + y))
        }
    }

    override fun quickMoveStack(player: Player, index: Int): ItemStack {
        return ItemStack.EMPTY
    }

    override fun stillValid(player: Player): Boolean {
        return true
    }
}