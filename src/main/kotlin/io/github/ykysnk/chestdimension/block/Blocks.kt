package io.github.ykysnk.chestdimension.block

import io.github.ykysnk.chestdimension.id
import io.github.ykysnk.chestdimension.utils.RegistryHelper
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.level.block.*
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.properties.BlockSetType
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.level.material.PushReaction
import net.minecraft.world.level.block.Blocks as MCBlocks

object Blocks : RegistryHelper<Block>() {
    private fun register(name: String, block: Block) =
        register(Registry.register(BuiltInRegistries.BLOCK, id(name), block))

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
            BlockBehaviour.Properties.of().mapColor(MCBlocks.OAK_PLANKS.defaultMapColor())
                .strength(2.0f, 1200.0f).sound(SoundType.WOOD)
        )
    )

    val CHEST_PLATFORM_ENTER_PLATE: Block = register(
        "chest_platform_enter_plate",
        PlatformEnterPlateBlock(
            BlockBehaviour.Properties.of().mapColor(MCBlocks.OAK_PLANKS.defaultMapColor()).forceSolidOn().noCollission()
                .strength(0.5f, 1200.0f).pushReaction(PushReaction.DESTROY)
        )
    )

    val BLAST_RESISTANT_GLASS: Block = register(
        "blast_resistant_glass",
        GlassBlock(
            BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.HAT).strength(0.3f, 1200.0f)
                .sound(SoundType.GLASS).noOcclusion().isValidSpawn(MCBlocks::never).isRedstoneConductor(MCBlocks::never)
                .isSuffocating(MCBlocks::never).isViewBlocking(MCBlocks::never)
        )
    )

    val BLAST_RESISTANT_GLASS_PANE: Block = register(
        "blast_resistant_glass_pane",
        IronBarsBlock(
            BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.HAT).strength(0.3f, 1200.0f)
                .sound(SoundType.GLASS).noOcclusion()
        )
    )

    val TELEPORT_PRESSURE_PLATE: Block = register(
        "teleport_pressure_plate",
        TeleportPressurePlateBlock(
            BlockBehaviour.Properties.of().mapColor(MCBlocks.OAK_PLANKS.defaultMapColor()).forceSolidOn().noCollission()
                .strength(0.5f, 1200.0f).pushReaction(PushReaction.DESTROY), BlockSetType.OAK
        )
    )

    val CHEST_DIMENSION: Block = register(
        "chest_dimension",
        ChestDimensionBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f, 1200.0f).sound(SoundType.WOOD)
        )
    )

    val WEATHER_TIME_CONTROLLER: Block = register(
        "weather_time_controller",
        WeatherTimeControllerBlock(
            BlockBehaviour.Properties.of().mapColor(MCBlocks.OAK_PLANKS.defaultMapColor()).forceSolidOn()
                .strength(2.0f, 1200.0f).sound(SoundType.WOOD)
        )
    )

    val TELEPORT_DOOR: Block = register(
        "teleport_door",
        TeleportDoorBlock(
            BlockBehaviour.Properties.of().mapColor(MCBlocks.OAK_PLANKS.defaultMapColor()).strength(3.0f, 1200.0f)
                .noOcclusion().pushReaction(PushReaction.DESTROY), BlockSetType.OAK
        )
    )

    val DEATH_BODY: Block = register(
        "death_body",
        DeathBodyBlock(
            BlockBehaviour.Properties.of().mapColor(MCBlocks.SNOW.defaultMapColor()).strength(5.0f).noOcclusion()
                .pushReaction(PushReaction.DESTROY).sound(SoundType.GRAVEL)
        )
    )
}