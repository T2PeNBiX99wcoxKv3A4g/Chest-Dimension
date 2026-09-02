@file:Suppress("unused")

package io.github.ykysnk.chestdimension.extensions

import net.minecraft.core.BlockPos
import net.minecraft.core.BlockPos.MutableBlockPos
import net.minecraft.core.Direction
import net.minecraft.core.Vec3i
import net.minecraft.server.level.PlayerRespawnLogic
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.Mth
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.RelativeMovement
import net.minecraft.world.entity.vehicle.DismountHelper
import net.minecraft.world.level.CollisionGetter
import net.minecraft.world.phys.Vec3

fun Entity.teleportToLevel(level: ServerLevel, resetRot: Boolean = false): Boolean =
    teleportToLevel(level, Vec3(0.5, 1.0, 0.5), resetRot)

fun Entity.teleportToLevel(level: ServerLevel, pos: Vec3, resetRot: Boolean = false): Boolean =
    teleportToLevel(level, pos, if (resetRot) 180f else yRot, if (resetRot) 0f else xRot)

fun Entity.teleportToLevel(level: ServerLevel, pos: Vec3i, resetRot: Boolean = false): Boolean =
    teleportToLevel(level, pos.toVec3(), resetRot)

fun Entity.teleportToLevel(level: ServerLevel, pos: Vec3, setDirection: Direction, resetRot: Boolean = false): Boolean {
    val offset = Mth.wrapDegrees(yRot - direction.toYRot())
    val targetYRot = setDirection.toYRot()
    val movement = Vec3(deltaMovement.x, 0.0, deltaMovement.z)
    val facing = Vec3(getViewVector(1.0f).x, 0.0, getViewVector(1.0f).z)
    val isMovingBackward = movement.horizontalDistanceSqr() > 0.0001 && movement.dot(facing) < 0
    val newYRot = if (resetRot) targetYRot else targetYRot + offset + if (isMovingBackward) 180f else 0f
    return teleportToLevel(level, pos, Mth.wrapDegrees(newYRot), if (resetRot) 0f else xRot)
}

fun Entity.teleportToLevel(
    level: ServerLevel,
    pos: Vec3i,
    setDirection: Direction,
    resetRot: Boolean = false
): Boolean = teleportToLevel(level, pos.toVec3(), setDirection, resetRot)

fun Entity.teleportToLevel(level: ServerLevel, pos: Vec3, yRot: Float, xRot: Float): Boolean =
    teleportToLevel(level, pos, setOf(), yRot, xRot)

fun Entity.teleportToLevel(level: ServerLevel, pos: Vec3i, yRot: Float, xRot: Float): Boolean =
    teleportToLevel(level, pos.toVec3(), setOf(), yRot, xRot)

fun Entity.teleportToLevel(
    level: ServerLevel,
    pos: Vec3,
    relativeMovements: Set<RelativeMovement>,
    yRot: Float,
    xRot: Float
): Boolean = teleportTo(level, pos.x, pos.y, pos.z, relativeMovements, yRot, xRot)

fun Entity.teleportToLevel(
    level: ServerLevel,
    pos: Vec3i,
    relativeMovements: Set<RelativeMovement>,
    yRot: Float,
    xRot: Float
): Boolean = teleportToLevel(level, pos.toVec3(), relativeMovements, yRot, xRot)

fun Entity.teleportToSafeLocation(
    level: ServerLevel,
    pos: Vec3i,
    inputSpawnRadius: Int = level.server.getSpawnRadius(level),
    resetRot: Boolean = false
): Boolean {
    var spawnRadius = inputSpawnRadius.coerceAtLeast(0)
    val borderDistance = Mth.floor(level.worldBorder.getDistanceToBorder(pos.x.toDouble(), pos.z.toDouble()))
    if (borderDistance < spawnRadius) spawnRadius = borderDistance
    if (borderDistance <= 1) spawnRadius = 1

    val searchSize = (spawnRadius * 2 + 1).toLong()
    val searchArea = searchSize * searchSize
    val searchCount = if (searchArea > 2147483647L) Int.MAX_VALUE else searchArea.toInt()
    val searchStep = getSearchStep(searchCount)
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
        candidatePos?.let {
            val safePos = findNonCollidingPosition(level, it)
            safePos?.let { safePos ->
                teleportToLevel(level, safePos, resetRot)
                return true
            }
        }
    }
    return false
}

fun Entity.teleportToSafeLocation(level: ServerLevel, pos: Vec3i, resetRot: Boolean = false): Boolean =
    teleportToSafeLocation(level, pos, level.server.getSpawnRadius(level), resetRot)

fun Entity.teleportToSpawnLocation(
    level: ServerLevel,
    spawnRadius: Int = level.server.getSpawnRadius(level),
    resetRot: Boolean = false
): Boolean =
    teleportToSafeLocation(level, level.sharedSpawnPos, spawnRadius, resetRot)

fun Entity.teleportToSpawnLocation(level: ServerLevel, resetRot: Boolean = false): Boolean =
    teleportToSafeLocation(level, level.sharedSpawnPos, level.server.getSpawnRadius(level), resetRot)

fun Entity.findNonCollidingAbovePosition(level: ServerLevel, pos: Vec3i, distance: Int = 1): Vec3? =
    findNonCollidingPosition(level, pos.above(distance))

fun Entity.findNonCollidingPosition(level: ServerLevel, pos: Vec3i): Vec3? {
    val targetCenter = Vec3.atBottomCenterOf(pos)
    val box = boundingBox
    val currentFeet = Vec3(x, box.minY, z)
    val offset = targetCenter.subtract(currentFeet)
    val targetBox = box.move(offset)
    return if (level.noCollision(this, targetBox)) targetCenter else null
}

fun Entity.findSafeLocation(level: CollisionGetter, pos: BlockPos, offsets: List<Vec3i>): Vec3? =
    findSafeLocation(type, level, pos, offsets)

fun findSafeLocation(entityType: EntityType<*>, level: CollisionGetter, pos: Vec3i, offsets: List<Vec3i>): Vec3? {
    val optional = findSafeLocation(entityType, level, pos, true, offsets)
    return optional ?: findSafeLocation(entityType, level, pos, false, offsets)
}

private fun findSafeLocation(
    entityType: EntityType<*>,
    level: CollisionGetter,
    pos: Vec3i,
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

private fun getSearchStep(searchArea: Int): Int {
    if (searchArea <= 2) return 1

    var step = minOf(17, searchArea - 1)

    while (gcd(searchArea, step) != 1) {
        step--
    }

    return step
}

private fun gcd(a: Int, b: Int): Int {
    var x = a
    var y = b

    while (y != 0) {
        val remainder = x % y
        x = y
        y = remainder
    }

    return x
}