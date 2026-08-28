package io.github.ykysnk.chestdimension.level.storage

import io.github.ykysnk.chestdimension.level.saveddata.ChestSavedData
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.MinecraftServer
import net.minecraft.world.Difficulty
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.GameType
import net.minecraft.world.level.border.WorldBorder
import net.minecraft.world.level.storage.ServerLevelData
import net.minecraft.world.level.storage.WorldData
import net.minecraft.world.level.timers.TimerQueue
import java.util.*

class ChestServerLevelData(private val worldData: WorldData, private val wrapped: ServerLevelData) : ServerLevelData {
    private var xSpawn = 0
    private var ySpawn = 1
    private var zSpawn = 0
    private var spawnAngle = 0f
    private var dayTime: Long = 0L
    private var clearWeatherTime = 0
    private var raining = false
    private var rainTime = 0
    private var thundering = false
    private var thunderTime = 0

    @Suppress("MemberVisibilityCanBePrivate")
    var freezeTime: Boolean = false
        set(value) {
            setDirty(field, value)
            field = value
        }

    @Suppress("MemberVisibilityCanBePrivate")
    var freezeWeather: Boolean = false
        set(value) {
            setDirty(field, value)
            field = value
        }
    var chestSavedData: ChestSavedData? = null
        internal set

    override fun getLevelName(): String = worldData.levelName

