package io.github.ykysnk.chestdimension.world.inventory

import io.github.ykysnk.chestdimension.Constants
import io.github.ykysnk.chestdimension.level.ChestServerLevel
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu

class WeatherTimeControllerFactory(
    private val level: ChestServerLevel
) : ExtendedScreenHandlerFactory {
    override fun getDisplayName(): Component {
        return Component.translatable(
            "container.${Constants.MOD_ID}.weather_time_controller"
        )
    }

    override fun writeScreenOpeningData(player: ServerPlayer, buf: FriendlyByteBuf) {
        buf.writeResourceLocation(level.dimension().location())
        var getFreezeTime = false
        var getFreezeWeather = false

        level.chestServerLevelData.apply {
            getFreezeTime = freezeTime
            getFreezeWeather = freezeWeather
        }

        buf.writeBoolean(getFreezeTime)
        buf.writeBoolean(getFreezeWeather)
    }

    override fun createMenu(containerId: Int, inventory: Inventory, player: Player): AbstractContainerMenu {
        var getFreezeTime = false
        var getFreezeWeather = false

        level.chestServerLevelData.apply {
            getFreezeTime = freezeTime
            getFreezeWeather = freezeWeather
        }

        return WeatherTimeControllerMenu(containerId, inventory, level.dimension(), getFreezeTime, getFreezeWeather)
    }
}