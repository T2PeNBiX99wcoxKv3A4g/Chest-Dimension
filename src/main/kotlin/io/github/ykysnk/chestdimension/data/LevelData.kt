package io.github.ykysnk.chestdimension.data

import kotlinx.serialization.Serializable

@Serializable
data class LevelData(
    val seed: Long,
    val chestDimension: DimensionData? = null,
    val chestPos: BlockPosData? = null,
    val spawnPosList: HashSet<BlockPosData> = HashSet()
)
