package io.github.ykysnk.chestdimension.client.datagen.provider

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.Registries
import java.util.concurrent.CompletableFuture

class DynamicRegistryProvider(output: FabricDataOutput, registriesFuture: CompletableFuture<HolderLookup.Provider>) :
    FabricDynamicRegistryProvider(output, registriesFuture) {
    override fun configure(registries: HolderLookup.Provider, entries: Entries) {
        registries.lookup(Registries.BIOME).ifPresent(entries::addAll)
        registries.lookup(Registries.DIMENSION_TYPE).ifPresent(entries::addAll)
        registries.lookup(Registries.DAMAGE_TYPE).ifPresent(entries::addAll)
        registries.lookup(Registries.NOISE_SETTINGS).ifPresent(entries::addAll)
    }

    override fun getName(): String {
        return "Chest Dimension Dynamic Registries"
    }
}