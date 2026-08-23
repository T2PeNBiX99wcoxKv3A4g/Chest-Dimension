package io.github.ykysnk.chestdimension.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.ykysnk.chestdimension.Constants;
import io.github.ykysnk.chestdimension.level.ChestLevelManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@SuppressWarnings({"AbstractClassWithoutAbstractMethods", "AbstractClassNeverImplemented"})
@Mixin(value = Entity.class, priority = 10000)
public abstract class EntityMixin {
    @Shadow
    private Level level;

    @WrapOperation(method = "checkBelowWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;onBelowWorld()V"))
    private void checkBelowWorld(Entity instance, Operation<Void> original) {
        var test = ChestLevelManager.INSTANCE.teleportEntityToExit(level, instance);
        if (!level.isClientSide)
            Constants.LOGGER.info("test: {}", test);
        if (test) return;
        original.call(instance);
    }
}
