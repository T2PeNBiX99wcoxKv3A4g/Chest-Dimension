package io.github.ykysnk.chestdimension.data

data class TaskData(val nextTick: Long, val task: () -> Unit)
