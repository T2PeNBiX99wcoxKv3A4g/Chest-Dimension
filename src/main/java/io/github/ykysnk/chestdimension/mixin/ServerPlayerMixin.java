package io.github.ykysnk.chestdimension.mixin;

import com.mojang.authlib.GameProfile;
import io.github.ykysnk.chestdimension.accessor.ChestDimensionData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player implements ChestDimensionData {
    @Unique
    private static final String OPENED_CHEST_DIMENSION = "OpenedChestDim";
    @Unique
    private static final String OPENED_CHEST_POS_X = "OpenedChestDimPosX";
    @Unique
    private static final String OPENED_CHEST_POS_Y = "OpenedChestDimPosY";
    @Unique
    private static final String OPENED_CHEST_POS_Z = "OpenedChestDimPosZ";
    @Unique
    private ResourceKey<Level> chestDimension$openedChestDimension;
    @Unique
    private BlockPos chestDimension$openedChestPos;

    protected ServerPlayerMixin(Level level, BlockPos pos, float yRot, GameProfile gameProfile) {
        super(level, pos, yRot, gameProfile);
    }

    @Override
    public ResourceKey<Level> chestDimension$getOpenedChestDimension() {
        return chestDimension$openedChestDimension;
    }

    @Override
    public void chestDimension$setOpenedChestDimension(ResourceKey<Level> dimension) {
        chestDimension$openedChestDimension = dimension;
    }

    @Override
    public BlockPos chestDimension$getOpenedChestPos() {
        return chestDimension$openedChestPos;
    }

    @Override
    public void chestDimension$setOpenedChestPos(BlockPos pos) {
        chestDimension$openedChestPos = pos;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void addAdditionalSaveData(CompoundTag compound, CallbackInfo ci) {
        compound.putString(OPENED_CHEST_DIMENSION, chestDimension$openedChestDimension.location().toString());
        compound.putInt(OPENED_CHEST_POS_X, chestDimension$openedChestPos.getX());
        compound.putInt(OPENED_CHEST_POS_Y, chestDimension$openedChestPos.getY());
        compound.putInt(OPENED_CHEST_POS_Z, chestDimension$openedChestPos.getZ());
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readAdditionalSaveData(CompoundTag compound, CallbackInfo ci) {
        var dimKey = ResourceLocation.tryParse(compound.getString(OPENED_CHEST_DIMENSION));
        if (dimKey != null)
            chestDimension$openedChestDimension = ResourceKey.create(Registries.DIMENSION, dimKey);
        chestDimension$openedChestPos = new BlockPos(compound.getInt(OPENED_CHEST_POS_X), compound.getInt(OPENED_CHEST_POS_Y), compound.getInt(OPENED_CHEST_POS_Z));
    }
}
