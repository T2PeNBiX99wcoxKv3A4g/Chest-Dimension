@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package io.github.ykysnk.chestdimension.utils

import io.github.ykysnk.chestdimension.Constants
import io.github.ykysnk.chestdimension.data.TaskData
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.world.entity.Entity

object TaskPool {
    private val tasks: MutableList<TaskData> = mutableListOf()
    private val nameTasks: MutableMap<String, TaskData> = mutableMapOf()
    private var currentTasks: MutableList<TaskData>? = null
    private var currentNameTasks: MutableMap<String, TaskData>? = null
    private var isStop = false
    private var tickCount = 0L

    fun run(task: () -> Unit) = run(0L, task)

    fun run(delayTick: Long, task: () -> Unit) {
        tasks.add(TaskData(tickCount + delayTick, task))
    }

    fun run(name: String, task: () -> Unit) = run(name, 0L, task)

    fun run(name: String, delayTick: Long, task: () -> Unit) {
        if (nameTasks.containsKey(name)) return
        nameTasks[name] = TaskData(tickCount + delayTick, task)
    }

    fun run(entity: Entity, task: () -> Unit) = run(entity, 0L, task)

    fun run(entity: Entity, delayTick: Long, task: () -> Unit) {
        val name = entity.uuid.toString()
        if (nameTasks.containsKey(name)) return
        nameTasks[name] = TaskData(tickCount + delayTick, task)
    }

    private fun runTasks() {
        currentTasks = tasks.toMutableList()
        currentNameTasks = nameTasks.toMutableMap()
        val removeTasks = mutableListOf<TaskData>()
        val removeNameTasks = mutableListOf<String>()
        currentTasks?.forEach { data ->
            if (data.nextTick > tickCount || isStop) return@forEach
            runCatching { data.task() }.getOrElse { Constants.LOGGER.error("Task Error: {}", it.localizedMessage, it) }
            removeTasks.add(data)
        }
        currentNameTasks?.forEach { (_, data) ->
            if (data.nextTick > tickCount || isStop) return@forEach
            runCatching { data.task() }.getOrElse { Constants.LOGGER.error("Task Error: {}", it.localizedMessage, it) }
            removeTasks.add(data)
        }
        currentTasks?.clear()
        removeTasks.forEach { tasks.remove(it) }
        removeNameTasks.forEach { nameTasks.remove(it) }
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
            currentNameTasks?.clear()
            tickCount = 0L
        }
    }
}