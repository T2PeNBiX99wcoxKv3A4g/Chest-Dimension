package io.github.ykysnk.chestdimension.utils

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents

object TaskPool {
    private val tasks = mutableListOf<() -> Unit>()
    private var isStop = false

    fun run(task: () -> Unit) {
        tasks.add(task)
    }

    private fun runTasks() {
        if (isStop) return
        tasks.forEach { it() }
        tasks.clear()
    }

    init {
        ServerTickEvents.END_SERVER_TICK.register {
            runTasks()
        }
        ServerLifecycleEvents.SERVER_STARTING.register {
            isStop = false
        }
        ServerLifecycleEvents.SERVER_STOPPING.register {
            isStop = true
        }
    }
}