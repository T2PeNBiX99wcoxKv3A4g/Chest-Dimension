package io.github.ykysnk.chestdimension.create

data class PatchMethodData(val name: String, val desc: String) {
    override fun toString(): String = "$name$desc"
}
