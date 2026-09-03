package io.github.ykysnk.chestdimension.event

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.progress.ChunkProgressListener

object UtilsEvents {
    @JvmField
    val AFTER_CREATE_LEVEL: Event<AfterCreateLevel> = EventFactory.createArrayBacked(AfterCreateLevel::class.java) { callbacks ->
        AfterCreateLevel { server, listener ->
            callbacks.forEach { it.onAfterCreateLevel(server, listener) }
        }
    }

    fun interface AfterCreateLevel {
        fun onAfterCreateLevel(server: MinecraftServer, listener: ChunkProgressListener)
    }
}