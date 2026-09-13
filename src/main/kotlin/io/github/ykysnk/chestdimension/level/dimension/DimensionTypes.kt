package io.github.ykysnk.chestdimension.level.dimension

import io.github.ykysnk.chestdimension.NameSpaces
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.BlockTags
import net.minecraft.util.valueproviders.ConstantInt
import net.minecraft.world.level.dimension.BuiltinDimensionTypes
import net.minecraft.world.level.dimension.DimensionType
import java.util.*

object DimensionTypes {
    val PLATFORM: ResourceKey<DimensionType> =
        ResourceKey.create(Registries.DIMENSION_TYPE, NameSpaces.MOD("platform"))
    val UNDEFINED: ResourceKey<DimensionType> =
        ResourceKey.create(Registries.DIMENSION_TYPE, NameSpaces.MOD("undefined"))

    val PlatformDimensionType by lazy {
        DimensionType(
            OptionalLong.empty(), // fixedTime
            true, // hasSkyLight
            false, // hasCeiling
            false, // ultraWarm
            true, // natural
            1.0, // coordinateScale
            true, // bedWorks
            true, // respawnAnchorWorks
            -128, // minY
            2160, // height
            2160, // logicalHeight
            BlockTags.INFINIBURN_OVERWORLD, // infiniburn
            BuiltinDimensionTypes.OVERWORLD_EFFECTS,
            0.0f, // ambientLight
            PlatformMonsterSettings
        )
    }

    val UndefinedDimensionType by lazy {
        DimensionType(
            OptionalLong.empty(), // fixedTime
            false, // hasSkyLight
            false, // hasCeiling
            false, // ultraWarm
            true, // natural
            1.0, // coordinateScale
            false, // bedWorks
            false, // respawnAnchorWorks
            -128, // minY
            320, // height
            320, // logicalHeight
            BlockTags.INFINIBURN_OVERWORLD, // infiniburn
            BuiltinDimensionTypes.OVERWORLD_EFFECTS,
            0.0f, // ambientLight
            UndefinedMonsterSettings
        )
    }

    private val PlatformMonsterSettings by lazy {
        DimensionType.MonsterSettings(
            true, // piglinSafe
            false, // hasRaids
            ConstantInt.of(0), // monsterSpawnLightTest
            0 // monsterSpawnBlockLightLimit
        )
    }

    private val UndefinedMonsterSettings by lazy {
        DimensionType.MonsterSettings(
            false, // piglinSafe
            false, // hasRaids
            ConstantInt.of(0), // monsterSpawnLightTest
            0 // monsterSpawnBlockLightLimit
        )
    }
}