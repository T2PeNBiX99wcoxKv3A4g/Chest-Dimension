package io.github.ykysnk.chestdimension.level.storage

import io.github.ykysnk.chestdimension.level.saveddata.ChestSavedData
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
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
    private var privateXSpawn = 0
    private var dirtyXSpawn: Int
        get() = privateXSpawn
        set(value) {
            setDirty(privateXSpawn, value)
            privateXSpawn = value
        }
    private var privateYSpawn = 1
    private var dirtyYSpawn: Int
        get() = privateYSpawn
        set(value) {
            setDirty(privateYSpawn, value)
            privateYSpawn = value
        }
    private var privateZSpawn = 0
    private var dirtyZSpawn: Int
        get() = privateZSpawn
        set(value) {
            setDirty(privateZSpawn, value)
            privateZSpawn = value
        }
    private var privateSpawnAngle = 0f
    private var dirtySpawnAngle: Float
        get() = privateSpawnAngle
        set(value) {
            setDirty(privateSpawnAngle, value)
            privateSpawnAngle = value
        }
    private var privateDayTime: Long = 0L
    private var dirtyDayTime: Long
        get() = privateDayTime
        set(value) {
            setDirty(privateDayTime, value)
            privateDayTime = value
        }
    private var privateClearWeatherTime = 0
    private var dirtyClearWeatherTime: Int
        get() = privateClearWeatherTime
        set(value) {
            setDirty(privateClearWeatherTime, value)
            privateClearWeatherTime = value
        }
    private var privateRaining = false
    private var dirtyRaining: Boolean
        get() = privateRaining
        set(value) {
            setDirty(privateRaining, value)
            privateRaining = value
        }
    private var privateRainTime = 0
    private var dirtyRainTime: Int
        get() = privateRainTime
        set(value) {
            setDirty(privateRainTime, value)
            privateRainTime = value
        }
    private var privateThundering = false
    private var dirtyThundering: Boolean
        get() = privateThundering
        set(value) {
            setDirty(privateThundering, value)
            privateThundering = value
        }
    private var privateThunderTime = 0
    private var dirtyThunderTime: Int
        get() = privateThunderTime
        set(value) {
            setDirty(privateThunderTime, value)
            privateThunderTime = value
        }
    private var privateFreezeTime: Boolean = false
    var freezeTime: Boolean
        get() = privateFreezeTime
        set(value) {
            setDirty(privateFreezeTime, value)
            privateFreezeTime = value
        }
    private var privateFreezeWeather: Boolean = false
    var freezeWeather: Boolean
        get() = privateFreezeWeather
        set(value) {
            setDirty(privateFreezeWeather, value)
            privateFreezeWeather = value
        }
    private val spawnPosList: HashSet<BlockPos> = hashSetOf()
    lateinit var chestSavedData: ChestSavedData
        internal set

    override fun getLevelName(): String = worldData.levelName

    override fun setThundering(value: Boolean) {
        if (freezeWeather) return
        dirtyThundering = value
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun setThunderingForce(value: Boolean) {
        dirtyThundering = value
    }

    override fun getRainTime(): Int = dirtyRainTime

    override fun setRainTime(value: Int) {
        if (freezeWeather) return
        dirtyRainTime = value
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun setRainTimeForce(value: Int) {
        dirtyRainTime = value
    }

    override fun getThunderTime(): Int = dirtyThunderTime

    override fun setThunderTime(value: Int) {
        if (freezeWeather) return
        dirtyThunderTime = value
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun setThunderTimeForce(value: Int) {
        dirtyThunderTime = value
    }

    override fun getClearWeatherTime(): Int = dirtyClearWeatherTime

    override fun setClearWeatherTime(value: Int) {
        if (freezeWeather) return
        dirtyClearWeatherTime = value
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun setClearWeatherTimeForce(value: Int) {
        dirtyClearWeatherTime = value
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

    override fun setGameType(value: GameType) {}

    override fun getScheduledEvents(): TimerQueue<MinecraftServer> = wrapped.scheduledEvents

    override fun setGameTime(value: Long) {}

    override fun setDayTime(value: Long) {
        if (freezeTime) return
        dirtyDayTime = value
    }

    @Suppress("unused")
    fun setDayTimeForce(value: Long) {
        dirtyDayTime = value
    }

    override fun setXSpawn(value: Int) {
        dirtyXSpawn = value
    }

    override fun setYSpawn(value: Int) {
        dirtyYSpawn = value
    }

    override fun setZSpawn(value: Int) {
        dirtyZSpawn = value
    }

    override fun setSpawnAngle(value: Float) {
        dirtySpawnAngle = value
    }

    override fun getXSpawn(): Int = dirtyXSpawn

    override fun getYSpawn(): Int = dirtyYSpawn

    override fun getZSpawn(): Int = dirtyZSpawn

    override fun getSpawnAngle(): Float = dirtySpawnAngle

    override fun getGameTime(): Long = wrapped.gameTime

    override fun getDayTime(): Long = dirtyDayTime

    override fun isThundering(): Boolean = dirtyThundering

    override fun isRaining(): Boolean = dirtyRaining

    override fun setRaining(value: Boolean) {
        if (freezeWeather) return
        dirtyRaining = value
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun setRainingForce(value: Boolean) {
        dirtyRaining = value
    }

    override fun isHardcore(): Boolean = worldData.isHardcore

    override fun getGameRules(): GameRules = worldData.gameRules

    override fun getDifficulty(): Difficulty = worldData.difficulty

    override fun isDifficultyLocked(): Boolean = worldData.isDifficultyLocked

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

    fun addSpawnPos(pos: BlockPos) {
        spawnPosList.add(pos)
        setDirty()
    }

    fun removeSpawnPos(pos: BlockPos) {
        spawnPosList.remove(pos)
        setDirty()
    }

    fun getSpawnPosList(): List<BlockPos> = spawnPosList.toList()

    fun load(tag: CompoundTag) {
        privateXSpawn = tag.getInt("SpawnX")
        privateYSpawn = tag.getInt("SpawnY")
        privateZSpawn = tag.getInt("SpawnZ")
        privateSpawnAngle = tag.getFloat("SpawnAngle")
        privateDayTime = tag.getLong("DayTime")
        privateClearWeatherTime = tag.getInt("clearWeatherTime")
        privateRaining = tag.getBoolean("raining")
        privateRainTime = tag.getInt("rainTime")
        privateThundering = tag.getBoolean("thundering")
        privateThunderTime = tag.getInt("thunderTime")
        privateFreezeTime = tag.getBoolean("freezeTime")
        privateFreezeWeather = tag.getBoolean("freezeWeather")

        spawnPosList.clear()

        val list = tag.getList("spawnPosList", Tag.TAG_COMPOUND.toInt())

        for (i in list.indices) {
            val posTag = list.getCompound(i)

            spawnPosList.add(BlockPos(posTag.getInt("x"), posTag.getInt("y"), posTag.getInt("z")))
        }
    }

    fun save(tag: CompoundTag): CompoundTag {
        tag.putInt("SpawnX", privateXSpawn)
        tag.putInt("SpawnY", privateYSpawn)
        tag.putInt("SpawnZ", privateZSpawn)
        tag.putFloat("SpawnAngle", privateSpawnAngle)
        tag.putLong("DayTime", privateDayTime)
        tag.putInt("clearWeatherTime", privateClearWeatherTime)
        tag.putBoolean("raining", privateRaining)
        tag.putInt("rainTime", privateRainTime)
        tag.putBoolean("thundering", privateThundering)
        tag.putInt("thunderTime", privateThunderTime)
        tag.putBoolean("freezeTime", privateFreezeTime)
        tag.putBoolean("freezeWeather", privateFreezeWeather)

        val spawnPosListTag = ListTag()

        for (pos in spawnPosList) {
            val posTag = CompoundTag()
            posTag.putInt("x", pos.x)
            posTag.putInt("y", pos.y)
            posTag.putInt("z", pos.z)

            spawnPosListTag.add(posTag)
        }

        tag.put("spawnPosList", spawnPosListTag)

        return tag
    }

    private fun <T> setDirty(oldValue: T, newValue: T) {
        if (oldValue == newValue) return
        setDirty()
    }

    private fun setDirty() {
        if (!::chestSavedData.isInitialized) throw NullPointerException("ChestSavedData is not initialized")
        chestSavedData.setDirty()
    }
}