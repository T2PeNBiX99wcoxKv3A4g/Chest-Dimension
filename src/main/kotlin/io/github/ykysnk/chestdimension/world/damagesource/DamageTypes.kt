package io.github.ykysnk.chestdimension.world.damagesource

import io.github.ykysnk.chestdimension.Constants
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.damagesource.DamageScaling
import net.minecraft.world.damagesource.DamageType

object DamageTypes {
    val EXPLOSION_BY_CHEST: ResourceKey<DamageType> =
        ResourceKey.create(Registries.DAMAGE_TYPE, Constants.id("explosion_by_chest"))

    val EXPLOSION_BY_CHEST_INSIDE: ResourceKey<DamageType> =
        ResourceKey.create(Registries.DAMAGE_TYPE, Constants.id("explosion_by_chest_inside"))

    val ExplosionByChestType by lazy { DamageType("explosion_by_chest", DamageScaling.NEVER, 4.0f) }

    val ExplosionByChestInsideType by lazy { DamageType("explosion_by_chest_inside", DamageScaling.NEVER, 10.0f) }
}