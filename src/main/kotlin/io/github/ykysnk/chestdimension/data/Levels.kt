package io.github.ykysnk.chestdimension.data

import kotlinx.serialization.Serializable

@Serializable
data class Levels(var levels: HashMap<String, LevelData> = HashMap(), var inactiveLevels: HashSet<String> = HashSet()) {
    fun deepCopy(): Levels = Levels(HashMap(levels), HashSet(inactiveLevels))
}
