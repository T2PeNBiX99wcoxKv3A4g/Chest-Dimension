package io.github.ykysnk.chestdimension.client.datagen.provider

import io.github.ykysnk.chestdimension.world.damagesource.DamageTypes
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.Registries
import net.minecraft.tags.DamageTypeTags
import net.minecraft.world.damagesource.DamageType
import java.util.concurrent.CompletableFuture

class DamageTypeTagProvider(output: FabricDataOutput, registries: CompletableFuture<HolderLookup.Provider>) :
    FabricTagProvider<DamageType>(output, Registries.DAMAGE_TYPE, registries) {
    override fun addTags(provider: HolderLookup.Provider) {
        getOrCreateTagBuilder(DamageTypeTags.ALWAYS_HURTS_ENDER_DRAGONS).apply {
            add(DamageTypes.EXPLOSION_BY_CHEST)
            add(DamageTypes.EXPLOSION_BY_CHEST_INSIDE)
        }
    }
}