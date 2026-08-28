package io.github.ykysnk.chestdimension.client.extensions

import io.github.ykysnk.chestdimension.world.Network
import io.github.ykysnk.chestdimension.world.WeatherType
import io.netty.buffer.Unpooled
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.Level

fun Network.sendSetTime(dimension: ResourceKey<Level>, time: Int) {
    ClientPlayNetworking.send(SET_TIME, FriendlyByteBuf(Unpooled.buffer()).apply {
        writeResourceKey(dimension)
        writeLong(time.toLong())
    })
}

fun Network.sendWeather(dimension: ResourceKey<Level>, weather: WeatherType) {
    ClientPlayNetworking.send(SET_WEATHER, FriendlyByteBuf(Unpooled.buffer()).apply {
        writeResourceKey(dimension)
        writeEnum(weather)
    })
}

fun Network.freezeTime(dimension: ResourceKey<Level>, value: Boolean) {
    ClientPlayNetworking.send(FREEZE_TIME, FriendlyByteBuf(Unpooled.buffer()).apply {
        writeResourceKey(dimension)
        writeBoolean(value)
    })
}

fun Network.freezeWeather(dimension: ResourceKey<Level>, value: Boolean) {
    ClientPlayNetworking.send(FREEZE_WEATHER, FriendlyByteBuf(Unpooled.buffer()).apply {
        writeResourceKey(dimension)
        writeBoolean(value)
    })
}