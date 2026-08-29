package io.github.ykysnk.chestdimension.mixin;

import io.github.ykysnk.chestdimension.level.ChestLevelManager;
import io.github.ykysnk.chestdimension.level.ChestServerLevel;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.GameRules;
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
        ChestLevelManager.INSTANCE.load((MinecraftServer) (Object) this, listener);
    }

    @Inject(method = "getAllLevels", at = @At("RETURN"), cancellable = true)
    private void getAllLevels(CallbackInfoReturnable<Iterable<ServerLevel>> cir) {
        cir.setReturnValue(new ArrayList<>((Collection<ServerLevel>) cir.getReturnValue()));
    }

    @Inject(method = "synchronizeTime", at = @At("HEAD"), cancellable = true)
    private void synchronizeTime(ServerLevel level, CallbackInfo ci) {
        if (!(level instanceof ChestServerLevel chestServerLevel)) return;
        playerList.broadcastAll(new ClientboundSetTimePacket(level.getGameTime(), level.getDayTime(), !chestServerLevel.getChestServerLevelData().getFreezeTime() && level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)), level.dimension());
        ci.cancel();
    }
}
