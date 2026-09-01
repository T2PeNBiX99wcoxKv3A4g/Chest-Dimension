package io.github.ykysnk.chestdimension.level.data

import java.util.*

interface RandomUUID {
    fun randomUUID(): UUID {
        while (true) {
            val uuid = UUID.randomUUID()
            if (!containsUUID(uuid)) return uuid
        }
    }

    fun containsUUID(uuid: UUID): Boolean
}