package io.github.ykysnk.chestdimension.client.renderer

import io.github.ykysnk.chestdimension.tags.NameSpaces
import net.minecraft.client.renderer.Sheets
import net.minecraft.client.resources.model.Material

object Sheets {
    @Suppress("SameParameterValue")
    private fun chestMaterial(chestName: String): Material {
        return Material(Sheets.CHEST_SHEET, NameSpaces.MOD("entity/chest/$chestName"))
    }

    @JvmField
    val CHEST_DIMENSION_LOCATION = chestMaterial("chest_dimension")
}