package io.github.ykysnk.chestdimension

import io.github.ykysnk.chestdimension.Constants.ForceInitialize
import io.github.ykysnk.chestdimension.command.ChestDimCommand
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback

object ChestDimension : ModInitializer {
    override fun onInitialize() {
        ForceInitialize

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            ChestDimCommand.register(dispatcher)
        }
    }
}
