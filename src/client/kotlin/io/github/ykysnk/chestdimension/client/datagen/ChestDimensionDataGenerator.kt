package io.github.ykysnk.chestdimension.client.datagen

import io.github.ykysnk.chestdimension.client.datagen.provider.*
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator

object ChestDimensionDataGenerator : DataGeneratorEntrypoint {
	override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
		val pack = fabricDataGenerator.createPack()
		pack.addProvider(::DimensionTypeProvider)
		pack.addProvider(::BiomeProvider)
		pack.addProvider(::ModelProvider)
		pack.addProvider(::BlockTagProvider)
		pack.addProvider(::BlockLootTableProvider)
	}
}