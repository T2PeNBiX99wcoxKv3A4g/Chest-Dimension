package io.github.ykysnk.chestdimension.client.datagen.provider

import io.github.ykysnk.chestdimension.level.biome.Biomes
import io.github.ykysnk.chestdimension.level.dimension.DimensionTypes
import io.github.ykysnk.chestdimension.world.damagesource.DamageTypes
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider
import net.minecraft.core.HolderLookup
import java.util.concurrent.CompletableFuture

class DynamicRegistryProvider(output: FabricDataOutput, registriesFuture: CompletableFuture<HolderLookup.Provider>) :
    FabricDynamicRegistryProvider(output, registriesFuture) {
    override fun configure(registries: HolderLookup.Provider, entries: Entries) {
        entries.add(Biomes.CHEST_BIOME, Biomes.ChestBiomeType)
        entries.add(DimensionTypes.CHEST, DimensionTypes.ChestDimensionType)
        entries.add(DamageTypes.EXPLOSION_BY_CHEST, DamageTypes.ExplosionByChestType)
        entries.add(DamageTypes.EXPLOSION_BY_CHEST_INSIDE, DamageTypes.ExplosionByChestInsideType)
        entries.add(DamageTypes.EXPLOSION_BY_TELEPORT_DOOR, DamageTypes.ExplosionByTeleportDoorType)
    }

    override fun getName(): String {
        return "Chest Dimension Dynamic Registries"
    }
}