    override fun setThundering(newThundering: Boolean) {
        if (freezeWeather) return
        setDirty(thundering, newThundering)
        thundering = newThundering
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun setThunderingForce(newThundering: Boolean) {
        setDirty(thundering, newThundering)
        thundering = newThundering
    }

    override fun getRainTime(): Int = rainTime

    override fun setRainTime(time: Int) {
        if (freezeWeather) return
        setDirty(rainTime, time)
        rainTime = time
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun setRainTimeForce(time: Int) {
        setDirty(rainTime, time)
        rainTime = time
    }

    override fun getThunderTime(): Int = thunderTime

    override fun setThunderTime(time: Int) {
        if (freezeWeather) return
        setDirty(thunderTime, time)
        thunderTime = time
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun setThunderTimeForce(time: Int) {
        setDirty(thunderTime, time)
        thunderTime = time
    }

    override fun getClearWeatherTime(): Int = clearWeatherTime

    override fun setClearWeatherTime(time: Int) {
        if (freezeWeather) return
        setDirty(clearWeatherTime, time)
        clearWeatherTime = time
    }

    fun setClearWeatherTimeForce(time: Int) {
        setDirty(clearWeatherTime, time)
        clearWeatherTime = time
    }

    override fun getWanderingTraderSpawnDelay(): Int = wrapped.wanderingTraderSpawnDelay

    override fun setWanderingTraderSpawnDelay(delay: Int) {}

    override fun getWanderingTraderSpawnChance(): Int = wrapped.wanderingTraderSpawnChance

    override fun setWanderingTraderSpawnChance(chance: Int) {}

    override fun getWanderingTraderId(): UUID? = wrapped.wanderingTraderId

    override fun setWanderingTraderId(id: UUID) {}

    override fun getGameType(): GameType = worldData.gameType

    override fun setWorldBorder(serializer: WorldBorder.Settings) {}

    override fun getWorldBorder(): WorldBorder.Settings = wrapped.worldBorder

    override fun isInitialized(): Boolean = wrapped.isInitialized

    override fun setInitialized(initialized: Boolean) {}

    override fun getAllowCommands(): Boolean = worldData.allowCommands

    override fun setGameType(type: GameType) {}

    override fun getScheduledEvents(): TimerQueue<MinecraftServer> = wrapped.scheduledEvents

    override fun setGameTime(time: Long) {}

    override fun setDayTime(time: Long) {
        if (freezeTime) return
        setDirty(dayTime, time)
        dayTime = time
    }

    @Suppress("unused")
    fun setDayTimeForce(time: Long) {
        setDirty(dayTime, time)
        dayTime = time
    }

    override fun setXSpawn(newXSpawn: Int) {
        setDirty(xSpawn, newXSpawn)
        xSpawn = newXSpawn
    }

    override fun setYSpawn(newYSpawn: Int) {
        setDirty(ySpawn, newYSpawn)
        ySpawn = newYSpawn
    }

    override fun setZSpawn(newZSpawn: Int) {
        setDirty(zSpawn, newZSpawn)
        zSpawn = newZSpawn
    }

    override fun setSpawnAngle(newSpawnAngle: Float) {
        setDirty(spawnAngle, newSpawnAngle)
        spawnAngle = newSpawnAngle
    }

    override fun getXSpawn(): Int = xSpawn

    override fun getYSpawn(): Int = ySpawn

    override fun getZSpawn(): Int = zSpawn

    override fun getSpawnAngle(): Float = spawnAngle

    override fun getGameTime(): Long = wrapped.gameTime

    override fun getDayTime(): Long = dayTime

    override fun isThundering(): Boolean = thundering

    override fun isRaining(): Boolean = raining

    override fun setRaining(newRaining: Boolean) {
        if (freezeWeather) return
        setDirty(raining, newRaining)
        raining = newRaining
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun setRainingForce(newRaining: Boolean) {
        setDirty(raining, newRaining)
        raining = newRaining
    }

    override fun isHardcore(): Boolean = worldData.isHardcore

    override fun getGameRules(): GameRules = worldData.gameRules

    override fun getDifficulty(): Difficulty = worldData.difficulty

    override fun isDifficultyLocked(): Boolean = worldData.isDifficultyLocked

    fun load(tag: CompoundTag) {
        xSpawn = tag.getInt("SpawnX")
        ySpawn = tag.getInt("SpawnY")
        zSpawn = tag.getInt("SpawnZ")
        spawnAngle = tag.getFloat("SpawnAngle")
        dayTime = tag.getLong("DayTime")
        clearWeatherTime = tag.getInt("clearWeatherTime")
        raining = tag.getBoolean("raining")
        rainTime = tag.getInt("rainTime")
        thundering = tag.getBoolean("thundering")
        thunderTime = tag.getInt("thunderTime")
        freezeTime = tag.getBoolean("freezeTime")
        freezeWeather = tag.getBoolean("freezeWeather")
    }

    fun save(tag: CompoundTag): CompoundTag {
        tag.putInt("SpawnX", xSpawn)
        tag.putInt("SpawnY", ySpawn)
        tag.putInt("SpawnZ", zSpawn)
        tag.putFloat("SpawnAngle", spawnAngle)
        tag.putLong("DayTime", dayTime)
        tag.putInt("clearWeatherTime", clearWeatherTime)
        tag.putBoolean("raining", raining)
        tag.putInt("rainTime", rainTime)
        tag.putBoolean("thundering", thundering)
        tag.putInt("thunderTime", thunderTime)
        tag.putBoolean("freezeTime", freezeTime)
        tag.putBoolean("freezeWeather", freezeWeather)
        return tag
    }

    @Suppress("unused")
    fun setWeatherParameters(clearTime: Int, weatherTime: Int, isRaining: Boolean, isThundering: Boolean) {
        setClearWeatherTime(clearTime)
        setRainTime(weatherTime)
        setThunderTime(weatherTime)
        setRaining(isRaining)
        setThundering(isThundering)
    }

    fun setWeatherParametersForce(clearTime: Int, weatherTime: Int, isRaining: Boolean, isThundering: Boolean) {
        setClearWeatherTimeForce(clearTime)
        setRainTimeForce(weatherTime)
        setThunderTimeForce(weatherTime)
        setRainingForce(isRaining)
        setThunderingForce(isThundering)
    }

    private fun <T> setDirty(oldValue: T, newValue: T) {
        if (oldValue == newValue) return
        chestSavedData?.setDirty()
    }
}