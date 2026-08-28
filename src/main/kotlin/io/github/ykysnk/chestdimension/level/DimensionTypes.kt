package io.github.ykysnk.chestdimension.level

import io.github.ykysnk.chestdimension.Constants
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.BlockTags
import net.minecraft.util.valueproviders.ConstantInt
import net.minecraft.world.level.dimension.BuiltinDimensionTypes
import net.minecraft.world.level.dimension.DimensionType
import java.util.*

object DimensionTypes {
    val CHEST: ResourceKey<DimensionType> = ResourceKey.create(Registries.DIMENSION_TYPE, Constants.id("chest"))

    val ChestDimensionType by lazy {
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
            ChestMonsterSettings
        )
    }

    private val ChestMonsterSettings by lazy {
        DimensionType.MonsterSettings(
            true, // piglinSafe
            false, // hasRaids
            ConstantInt.of(0), // monsterSpawnLightTest
            0 // monsterSpawnBlockLightLimit
        )
    }
}