package io.github.ykysnk.chestdimension.utils

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents

object TaskPool {
    private val tasks = mutableListOf<() -> Unit>()

    fun run(task: () -> Unit) {
        tasks.add(task)
    }

    init {
        ServerTickEvents.END_SERVER_TICK.register {
            tasks.forEach { it() }
            tasks.clear()
        }
    }
}