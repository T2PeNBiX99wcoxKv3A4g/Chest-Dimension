package io.github.ykysnk.chestdimension.data

import kotlinx.serialization.Serializable

@Serializable
data class LevelData(
    val seed: Long,
    val chestDimensionPositions: HashMap<String, ChestDimensionPosition> = hashMapOf(),
)
