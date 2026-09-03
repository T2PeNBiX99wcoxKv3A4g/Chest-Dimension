package io.github.ykysnk.chestdimension.level.dimension

import io.github.ykysnk.chestdimension.id
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.BlockTags
import net.minecraft.util.valueproviders.ConstantInt
import net.minecraft.world.level.dimension.BuiltinDimensionTypes
import net.minecraft.world.level.dimension.DimensionType
import java.util.*

object DimensionTypes {
    val CHEST_PLATFORM: ResourceKey<DimensionType> = ResourceKey.create(Registries.DIMENSION_TYPE, id("chest_platform"))
    val CHEST_UNDEFINED: ResourceKey<DimensionType> =
        ResourceKey.create(Registries.DIMENSION_TYPE, id("chest_undefined"))

    val ChestPlatformDimensionType by lazy {
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
            ChestPlatformMonsterSettings
        )
    }

    val ChestUndefinedDimensionType by lazy {
        DimensionType(
            OptionalLong.empty(), // fixedTime
            false, // hasSkyLight
            false, // hasCeiling
            false, // ultraWarm
            true, // natural
            1.0, // coordinateScale
            false, // bedWorks
            false, // respawnAnchorWorks
            -64, // minY
            1024, // height
            1024, // logicalHeight
            BlockTags.INFINIBURN_OVERWORLD, // infiniburn
            BuiltinDimensionTypes.NETHER_EFFECTS,
            0.0f, // ambientLight
            ChestUndefinedMonsterSettings
        )
    }

    private val ChestPlatformMonsterSettings by lazy {
        DimensionType.MonsterSettings(
            false, // piglinSafe
            false, // hasRaids
            ConstantInt.of(0), // monsterSpawnLightTest
            0 // monsterSpawnBlockLightLimit
        )
    }

    private val ChestUndefinedMonsterSettings by lazy {
        DimensionType.MonsterSettings(
            true, // piglinSafe
            false, // hasRaids
            ConstantInt.of(0), // monsterSpawnLightTest
            0 // monsterSpawnBlockLightLimit
        )
    }
}