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
        tasks.clear()
        currentTasks.forEach {
            if (it.nextTick <= tickCount || isStop) return@forEach
            it.task()
        }
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
            tickCount = 0L
        }
    }
}