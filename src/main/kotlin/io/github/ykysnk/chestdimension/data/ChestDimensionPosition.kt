package io.github.ykysnk.chestdimension.data

import kotlinx.serialization.Serializable

@Serializable
data class ChestDimensionPosition(
    val dimension: DimensionData? = null,
    val blockPos: BlockPosData? = null
)
