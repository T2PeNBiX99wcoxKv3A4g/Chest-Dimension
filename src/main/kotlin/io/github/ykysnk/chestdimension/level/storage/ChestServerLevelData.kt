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
    var chestSavedData: ChestSavedData? = null
        internal set

    override fun getLevelName(): String = worldData.levelName

    override fun setThundering(newThundering: Boolean) {
        setDirty(thundering, newThundering)
        thundering = newThundering
    }

    override fun getRainTime(): Int = rainTime

    override fun setRainTime(time: Int) {
        setDirty(rainTime, time)
        rainTime = time
    }

    override fun setThunderTime(time: Int) {
        setDirty(thunderTime, time)
        thunderTime = time
    }

    override fun getThunderTime(): Int = thunderTime

    override fun getClearWeatherTime(): Int = clearWeatherTime

    override fun setClearWeatherTime(time: Int) {
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
        return tag
    }

    private fun <T> setDirty(oldValue: T, newValue: T) {
        if (oldValue == newValue) return
        chestSavedData?.setDirty()
    }
}