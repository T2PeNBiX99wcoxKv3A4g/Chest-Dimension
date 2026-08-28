package io.github.ykysnk.chestdimension.world

import io.github.ykysnk.chestdimension.Constants
import io.github.ykysnk.chestdimension.level.ChestServerLevel
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey

object Network {
    val SET_TIME = Constants.id("set_time")
    val SET_WEATHER = Constants.id("set_weather")

    init {
        ServerPlayNetworking.registerGlobalReceiver(SET_TIME) { server, player, _, buf, _ ->
            val dimension = ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation())
            val time = buf.readLong()

            server.execute {
                (server.getLevel(dimension) as? ChestServerLevel)?.apply {
                    val currentDay = dayTime / 24000L
                    dayTime = currentDay * 24000L + time
                }
            }
        }

        ServerPlayNetworking.registerGlobalReceiver(SET_WEATHER) { server, player, _, buf, _ ->
            val dimension = ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation())
            val weather = buf.readEnum(WeatherType::class.java)

            server.execute {
                (server.getLevel(dimension) as? ChestServerLevel)?.apply {
                    when (weather) {
                        WeatherType.CLEAR -> {
                            setWeatherParameters(6000, 0, false, false)
                        }

                        WeatherType.RAIN -> {
                            setWeatherParameters(0, 6000, true, false)
                        }

                        WeatherType.THUNDER -> {
                            setWeatherParameters(0, 6000, true, true)
                        }
                    }
                }
            }
        }
    }
}