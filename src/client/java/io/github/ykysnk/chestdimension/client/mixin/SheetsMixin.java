package io.github.ykysnk.chestdimension.client.mixin;

import io.github.ykysnk.chestdimension.block.entity.ChestDimensionBlockEntity;
import io.github.ykysnk.chestdimension.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = net.minecraft.client.renderer.Sheets.class, priority = 10000)
public abstract class SheetsMixin {
    @Inject(method = "chooseMaterial(Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/level/block/state/properties/ChestType;Z)Lnet/minecraft/client/resources/model/Material;", at = @At("HEAD"), cancellable = true)
    private static void chooseMaterial(BlockEntity blockEntity, ChestType chestType, boolean holiday, CallbackInfoReturnable<Material> cir) {
        if (!(blockEntity instanceof ChestDimensionBlockEntity)) return;
        cir.setReturnValue(Sheets.CHEST_DIMENSION_LOCATION);
        cir.cancel();
    }
}
