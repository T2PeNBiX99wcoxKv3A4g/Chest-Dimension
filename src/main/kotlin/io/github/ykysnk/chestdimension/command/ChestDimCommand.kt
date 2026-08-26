package io.github.ykysnk.chestdimension.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import io.github.ykysnk.chestdimension.Constants
import io.github.ykysnk.chestdimension.block.entity.BlockEntityTypes
import io.github.ykysnk.chestdimension.extensions.teleportToLevel
import io.github.ykysnk.chestdimension.extensions.teleportToSafeLocation
import io.github.ykysnk.chestdimension.extensions.teleportToSpawnLocation
import io.github.ykysnk.chestdimension.extensions.toVec3
import io.github.ykysnk.chestdimension.item.Items
import io.github.ykysnk.chestdimension.level.ChestLevelManager
import io.github.ykysnk.chestdimension.level.UUIDManager
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
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        Commands.literal("chestdim").let { builder ->
            Commands.literal("create").requires { it.hasPermission(2) }.let { createBuilder ->
                createBuilder.executes { context ->
                    runCatching {
                        val player = context.source.playerOrException
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
                        val world = ChestLevelManager.getOrCreate(context.source.server, uuid)
                        ChestLevelManager.teleportEntityToEnter(world, player)
                        context.source.sendSuccess({ Component.literal("Created world: ").append(uuidComponent) }, true)
                        1
                    }.getOrElse {
                        Constants.LOGGER.error(it.localizedMessage, it)
                        context.source.sendFailure(Component.literal("Command error (${it.localizedMessage})"))
                        0
                    }
                }

                builder.then(createBuilder)
            }

            Commands.literal("tp").requires { it.hasPermission(2) }.let { tpBuilder ->
                tpBuilder.then(allDimsArg.then(Commands.argument("pos", Vec3Argument.vec3()).executes { context ->
                    val player = context.source.playerOrException
                    val pos = Vec3Argument.getVec3(context, "pos")
                    teleport(player, pos, StringArgumentType.getString(context, "uuid"), context.source)
                }))

                tpBuilder.then(
                    allDimsArg.then(
                        Commands.argument("targets", EntityArgument.entities())
                            .then(Commands.argument("pos", Vec3Argument.vec3()).executes { context ->
                                val entities = EntityArgument.getEntities(context, "targets")
                                val pos = Vec3Argument.getVec3(context, "pos")
                                teleport(entities, pos, StringArgumentType.getString(context, "uuid"), context.source)
                            })
                    )
                )

                tpBuilder.then(
                    Commands.argument("dimension", DimensionArgument.dimension())
                        .then(Commands.argument("pos", Vec3Argument.vec3()).executes { context ->
                            val player = context.source.playerOrException
                            val pos = Vec3Argument.getVec3(context, "pos")
                            val level = DimensionArgument.getDimension(context, "dimension")
                            teleport(player, pos, level, context.source)
                        })
                )

                tpBuilder.then(
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

                tpBuilder.then(Commands.argument("pos", Vec3Argument.vec3()).executes { context ->
                    val player = context.source.playerOrException
                    val pos = Vec3Argument.getVec3(context, "pos")
                    teleport(player, pos, context.source.level, context.source)
                })

                tpBuilder.then(
                    Commands.argument("targets", EntityArgument.entities())
                        .then(Commands.argument("pos", Vec3Argument.vec3()).executes { context ->
                            val entities = EntityArgument.getEntities(context, "targets")
                            val pos = Vec3Argument.getVec3(context, "pos")
                            teleport(entities, pos, context.source.level, context.source)
                        })
                )

                builder.then(tpBuilder)
            }

            Commands.literal("tp-safe").requires { it.hasPermission(2) }.let { tpSafeBuilder ->
                tpSafeBuilder.then(
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

                tpSafeBuilder.then(
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

                tpSafeBuilder.then(
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

                tpSafeBuilder.then(
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

                tpSafeBuilder.then(
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

                tpSafeBuilder.then(
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

                tpSafeBuilder.then(
                    Commands.argument("dimension", DimensionArgument.dimension()).then(
                        Commands.argument("pos", BlockPosArgument.blockPos()).executes { context ->
                            val player = context.source.playerOrException
                            val pos = BlockPosArgument.getBlockPos(context, "pos")
                            val level = DimensionArgument.getDimension(context, "dimension")
                            val spawnRadius = context.source.server.getSpawnRadius(level)
                            teleportSafe(player, pos, level, spawnRadius, context.source)
                        })
                )

                tpSafeBuilder.then(
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

                tpSafeBuilder.then(
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

                tpSafeBuilder.then(
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

                tpSafeBuilder.then(
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

                tpSafeBuilder.then(
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

                builder.then(tpSafeBuilder)
            }

            Commands.literal("tp-spawn").requires { it.hasPermission(2) }.let { tpSpawnBuilder ->
                tpSpawnBuilder.then(
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

                tpSpawnBuilder.then(
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

                tpSpawnBuilder.then(allDimsArg.executes { context ->
                    val player = context.source.playerOrException
                    val spawnRadius = context.source.server.getSpawnRadius(context.source.level)
                    teleportSpawn(player, StringArgumentType.getString(context, "uuid"), spawnRadius, context.source)
                })

                tpSpawnBuilder.then(
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

                tpSpawnBuilder.then(
                    Commands.argument("dimension", DimensionArgument.dimension()).then(
                        Commands.argument("spawn-radius", IntegerArgumentType.integer(1)).executes { context ->
                            val player = context.source.playerOrException
                            val spawnRadius = IntegerArgumentType.getInteger(context, "spawn-radius")
                            val level = DimensionArgument.getDimension(context, "dimension")
                            teleportSpawn(player, level, spawnRadius, context.source)
                        })
                )

                tpSpawnBuilder.then(
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

                tpSpawnBuilder.then(Commands.argument("dimension", DimensionArgument.dimension()).executes { context ->
                    val player = context.source.playerOrException
                    val level = DimensionArgument.getDimension(context, "dimension")
                    val spawnRadius = context.source.server.getSpawnRadius(level)
                    teleportSpawn(player, level, spawnRadius, context.source)
                })

                tpSpawnBuilder.then(
                    Commands.argument("dimension", DimensionArgument.dimension())
                        .then(Commands.argument("targets", EntityArgument.entities()).executes { context ->
                            val entities = EntityArgument.getEntities(context, "targets")
                            val level = DimensionArgument.getDimension(context, "dimension")
                            val spawnRadius = context.source.server.getSpawnRadius(level)
                            teleportSpawn(entities, level, spawnRadius, context.source)
                        })
                )

                tpSpawnBuilder.then(
                    Commands.argument("spawn-radius", IntegerArgumentType.integer(1)).executes { context ->
                        val player = context.source.playerOrException
                        val spawnRadius = IntegerArgumentType.getInteger(context, "spawn-radius")
                        teleportSpawn(player, THIS, spawnRadius, context.source)
                    }
                )

                tpSpawnBuilder.then(
                    Commands.argument("targets", EntityArgument.entities()).then(
                        Commands.argument("spawn-radius", IntegerArgumentType.integer(1)).executes { context ->
                            val entities = EntityArgument.getEntities(context, "targets")
                            val spawnRadius = IntegerArgumentType.getInteger(context, "spawn-radius")
                            teleportSpawn(entities, THIS, spawnRadius, context.source)
                        }
                    ))

                tpSpawnBuilder.executes { context ->
                    val player = context.source.playerOrException
                    val spawnRadius = context.source.server.getSpawnRadius(context.source.level)
                    teleportSpawn(player, THIS, spawnRadius, context.source)
                }

                tpSpawnBuilder.then(Commands.argument("targets", EntityArgument.entities()).executes { context ->
                    val entities = EntityArgument.getEntities(context, "targets")
                    val spawnRadius = context.source.server.getSpawnRadius(context.source.level)
                    teleportSpawn(entities, THIS, spawnRadius, context.source)
                })

                builder.then(tpSpawnBuilder)
            }

            Commands.literal("give-chest").requires { it.hasPermission(2) }.let { giveChestBuilder ->
                giveChestBuilder.then(
                    uuidArg.then(
                        Commands.argument("player", EntityArgument.player()).executes { context ->
                            val player =
                                EntityArgument.getPlayer(context, "player")
                                    ?: throw CommandSourceStack.ERROR_NOT_PLAYER.create()
                            giveChest(player, StringArgumentType.getString(context, "uuid"), context.source)
                        })
                )

                giveChestBuilder.then(Commands.argument("player", EntityArgument.player()).executes { context ->
                    val player =
                        EntityArgument.getPlayer(context, "player")
                            ?: throw CommandSourceStack.ERROR_NOT_PLAYER.create()
                    giveChest(player, THIS, context.source)
                })

                giveChestBuilder.then(uuidArg.executes { context ->
                    val player = context.source.playerOrException
                    giveChest(player, StringArgumentType.getString(context, "uuid"), context.source)
                })

                giveChestBuilder.executes { context ->
                    val player = context.source.playerOrException
                    giveChest(player, THIS, context.source)
                }

                builder.then(giveChestBuilder)
            }

            Commands.literal("time").requires { it.hasPermission(2) }.let { timeBuilder ->
                Commands.literal("set").let { setBuilder ->
                    setBuilder.then(Commands.literal("day").executes {
                        val uuidString = StringArgumentType.getString(it, "uuid")
                        setTime(uuidString, it.getSource(), 1000)
                    })

                    setBuilder.then(Commands.literal("noon").executes {
                        val uuidString = StringArgumentType.getString(it, "uuid")
                        setTime(uuidString, it.getSource(), 6000)
                    })

                    setBuilder.then(Commands.literal("night").executes {
                        val uuidString = StringArgumentType.getString(it, "uuid")
                        setTime(uuidString, it.getSource(), 13000)
                    })

                    setBuilder.then(Commands.literal("midnight").executes {
                        val uuidString = StringArgumentType.getString(it, "uuid")
                        setTime(uuidString, it.getSource(), 18000)
                    })

                    setBuilder.then(Commands.argument("time", TimeArgument.time()).executes {
                        val uuidString = StringArgumentType.getString(it, "uuid")
                        val time = IntegerArgumentType.getInteger(it, "time")
                        setTime(uuidString, it.getSource(), time)
                    })

                    timeBuilder.then(allDimsWithAllArg.then(setBuilder))
                }

                Commands.literal("set").let { setBuilder ->
                    setBuilder.then(Commands.literal("day").executes {
                        val level = DimensionArgument.getDimension(it, "dimension")
                        setTime(level, it.getSource(), 1000)
                    })

                    setBuilder.then(Commands.literal("noon").executes {
                        val level = DimensionArgument.getDimension(it, "dimension")
                        setTime(level, it.getSource(), 6000)
                    })

                    setBuilder.then(Commands.literal("night").executes {
                        val level = DimensionArgument.getDimension(it, "dimension")
                        setTime(level, it.getSource(), 13000)
                    })

                    setBuilder.then(Commands.literal("midnight").executes {
                        val level = DimensionArgument.getDimension(it, "dimension")
                        setTime(level, it.getSource(), 18000)
                    })

                    setBuilder.then(Commands.argument("time", TimeArgument.time()).executes {
                        val level = DimensionArgument.getDimension(it, "dimension")
                        val time = IntegerArgumentType.getInteger(it, "time")
                        setTime(level, it.getSource(), time)
                    })

                    timeBuilder.then(Commands.argument("dimension", DimensionArgument.dimension()).then(setBuilder))
                }

                Commands.literal("set").let { setBuilder ->
                    setBuilder.then(Commands.literal("day").executes {
                        setTime(THIS, it.getSource(), 1000)
                    })

                    setBuilder.then(Commands.literal("noon").executes {
                        setTime(THIS, it.getSource(), 6000)
                    })

                    setBuilder.then(Commands.literal("night").executes {
                        setTime(THIS, it.getSource(), 13000)
                    })

                    setBuilder.then(Commands.literal("midnight").executes {
                        setTime(THIS, it.getSource(), 18000)
                    })

                    setBuilder.then(Commands.argument("time", TimeArgument.time()).executes {
                        val time = IntegerArgumentType.getInteger(it, "time")
                        setTime(THIS, it.getSource(), time)
                    })

                    timeBuilder.then(setBuilder)
                }

                Commands.literal("add").let { addBuilder ->
                    addBuilder.then(Commands.argument("time", TimeArgument.time()).executes {
                        val uuidString = StringArgumentType.getString(it, "uuid")
                        val time = IntegerArgumentType.getInteger(it, "time")
                        addTime(uuidString, it.getSource(), time)
                    })

                    timeBuilder.then(allDimsWithAllArg.then(addBuilder))
                }

                Commands.literal("add").let { addBuilder ->
                    addBuilder.then(Commands.argument("time", TimeArgument.time()).executes {
                        val level = DimensionArgument.getDimension(it, "dimension")
                        val time = IntegerArgumentType.getInteger(it, "time")
                        addTime(level, it.getSource(), time)
                    })

                    timeBuilder.then(Commands.argument("dimension", DimensionArgument.dimension()).then(addBuilder))
                }

                Commands.literal("add").let { addBuilder ->
                    addBuilder.then(Commands.argument("time", TimeArgument.time()).executes {
                        val time = IntegerArgumentType.getInteger(it, "time")
                        addTime(THIS, it.getSource(), time)
                    })

                    timeBuilder.then(addBuilder)
                }

                Commands.literal("query").let { queryBuilder ->
                    queryBuilder.then(Commands.literal("daytime").executes {
                        val uuidString = StringArgumentType.getString(it, "uuid")
                        val level = getLevel(it.source, uuidString)
                        if (level == null) {
                            it.source.sendFailure(Component.literal("UUID is not valid."))
                            return@executes 0
                        }
                        queryTime(it.getSource(), getDayTime(level))
                    })

                    queryBuilder.then(Commands.literal("gametime").executes {
                        val uuidString = StringArgumentType.getString(it, "uuid")
                        val level = getLevel(it.source, uuidString)
                        if (level == null) {
                            it.source.sendFailure(Component.literal("UUID is not valid."))
                            return@executes 0
                        }
                        queryTime(it.getSource(), (level.gameTime % 2147483647L).toInt())
                    })

                    queryBuilder.then(Commands.literal("day").executes {
                        val uuidString = StringArgumentType.getString(it, "uuid")
                        val level = getLevel(it.source, uuidString)
                        if (level == null) {
                            it.source.sendFailure(Component.literal("UUID is not valid."))
                            return@executes 0
                        }
                        queryTime(it.getSource(), (level.dayTime / 24000L % 2147483647L).toInt())
                    })

                    timeBuilder.then(allDimsArg.then(queryBuilder))
                }

                Commands.literal("query").let { queryBuilder ->
                    queryBuilder.then(Commands.literal("daytime").executes {
                        val level = DimensionArgument.getDimension(it, "dimension")
                        queryTime(it.getSource(), getDayTime(level))
                    })

                    queryBuilder.then(Commands.literal("gametime").executes {
                        val level = DimensionArgument.getDimension(it, "dimension")
                        queryTime(it.getSource(), (level.gameTime % 2147483647L).toInt())
                    })

                    queryBuilder.then(Commands.literal("day").executes {
                        val level = DimensionArgument.getDimension(it, "dimension")
                        queryTime(it.getSource(), (level.dayTime / 24000L % 2147483647L).toInt())
                    })

                    timeBuilder.then(Commands.argument("dimension", DimensionArgument.dimension()).then(queryBuilder))
                }

                Commands.literal("query").let { queryBuilder ->
                    queryBuilder.then(Commands.literal("daytime").executes {
                        val level = getLevel(it.source, THIS)
                        if (level == null) {
                            it.source.sendFailure(Component.literal("UUID is not valid."))
                            return@executes 0
                        }
                        queryTime(it.getSource(), getDayTime(level))
                    })

                    queryBuilder.then(Commands.literal("gametime").executes {
                        val level = getLevel(it.source, THIS)
                        if (level == null) {
                            it.source.sendFailure(Component.literal("UUID is not valid."))
                            return@executes 0
                        }
                        queryTime(it.getSource(), (level.gameTime % 2147483647L).toInt())
                    })

                    queryBuilder.then(Commands.literal("day").executes {
                        val level = getLevel(it.source, THIS)
                        if (level == null) {
                            it.source.sendFailure(Component.literal("UUID is not valid."))
                            return@executes 0
                        }
                        queryTime(it.getSource(), (level.dayTime / 24000L % 2147483647L).toInt())
                    })

                    timeBuilder.then(queryBuilder)
                }
                builder.then(timeBuilder)
            }

            Commands.literal("weather").requires { it.hasPermission(2) }.let { weatherBuilder ->
                Commands.literal("clear").let { clearBuilder ->
                    clearBuilder.executes {
                        val uuidString = StringArgumentType.getString(it, "uuid")
                        setClear(uuidString, it.source, -1)
                    }

                    clearBuilder.then(Commands.argument("duration", TimeArgument.time(1)).executes {
                        val uuidString = StringArgumentType.getString(it, "uuid")
                        setClear(uuidString, it.source, IntegerArgumentType.getInteger(it, "duration"))
                    })

                    weatherBuilder.then(allDimsWithAllArg.then(clearBuilder))
                }

                Commands.literal("clear").let { clearBuilder ->
                    clearBuilder.executes {
                        val level = DimensionArgument.getDimension(it, "dimension")
                        setClear(level, it.source, -1)
                    }

                    clearBuilder.then(Commands.argument("duration", TimeArgument.time(1)).executes {
                        val level = DimensionArgument.getDimension(it, "dimension")
                        setClear(level, it.source, IntegerArgumentType.getInteger(it, "duration"))
                    })

                    weatherBuilder.then(
                        Commands.argument("dimension", DimensionArgument.dimension()).then(clearBuilder)
                    )
                }

                Commands.literal("clear").let { clearBuilder ->
                    clearBuilder.executes {
                        setClear(THIS, it.source, -1)
                    }

                    clearBuilder.then(Commands.argument("duration", TimeArgument.time(1)).executes {
                        setClear(THIS, it.source, IntegerArgumentType.getInteger(it, "duration"))
                    })

                    weatherBuilder.then(clearBuilder)
                }

                Commands.literal("rain").let { rainBuilder ->
                    rainBuilder.executes {
                        val uuidString = StringArgumentType.getString(it, "uuid")
                        setRain(uuidString, it.source, -1)
                    }

                    rainBuilder.then(Commands.argument("duration", TimeArgument.time(1)).executes {
                        val uuidString = StringArgumentType.getString(it, "uuid")
                        setRain(uuidString, it.source, IntegerArgumentType.getInteger(it, "duration"))
                    })

                    weatherBuilder.then(allDimsWithAllArg.then(rainBuilder))
                }

                Commands.literal("rain").let { rainBuilder ->
                    rainBuilder.executes {
                        val level = DimensionArgument.getDimension(it, "dimension")
                        setRain(level, it.source, -1)
                    }

                    rainBuilder.then(Commands.argument("duration", TimeArgument.time(1)).executes {
                        val level = DimensionArgument.getDimension(it, "dimension")
                        setRain(level, it.source, IntegerArgumentType.getInteger(it, "duration"))
                    })

                    weatherBuilder.then(Commands.argument("dimension", DimensionArgument.dimension()).then(rainBuilder))
                }

                Commands.literal("rain").let { rainBuilder ->
                    rainBuilder.executes {
                        setRain(THIS, it.source, -1)
                    }

                    rainBuilder.then(Commands.argument("duration", TimeArgument.time(1)).executes {
                        setRain(THIS, it.source, IntegerArgumentType.getInteger(it, "duration"))
                    })

                    weatherBuilder.then(rainBuilder)
                }

                Commands.literal("thunder").let { thunderBuilder ->
                    thunderBuilder.executes {
                        val uuidString = StringArgumentType.getString(it, "uuid")
                        setThunder(uuidString, it.source, -1)
                    }

                    thunderBuilder.then(Commands.argument("duration", TimeArgument.time(1)).executes {
                        val uuidString = StringArgumentType.getString(it, "uuid")
                        setThunder(uuidString, it.source, IntegerArgumentType.getInteger(it, "duration"))
                    })

                    weatherBuilder.then(allDimsWithAllArg.then(thunderBuilder))
                }

                Commands.literal("thunder").let { thunderBuilder ->
                    thunderBuilder.executes {
                        val level = DimensionArgument.getDimension(it, "dimension")
                        setThunder(level, it.source, -1)
                    }

                    thunderBuilder.then(Commands.argument("duration", TimeArgument.time(1)).executes {
                        val level = DimensionArgument.getDimension(it, "dimension")
                        setThunder(level, it.source, IntegerArgumentType.getInteger(it, "duration"))
                    })

                    weatherBuilder.then(
                        Commands.argument("dimension", DimensionArgument.dimension()).then(thunderBuilder)
                    )
                }

                Commands.literal("thunder").let { thunderBuilder ->
                    thunderBuilder.executes {
                        setThunder(THIS, it.source, -1)
                    }

                    thunderBuilder.then(Commands.argument("duration", TimeArgument.time(1)).executes {
                        setThunder(THIS, it.source, IntegerArgumentType.getInteger(it, "duration"))
                    })

                    weatherBuilder.then(thunderBuilder)
                }

                builder.then(weatherBuilder)
            }
            dispatcher.register(builder)
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

    private fun formatDouble(value: Double): String {
        return String.format(Locale.ROOT, "%f", value)
    }

    private fun getLevel(source: CommandSourceStack, dimension: String): ServerLevel? {
        if (dimension == THIS) return source.level

        runCatching { UUID.fromString(dimension) }.getOrNull()?.let { uuid ->
            return ChestLevelManager.getOrCreate(source.server, uuid)
        }

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