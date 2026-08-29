package io.github.ykysnk.chestdimension.data

import kotlinx.serialization.Serializable

@Serializable
data class Levels(
    var levels: HashMap<String, LevelData> = hashMapOf(),
    var inactiveLevels: HashMap<String, HashSet<String>> = hashMapOf()
) {
    fun deepCopy(): Levels =
        Levels(HashMap(levels), HashMap(inactiveLevels.mapValues { (_, value) -> value.toHashSet() }))
}
