package io.github.ykysnk.chestdimension.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import io.github.ykysnk.chestdimension.Constants
import io.github.ykysnk.chestdimension.block.entity.BlockEntityTypes
import io.github.ykysnk.chestdimension.extensions.*
import io.github.ykysnk.chestdimension.item.Items
import io.github.ykysnk.chestdimension.level.ChestLevelManager
import io.github.ykysnk.chestdimension.level.UUIDManager
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.*
import net.minecraft.commands.arguments.coordinates.BlockPosArgument
import net.minecraft.commands.arguments.coordinates.Vec3Argument
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.valueproviders.IntProvider
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.Vec3
import java.util.*

object ChestDimCommand {
    private val ERROR_INVULNERABLE = SimpleCommandExceptionType(Component.translatable("commands.damage.invulnerable"))

    @Suppress("SpellCheckingInspection")
    fun register(
        dispatcher: CommandDispatcher<CommandSourceStack>,
        registryAccess: CommandBuildContext,
        environment: Commands.CommandSelection
    ) {
        Commands.literal("chestdim").apply {
            literal("create") {
                requires { it.hasPermission(2) }

                executes { context ->
                    val player = context.source.playerOrException
                    create(player, context.source)
                }

                argument("targets", EntityArgument.entities()) {
                    executes {
                        val entities = EntityArgument.getEntities(it, "targets")
                        create(entities, it.source)
                    }
                }
            }

            literal("tp") {
                requires { it.hasPermission(2) }

                literal("direct") {
                    uuidArg {
                        argument("pos", Vec3Argument.vec3()) {
                            executes {
                                val player = it.source.playerOrException
                                val pos = Vec3Argument.getVec3(it, "pos")
                                val uuid = UuidArgument.getUuid(it, "uuid")
                                teleport(player, pos, uuid, it.source)
                            }
                        }

                        argument("targets", EntityArgument.entities()) {
                            argument("pos", Vec3Argument.vec3()) {
                                executes {
                                    val entities = EntityArgument.getEntities(it, "targets")
                                    val pos = Vec3Argument.getVec3(it, "pos")
                                    val uuid = UuidArgument.getUuid(it, "uuid")
                                    teleport(entities, pos, uuid, it.source)
                                }
                            }
                        }
                    }

                    argument("dimension", DimensionArgument.dimension()) {
                        argument("pos", Vec3Argument.vec3()) {
                            executes {
                                val player = it.source.playerOrException
                                val pos = Vec3Argument.getVec3(it, "pos")
                                val level = DimensionArgument.getDimension(it, "dimension")
                                teleport(player, pos, level, it.source)
                            }
                        }

                        argument("targets", EntityArgument.entities()) {
                            argument("pos", Vec3Argument.vec3()) {
                                executes {
                                    val entities = EntityArgument.getEntities(it, "targets")
                                    val pos = Vec3Argument.getVec3(it, "pos")
                                    val level = DimensionArgument.getDimension(it, "dimension")
                                    teleport(entities, pos, level, it.source)
                                }
                            }
                        }
                    }

                    argument("pos", Vec3Argument.vec3()) {
                        executes {
                            val player = it.source.playerOrException
                            val pos = Vec3Argument.getVec3(it, "pos")
                            teleport(player, pos, it.source.level, it.source)
                        }
                    }

                    argument("targets", EntityArgument.entities()) {
                        argument("pos", Vec3Argument.vec3()) {
                            executes {
                                val entities = EntityArgument.getEntities(it, "targets")
                                val pos = Vec3Argument.getVec3(it, "pos")
                                teleport(entities, pos, it.source.level, it.source)
                            }
                        }
                    }
                }

                literal("safe") {
                    requires { it.hasPermission(2) }

                    argument("pos", BlockPosArgument.blockPos()) {
                        executes {
                            val player = it.source.playerOrException
                            val pos = BlockPosArgument.getBlockPos(it, "pos")
                            val spawnRadius = it.source.server.getSpawnRadius(it.source.level)
                            teleportSafe(player, pos, it.source.level, spawnRadius, it.source)
                        }

                        argument("spawn-radius", IntegerArgumentType.integer(1)) {
                            executes {
                                val player = it.source.playerOrException
                                val pos = BlockPosArgument.getBlockPos(it, "pos")
                                val spawnRadius = IntegerArgumentType.getInteger(it, "spawn-radius")
                                teleportSafe(player, pos, it.source.level, spawnRadius, it.source)
                            }
                        }

                        argument("targets", EntityArgument.entities()) {
                            argument("pos", BlockPosArgument.blockPos()) {
                                executes {
                                    val entities = EntityArgument.getEntities(it, "targets")
                                    val pos = BlockPosArgument.getBlockPos(it, "pos")
                                    val spawnRadius = it.source.server.getSpawnRadius(it.source.level)
                                    teleportSafe(entities, pos, it.source.level, spawnRadius, it.source)
                                }

                                argument("spawn-radius", IntegerArgumentType.integer(1)) {
                                    executes {
                                        val entities = EntityArgument.getEntities(it, "targets")
                                        val pos = BlockPosArgument.getBlockPos(it, "pos")
                                        val spawnRadius = IntegerArgumentType.getInteger(it, "spawn-radius")
                                        teleportSafe(entities, pos, it.source.level, spawnRadius, it.source)
                                    }
                                }
                            }
                        }
                    }

                    uuidArg {
                        argument("pos", BlockPosArgument.blockPos()) {
                            executes {
                                val player = it.source.playerOrException
                                val pos = BlockPosArgument.getBlockPos(it, "pos")
                                val uuid = UuidArgument.getUuid(it, "uuid")
                                val spawnRadius = it.source.server.getSpawnRadius(it.source.level)
                                teleportSafe(player, pos, uuid, spawnRadius, it.source)
                            }

                            argument("spawn-radius", IntegerArgumentType.integer(1)) {
                                executes {
                                    val player = it.source.playerOrException
                                    val pos = BlockPosArgument.getBlockPos(it, "pos")
                                    val uuid = UuidArgument.getUuid(it, "uuid")
                                    val spawnRadius = IntegerArgumentType.getInteger(it, "spawn-radius")
                                    teleportSafe(player, pos, uuid, spawnRadius, it.source)
                                }
                            }
                        }

                        argument("targets", EntityArgument.entities()) {
                            argument("pos", BlockPosArgument.blockPos()) {
                                executes {
                                    val entities = EntityArgument.getEntities(it, "targets")
                                    val pos = BlockPosArgument.getBlockPos(it, "pos")
                                    val uuid = UuidArgument.getUuid(it, "uuid")
                                    val spawnRadius = it.source.server.getSpawnRadius(it.source.level)
                                    teleportSafe(entities, pos, uuid, spawnRadius, it.source)
                                }

                                argument("spawn-radius", IntegerArgumentType.integer(1)) {
                                    executes {
                                        val entities = EntityArgument.getEntities(it, "targets")
                                        val pos = BlockPosArgument.getBlockPos(it, "pos")
                                        val uuid = UuidArgument.getUuid(it, "uuid")
                                        val spawnRadius = IntegerArgumentType.getInteger(it, "spawn-radius")
                                        teleportSafe(entities, pos, uuid, spawnRadius, it.source)
                                    }
                                }
                            }
                        }
                    }

                    argument("dimension", DimensionArgument.dimension()) {
                        argument("pos", BlockPosArgument.blockPos()) {
                            executes {
                                val player = it.source.playerOrException
                                val pos = BlockPosArgument.getBlockPos(it, "pos")
                                val level = DimensionArgument.getDimension(it, "dimension")
                                val spawnRadius = it.source.server.getSpawnRadius(level)
                                teleportSafe(player, pos, level, spawnRadius, it.source)
                            }

                            argument("spawn-radius", IntegerArgumentType.integer(1)) {
                                executes {
                                    val player = it.source.playerOrException
                                    val pos = BlockPosArgument.getBlockPos(it, "pos")
                                    val spawnRadius = IntegerArgumentType.getInteger(it, "spawn-radius")
                                    val level = DimensionArgument.getDimension(it, "dimension")
                                    teleportSafe(player, pos, level, spawnRadius, it.source)
                                }
                            }
                        }

                        argument("targets", EntityArgument.entities()) {
                            argument("pos", BlockPosArgument.blockPos()) {
                                executes {
                                    val entities = EntityArgument.getEntities(it, "targets")
                                    val pos = BlockPosArgument.getBlockPos(it, "pos")
                                    val level = DimensionArgument.getDimension(it, "dimension")
                                    val spawnRadius = it.source.server.getSpawnRadius(level)
                                    teleportSafe(entities, pos, level, spawnRadius, it.source)
                                }

                                argument("spawn-radius", IntegerArgumentType.integer(1)) {
                                    executes {
                                        val entities = EntityArgument.getEntities(it, "targets")
                                        val pos = BlockPosArgument.getBlockPos(it, "pos")
                                        val spawnRadius = IntegerArgumentType.getInteger(it, "spawn-radius")
                                        val level = DimensionArgument.getDimension(it, "dimension")
                                        teleportSafe(entities, pos, level, spawnRadius, it.source)
                                    }
                                }
                            }
                        }
                    }
                }

                literal("spawn") {
                    requires { it.hasPermission(2) }

                    executes {
                        val player = it.source.playerOrException
                        val spawnRadius = it.source.server.getSpawnRadius(it.source.level)
                        teleportSpawn(player, it.source.level, spawnRadius, it.source)
                    }

                    argument("spawn-radius", IntegerArgumentType.integer(1)) {
                        executes {
                            val player = it.source.playerOrException
                            val spawnRadius = IntegerArgumentType.getInteger(it, "spawn-radius")
                            teleportSpawn(player, it.source.level, spawnRadius, it.source)
                        }
                    }

                    argument("targets", EntityArgument.entities()) {
                        executes {
                            val entities = EntityArgument.getEntities(it, "targets")
                            val spawnRadius = it.source.server.getSpawnRadius(it.source.level)
                            teleportSpawn(entities, it.source.level, spawnRadius, it.source)
                        }

                        argument("spawn-radius", IntegerArgumentType.integer(1)) {
                            executes {
                                val entities = EntityArgument.getEntities(it, "targets")
                                val spawnRadius = IntegerArgumentType.getInteger(it, "spawn-radius")
                                teleportSpawn(entities, it.source.level, spawnRadius, it.source)
                            }
                        }
                    }

                    uuidArg {
                        executes {
                            val player = it.source.playerOrException
                            val uuid = UuidArgument.getUuid(it, "uuid")
                            val spawnRadius = it.source.server.getSpawnRadius(it.source.level)
                            teleportSpawn(player, uuid, spawnRadius, it.source)
                        }

                        argument("spawn-radius", IntegerArgumentType.integer(1)) {
                            executes {
                                val player = it.source.playerOrException
                                val uuid = UuidArgument.getUuid(it, "uuid")
                                val spawnRadius = IntegerArgumentType.getInteger(it, "spawn-radius")
                                teleportSpawn(player, uuid, spawnRadius, it.source)
                            }
                        }

                        argument("targets", EntityArgument.entities()) {
                            executes {
                                val entities = EntityArgument.getEntities(it, "targets")
                                val uuid = UuidArgument.getUuid(it, "uuid")
                                val spawnRadius = it.source.server.getSpawnRadius(it.source.level)
                                teleportSpawn(entities, uuid, spawnRadius, it.source)
                            }

                            argument("spawn-radius", IntegerArgumentType.integer(1)) {
                                executes {
                                    val entities = EntityArgument.getEntities(it, "targets")
                                    val uuid = UuidArgument.getUuid(it, "uuid")
                                    val spawnRadius = IntegerArgumentType.getInteger(it, "spawn-radius")
                                    teleportSpawn(entities, uuid, spawnRadius, it.source)
                                }
                            }
                        }
                    }

                    argument("dimension", DimensionArgument.dimension()) {
                        executes {
                            val player = it.source.playerOrException
                            val level = DimensionArgument.getDimension(it, "dimension")
                            val spawnRadius = it.source.server.getSpawnRadius(level)
                            teleportSpawn(player, level, spawnRadius, it.source)
                        }

                        argument("spawn-radius", IntegerArgumentType.integer(1)) {
                            executes {
                                val player = it.source.playerOrException
                                val spawnRadius = IntegerArgumentType.getInteger(it, "spawn-radius")
                                val level = DimensionArgument.getDimension(it, "dimension")
                                teleportSpawn(player, level, spawnRadius, it.source)
                            }
                        }

                        argument("targets", EntityArgument.entities()) {
                            executes {
                                val entities = EntityArgument.getEntities(it, "targets")
                                val level = DimensionArgument.getDimension(it, "dimension")
                                val spawnRadius = it.source.server.getSpawnRadius(level)
                                teleportSpawn(entities, level, spawnRadius, it.source)
                            }

                            argument("spawn-radius", IntegerArgumentType.integer(1)) {
                                executes {
                                    val entities = EntityArgument.getEntities(it, "targets")
                                    val spawnRadius = IntegerArgumentType.getInteger(it, "spawn-radius")
                                    val level = DimensionArgument.getDimension(it, "dimension")
                                    teleportSpawn(entities, level, spawnRadius, it.source)
                                }
                            }
                        }
                    }
                }

                literal("enter") {
                    requires { it.hasPermission(2) }

                    uuidArg {
                        executes {
                            val player = it.source.playerOrException
                            val uuid = UuidArgument.getUuid(it, "uuid")
                            teleportEnter(player, uuid, it.source)
                        }

                        argument("targets", EntityArgument.entities()) {
                            executes {
                                val entities = EntityArgument.getEntities(it, "targets")
                                val uuid = UuidArgument.getUuid(it, "uuid")
                                teleportEnter(entities, uuid, it.source)
                            }
                        }
                    }
                }
            }

            literal("give-chest") {
                requires { it.hasPermission(2) }

                executes { context ->
                    val player = context.source.playerOrException
                    giveChest(player, context.source)
                }

                uuidArg {
                    executes {
                        val player = it.source.playerOrException
                        val uuid = UuidArgument.getUuid(it, "uuid")
                        giveChest(player, uuid)
                    }

                    argument("player", EntityArgument.player()) {
                        executes {
                            val player = EntityArgument.getPlayer(it, "player")
                                ?: throw CommandSourceStack.ERROR_NOT_PLAYER.create()
                            val uuid = UuidArgument.getUuid(it, "uuid")
                            giveChest(player, uuid)
                        }
                    }
                }

                argument("player", EntityArgument.player()) {
                    executes {
                        val player =
                            EntityArgument.getPlayer(it, "player") ?: throw CommandSourceStack.ERROR_NOT_PLAYER.create()
                        giveChest(player, it.source)
                    }
                }
            }

            literal("time") {
                requires { it.hasPermission(2) }

                literal("set") {
                    literal("day") {
                        executes {
                            setTime(it.source.level, it.source, 1000)
                        }
                    }

                    literal("noon") {
                        executes {
                            setTime(it.source.level, it.source, 6000)
                        }
                    }

                    literal("night") {
                        executes {
                            setTime(it.source.level, it.source, 13000)
                        }
                    }

                    literal("midnight") {
                        executes {
                            setTime(it.source.level, it.source, 18000)
                        }
                    }

                    argument("time", TimeArgument.time()) {
                        executes {
                            val time = IntegerArgumentType.getInteger(it, "time")
                            setTime(it.source.level, it.source, time)
                        }
                    }
                }

                literal("add") {
                    argument("time", TimeArgument.time()) {
                        executes {
                            val time = IntegerArgumentType.getInteger(it, "time")
                            addTime(it.source.level, it.source, time)
                        }
                    }
                }

                literal("query") {
                    literal("daytime") {
                        executes {
                            queryTime(it.source, getDayTime(it.source.level))
                        }
                    }

                    literal("gametime") {
                        executes {
                            queryTime(it.source, (it.source.level.gameTime % 2147483647L).toInt())
                        }
                    }

                    literal("day") {
                        executes {
                            queryTime(it.source, (it.source.level.dayTime / 24000L % 2147483647L).toInt())
                        }
                    }
                }

                uuidArg {
                    literal("set") {
                        literal("day") {
                            executes {
                                val uuid = UuidArgument.getUuid(it, "uuid")
                                setTime(uuid, it.source, 1000)
                            }
                        }

                        literal("noon") {
                            executes {
                                val uuid = UuidArgument.getUuid(it, "uuid")
                                setTime(uuid, it.source, 6000)
                            }
                        }

                        literal("night") {
                            executes {
                                val uuid = UuidArgument.getUuid(it, "uuid")
                                setTime(uuid, it.source, 13000)
                            }
                        }

                        literal("midnight") {
                            executes {
                                val uuid = UuidArgument.getUuid(it, "uuid")
                                setTime(uuid, it.source, 18000)
                            }
                        }

                        argument("time", TimeArgument.time()) {
                            executes {
                                val uuid = UuidArgument.getUuid(it, "uuid")
                                val time = IntegerArgumentType.getInteger(it, "time")
                                setTime(uuid, it.source, time)
                            }
                        }
                    }

                    literal("add") {
                        argument("time", TimeArgument.time()) {
                            executes {
                                val uuid = UuidArgument.getUuid(it, "uuid")
                                val time = IntegerArgumentType.getInteger(it, "time")
                                addTime(uuid, it.source, time)
                            }
                        }
                    }

                    literal("query") {
                        literal("daytime") {
                            executes {
                                val uuid = UuidArgument.getUuid(it, "uuid")
                                val level = getLevel(it.source, uuid)
                                queryTime(it.source, getDayTime(level))
                            }
                        }

                        literal("gametime") {
                            executes {
                                val uuid = UuidArgument.getUuid(it, "uuid")
                                val level = getLevel(it.source, uuid)
                                queryTime(it.source, (level.gameTime % 2147483647L).toInt())
                            }
                        }

                        literal("day") {
                            executes {
                                val uuid = UuidArgument.getUuid(it, "uuid")
                                val level = getLevel(it.source, uuid)
                                queryTime(it.source, (level.dayTime / 24000L % 2147483647L).toInt())
                            }
                        }
                    }
                }

                argument("dimension", DimensionArgument.dimension()) {
                    literal("set") {
                        literal("day") {
                            executes {
                                val level = DimensionArgument.getDimension(it, "dimension")
                                setTime(level, it.source, 1000)
                            }
                        }

                        literal("noon") {
                            executes {
                                val level = DimensionArgument.getDimension(it, "dimension")
                                setTime(level, it.source, 6000)
                            }
                        }

                        literal("night") {
                            executes {
                                val level = DimensionArgument.getDimension(it, "dimension")
                                setTime(level, it.source, 13000)
                            }
                        }

                        literal("midnight") {
                            executes {
                                val level = DimensionArgument.getDimension(it, "dimension")
                                setTime(level, it.source, 18000)
                            }
                        }

                        argument("time", TimeArgument.time()) {
                            executes {
                                val level = DimensionArgument.getDimension(it, "dimension")
                                val time = IntegerArgumentType.getInteger(it, "time")
                                setTime(level, it.source, time)
                            }
                        }
                    }

                    literal("add") {
                        argument("time", TimeArgument.time()) {
                            executes {
                                val level = DimensionArgument.getDimension(it, "dimension")
                                val time = IntegerArgumentType.getInteger(it, "time")
                                addTime(level, it.source, time)
                            }
                        }
                    }

                    literal("query") {
                        literal("daytime") {
                            executes {
                                val level = DimensionArgument.getDimension(it, "dimension")
                                queryTime(it.source, getDayTime(level))
                            }
                        }

                        literal("gametime") {
                            executes {
                                val level = DimensionArgument.getDimension(it, "dimension")
                                queryTime(it.source, (level.gameTime % 2147483647L).toInt())
                            }
                        }

                        literal("day") {
                            executes {
                                val level = DimensionArgument.getDimension(it, "dimension")
                                queryTime(it.source, (level.dayTime / 24000L % 2147483647L).toInt())
                            }
                        }
                    }
                }

                literal("all") {
                    literal("set") {
                        literal("day") {
                            executes {
                                setTime(it.source, 1000)
                            }
                        }

                        literal("noon") {
                            executes {
                                setTime(it.source, 6000)
                            }
                        }

                        literal("night") {
                            executes {
                                setTime(it.source, 13000)
                            }
                        }

                        literal("midnight") {
                            executes {
                                setTime(it.source, 18000)
                            }
                        }

                        argument("time", TimeArgument.time()) {
                            executes {
                                val time = IntegerArgumentType.getInteger(it, "time")
                                setTime(it.source, time)
                            }
                        }
                    }

                    literal("add") {
                        argument("time", TimeArgument.time()) {
                            executes {
                                val time = IntegerArgumentType.getInteger(it, "time")
                                addTime(it.source, time)
                            }
                        }
                    }
                }
            }

            literal("weather") {
                requires { it.hasPermission(2) }

                literal("clear") {
                    executes {
                        setClear(it.source.level, it.source, -1)
                    }

                    argument("duration", TimeArgument.time(1)) {
                        executes {
                            setClear(it.source.level, it.source, IntegerArgumentType.getInteger(it, "duration"))
                        }
                    }
                }

                literal("rain") {
                    executes {
                        setRain(it.source.level, it.source, -1)
                    }

                    argument("duration", TimeArgument.time(1)) {
                        executes {
                            setRain(it.source.level, it.source, IntegerArgumentType.getInteger(it, "duration"))
                        }
                    }
                }

                literal("thunder") {
                    executes {
                        setThunder(it.source.level, it.source, -1)
                    }

                    argument("duration", TimeArgument.time(1)) {
                        executes {
                            setThunder(it.source.level, it.source, IntegerArgumentType.getInteger(it, "duration"))
                        }
                    }
                }

                uuidArg {
                    literal("clear") {
                        executes {
                            val uuid = UuidArgument.getUuid(it, "uuid")
                            setClear(uuid, it.source, -1)
                        }

                        argument("duration", TimeArgument.time(1)) {
                            executes {
                                val uuid = UuidArgument.getUuid(it, "uuid")
                                setClear(uuid, it.source, IntegerArgumentType.getInteger(it, "duration"))
                            }
                        }
                    }

                    literal("rain") {
                        executes {
                            val uuid = UuidArgument.getUuid(it, "uuid")
                            setRain(uuid, it.source, -1)
                        }

                        argument("duration", TimeArgument.time(1)) {
                            executes {
                                val uuid = UuidArgument.getUuid(it, "uuid")
                                setRain(uuid, it.source, IntegerArgumentType.getInteger(it, "duration"))
                            }
                        }
                    }

                    literal("thunder") {
                        executes {
                            val uuid = UuidArgument.getUuid(it, "uuid")
                            setThunder(uuid, it.source, -1)
                        }

                        argument("duration", TimeArgument.time(1)) {
                            executes {
                                val uuid = UuidArgument.getUuid(it, "uuid")
                                setThunder(uuid, it.source, IntegerArgumentType.getInteger(it, "duration"))
                            }
                        }
                    }
                }

                argument("dimension", DimensionArgument.dimension()) {
                    literal("clear") {
                        executes {
                            val level = DimensionArgument.getDimension(it, "dimension")
                            setClear(level, it.source, -1)
                        }

                        argument("duration", TimeArgument.time(1)) {
                            executes {
                                val level = DimensionArgument.getDimension(it, "dimension")
                                setClear(level, it.source, IntegerArgumentType.getInteger(it, "duration"))
                            }
                        }
                    }

                    literal("rain") {
                        executes {
                            val level = DimensionArgument.getDimension(it, "dimension")
                            setRain(level, it.source, -1)
                        }

                        argument("duration", TimeArgument.time(1)) {
                            executes {
                                val level = DimensionArgument.getDimension(it, "dimension")
                                setRain(level, it.source, IntegerArgumentType.getInteger(it, "duration"))
                            }
                        }
                    }

                    literal("thunder") {
                        executes {
                            val level = DimensionArgument.getDimension(it, "dimension")
                            setThunder(level, it.source, -1)
                        }

                        argument("duration", TimeArgument.time(1)) {
                            executes {
                                val level = DimensionArgument.getDimension(it, "dimension")
                                setThunder(level, it.source, IntegerArgumentType.getInteger(it, "duration"))
                            }
                        }
                    }
                }

                literal("all") {
                    literal("clear") {
                        executes {
                            setClear(it.source, -1)
                        }

                        argument("duration", TimeArgument.time(1)) {
                            executes {
                                setClear(it.source, IntegerArgumentType.getInteger(it, "duration"))
                            }
                        }
                    }

                    literal("rain") {
                        executes {
                            setRain(it.source, -1)
                        }

                        argument("duration", TimeArgument.time(1)) {
                            executes {
                                setRain(it.source, IntegerArgumentType.getInteger(it, "duration"))
                            }
                        }
                    }

                    literal("thunder") {
                        executes {
                            setThunder(it.source, -1)
                        }

                        argument("duration", TimeArgument.time(1)) {
                            executes {
                                setThunder(it.source, IntegerArgumentType.getInteger(it, "duration"))
                            }
                        }
                    }
                }
            }

            literal("damage") {
                requires { it.hasPermission(2) }

                argument("targets", EntityArgument.entities()) {
                    argument("amount", FloatArgumentType.floatArg(0.0f)) {
                        executes {
                            val entities = EntityArgument.getEntities(it, "targets")
                            val amount = FloatArgumentType.getFloat(it, "amount")
                            damage(it.source, entities, amount, it.source.level.damageSources().generic())
                        }

                        argument("damageType", ResourceArgument.resource(registryAccess, Registries.DAMAGE_TYPE)) {
                            executes {
                                val entities = EntityArgument.getEntities(it, "targets")
                                val amount = FloatArgumentType.getFloat(it, "amount")
                                val damageType = ResourceArgument.getResource(it, "damageType", Registries.DAMAGE_TYPE)
                                damage(
                                    it.source,
                                    entities,
                                    amount,
                                    DamageSource(damageType)
                                )
                            }

                            literal("at") {
                                argument("location", Vec3Argument.vec3()) {
                                    executes {
                                        val entities = EntityArgument.getEntities(it, "targets")
                                        val amount = FloatArgumentType.getFloat(it, "amount")
                                        val damageType =
                                            ResourceArgument.getResource(it, "damageType", Registries.DAMAGE_TYPE)
                                        val location = Vec3Argument.getVec3(it, "location")
                                        damage(
                                            it.source,
                                            entities,
                                            amount,
                                            DamageSource(damageType, location)
                                        )
                                    }
                                }
                            }

                            literal("by") {
                                argument("entity", EntityArgument.entity()) {
                                    executes {
                                        val entities = EntityArgument.getEntities(it, "targets")
                                        val amount = FloatArgumentType.getFloat(it, "amount")
                                        val damageType =
                                            ResourceArgument.getResource(it, "damageType", Registries.DAMAGE_TYPE)
                                        val entity = EntityArgument.getEntity(it, "entity")
                                        damage(
                                            it.source,
                                            entities,
                                            amount,
                                            DamageSource(damageType, entity)
                                        )
                                    }

                                    literal("from") {
                                        argument("cause", EntityArgument.entity()) {
                                            executes {
                                                val entities = EntityArgument.getEntities(it, "targets")
                                                val amount = FloatArgumentType.getFloat(it, "amount")
                                                val damageType =
                                                    ResourceArgument.getResource(
                                                        it,
                                                        "damageType",
                                                        Registries.DAMAGE_TYPE
                                                    )
                                                val entity = EntityArgument.getEntity(it, "entity")
                                                val cause = EntityArgument.getEntity(it, "cause")
                                                damage(
                                                    it.source,
                                                    entities,
                                                    amount,
                                                    DamageSource(damageType, entity, cause)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            literal("ping") {
                executes {
                    val player = it.source.playerOrException
                    it.source.sendSuccess({ Component.literal("Ping: ${player.latency}ms") }, true)
                    1
                }

                argument("player", EntityArgument.player()) {
                    executes {
                        val player = EntityArgument.getPlayer(it, "player")
                            ?: throw CommandSourceStack.ERROR_NOT_PLAYER.create()
                        it.source.sendSuccess({ Component.literal("Ping: ${player.latency}ms") }, true)
                        1
                    }
                }
            }

            dispatcher.register(this)
        }
    }

    private fun create(entity: Entity, source: CommandSourceStack): Int = create(setOf(entity), source)

    private fun create(entities: Collection<Entity>, source: CommandSourceStack): Int {
        return runCatching {
            val uuid = UUIDManager.randomUUID()
            val uuidComponent: MutableComponent = Component.literal(uuid.toString()).withStyle {
                it.withUnderlined(true)
                    .withClickEvent(ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, uuid.toString()))
                    .withHoverEvent(
                        HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            Component.literal("Click to copy UUID")
                        )
                    )
            }
            val world = ChestLevelManager.getOrCreate(source.server, uuid)
            entities.forEach { entity ->
                ChestLevelManager.teleportEntityToEnter(world, entity)
            }
            source.sendSuccess({ Component.literal("Created world: ").append(uuidComponent) }, true)
            1
        }.getOrElse {
            Constants.LOGGER.error(it.localizedMessage, it)
            source.sendFailure(Component.literal("Command error (${it.localizedMessage})"))
            0
        }
    }

    private fun formatDouble(value: Double): String {
        return String.format(Locale.ROOT, "%f", value)
    }

    private fun getLevel(source: CommandSourceStack, dimension: String): ServerLevel? {
        val id = ResourceLocation.tryParse(dimension.replace("\"", "")) ?: return null
        return source.server.getLevel(ResourceKey.create(Registries.DIMENSION, id))
    }

    private fun getLevel(source: CommandSourceStack, dimension: UUID): ServerLevel =
        ChestLevelManager.getOrCreate(source.server, dimension)

    private fun teleport(entity: Entity, pos: Vec3, uuid: UUID, source: CommandSourceStack): Int =
        teleport(setOf(entity), pos, uuid, source)

    private fun teleport(entities: Collection<Entity>, pos: Vec3, uuid: UUID, source: CommandSourceStack): Int {
        val level = getLevel(source, uuid)
        return teleport(entities, pos, level, source)
    }

    private fun teleport(entity: Entity, pos: Vec3, level: ServerLevel, source: CommandSourceStack): Int =
        teleport(setOf(entity), pos, level, source)

    private fun teleport(entities: Collection<Entity>, pos: Vec3, level: ServerLevel, source: CommandSourceStack): Int {
        entities.forEach {
            it.teleportToLevel(level, pos)
        }
        if (entities.size == 1)
            source.sendSuccess({
                Component.translatable(
                    "commands.teleport.success.location.single",
                    entities.first().displayName,
                    formatDouble(pos.x),
                    formatDouble(pos.y),
                    formatDouble(pos.z)
                )
            }, true)
        else
            source.sendSuccess({
                Component.translatable(
                    "commands.teleport.success.location.multiple",
                    entities.size,
                    formatDouble(pos.x),
                    formatDouble(pos.y),
                    formatDouble(pos.z)
                )
            }, true)
        return 1
    }

    private fun teleportSafe(
        entity: Entity,
        pos: BlockPos,
        uuid: UUID,
        spawnRadius: Int,
        source: CommandSourceStack
    ): Int = teleportSafe(setOf(entity), pos, uuid, spawnRadius, source)

    private fun teleportSafe(
        entities: Collection<Entity>,
        pos: BlockPos,
        uuid: UUID,
        spawnRadius: Int,
        source: CommandSourceStack
    ): Int {
        val level = getLevel(source, uuid)
        return teleportSafe(entities, pos, level, spawnRadius, source)
    }

    private fun teleportSafe(
        entity: Entity,
        pos: BlockPos,
        level: ServerLevel,
        spawnRadius: Int,
        source: CommandSourceStack
    ): Int = teleportSafe(setOf(entity), pos, level, spawnRadius, source)

    private fun teleportSafe(
        entities: Collection<Entity>,
        pos: BlockPos,
        level: ServerLevel,
        spawnRadius: Int,
        source: CommandSourceStack
    ): Int {
        val teleported = hashSetOf<Entity>()
        entities.forEach {
            if (it.teleportToSafeLocation(level, pos, spawnRadius))
                teleported.add(it)
        }
        val vec3 = pos.toVec3()
        if (teleported.size == 1)
            source.sendSuccess({
                Component.translatable(
                    "commands.teleport.success.location.single",
                    teleported.first().displayName,
                    formatDouble(vec3.x),
                    formatDouble(vec3.y),
                    formatDouble(vec3.z)
                )
            }, true)
        else
            source.sendSuccess({
                Component.translatable(
                    "commands.teleport.success.location.multiple",
                    entities.size,
                    formatDouble(vec3.x),
                    formatDouble(vec3.y),
                    formatDouble(vec3.z)
                )
            }, true)
        return 1
    }

    private fun teleportSpawn(
        entity: Entity,
        uuid: UUID,
        spawnRadius: Int,
        source: CommandSourceStack
    ): Int = teleportSpawn(setOf(entity), uuid, spawnRadius, source)

    private fun teleportSpawn(
        entities: Collection<Entity>,
        uuid: UUID,
        spawnRadius: Int,
        source: CommandSourceStack
    ): Int {
        val level = getLevel(source, uuid)
        return teleportSpawn(entities, level, spawnRadius, source)
    }

    private fun teleportSpawn(
        entity: Entity,
        level: ServerLevel,
        spawnRadius: Int,
        source: CommandSourceStack
    ): Int = teleportSpawn(setOf(entity), level, spawnRadius, source)

    private fun teleportSpawn(
        entities: Collection<Entity>,
        level: ServerLevel,
        spawnRadius: Int,
        source: CommandSourceStack
    ): Int {
        val teleported = hashSetOf<Entity>()
        entities.forEach {
            if (it.teleportToSpawnLocation(level, spawnRadius))
                teleported.add(it)
        }
        val vec3 = level.sharedSpawnPos.toVec3()
        if (teleported.size == 1)
            source.sendSuccess({
                Component.translatable(
                    "commands.teleport.success.location.single",
                    teleported.first().displayName,
                    formatDouble(vec3.x),
                    formatDouble(vec3.y),
                    formatDouble(vec3.z)
                )
            }, true)
        else
            source.sendSuccess({
                Component.translatable(
                    "commands.teleport.success.location.multiple",
                    entities.size,
                    formatDouble(vec3.x),
                    formatDouble(vec3.y),
                    formatDouble(vec3.z)
                )
            }, true)
        return 1
    }

    private fun teleportEnter(entity: Entity, uuid: UUID, source: CommandSourceStack): Int =
        teleportEnter(setOf(entity), uuid, source)

    private fun teleportEnter(entities: Collection<Entity>, uuid: UUID, source: CommandSourceStack): Int {
        val level = getLevel(source, uuid)
        entities.forEach { entity ->
            ChestLevelManager.teleportEntityToEnter(level, entity)
        }
        return 1
    }

    private fun giveChest(player: ServerPlayer, source: CommandSourceStack): Int {
        val uuid = ChestLevelManager.findUUIDByLevel(source.level)
        if (uuid == null) {
            source.sendFailure(Component.literal("Can't find any UUID using this level."))
            return 0
        }
        return giveChest(player, uuid)
    }

    private fun giveChest(player: ServerPlayer, uuid: UUID): Int {
        player.addItem(createChestItem(uuid))
        return 1
    }

    private fun createChestItem(uuid: UUID): ItemStack {
        val stack = ItemStack(Items.CHEST_DIMENSION)
        val tag = CompoundTag()
        tag.putUUID("UUID", uuid)
        BlockItem.setBlockEntityData(stack, BlockEntityTypes.CHEST_DIMENSION, tag)
        return stack
    }

    private fun getDayTime(level: ServerLevel): Int {
        return (level.dayTime % 24000L).toInt()
    }

    private fun queryTime(source: CommandSourceStack, time: Int): Int {
        source.sendSuccess({ Component.translatable("commands.time.query", time) }, false)
        return time
    }

    private fun setTime(source: CommandSourceStack, time: Int): Int {
        for (serverLevel in source.server.allLevels) serverLevel.dayTime = time.toLong()

        source.sendSuccess({ Component.translatable("commands.time.set", time) }, true)
        return getDayTime(source.level)
    }

    private fun setTime(uuid: UUID, source: CommandSourceStack, time: Int): Int {
        val level = getLevel(source, uuid)
        return setTime(level, source, time)
    }

    private fun setTime(level: ServerLevel, source: CommandSourceStack, time: Int): Int {
        level.dayTime = time.toLong()
        source.sendSuccess({ Component.translatable("commands.time.set", time) }, true)
        return getDayTime(level)
    }

    private fun addTime(source: CommandSourceStack, amount: Int): Int {
        for (serverLevel in source.server.allLevels) serverLevel.dayTime += amount.toLong()

        val i = getDayTime(source.level)
        source.sendSuccess({ Component.translatable("commands.time.set", i) }, true)
        return i
    }

    private fun addTime(uuid: UUID, source: CommandSourceStack, amount: Int): Int {
        val level = getLevel(source, uuid)
        return addTime(level, source, amount)
    }

    private fun addTime(level: ServerLevel, source: CommandSourceStack, amount: Int): Int {
        level.dayTime += amount.toLong()
        val i = getDayTime(level)
        source.sendSuccess({ Component.translatable("commands.time.set", i) }, true)
        return i
    }

    private fun getDuration(level: ServerLevel, time: Int, timeProvider: IntProvider): Int {
        return if (time == -1) timeProvider.sample(level.getRandom()) else time
    }

    private fun setClear(source: CommandSourceStack, time: Int): Int {
        for (serverLevel in source.server.allLevels) serverLevel.setWeatherParameters(
            getDuration(
                serverLevel,
                time,
                ServerLevel.RAIN_DELAY
            ), 0, false, false
        )

        source.sendSuccess({ Component.translatable("commands.weather.set.clear") }, true)
        return time
    }

    private fun setClear(uuid: UUID, source: CommandSourceStack, time: Int): Int {
        val level = getLevel(source, uuid)
        return setClear(level, source, time)
    }

    private fun setClear(level: ServerLevel, source: CommandSourceStack, time: Int): Int {
        level.setWeatherParameters(getDuration(level, time, ServerLevel.RAIN_DELAY), 0, false, false)
        source.sendSuccess({ Component.translatable("commands.weather.set.clear") }, true)
        return time
    }

    private fun setRain(source: CommandSourceStack, time: Int): Int {
        for (serverLevel in source.server.allLevels) serverLevel.setWeatherParameters(
            0,
            getDuration(serverLevel, time, ServerLevel.RAIN_DURATION),
            true,
            false
        )

        source.sendSuccess({ Component.translatable("commands.weather.set.rain") }, true)
        return time
    }

    private fun setRain(uuid: UUID, source: CommandSourceStack, time: Int): Int {
        val level = getLevel(source, uuid)
        return setRain(level, source, time)
    }

    private fun setRain(level: ServerLevel, source: CommandSourceStack, time: Int): Int {
        level.setWeatherParameters(0, getDuration(level, time, ServerLevel.RAIN_DURATION), true, false)
        source.sendSuccess({ Component.translatable("commands.weather.set.rain") }, true)
        return time
    }

    private fun setThunder(source: CommandSourceStack, time: Int): Int {
        for (serverLevel in source.server.allLevels) serverLevel.setWeatherParameters(
            0,
            getDuration(serverLevel, time, ServerLevel.THUNDER_DURATION),
            true,
            true
        )

        source.sendSuccess({ Component.translatable("commands.weather.set.thunder") }, true)
        return time
    }

    private fun setThunder(uuid: UUID, source: CommandSourceStack, time: Int): Int {
        val level = getLevel(source, uuid)
        return setThunder(level, source, time)
    }

    private fun setThunder(level: ServerLevel, source: CommandSourceStack, time: Int): Int {
        level.setWeatherParameters(0, getDuration(level, time, ServerLevel.THUNDER_DURATION), true, true)
        source.sendSuccess({ Component.translatable("commands.weather.set.thunder") }, true)
        return time
    }

    @Throws(CommandSyntaxException::class)
    private fun damage(
        source: CommandSourceStack,
        targets: Collection<Entity>,
        amount: Float,
        damageType: DamageSource
    ): Int {
        var count = 0
        // TODO: Better message
        targets.forEach {
            if (!it.hurt(damageType, amount)) return@forEach
            source.sendSuccess({ Component.translatable("commands.damage.success", amount, it.displayName) }, true)
            count++
        }
        if (count == 0) throw ERROR_INVULNERABLE.create()
        return count
    }
}