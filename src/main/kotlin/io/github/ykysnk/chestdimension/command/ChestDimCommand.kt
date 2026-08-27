package io.github.ykysnk.chestdimension.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import io.github.ykysnk.chestdimension.Constants
import io.github.ykysnk.chestdimension.block.entity.BlockEntityTypes
import io.github.ykysnk.chestdimension.extensions.*
import io.github.ykysnk.chestdimension.item.Items
import io.github.ykysnk.chestdimension.level.ChestLevelManager
import io.github.ykysnk.chestdimension.level.UUIDManager
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.DimensionArgument
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.TimeArgument
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
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.Vec3
import java.util.*

object ChestDimCommand {
    private const val THIS = "this"
    private const val ALL = "all"

    @Suppress("SpellCheckingInspection")
    fun register(
        dispatcher: CommandDispatcher<CommandSourceStack>,
        registryAccess: CommandBuildContext,
        environment: Commands.CommandSelection
    ) {
        Commands.literal("chestdim").apply {
            literal("create") {
                requires { it.hasPermission(2) }

                then(Commands.argument("targets", EntityArgument.entities()).executes { context ->
                    val entities = EntityArgument.getEntities(context, "targets")
                    create(entities, context.source)
                })

                executes { context ->
                    val player = context.source.playerOrException
                    create(player, context.source)
                }
            }

            literal("tp") {
                requires { it.hasPermission(2) }

                then(allDimsArg.then(Commands.argument("pos", Vec3Argument.vec3()).executes { context ->
                    val player = context.source.playerOrException
                    val pos = Vec3Argument.getVec3(context, "pos")
                    teleport(player, pos, StringArgumentType.getString(context, "uuid"), context.source)
                }))

                then(
                    allDimsArg.then(
                        Commands.argument("targets", EntityArgument.entities())
                            .then(Commands.argument("pos", Vec3Argument.vec3()).executes { context ->
                                val entities = EntityArgument.getEntities(context, "targets")
                                val pos = Vec3Argument.getVec3(context, "pos")
                                teleport(entities, pos, StringArgumentType.getString(context, "uuid"), context.source)
                            })
                    )
                )

                then(
                    Commands.argument("dimension", DimensionArgument.dimension())
                        .then(Commands.argument("pos", Vec3Argument.vec3()).executes { context ->
                            val player = context.source.playerOrException
                            val pos = Vec3Argument.getVec3(context, "pos")
                            val level = DimensionArgument.getDimension(context, "dimension")
                            teleport(player, pos, level, context.source)
                        })
                )

                then(
                    Commands.argument("dimension", DimensionArgument.dimension()).then(
                        Commands.argument("targets", EntityArgument.entities())
                            .then(Commands.argument("pos", Vec3Argument.vec3()).executes { context ->
                                val entities = EntityArgument.getEntities(context, "targets")
                                val pos = Vec3Argument.getVec3(context, "pos")
                                val level = DimensionArgument.getDimension(context, "dimension")
                                teleport(entities, pos, level, context.source)
                            })
                    )
                )

                then(Commands.argument("pos", Vec3Argument.vec3()).executes { context ->
                    val player = context.source.playerOrException
                    val pos = Vec3Argument.getVec3(context, "pos")
                    teleport(player, pos, context.source.level, context.source)
                })

                then(
                    Commands.argument("targets", EntityArgument.entities())
                        .then(Commands.argument("pos", Vec3Argument.vec3()).executes { context ->
                            val entities = EntityArgument.getEntities(context, "targets")
                            val pos = Vec3Argument.getVec3(context, "pos")
                            teleport(entities, pos, context.source.level, context.source)
                        })
                )
            }

            literal("tp-safe") {
                requires { it.hasPermission(2) }

                then(
                    allDimsArg.then(
                        Commands.argument("pos", BlockPosArgument.blockPos()).then(
                            Commands.argument(
                                "spawn-radius",
                                IntegerArgumentType.integer(1)
                            ).executes { context ->
                                val player = context.source.playerOrException
                                val pos = BlockPosArgument.getBlockPos(context, "pos")
                                val spawnRadius = IntegerArgumentType.getInteger(context, "spawn-radius")
                                teleportSafe(
                                    player,
                                    pos,
                                    StringArgumentType.getString(context, "uuid"),
                                    spawnRadius,
                                    context.source
                                )
                            })
                    )
                )

                then(
                    allDimsArg.then(
                        Commands.argument("targets", EntityArgument.entities()).then(
                            Commands.argument("pos", BlockPosArgument.blockPos()).then(
                                Commands.argument(
                                    "spawn-radius",
                                    IntegerArgumentType.integer(1)
                                ).executes { context ->
                                    val entities = EntityArgument.getEntities(context, "targets")
                                    val pos = BlockPosArgument.getBlockPos(context, "pos")
                                    val spawnRadius = IntegerArgumentType.getInteger(context, "spawn-radius")
                                    teleportSafe(
                                        entities,
                                        pos,
                                        StringArgumentType.getString(context, "uuid"),
                                        spawnRadius,
                                        context.source
                                    )
                                })
                        )
                    )
                )

                then(
                    allDimsArg.then(
                        Commands.argument("pos", BlockPosArgument.blockPos()).executes { context ->
                            val player = context.source.playerOrException
                            val pos = BlockPosArgument.getBlockPos(context, "pos")
                            val spawnRadius = context.source.server.getSpawnRadius(context.source.level)
                            teleportSafe(
                                player,
                                pos,
                                StringArgumentType.getString(context, "uuid"),
                                spawnRadius,
                                context.source
                            )
                        })
                )

                then(
                    allDimsArg.then(
                        Commands.argument("targets", EntityArgument.entities()).then(
                            Commands.argument("pos", BlockPosArgument.blockPos()).executes { context ->
                                val entities = EntityArgument.getEntities(context, "targets")
                                val pos = BlockPosArgument.getBlockPos(context, "pos")
                                val spawnRadius = context.source.server.getSpawnRadius(context.source.level)
                                teleportSafe(
                                    entities,
                                    pos,
                                    StringArgumentType.getString(context, "uuid"),
                                    spawnRadius,
                                    context.source
                                )
                            })
                    )
                )

                then(
                    Commands.argument("dimension", DimensionArgument.dimension()).then(
                        Commands.argument("pos", BlockPosArgument.blockPos()).then(
                            Commands.argument(
                                "spawn-radius",
                                IntegerArgumentType.integer(1)
                            ).executes { context ->
                                val player = context.source.playerOrException
                                val pos = BlockPosArgument.getBlockPos(context, "pos")
                                val spawnRadius = IntegerArgumentType.getInteger(context, "spawn-radius")
                                val level = DimensionArgument.getDimension(context, "dimension")
                                teleportSafe(player, pos, level, spawnRadius, context.source)
                            })
                    )
                )

                then(
                    Commands.argument("dimension", DimensionArgument.dimension()).then(
                        Commands.argument("targets", EntityArgument.entities()).then(
                            Commands.argument("pos", BlockPosArgument.blockPos()).then(
                                Commands.argument(
                                    "spawn-radius",
                                    IntegerArgumentType.integer(1)
                                ).executes { context ->
                                    val entities = EntityArgument.getEntities(context, "targets")
                                    val pos = BlockPosArgument.getBlockPos(context, "pos")
                                    val spawnRadius = IntegerArgumentType.getInteger(context, "spawn-radius")
                                    val level = DimensionArgument.getDimension(context, "dimension")
                                    teleportSafe(entities, pos, level, spawnRadius, context.source)
                                })
                        )
                    )
                )

                then(
                    Commands.argument("dimension", DimensionArgument.dimension()).then(
                        Commands.argument("pos", BlockPosArgument.blockPos()).executes { context ->
                            val player = context.source.playerOrException
                            val pos = BlockPosArgument.getBlockPos(context, "pos")
                            val level = DimensionArgument.getDimension(context, "dimension")
                            val spawnRadius = context.source.server.getSpawnRadius(level)
                            teleportSafe(player, pos, level, spawnRadius, context.source)
                        })
                )

                then(
                    Commands.argument("dimension", DimensionArgument.dimension()).then(
                        Commands.argument("targets", EntityArgument.entities()).then(
                            Commands.argument("pos", BlockPosArgument.blockPos()).executes { context ->
                                val entities = EntityArgument.getEntities(context, "targets")
                                val pos = BlockPosArgument.getBlockPos(context, "pos")
                                val level = DimensionArgument.getDimension(context, "dimension")
                                val spawnRadius = context.source.server.getSpawnRadius(level)
                                teleportSafe(entities, pos, level, spawnRadius, context.source)
                            })
                    )
                )

                then(
                    Commands.argument("pos", BlockPosArgument.blockPos()).then(
                        Commands.argument(
                            "spawn-radius",
                            IntegerArgumentType.integer(1)
                        ).executes { context ->
                            val player = context.source.playerOrException
                            val pos = BlockPosArgument.getBlockPos(context, "pos")
                            val spawnRadius = IntegerArgumentType.getInteger(context, "spawn-radius")
                            teleportSafe(
                                player,
                                pos,
                                THIS,
                                spawnRadius,
                                context.source
                            )
                        })
                )

                then(
                    Commands.argument("targets", EntityArgument.entities()).then(
                        Commands.argument("pos", BlockPosArgument.blockPos()).then(
                            Commands.argument(
                                "spawn-radius",
                                IntegerArgumentType.integer(1)
                            ).executes { context ->
                                val entities = EntityArgument.getEntities(context, "targets")
                                val pos = BlockPosArgument.getBlockPos(context, "pos")
                                val spawnRadius = IntegerArgumentType.getInteger(context, "spawn-radius")
                                teleportSafe(
                                    entities,
                                    pos,
                                    THIS,
                                    spawnRadius,
                                    context.source
                                )
                            })
                    )
                )

                then(
                    Commands.argument("pos", BlockPosArgument.blockPos()).executes { context ->
                        val player = context.source.playerOrException
                        val pos = BlockPosArgument.getBlockPos(context, "pos")
                        val spawnRadius = context.source.server.getSpawnRadius(context.source.level)
                        teleportSafe(
                            player,
                            pos,
                            THIS,
                            spawnRadius,
                            context.source
                        )
                    })

                then(
                    Commands.argument("targets", EntityArgument.entities()).then(
                        Commands.argument("pos", BlockPosArgument.blockPos()).executes { context ->
                            val entities = EntityArgument.getEntities(context, "targets")
                            val pos = BlockPosArgument.getBlockPos(context, "pos")
                            val spawnRadius = context.source.server.getSpawnRadius(context.source.level)
                            teleportSafe(
                                entities,
                                pos,
                                THIS,
                                spawnRadius,
                                context.source
                            )
                        })
                )
            }

            literal("tp-spawn") {
                requires { it.hasPermission(2) }

                then(
                    allDimsArg.then(
                        Commands.argument("spawn-radius", IntegerArgumentType.integer(1)).executes { context ->
                            val player = context.source.playerOrException
                            val spawnRadius = IntegerArgumentType.getInteger(context, "spawn-radius")
                            teleportSpawn(
                                player,
                                StringArgumentType.getString(context, "uuid"),
                                spawnRadius,
                                context.source
                            )
                        })
                )

                then(
                    allDimsArg.then(
                        Commands.argument("targets", EntityArgument.entities()).then(
                            Commands.argument("spawn-radius", IntegerArgumentType.integer(1)).executes { context ->
                                val entities = EntityArgument.getEntities(context, "targets")
                                val spawnRadius = IntegerArgumentType.getInteger(context, "spawn-radius")
                                teleportSpawn(
                                    entities,
                                    StringArgumentType.getString(context, "uuid"),
                                    spawnRadius,
                                    context.source
                                )
                            })
                    )
                )

                then(allDimsArg.executes { context ->
                    val player = context.source.playerOrException
                    val spawnRadius = context.source.server.getSpawnRadius(context.source.level)
                    teleportSpawn(player, StringArgumentType.getString(context, "uuid"), spawnRadius, context.source)
                })

                then(
                    allDimsArg.then(
                        Commands.argument("targets", EntityArgument.entities()).executes { context ->
                            val entities = EntityArgument.getEntities(context, "targets")
                            val spawnRadius = context.source.server.getSpawnRadius(context.source.level)
                            teleportSpawn(
                                entities,
                                StringArgumentType.getString(context, "uuid"),
                                spawnRadius,
                                context.source
                            )
                        })
                )

                then(
                    Commands.argument("dimension", DimensionArgument.dimension()).then(
                        Commands.argument("spawn-radius", IntegerArgumentType.integer(1)).executes { context ->
                            val player = context.source.playerOrException
                            val spawnRadius = IntegerArgumentType.getInteger(context, "spawn-radius")
                            val level = DimensionArgument.getDimension(context, "dimension")
                            teleportSpawn(player, level, spawnRadius, context.source)
                        })
                )

                then(
                    Commands.argument("dimension", DimensionArgument.dimension()).then(
                        Commands.argument("targets", EntityArgument.entities()).then(
                            Commands.argument("spawn-radius", IntegerArgumentType.integer(1)).executes { context ->
                                val entities = EntityArgument.getEntities(context, "targets")
                                val spawnRadius = IntegerArgumentType.getInteger(context, "spawn-radius")
                                val level = DimensionArgument.getDimension(context, "dimension")
                                teleportSpawn(entities, level, spawnRadius, context.source)
                            })
                    )
                )

                then(Commands.argument("dimension", DimensionArgument.dimension()).executes { context ->
                    val player = context.source.playerOrException
                    val level = DimensionArgument.getDimension(context, "dimension")
                    val spawnRadius = context.source.server.getSpawnRadius(level)
                    teleportSpawn(player, level, spawnRadius, context.source)
                })

                then(
                    Commands.argument("dimension", DimensionArgument.dimension())
                        .then(Commands.argument("targets", EntityArgument.entities()).executes { context ->
                            val entities = EntityArgument.getEntities(context, "targets")
                            val level = DimensionArgument.getDimension(context, "dimension")
                            val spawnRadius = context.source.server.getSpawnRadius(level)
                            teleportSpawn(entities, level, spawnRadius, context.source)
                        })
                )

                then(
                    Commands.argument("spawn-radius", IntegerArgumentType.integer(1)).executes { context ->
                        val player = context.source.playerOrException
                        val spawnRadius = IntegerArgumentType.getInteger(context, "spawn-radius")
                        teleportSpawn(player, THIS, spawnRadius, context.source)
                    }
                )

                then(
                    Commands.argument("targets", EntityArgument.entities()).then(
                        Commands.argument("spawn-radius", IntegerArgumentType.integer(1)).executes { context ->
                            val entities = EntityArgument.getEntities(context, "targets")
                            val spawnRadius = IntegerArgumentType.getInteger(context, "spawn-radius")
                            teleportSpawn(entities, THIS, spawnRadius, context.source)
                        }
                    ))

                executes { context ->
                    val player = context.source.playerOrException
                    val spawnRadius = context.source.server.getSpawnRadius(context.source.level)
                    teleportSpawn(player, THIS, spawnRadius, context.source)
                }

                then(Commands.argument("targets", EntityArgument.entities()).executes { context ->
                    val entities = EntityArgument.getEntities(context, "targets")
                    val spawnRadius = context.source.server.getSpawnRadius(context.source.level)
                    teleportSpawn(entities, THIS, spawnRadius, context.source)
                })
            }

            literal("tp-enter") {
                requires { it.hasPermission(2) }

                then(uuidArg.executes { context ->
                    val player = context.source.playerOrException
                    teleportEnter(player, StringArgumentType.getString(context, "uuid"), context.source)
                })

                then(
                    uuidArg.then(
                        Commands.argument("targets", EntityArgument.entities()).executes { context ->
                            val entities = EntityArgument.getEntities(context, "targets")
                            teleportEnter(entities, StringArgumentType.getString(context, "uuid"), context.source)
                        })
                )
            }

            literal("give-chest") {
                requires { it.hasPermission(2) }

                then(
                    uuidArg.then(
                        Commands.argument("player", EntityArgument.player()).executes { context ->
                            val player =
                                EntityArgument.getPlayer(context, "player")
                                    ?: throw CommandSourceStack.ERROR_NOT_PLAYER.create()
                            giveChest(player, StringArgumentType.getString(context, "uuid"), context.source)
                        })
                )

                then(Commands.argument("player", EntityArgument.player()).executes { context ->
                    val player =
                        EntityArgument.getPlayer(context, "player")
                            ?: throw CommandSourceStack.ERROR_NOT_PLAYER.create()
                    giveChest(player, THIS, context.source)
                })

                then(uuidArg.executes { context ->
                    val player = context.source.playerOrException
                    giveChest(player, StringArgumentType.getString(context, "uuid"), context.source)
                })

                executes { context ->
                    val player = context.source.playerOrException
                    giveChest(player, THIS, context.source)
                }
            }

            literal("time") {
                requires { it.hasPermission(2) }

                literal("set", allDimsWithAllArg) {
                    then(Commands.literal("day").executes {
                        val uuidString = StringArgumentType.getString(it, "uuid")
                        setTime(uuidString, it.getSource(), 1000)
                    })

                    then(Commands.literal("noon").executes {
                        val uuidString = StringArgumentType.getString(it, "uuid")
                        setTime(uuidString, it.getSource(), 6000)
                    })

                    then(Commands.literal("night").executes {
                        val uuidString = StringArgumentType.getString(it, "uuid")
                        setTime(uuidString, it.getSource(), 13000)
                    })

                    then(Commands.literal("midnight").executes {
                        val uuidString = StringArgumentType.getString(it, "uuid")
                        setTime(uuidString, it.getSource(), 18000)
                    })

                    then(Commands.argument("time", TimeArgument.time()).executes {
                        val uuidString = StringArgumentType.getString(it, "uuid")
                        val time = IntegerArgumentType.getInteger(it, "time")
                        setTime(uuidString, it.getSource(), time)
                    })
                }

                literal("set", Commands.argument("dimension", DimensionArgument.dimension())) {
                    then(Commands.literal("day").executes {
                        val level = DimensionArgument.getDimension(it, "dimension")
                        setTime(level, it.getSource(), 1000)
                    })

                    then(Commands.literal("noon").executes {
                        val level = DimensionArgument.getDimension(it, "dimension")
                        setTime(level, it.getSource(), 6000)
                    })

                    then(Commands.literal("night").executes {
                        val level = DimensionArgument.getDimension(it, "dimension")
                        setTime(level, it.getSource(), 13000)
                    })

                    then(Commands.literal("midnight").executes {
                        val level = DimensionArgument.getDimension(it, "dimension")
                        setTime(level, it.getSource(), 18000)
                    })

                    then(Commands.argument("time", TimeArgument.time()).executes {
                        val level = DimensionArgument.getDimension(it, "dimension")
                        val time = IntegerArgumentType.getInteger(it, "time")
                        setTime(level, it.getSource(), time)
                    })
                }

                literal("set") {
                    then(Commands.literal("day").executes {
                        setTime(THIS, it.getSource(), 1000)
                    })

                    then(Commands.literal("noon").executes {
                        setTime(THIS, it.getSource(), 6000)
                    })

                    then(Commands.literal("night").executes {
                        setTime(THIS, it.getSource(), 13000)
                    })

                    then(Commands.literal("midnight").executes {
                        setTime(THIS, it.getSource(), 18000)
                    })

                    then(Commands.argument("time", TimeArgument.time()).executes {
                        val time = IntegerArgumentType.getInteger(it, "time")
                        setTime(THIS, it.getSource(), time)
                    })
                }

                literal("add", allDimsWithAllArg) {
                    then(Commands.argument("time", TimeArgument.time()).executes {
                        val uuidString = StringArgumentType.getString(it, "uuid")
                        val time = IntegerArgumentType.getInteger(it, "time")
                        addTime(uuidString, it.getSource(), time)
                    })
                }

                literal("add", Commands.argument("dimension", DimensionArgument.dimension())) {
                    then(Commands.argument("time", TimeArgument.time()).executes {
                        val level = DimensionArgument.getDimension(it, "dimension")
                        val time = IntegerArgumentType.getInteger(it, "time")
                        addTime(level, it.getSource(), time)
                    })
                }

                literal("add") {
                    then(Commands.argument("time", TimeArgument.time()).executes {
                        val time = IntegerArgumentType.getInteger(it, "time")
                        addTime(THIS, it.getSource(), time)
                    })
                }

                literal("query", allDimsArg) {
                    then(Commands.literal("daytime").executes {
                        val uuidString = StringArgumentType.getString(it, "uuid")
                        val level = getLevel(it.source, uuidString)
                        if (level == null) {
                            it.source.sendFailure(Component.literal("UUID is not valid."))
                            return@executes 0
                        }
                        queryTime(it.getSource(), getDayTime(level))
                    })

                    then(Commands.literal("gametime").executes {
                        val uuidString = StringArgumentType.getString(it, "uuid")
                        val level = getLevel(it.source, uuidString)
                        if (level == null) {
                            it.source.sendFailure(Component.literal("UUID is not valid."))
                            return@executes 0
                        }
                        queryTime(it.getSource(), (level.gameTime % 2147483647L).toInt())
                    })

                    then(Commands.literal("day").executes {
                        val uuidString = StringArgumentType.getString(it, "uuid")
                        val level = getLevel(it.source, uuidString)
                        if (level == null) {
                            it.source.sendFailure(Component.literal("UUID is not valid."))
                            return@executes 0
                        }
                        queryTime(it.getSource(), (level.dayTime / 24000L % 2147483647L).toInt())
                    })
                }

                literal("query", Commands.argument("dimension", DimensionArgument.dimension())) {
                    then(Commands.literal("daytime").executes {
                        val level = DimensionArgument.getDimension(it, "dimension")
                        queryTime(it.getSource(), getDayTime(level))
                    })

                    then(Commands.literal("gametime").executes {
                        val level = DimensionArgument.getDimension(it, "dimension")
                        queryTime(it.getSource(), (level.gameTime % 2147483647L).toInt())
                    })

                    then(Commands.literal("day").executes {
                        val level = DimensionArgument.getDimension(it, "dimension")
                        queryTime(it.getSource(), (level.dayTime / 24000L % 2147483647L).toInt())
                    })
                }

                literal("query") {
                    then(Commands.literal("daytime").executes {
                        val level = getLevel(it.source, THIS)
                        if (level == null) {
                            it.source.sendFailure(Component.literal("UUID is not valid."))
                            return@executes 0
                        }
                        queryTime(it.getSource(), getDayTime(level))
                    })

                    then(Commands.literal("gametime").executes {
                        val level = getLevel(it.source, THIS)
                        if (level == null) {
                            it.source.sendFailure(Component.literal("UUID is not valid."))
                            return@executes 0
                        }
                        queryTime(it.getSource(), (level.gameTime % 2147483647L).toInt())
                    })

                    then(Commands.literal("day").executes {
                        val level = getLevel(it.source, THIS)
                        if (level == null) {
                            it.source.sendFailure(Component.literal("UUID is not valid."))
                            return@executes 0
                        }
                        queryTime(it.getSource(), (level.dayTime / 24000L % 2147483647L).toInt())
                    })
                }
            }

            literal("weather") {
                requires { it.hasPermission(2) }

                literal("clear", allDimsWithAllArg) {
                    executes {
                        val uuidString = StringArgumentType.getString(it, "uuid")
                        setClear(uuidString, it.source, -1)
                    }

                    then(Commands.argument("duration", TimeArgument.time(1)).executes {
                        val uuidString = StringArgumentType.getString(it, "uuid")
                        setClear(uuidString, it.source, IntegerArgumentType.getInteger(it, "duration"))
                    })
                }

                literal("clear", Commands.argument("dimension", DimensionArgument.dimension())) {
                    executes {
                        val level = DimensionArgument.getDimension(it, "dimension")
                        setClear(level, it.source, -1)
                    }

                    then(Commands.argument("duration", TimeArgument.time(1)).executes {
                        val level = DimensionArgument.getDimension(it, "dimension")
                        setClear(level, it.source, IntegerArgumentType.getInteger(it, "duration"))
                    })
                }

                literal("clear") {
                    executes {
                        setClear(THIS, it.source, -1)
                    }

                    then(Commands.argument("duration", TimeArgument.time(1)).executes {
                        setClear(THIS, it.source, IntegerArgumentType.getInteger(it, "duration"))
                    })
                }
            }

            literal("weather") {
                requires { it.hasPermission(2) }

                literal("rain", allDimsWithAllArg) {
                    executes {
                        val uuidString = StringArgumentType.getString(it, "uuid")
                        setRain(uuidString, it.source, -1)
                    }

                    then(Commands.argument("duration", TimeArgument.time(1)).executes {
                        val uuidString = StringArgumentType.getString(it, "uuid")
                        setRain(uuidString, it.source, IntegerArgumentType.getInteger(it, "duration"))
                    })
                }

                literal("rain", Commands.argument("dimension", DimensionArgument.dimension())) {
                    executes {
                        val level = DimensionArgument.getDimension(it, "dimension")
                        setRain(level, it.source, -1)
                    }

                    then(Commands.argument("duration", TimeArgument.time(1)).executes {
                        val level = DimensionArgument.getDimension(it, "dimension")
                        setRain(level, it.source, IntegerArgumentType.getInteger(it, "duration"))
                    })
                }

                literal("rain") {
                    executes {
                        setRain(THIS, it.source, -1)
                    }

                    then(Commands.argument("duration", TimeArgument.time(1)).executes {
                        setRain(THIS, it.source, IntegerArgumentType.getInteger(it, "duration"))
                    })
                }

                literal("thunder", allDimsWithAllArg) {
                    executes {
                        val uuidString = StringArgumentType.getString(it, "uuid")
                        setThunder(uuidString, it.source, -1)
                    }

                    then(Commands.argument("duration", TimeArgument.time(1)).executes {
                        val uuidString = StringArgumentType.getString(it, "uuid")
                        setThunder(uuidString, it.source, IntegerArgumentType.getInteger(it, "duration"))
                    })
                }

                literal("thunder", Commands.argument("dimension", DimensionArgument.dimension())) {
                    executes {
                        val level = DimensionArgument.getDimension(it, "dimension")
                        setThunder(level, it.source, -1)
                    }

                    then(Commands.argument("duration", TimeArgument.time(1)).executes {
                        val level = DimensionArgument.getDimension(it, "dimension")
                        setThunder(level, it.source, IntegerArgumentType.getInteger(it, "duration"))
                    })
                }

                literal("thunder") {
                    executes {
                        setThunder(THIS, it.source, -1)
                    }

                    then(Commands.argument("duration", TimeArgument.time(1)).executes {
                        setThunder(THIS, it.source, IntegerArgumentType.getInteger(it, "duration"))
                    })
                }
            }


            literal("ping") {
                executes {
                    val player = it.source.playerOrException
                    it.source.sendSuccess({ Component.literal("Ping: ${player.latency}ms") }, true)
                    1
                }
            }

            dispatcher.register(this)
        }
    }

