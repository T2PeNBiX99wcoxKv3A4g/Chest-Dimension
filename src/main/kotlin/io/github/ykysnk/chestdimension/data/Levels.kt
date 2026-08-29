package io.github.ykysnk.chestdimension.data

import kotlinx.serialization.Serializable

@Serializable
data class Levels(
    var levels: HashMap<String, LevelData> = hashMapOf(),
    var inactiveLevels: HashSet<String> = hashSetOf()
) {
    fun deepCopy(): Levels = Levels(HashMap(levels), HashSet(inactiveLevels))
}
