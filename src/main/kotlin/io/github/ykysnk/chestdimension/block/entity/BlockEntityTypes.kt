package io.github.ykysnk.chestdimension.block.entity

import io.github.ykysnk.chestdimension.Constants
import io.github.ykysnk.chestdimension.block.Blocks
import net.minecraft.Util
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.util.datafix.fixes.References
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType

@Suppress("SameParameterValue")
object BlockEntityTypes {
    private fun <T : BlockEntity> register(key: String, builder: BlockEntityType.Builder<T>): BlockEntityType<T> {
        if (builder.validBlocks.isEmpty())
            Constants.LOGGER.warn("Block entity type {} requires at least one valid block to be defined!", key)

        val type = Util.fetchChoiceType(References.BLOCK_ENTITY, key)
        @Suppress("TYPE_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Constants.id(key), builder.build(type))
    }

    val CHEST_DIMENSION: BlockEntityType<ChestDimensionBlockEntity> = register("chest_dimension", BlockEntityType.Builder.of(::ChestDimensionBlockEntity, Blocks.CHEST_DIMENSION))
}