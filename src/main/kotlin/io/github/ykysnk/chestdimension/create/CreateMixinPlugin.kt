package io.github.ykysnk.chestdimension.create

import io.github.ykysnk.chestdimension.Constants
import net.fabricmc.loader.api.FabricLoader
import org.objectweb.asm.tree.ClassNode
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin
import org.spongepowered.asm.mixin.extensibility.IMixinInfo
import org.spongepowered.asm.service.MixinService

class CreateMixinPlugin : IMixinConfigPlugin {
    override fun onLoad(mixinPackage: String) {}

    override fun getRefMapperConfig(): String? = null

    override fun shouldApplyMixin(targetClassName: String, mixinClassName: String): Boolean {
        if (!FabricLoader.getInstance().isModLoaded("create")) return false
        val classNode =
            runCatching { MixinService.getService().bytecodeProvider.getClassNode(targetClassName) }.getOrElse { return false }
        if (classNode == null) {
            Constants.LOGGER.warn("Mixin {} failed to apply to {}", mixinClassName, targetClassName)
            return false
        }
        return true
    }

    override fun acceptTargets(myTargets: MutableSet<String>, otherTargets: MutableSet<String>) {}

    override fun getMixins(): MutableList<String> = mutableListOf()

    override fun preApply(
        targetClassName: String,
        targetClass: ClassNode,
        mixinClassName: String,
        mixinInfo: IMixinInfo
    ) {
    }

    override fun postApply(
        targetClassName: String,
        targetClass: ClassNode,
        mixinClassName: String,
        mixinInfo: IMixinInfo
    ) {
    }
}