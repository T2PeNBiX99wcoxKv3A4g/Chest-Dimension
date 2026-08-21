package io.github.ykysnk.chestdimension.mixin;

import io.github.ykysnk.chestdimension.level.ChestLevelManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Inject(method = "createLevels", at = @At("TAIL"))
    private void createLevels(ChunkProgressListener listener, CallbackInfo ci) {
        ChestLevelManager.INSTANCE.load((MinecraftServer) (Object) this, listener);
    }

    @Inject(method = "getAllLevels", at = @At("RETURN"), cancellable = true)
    private void getAllLevels(CallbackInfoReturnable<Iterable<ServerLevel>> cir){
        cir.setReturnValue(new ArrayList<>((Collection<ServerLevel>) cir.getReturnValue()));
    }
}
