package io.github.ykysnk.chestdimension.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.ykysnk.chestdimension.Constants;
import io.github.ykysnk.chestdimension.level.ChestServerLevel;
import io.github.ykysnk.chestdimension.level.ChestServerLevelContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.WorldData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/dimension/LevelStem;generator()Lnet/minecraft/world/level/chunk/ChunkGenerator;"))
    private ChunkGenerator newArrayList(LevelStem instance, Operation<ChunkGenerator> original) {
        var generator = original.call(instance);
        if ((Object) this instanceof ChestServerLevel chestServerLevel) {
            var options = ChestServerLevelContext.worldOptions.get();
            if (options == null) {
                Constants.LOGGER.warn("Chest dimension world options not found in context");
                return generator;
            }
            chestServerLevel.levelWorldOptions = options;
        }
        return generator;
    }

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/WorldData;worldGenOptions()Lnet/minecraft/world/level/levelgen/WorldOptions;"))
    private WorldOptions worldGenOptions(WorldData instance, Operation<WorldOptions> original) {
        var originalOptions = original.call(instance);
        if ((Object) this instanceof ChestServerLevel) {
            var options = ChestServerLevelContext.worldOptions.get();
            if (options == null) {
                Constants.LOGGER.warn("Chest dimension world options not found in context");
                return originalOptions;
            }
            return options;
        }
        return originalOptions;
    }
}
