package io.github.ykysnk.chestdimension.client.datagen.provider

import io.github.ykysnk.chestdimension.level.Biomes
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider
import net.minecraft.core.HolderLookup
import java.util.concurrent.CompletableFuture

class BiomeProvider(output: FabricDataOutput, registriesFuture: CompletableFuture<HolderLookup.Provider>) :
    FabricDynamicRegistryProvider(output, registriesFuture) {
    override fun configure(registries: HolderLookup.Provider, entries: Entries) {
        entries.add(Biomes.CHEST_BIOME, Biomes.ChestBiomeType)
    }

    override fun getName(): String {
        return "Chest Biome Dynamic Registries"
    }
}