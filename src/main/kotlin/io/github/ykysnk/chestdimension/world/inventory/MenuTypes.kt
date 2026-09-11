package io.github.ykysnk.chestdimension.world.inventory

import io.github.ykysnk.chestdimension.tags.NameSpaces
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.inventory.MenuType

object MenuTypes {
    val WEATHER_TIME_CONTROLLER: MenuType<WeatherTimeControllerMenu> =
        Registry.register(
            BuiltInRegistries.MENU,
            NameSpaces.MOD("weather_time_controller"),
            ExtendedScreenHandlerType(::WeatherTimeControllerMenu)
        )
}