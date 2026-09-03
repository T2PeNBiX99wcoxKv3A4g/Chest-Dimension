package io.github.ykysnk.chestdimension.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.ykysnk.chestdimension.event.UtilsEvents;
import io.github.ykysnk.chestdimension.level.ChestLevelManager;
import io.github.ykysnk.chestdimension.level.ChestServerLevel;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings({"AbstractClassWithoutAbstractMethods", "AbstractClassNeverImplemented", "MethodMayBeStatic"})
@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Shadow
    private PlayerList playerList;

    @Inject(method = "createLevels", at = @At("TAIL"))
    private void createLevels(ChunkProgressListener listener, CallbackInfo ci) {
        UtilsEvents.AFTER_CREATE_LEVEL.invoker().onAfterCreateLevel((MinecraftServer) (Object) this, listener);
    }

    @Inject(method = "getAllLevels", at = @At("RETURN"), cancellable = true)
    private void getAllLevels(CallbackInfoReturnable<Iterable<ServerLevel>> cir) {
        cir.setReturnValue(new ArrayList<>((Collection<ServerLevel>) cir.getReturnValue()));
    }

    @ModifyExpressionValue(method = "synchronizeTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z"))
    private boolean synchronizeTime(boolean original, @Local(argsOnly = true) ServerLevel level) {
        if (!(level instanceof ChestServerLevel chestServerLevel)) return original;
        return !chestServerLevel.getChestServerLevelData().getFreezeTime() && original;
    }
}
