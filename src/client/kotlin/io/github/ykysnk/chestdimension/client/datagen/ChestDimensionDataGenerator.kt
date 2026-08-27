package io.github.ykysnk.chestdimension.client.datagen

import io.github.ykysnk.chestdimension.client.datagen.provider.*
import io.github.ykysnk.chestdimension.world.damagesource.DamageTypes
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.minecraft.core.RegistrySetBuilder
import net.minecraft.core.registries.Registries

object ChestDimensionDataGenerator : DataGeneratorEntrypoint {
    override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
        val pack = fabricDataGenerator.createPack()
        pack.addProvider(::DynamicRegistryProvider)
        pack.addProvider(::ModelProvider)
        pack.addProvider(::BlockTagProvider)
        pack.addProvider(::DamageTypeTagProvider)
        pack.addProvider(::BlockLootTableProvider)
    }

    override fun buildRegistry(registryBuilder: RegistrySetBuilder) {
        registryBuilder.add(Registries.DAMAGE_TYPE) { context ->
            context.register(DamageTypes.EXPLOSION_BY_CHEST, DamageTypes.ExplosionByChestType)
        }
    }
}