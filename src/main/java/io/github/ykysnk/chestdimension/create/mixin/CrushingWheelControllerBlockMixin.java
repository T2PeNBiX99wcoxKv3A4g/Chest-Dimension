package io.github.ykysnk.chestdimension.create.mixin;

import com.simibubi.create.content.kinetics.crusher.CrushingWheelControllerBlock;
import io.github.ykysnk.chestdimension.item.Items;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(value = CrushingWheelControllerBlock.class, remap = false)
public abstract class CrushingWheelControllerBlockMixin {
    @Inject(method = "checkEntityForProcessing", at = @At("HEAD"), require = 0, cancellable = true)
    private void checkEntityForProcessing(Level worldIn, BlockPos pos, Entity entityIn, CallbackInfo ci) {
        if (!(entityIn instanceof ItemEntity itemEntity) || !itemEntity.getItem().is(Items.CHEST_DIMENSION)) return;
        ci.cancel();
    }
}
