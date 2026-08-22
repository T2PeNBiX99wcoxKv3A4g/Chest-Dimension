@file:Suppress("unused")

package io.github.ykysnk.chestdimension.extensions

import net.minecraft.core.BlockPos
import net.minecraft.core.BlockPos.MutableBlockPos
import net.minecraft.core.Vec3i
import net.minecraft.server.level.PlayerRespawnLogic
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.Mth
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.vehicle.DismountHelper
import net.minecraft.world.level.CollisionGetter
import net.minecraft.world.level.GameType
import net.minecraft.world.phys.Vec3
import kotlin.math.max

fun Entity.teleportToLevel(level: ServerLevel) = teleportTo(level, 0.5, 100.0, 0.5, setOf(), yRot, xRot)
fun Entity.teleportToLevel(level: ServerLevel, pos: Vec3) =
    teleportTo(level, pos.x, pos.y, pos.z, setOf(), yRot, xRot)

fun Entity.teleportToLevel(level: ServerLevel, pos: Vec3i) =
    teleportTo(level, pos.x.toDouble() + 0.5, pos.y.toDouble(), pos.z.toDouble() + 0.5, setOf(), yRot, xRot)

fun Entity.teleportToSafeLocation(level: ServerLevel, pos: Vec3i): Boolean {
    if (level.dimensionType().hasSkyLight() && level.server.getWorldData().gameType != GameType.ADVENTURE) {
        var spawnRadius = max(0, level.server.getSpawnRadius(level))
        val borderDistance =
            Mth.floor(level.worldBorder.getDistanceToBorder(pos.x.toDouble(), pos.z.toDouble()))
        if (borderDistance < spawnRadius) {
            spawnRadius = borderDistance
        }

        if (borderDistance <= 1) {
            spawnRadius = 1
        }

        val searchSize = (spawnRadius * 2 + 1).toLong()
        val searchArea = searchSize * searchSize
        val searchCount = if (searchArea > 2147483647L) Int.MAX_VALUE else searchArea.toInt()
        val searchStep = getCoprime(searchCount)
        val searchStart = RandomSource.create().nextInt(searchCount)

        for (searchIndex in 0..<searchCount) {
            val searchOffset = (searchStart + searchStep * searchIndex) % searchCount
            val candidateX = searchOffset % (spawnRadius * 2 + 1)
            val candidateZ = searchOffset / (spawnRadius * 2 + 1)
            val candidatePos = PlayerRespawnLogic.getOverworldRespawnPos(
                level,
                pos.x + candidateX - spawnRadius,
                pos.z + candidateZ - spawnRadius
            )
            if (candidatePos != null) {
                teleportToLevel(level, candidatePos)
                if (level.noCollision(this)) {
                    return true
                }
            }
        }
    } else {
        teleportToLevel(level, pos)

        while (!level.noCollision(this) && y < (level.maxBuildHeight - 1).toDouble()) {
            setPos(x, y + 1.0, z)
        }

        return true
    }
    return false
}

fun Entity.teleportToSpawnLocation(level: ServerLevel): Boolean =
    teleportToSafeLocation(level, level.sharedSpawnPos)

fun Entity.findStandUpPosition(level: CollisionGetter, pos: BlockPos, offsets: List<Vec3i>): Vec3? =
    findStandUpPosition(type, level, pos, offsets)

fun findStandUpPosition(entityType: EntityType<*>, level: CollisionGetter, pos: BlockPos, offsets: List<Vec3i>): Vec3? {
    val optional = findStandUpPosition(entityType, level, pos, true, offsets)
    return optional ?: findStandUpPosition(entityType, level, pos, false, offsets)
}

private fun findStandUpPosition(
    entityType: EntityType<*>,
    level: CollisionGetter,
    pos: BlockPos,
    simulate: Boolean,
    offsets: List<Vec3i>
): Vec3? {
    val mutableBlockPos = MutableBlockPos()

    for (vec3i in offsets) {
        mutableBlockPos.set(pos).move(vec3i)
        val vec3 = DismountHelper.findSafeDismountLocation(entityType, level, mutableBlockPos, simulate)
        if (vec3 != null) {
            return vec3
        }
    }

    return null
}

private fun getCoprime(spawnArea: Int): Int = if (spawnArea <= 16) spawnArea - 1 else 17