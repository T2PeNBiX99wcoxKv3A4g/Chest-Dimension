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
    private var chestXSpawn = 0
        set(value) {
            setDirty(field, value)
            field = value
        }
    private var chestYSpawn = 1
        set(value) {
            setDirty(field, value)
            field = value
        }
    private var chestZSpawn = 0
        set(value) {
            setDirty(field, value)
            field = value
        }
    private var chestSpawnAngle = 0f
        set(value) {
            setDirty(field, value)
            field = value
        }
    private var chestDayTime: Long = 0L
        set(value) {
            setDirty(field, value)
            field = value
        }
    private var chestClearWeatherTime = 0
        set(value) {
            setDirty(field, value)
            field = value
        }
    private var chestRaining = false
        set(value) {
            setDirty(field, value)
            field = value
        }
    private var chestRainTime = 0
        set(value) {
            setDirty(field, value)
            field = value
        }
    private var chestThundering = false
        set(value) {
            setDirty(field, value)
            field = value
        }
    private var chestThunderTime = 0
        set(value) {
            setDirty(field, value)
            field = value
        }

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
    lateinit var chestSavedData: ChestSavedData
        internal set

    override fun getLevelName(): String = worldData.levelName

    override fun setThundering(newThundering: Boolean) {
        if (freezeWeather) return
        chestThundering = newThundering
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun setThunderingForce(newThundering: Boolean) {
        chestThundering = newThundering
    }

    override fun getRainTime(): Int = chestRainTime

    override fun setRainTime(time: Int) {
        if (freezeWeather) return
        chestRainTime = time
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun setRainTimeForce(time: Int) {
        chestRainTime = time
    }

    override fun getThunderTime(): Int = chestThunderTime

    override fun setThunderTime(time: Int) {
        if (freezeWeather) return
        chestThunderTime = time
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun setThunderTimeForce(time: Int) {
        chestThunderTime = time
    }

    override fun getClearWeatherTime(): Int = chestClearWeatherTime

    override fun setClearWeatherTime(time: Int) {
        if (freezeWeather) return
        chestClearWeatherTime = time
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun setClearWeatherTimeForce(time: Int) {
        chestClearWeatherTime = time
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
        chestDayTime = time
    }

    @Suppress("unused")
    fun setDayTimeForce(time: Long) {
        chestDayTime = time
    }

    override fun setXSpawn(newXSpawn: Int) {
        chestXSpawn = newXSpawn
    }

    override fun setYSpawn(newYSpawn: Int) {
        chestYSpawn = newYSpawn
    }

    override fun setZSpawn(newZSpawn: Int) {
        chestZSpawn = newZSpawn
    }

    override fun setSpawnAngle(newSpawnAngle: Float) {
        chestSpawnAngle = newSpawnAngle
    }

    override fun getXSpawn(): Int = chestXSpawn

    override fun getYSpawn(): Int = chestYSpawn

    override fun getZSpawn(): Int = chestZSpawn

    override fun getSpawnAngle(): Float = chestSpawnAngle

    override fun getGameTime(): Long = wrapped.gameTime

    override fun getDayTime(): Long = chestDayTime

    override fun isThundering(): Boolean = chestThundering

    override fun isRaining(): Boolean = chestRaining

    override fun setRaining(newRaining: Boolean) {
        if (freezeWeather) return
        chestRaining = newRaining
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun setRainingForce(newRaining: Boolean) {
        chestRaining = newRaining
    }

    override fun isHardcore(): Boolean = worldData.isHardcore

    override fun getGameRules(): GameRules = worldData.gameRules

    override fun getDifficulty(): Difficulty = worldData.difficulty

    override fun isDifficultyLocked(): Boolean = worldData.isDifficultyLocked

    fun load(tag: CompoundTag) {
        chestXSpawn = tag.getInt("SpawnX")
        chestYSpawn = tag.getInt("SpawnY")
        chestZSpawn = tag.getInt("SpawnZ")
        chestSpawnAngle = tag.getFloat("SpawnAngle")
        chestDayTime = tag.getLong("DayTime")
        chestClearWeatherTime = tag.getInt("clearWeatherTime")
        chestRaining = tag.getBoolean("raining")
        chestRainTime = tag.getInt("rainTime")
        chestThundering = tag.getBoolean("thundering")
        chestThunderTime = tag.getInt("thunderTime")
        freezeTime = tag.getBoolean("freezeTime")
        freezeWeather = tag.getBoolean("freezeWeather")
    }

    fun save(tag: CompoundTag): CompoundTag {
        tag.putInt("SpawnX", chestXSpawn)
        tag.putInt("SpawnY", chestYSpawn)
        tag.putInt("SpawnZ", chestZSpawn)
        tag.putFloat("SpawnAngle", chestSpawnAngle)
        tag.putLong("DayTime", chestDayTime)
        tag.putInt("clearWeatherTime", chestClearWeatherTime)
        tag.putBoolean("raining", chestRaining)
        tag.putInt("rainTime", chestRainTime)
        tag.putBoolean("thundering", chestThundering)
        tag.putInt("thunderTime", chestThunderTime)
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
        chestSavedData.setDirty()
    }
}