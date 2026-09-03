package io.github.ykysnk.chestdimension.block

import io.github.ykysnk.chestdimension.block.entity.BlockEntityTypes
import io.github.ykysnk.chestdimension.block.entity.ChestDimensionBlockEntity
import io.github.ykysnk.chestdimension.level.ChestLevelManager
import io.github.ykysnk.chestdimension.level.UUIDManager
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.stats.Stats
import net.minecraft.util.RandomSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.monster.piglin.PiglinAi
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.block.*
import net.minecraft.world.level.block.DoubleBlockCombiner.NeighborCombineResult
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.entity.ChestBlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.block.state.properties.DirectionProperty
import net.minecraft.world.level.material.FluidState
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.level.pathfinder.PathComputationType
import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape

class ChestDimensionBlock(properties: Properties) :
    AbstractChestBlock<ChestDimensionBlockEntity>(properties, { BlockEntityTypes.CHEST_DIMENSION }),
    SimpleWaterloggedBlock {
    init {
        registerDefaultState(
            stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(WATERLOGGED, false)
        )
    }

    companion object {
        val FACING: DirectionProperty = HorizontalDirectionalBlock.FACING
        val WATERLOGGED: BooleanProperty = BlockStateProperties.WATERLOGGED
        val SHAPE: VoxelShape = box(1.0, 0.0, 1.0, 15.0, 14.0, 15.0)
    }

    override fun combine(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        override: Boolean
    ): NeighborCombineResult<out ChestBlockEntity> {
        return object : NeighborCombineResult<ChestBlockEntity> {
            override fun <T> apply(combiner: DoubleBlockCombiner.Combiner<in ChestBlockEntity, T>): T {
                return combiner.acceptNone()
            }
        }
    }

    @Deprecated(
        "Deprecated in Java",
        ReplaceWith("SHAPE", "io.github.ykysnk.chestdimension.block.ChestDimensionBlock.Companion.SHAPE")
    )
    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape {
        return SHAPE
    }

    override fun getRenderShape(state: BlockState): RenderShape {
        return RenderShape.ENTITYBLOCK_ANIMATED
    }

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState {
        val fluidState = context.level.getFluidState(context.clickedPos)
        return defaultBlockState().setValue(FACING, context.horizontalDirection.opposite)
            .setValue(WATERLOGGED, fluidState.type === Fluids.WATER)
    }

    @Deprecated("Deprecated in Java")
    override fun use(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hand: InteractionHand,
        hit: BlockHitResult
    ): InteractionResult {
        val blockEntity = level.getBlockEntity(pos)
        if (blockEntity is ChestDimensionBlockEntity) {
            val blockPos = pos.above()
            when {
                level.getBlockState(blockPos)
                    .isRedstoneConductor(level, blockPos) -> return InteractionResult.sidedSuccess(level.isClientSide)

                level.isClientSide -> return InteractionResult.SUCCESS
                else -> {
                    level.scheduleTick(pos, this, 20)
                    blockEntity.startOpen(player)
                    player.awardStat(Stats.CUSTOM.get(Stats.OPEN_CHEST))
                    PiglinAi.angerNearbyPiglins(player, true)
                    return InteractionResult.CONSUME
                }
            }
        } else {
            return InteractionResult.sidedSuccess(level.isClientSide)
        }
    }

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = ChestDimensionBlockEntity(pos, state)

    override fun <T : BlockEntity> getTicker(
        level: Level,
        state: BlockState,
        blockEntityType: BlockEntityType<T>
    ) = if (level.isClientSide) createTickerHelper(
        blockEntityType,
        BlockEntityTypes.CHEST_DIMENSION
    ) { level, pos, state, blockEntity ->
        ChestDimensionBlockEntity.lidAnimateTick(
            level,
            pos,
            state,
            blockEntity
        )
    } else null

    override fun animateTick(state: BlockState, level: Level, pos: BlockPos, random: RandomSource) {
        for (i in 0..2) {
            val j = random.nextInt(2) * 2 - 1
            val k = random.nextInt(2) * 2 - 1
            val d = pos.x.toDouble() + 0.5 + 0.25 * j.toDouble()
            val e = (pos.y.toFloat() + random.nextFloat()).toDouble()
            val f = pos.z.toDouble() + 0.5 + 0.25 * k.toDouble()
            val g = (random.nextFloat() * j.toFloat()).toDouble()
            val h = (random.nextFloat().toDouble() - 0.5) * 0.125
            val l = (random.nextFloat() * k.toFloat()).toDouble()
            level.addParticle(ParticleTypes.PORTAL, d, e, f, g, h, l)
        }
    }

    @Deprecated(
        "Deprecated in Java",
        ReplaceWith(
            "state.setValue(FACING, rotation.rotate(state.getValue(FACING)))",
            "net.minecraft.world.level.block.EnderChestBlock"
        )
    )
    override fun rotate(state: BlockState, rotation: Rotation): BlockState =
        state.setValue(FACING, rotation.rotate(state.getValue(FACING)))

    @Deprecated(
        "Deprecated in Java",
        ReplaceWith(
            "state.rotate(mirror.getRotation(state.getValue(FACING)))",
            "net.minecraft.world.level.block.EnderChestBlock"
        )
    )
    override fun mirror(state: BlockState, mirror: Mirror): BlockState =
        state.rotate(mirror.getRotation(state.getValue(FACING)))

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING, WATERLOGGED)
    }

    @Deprecated(
        "Deprecated in Java", ReplaceWith(
            "if (state.getValue(WATERLOGGED)) Fluids.WATER.getSource(false) else super.getFluidState(state)",
            "net.minecraft.world.level.block.EnderChestBlock",
            "net.minecraft.world.level.material.Fluids",
            "net.minecraft.world.level.block.AbstractChestBlock"
        )
    )
    override fun getFluidState(state: BlockState): FluidState =
        if (state.getValue(WATERLOGGED)) Fluids.WATER.getSource(false) else super.getFluidState(state)

    @Deprecated("Deprecated in Java")
    override fun updateShape(
        state: BlockState,
        direction: Direction,
        neighborState: BlockState,
        level: LevelAccessor,
        pos: BlockPos,
        neighborPos: BlockPos
    ): BlockState {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level))
        }

        return super.updateShape(state, direction, neighborState, level, pos, neighborPos)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("false"))
    override fun isPathfindable(
        state: BlockState,
        level: BlockGetter,
        pos: BlockPos,
        type: PathComputationType
    ): Boolean = false

    @Deprecated("Deprecated in Java")
    override fun tick(state: BlockState, level: ServerLevel, pos: BlockPos, random: RandomSource) {
        val blockEntity = level.getBlockEntity(pos)
        if (blockEntity !is ChestDimensionBlockEntity) return
        blockEntity.checkCache()
        blockEntity.recheckOpen()
    }

    @Deprecated("Deprecated in Java")
    override fun getDrops(state: BlockState, params: LootParams.Builder): List<ItemStack> {
        val drops = super.getDrops(state, params)
        val blockEntity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY)

        if (blockEntity is ChestDimensionBlockEntity) {
            for (stack in drops) {
                if (stack.item != asItem()) continue
                val tag = CompoundTag()
                tag.putUUID("UUID", blockEntity.uuid)
                BlockItem.setBlockEntityData(stack, BlockEntityTypes.CHEST_DIMENSION, tag)
            }
        }

        return drops
    }

    @Deprecated("Deprecated in Java")
    override fun onRemove(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        newState: BlockState,
        movedByPiston: Boolean
    ) {
        if (!state.`is`(newState.block)) {
            val blockEntity = level.getBlockEntity(pos)
            if (blockEntity is ChestDimensionBlockEntity && !blockEntity.destroyByItSelf && !blockEntity.haveSameChest())
                ChestLevelManager.setInactive(blockEntity.uuid)
            UUIDManager.save()
        }

        super.onRemove(state, level, pos, newState, movedByPiston)
    }
}