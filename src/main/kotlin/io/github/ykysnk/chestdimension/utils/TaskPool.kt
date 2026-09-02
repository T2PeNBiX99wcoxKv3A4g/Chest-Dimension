package io.github.ykysnk.chestdimension.utils

import io.github.ykysnk.chestdimension.data.TaskData
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents

object TaskPool {
    private val tasks: MutableList<TaskData> = mutableListOf()
    private var isStop = false
    private var tickCount = 0L

    fun run(task: () -> Unit, delayTick: Long = 0L) {
        tasks.add(TaskData(tickCount + delayTick, task))
    }

    private fun runTasks() {
        val currentTasks = tasks.toList()
        val removeTasks = mutableListOf<TaskData>()
        currentTasks.forEach {
            if (it.nextTick > tickCount || isStop) return@forEach
            it.task()
            removeTasks.add(it)
        }
        removeTasks.forEach { tasks.remove(it) }
    }

    init {
        ServerTickEvents.END_SERVER_TICK.register {
            if (isStop) return@register
            runTasks()
            tickCount++
        }
        ServerLifecycleEvents.SERVER_STARTING.register {
            isStop = false
        }
        ServerLifecycleEvents.SERVER_STOPPING.register {
            isStop = true
            tasks.clear()
            tickCount = 0L
        }
    }
}