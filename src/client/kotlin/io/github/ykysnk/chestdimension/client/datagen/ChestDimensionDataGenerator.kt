package io.github.ykysnk.chestdimension.client.datagen

import io.github.ykysnk.chestdimension.client.datagen.provider.BlockLootTableProvider
import io.github.ykysnk.chestdimension.client.datagen.provider.BlockTagProvider
import io.github.ykysnk.chestdimension.client.datagen.provider.DynamicRegistryProvider
import io.github.ykysnk.chestdimension.client.datagen.provider.ModelProvider
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator

object ChestDimensionDataGenerator : DataGeneratorEntrypoint {
    override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
        val pack = fabricDataGenerator.createPack()
        pack.addProvider(::DynamicRegistryProvider)
        pack.addProvider(::ModelProvider)
        pack.addProvider(::BlockTagProvider)
        pack.addProvider(::BlockLootTableProvider)
    }
}