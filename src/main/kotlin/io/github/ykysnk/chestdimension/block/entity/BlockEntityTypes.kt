package io.github.ykysnk.chestdimension.block.entity

import io.github.ykysnk.chestdimension.Constants
import io.github.ykysnk.chestdimension.block.Blocks
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType

@Suppress("SameParameterValue")
object BlockEntityTypes {
    private fun <T : BlockEntity> register(key: String, builder: BlockEntityType.Builder<T>): BlockEntityType<T> {
        if (builder.validBlocks.isEmpty())
            Constants.LOGGER.warn("Block entity type {} requires at least one valid block to be defined!", key)

        @Suppress("TYPE_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Constants.id(key), builder.build(null))
    }

    val CHEST_DIMENSION: BlockEntityType<ChestDimensionBlockEntity> =
        register("chest_dimension", BlockEntityType.Builder.of(::ChestDimensionBlockEntity, Blocks.CHEST_DIMENSION))

    val TELEPORT_PRESSURE_PLATE: BlockEntityType<TeleportPressurePlateBlockEntity> =
        register(
            "teleport_pressure_plate",
            BlockEntityType.Builder.of(::TeleportPressurePlateBlockEntity, Blocks.TELEPORT_PRESSURE_PLATE)
        )
}