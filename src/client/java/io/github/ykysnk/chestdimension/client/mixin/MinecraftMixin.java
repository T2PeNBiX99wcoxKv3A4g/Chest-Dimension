package io.github.ykysnk.chestdimension.client.mixin;

import io.github.ykysnk.chestdimension.level.UndefinedLevelManager;
import io.github.ykysnk.chestdimension.sounds.Musics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.Music;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow
    @Nullable
    public LocalPlayer player;

    @Inject(method = "getSituationalMusic", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;player:Lnet/minecraft/client/player/LocalPlayer;", opcode = Opcodes.GETFIELD, ordinal = 1), cancellable = true)
    private void getSituationalMusic(CallbackInfoReturnable<Music> cir) {
        //noinspection DataFlowIssue,resource
        if (!UndefinedLevelManager.INSTANCE.isInsideUndefinedDimension(player.level().dimension())) return;
        cir.setReturnValue(Musics.CHEST_UNDEFINED);
        cir.cancel();
    }
}
