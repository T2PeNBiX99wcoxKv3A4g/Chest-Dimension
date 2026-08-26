package io.github.ykysnk.chestdimension.accessor;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

@SuppressWarnings("unused")
public interface ChestDimensionData {
    ResourceKey<Level> chestDimension$getOpenedChestDimension();

    void chestDimension$setOpenedChestDimension(ResourceKey<Level> dimension);

    BlockPos chestDimension$getOpenedChestPos();

    void chestDimension$setOpenedChestPos(BlockPos pos);
}
