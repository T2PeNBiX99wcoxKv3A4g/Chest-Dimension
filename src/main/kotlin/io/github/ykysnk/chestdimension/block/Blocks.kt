package io.github.ykysnk.chestdimension.block

import io.github.ykysnk.chestdimension.Constants
import io.github.ykysnk.chestdimension.utils.RegistryHelper
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.FenceBlock
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.WallBlock
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.level.block.Blocks as MCBlocks

object Blocks : RegistryHelper<Block>() {
    private fun register(name: String, block: Block) =
        register { Registry.register(BuiltInRegistries.BLOCK, Constants.id(name), block) }

    val CHEST_PLATFORM: Block = register(
        "chest_platform",
        Block(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f, 1200.0f).sound(SoundType.WOOD))
    )
    val CHEST_PLATFORM_WALL: Block = register(
        "chest_platform_wall",
        WallBlock(
            BlockBehaviour.Properties.of().mapColor(MCBlocks.OAK_PLANKS.defaultMapColor()).forceSolidOn()
                .strength(2.0f, 1200.0f).sound(SoundType.WOOD)
        )
    )

    val CHEST_PLATFORM_FENCE: Block = register(
        "chest_platform_fence",
        FenceBlock(
            BlockBehaviour.Properties.of().mapColor(MCBlocks.OAK_PLANKS.defaultMapColor()).forceSolidOn()
                .strength(2.0f, 1200.0f).sound(SoundType.WOOD)
        )
    )

    val CHEST_DIMENSION: Block = register(
        "chest_dimension",
        ChestDimensionBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f, 1200.0f).sound(SoundType.WOOD)
        )
    )
}