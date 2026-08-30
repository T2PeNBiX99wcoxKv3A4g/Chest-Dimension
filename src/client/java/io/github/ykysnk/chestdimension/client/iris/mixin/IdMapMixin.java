package io.github.ykysnk.chestdimension.client.iris.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.ykysnk.chestdimension.client.iris.IrisBlockRegistry;
import net.irisshaders.iris.shaderpack.IdMap;
import net.irisshaders.iris.shaderpack.materialmap.BlockEntry;
import net.irisshaders.iris.shaderpack.materialmap.NamespacedId;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.HashMap;
import java.util.List;

@Mixin(value = IdMap.class, remap = false)
public abstract class IdMapMixin {
    @WrapOperation(method = "lambda$parseBlockMap$2", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0), require = 0)
    private static boolean blockEntriesAdd(List<BlockEntry> instance, Object be, Operation<Boolean> original) {
        if (be instanceof BlockEntry blockEntry) {
            var list = IrisBlockRegistry.INSTANCE.getList(blockEntry.id().toString());
            if (!list.isEmpty()) {
                list.forEach((value) -> {
                    var copyPropertyPredicates = new HashMap<>(blockEntry.propertyPredicates());
                    var newBlockEntry = new BlockEntry(new NamespacedId(value.getNamespace(), value.getPath()), copyPropertyPredicates);
                    original.call(instance, newBlockEntry);
                });
            }
        }
        return original.call(instance, be);
    }
}
