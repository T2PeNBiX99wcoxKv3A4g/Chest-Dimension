package io.github.ykysnk.chestdimension.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents

object TaskPool {
    private val tasks = mutableListOf<() -> Unit>()
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isStop = false

    fun run(task: () -> Unit) = tasks.add(task)

    private fun runTasks() {
        val currentTasks = tasks.toList()
        tasks.clear()
        currentTasks.forEach { scope.launch { it() } }
    }

    init {
        ServerTickEvents.END_SERVER_TICK.register {
            if (isStop) return@register
            runTasks()
        }
        ServerLifecycleEvents.SERVER_STARTING.register {
            isStop = false
        }
        ServerLifecycleEvents.SERVER_STOPPING.register {
            isStop = true
            tasks.clear()
        }
    }
}