    private val uuidArg: RequiredArgumentBuilder<CommandSourceStack, String>
        get() = Commands.argument("uuid", StringArgumentType.string()).suggests { _, builder ->
            val tempSet = hashSetOf<String>()
            UUIDManager.getMap().keys.forEach(tempSet::add)
            UUIDManager.getInactiveList().forEach(tempSet::add)
            tempSet.forEach(builder::suggest)
            builder.suggest(THIS)
            builder.buildFuture()
        }

    private val allDimsArg: RequiredArgumentBuilder<CommandSourceStack, String>
        get() = Commands.argument("uuid", StringArgumentType.string()).suggests { context, builder ->
            val server = context.source.server
            val tempSet = hashSetOf<String>()
            UUIDManager.getMap().keys.forEach(tempSet::add)
            UUIDManager.getInactiveList().forEach(tempSet::add)
            tempSet.forEach(builder::suggest)

            server.allLevels.forEach {
                if (ChestLevelManager.isInsideChestDimension(it)) return@forEach
                builder.suggest("\"${it.dimension().location()}\"")
            }

            builder.suggest(THIS)
            builder.buildFuture()
        }

    private val allDimsWithAllArg: RequiredArgumentBuilder<CommandSourceStack, String>
        get() = Commands.argument("uuid", StringArgumentType.string()).suggests { context, builder ->
            val server = context.source.server
            val tempSet = hashSetOf<String>()
            UUIDManager.getMap().keys.forEach(tempSet::add)
            UUIDManager.getInactiveList().forEach(tempSet::add)
            tempSet.forEach(builder::suggest)

            server.allLevels.forEach {
                if (ChestLevelManager.isInsideChestDimension(it)) return@forEach
                builder.suggest("\"${it.dimension().location()}\"")
            }

            builder.suggest(THIS)
            builder.suggest(ALL)
            builder.buildFuture()
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

    private fun getLevel(source: CommandSourceStack, dimension: String, onlyChestDim: Boolean = false): ServerLevel? {
        if (dimension == THIS) return source.level

        runCatching { UUID.fromString(dimension) }.getOrNull()?.let { uuid ->
            return ChestLevelManager.getOrCreate(source.server, uuid)
        }

        if (onlyChestDim) return null

        val id = ResourceLocation.tryParse(dimension.replace("\"", "")) ?: return null
        return source.server.getLevel(ResourceKey.create(Registries.DIMENSION, id))
    }

    private fun teleport(entity: Entity, pos: Vec3, uuidString: String, source: CommandSourceStack): Int =
        teleport(setOf(entity), pos, uuidString, source)

    private fun teleport(entities: Collection<Entity>, pos: Vec3, uuidString: String, source: CommandSourceStack): Int {
        val level = getLevel(source, uuidString)
        if (level == null) {
            source.sendFailure(Component.literal("UUID is not valid."))
            return 0
        }
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
        uuidString: String,
        spawnRadius: Int,
        source: CommandSourceStack
    ): Int = teleportSafe(setOf(entity), pos, uuidString, spawnRadius, source)

    private fun teleportSafe(
        entities: Collection<Entity>,
        pos: BlockPos,
        uuidString: String,
        spawnRadius: Int,
        source: CommandSourceStack
    ): Int {
        val level = getLevel(source, uuidString)
        if (level == null) {
            source.sendFailure(Component.literal("UUID is not valid."))
            return 0
        }
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
        uuidString: String,
        spawnRadius: Int,
        source: CommandSourceStack
    ): Int = teleportSpawn(setOf(entity), uuidString, spawnRadius, source)

    private fun teleportSpawn(
        entities: Collection<Entity>,
        uuidString: String,
        spawnRadius: Int,
        source: CommandSourceStack
    ): Int {
        val level = getLevel(source, uuidString)
        if (level == null) {
            source.sendFailure(Component.literal("UUID is not valid."))
            return 0
        }
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

    private fun teleportEnter(entity: Entity, uuidString: String, source: CommandSourceStack): Int =
        teleportEnter(setOf(entity), uuidString, source)

    private fun teleportEnter(entities: Collection<Entity>, uuidString: String, source: CommandSourceStack): Int {
        val level = getLevel(source, uuidString, true)
        if (level == null) {
            source.sendFailure(Component.literal("UUID is not valid."))
            return 0
        }
        entities.forEach { entity ->
            ChestLevelManager.teleportEntityToEnter(level, entity)
        }
        return 1
    }

    private fun giveChest(player: ServerPlayer, uuidString: String, source: CommandSourceStack): Int {
        when (uuidString) {
            THIS -> {
                val uuid = ChestLevelManager.findUUIDByLevel(source.level)
                if (uuid == null) {
                    source.sendFailure(Component.literal("Can't find any UUID using this level."))
                    return 0
                }
                player.addItem(createChestItem(uuid))
                return 1
            }

            else -> {
                val uuid = runCatching { UUID.fromString(uuidString) }.getOrElse {
                    Constants.LOGGER.error(it.localizedMessage, it)
                    source.sendFailure(Component.literal("UUID is not valid."))
                    return 0
                }
                player.addItem(createChestItem(uuid))
                return 1
            }
        }
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

    private fun setTime(uuidString: String, source: CommandSourceStack, time: Int): Int {
        when (uuidString) {
            ALL -> {
                for (serverLevel in source.server.allLevels) serverLevel.dayTime = time.toLong()

                source.sendSuccess({ Component.translatable("commands.time.set", time) }, true)
                return getDayTime(source.level)
            }

            else -> {
                val level = getLevel(source, uuidString)
                if (level == null) {
                    source.sendFailure(Component.literal("UUID is not valid."))
                    return 0
                }
                return setTime(level, source, time)
            }
        }
    }

    private fun setTime(level: ServerLevel, source: CommandSourceStack, time: Int): Int {
        level.dayTime = time.toLong()
        source.sendSuccess({ Component.translatable("commands.time.set", time) }, true)
        return getDayTime(level)
    }

    private fun addTime(uuidString: String, source: CommandSourceStack, amount: Int): Int {
        when (uuidString) {
            ALL -> {
                for (serverLevel in source.server.allLevels) serverLevel.dayTime += amount.toLong()

                val i = getDayTime(source.level)
                source.sendSuccess({ Component.translatable("commands.time.set", i) }, true)
                return i
            }

            else -> {
                val level = getLevel(source, uuidString)
                if (level == null) {
                    source.sendFailure(Component.literal("UUID is not valid."))
                    return 0
                }
                return addTime(level, source, amount)
            }
        }
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

    private fun setClear(uuidString: String, source: CommandSourceStack, time: Int): Int {
        when (uuidString) {
            ALL -> {
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

            else -> {
                val level = getLevel(source, uuidString)
                if (level == null) {
                    source.sendFailure(Component.literal("UUID is not valid."))
                    return 0
                }
                return setClear(level, source, time)
            }
        }
    }

    private fun setClear(level: ServerLevel, source: CommandSourceStack, time: Int): Int {
        level.setWeatherParameters(getDuration(level, time, ServerLevel.RAIN_DELAY), 0, false, false)
        source.sendSuccess({ Component.translatable("commands.weather.set.clear") }, true)
        return time
    }

    private fun setRain(uuidString: String, source: CommandSourceStack, time: Int): Int {
        when (uuidString) {
            ALL -> {
                for (serverLevel in source.server.allLevels) serverLevel.setWeatherParameters(
                    0,
                    getDuration(serverLevel, time, ServerLevel.RAIN_DURATION),
                    true,
                    false
                )

                source.sendSuccess({ Component.translatable("commands.weather.set.rain") }, true)
                return time
            }

            else -> {
                val level = getLevel(source, uuidString)
                if (level == null) {
                    source.sendFailure(Component.literal("UUID is not valid."))
                    return 0
                }
                return setRain(level, source, time)
            }
        }
    }

    private fun setRain(level: ServerLevel, source: CommandSourceStack, time: Int): Int {
        level.setWeatherParameters(0, getDuration(level, time, ServerLevel.RAIN_DURATION), true, false)
        source.sendSuccess({ Component.translatable("commands.weather.set.rain") }, true)
        return time
    }

    private fun setThunder(uuidString: String, source: CommandSourceStack, time: Int): Int {
        when (uuidString) {
            ALL -> {
                for (serverLevel in source.server.allLevels) serverLevel.setWeatherParameters(
                    0,
                    getDuration(serverLevel, time, ServerLevel.THUNDER_DURATION),
                    true,
                    true
                )

                source.sendSuccess({ Component.translatable("commands.weather.set.thunder") }, true)
                return time
            }

            else -> {
                val level = getLevel(source, uuidString)
                if (level == null) {
                    source.sendFailure(Component.literal("UUID is not valid."))
                    return 0
                }
                return setThunder(level, source, time)
            }
        }
    }

    private fun setThunder(level: ServerLevel, source: CommandSourceStack, time: Int): Int {
        level.setWeatherParameters(0, getDuration(level, time, ServerLevel.THUNDER_DURATION), true, true)
        source.sendSuccess({ Component.translatable("commands.weather.set.thunder") }, true)
        return time
    }
}