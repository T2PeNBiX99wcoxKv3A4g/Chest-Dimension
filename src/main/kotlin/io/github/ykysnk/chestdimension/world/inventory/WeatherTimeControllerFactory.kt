package io.github.ykysnk.chestdimension.world.inventory

import io.github.ykysnk.chestdimension.Constants
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu

class WeatherTimeControllerFactory(
    private val level: ServerLevel
) : ExtendedScreenHandlerFactory {
    override fun getDisplayName(): Component {
        return Component.translatable(
            "container.${Constants.MOD_ID}.weather_time_controller"
        )
    }

    override fun writeScreenOpeningData(player: ServerPlayer, buf: FriendlyByteBuf) {
        buf.writeResourceLocation(level.dimension().location())
    }

    override fun createMenu(containerId: Int, inventory: Inventory, player: Player): AbstractContainerMenu {
        return WeatherTimeControllerMenu(containerId, inventory, level.dimension())
    }
}