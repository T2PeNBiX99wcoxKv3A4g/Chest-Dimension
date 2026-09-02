@file:Suppress("MemberVisibilityCanBePrivate")

package io.github.ykysnk.chestdimension.utils

import io.github.ykysnk.chestdimension.Constants
import io.github.ykysnk.chestdimension.data.TaskData
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents

object TaskPool {
    private val tasks: MutableList<TaskData> = mutableListOf()
    private var currentTasks: MutableList<TaskData>? = null
    private var isStop = false
    private var tickCount = 0L

    fun run(task: () -> Unit) = run(0L, task)

    fun run(delayTick: Long, task: () -> Unit) {
        tasks.add(TaskData(tickCount + delayTick, task))
    }

    private fun runTasks() {
        currentTasks = tasks.toMutableList()
        val removeTasks = mutableListOf<TaskData>()
        currentTasks?.forEach { data ->
            if (data.nextTick > tickCount || isStop) return@forEach
            runCatching { data.task() }.getOrElse { Constants.LOGGER.error("Task Error: {}", it.localizedMessage, it) }
            removeTasks.add(data)
        }
        currentTasks?.clear()
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
            currentTasks?.clear()
            tickCount = 0L
        }
    }
}