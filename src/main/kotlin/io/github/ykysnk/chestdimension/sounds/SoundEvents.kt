@file:Suppress("SameParameterValue", "unused")

package io.github.ykysnk.chestdimension.sounds

import io.github.ykysnk.chestdimension.tags.NameSpaces
import io.github.ykysnk.chestdimension.utils.RegistryHelper
import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent

object SoundEvents : RegistryHelper<SoundEvent>() {
    private fun register(name: ResourceLocation, location: ResourceLocation, range: Float) = Registry.registerForHolder(
        BuiltInRegistries.SOUND_EVENT,
        name,
        SoundEvent.createFixedRangeEvent(location, range)
    )

    private fun register(name: String): SoundEvent = register(NameSpaces.MOD(name))

    private fun register(name: ResourceLocation): SoundEvent = register(name, name)

    private fun registerForHolder(name: String): Holder.Reference<SoundEvent> = registerForHolder(NameSpaces.MOD(name))

    private fun registerForHolder(name: ResourceLocation): Holder.Reference<SoundEvent> = registerForHolder(name, name)

    private fun register(name: ResourceLocation, location: ResourceLocation): SoundEvent =
        register(Registry.register(BuiltInRegistries.SOUND_EVENT, name, SoundEvent.createVariableRangeEvent(location)))

    private fun registerForHolder(name: ResourceLocation, location: ResourceLocation): Holder.Reference<SoundEvent> =
        Registry.registerForHolder(BuiltInRegistries.SOUND_EVENT, name, SoundEvent.createVariableRangeEvent(location))

    val MUSIC_CHEST_UNDEFINED = registerForHolder("music.chest_undefined")
}