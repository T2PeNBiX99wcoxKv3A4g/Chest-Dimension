package io.github.ykysnk.chestdimension.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.ykysnk.chestdimension.extensions.EntityExtensionsKt;
import io.github.ykysnk.chestdimension.item.Items;
import io.github.ykysnk.chestdimension.level.ChestLevelManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@SuppressWarnings({"AbstractClassWithoutAbstractMethods", "AbstractClassNeverImplemented"})
@Mixin(value = Entity.class, priority = 10000)
public abstract class EntityMixin {
    @Shadow
    private Level level;

    @Shadow
    private Vec3 position;

    @Shadow
    public abstract void setPos(Vec3 pos);

    @WrapOperation(method = "checkBelowWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;onBelowWorld()V"))
    private void checkBelowWorld(Entity instance, Operation<Void> original) {
        if (ChestLevelManager.INSTANCE.teleportEntityToExit(level, instance)) return;
        if (!level.isClientSide && level instanceof ServerLevel serverLevel && instance instanceof ItemEntity itemEntity && itemEntity.getItem().is(Items.CHEST_DIMENSION)) {
            var blockPos = BlockPos.containing(position);
            EntityExtensionsKt.teleportToSafeLocation(instance, serverLevel, blockPos, 20, false);
            setPos(new Vec3(position.x, Math.max(level.getMinBuildHeight() + 2, position.y), position.z));
            return;
        }
        original.call(instance);
    }
}
