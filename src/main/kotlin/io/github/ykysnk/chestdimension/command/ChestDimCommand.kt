package io.github.ykysnk.chestdimension.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import io.github.ykysnk.chestdimension.Constants
import io.github.ykysnk.chestdimension.block.entity.BlockEntityTypes
import io.github.ykysnk.chestdimension.extensions.teleportToLevel
import io.github.ykysnk.chestdimension.item.Items
import io.github.ykysnk.chestdimension.level.ChestLevelManager
import io.github.ykysnk.chestdimension.level.UUIDManager
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.DimensionArgument
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.TimeArgument
import net.minecraft.commands.arguments.coordinates.Vec3Argument
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack
import java.util.*

object ChestDimCommand {
    private const val THIS = "this"
    private const val ALL = "*"

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        val builder = Commands.literal("chestdim").requires { it.hasPermission(2) }

        builder.then(Commands.literal("create").executes { context ->
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
        })

        builder.then(
            Commands.literal("tp")
                .then(uuidArg.then(Commands.argument("pos", Vec3Argument.vec3()).executes { context ->
                    val player = context.source.playerOrException
                    val pos = Vec3Argument.getVec3(context, "pos")
                    when (val uuidString = StringArgumentType.getString(context, "uuid")) {
                        THIS -> {
                            val uuid = ChestLevelManager.findUUIDByLevel(context.source.level)
                            if (uuid == null) {
                                context.source.sendFailure(Component.literal("Can't find any UUID using this level."))
                                return@executes 0
                            }
                            val level = ChestLevelManager.getOrCreate(context.source.server, uuid)
                            player.teleportToLevel(level, pos)
                            1
                        }

                        else -> {
                            val uuid = runCatching { UUID.fromString(uuidString) }.getOrElse {
                                Constants.LOGGER.error(it.localizedMessage, it)
                                context.source.sendFailure(Component.literal("UUID is not valid."))
                                return@executes 0
                            }
                            val level = ChestLevelManager.getOrCreate(context.source.server, uuid)
                            player.teleportToLevel(level, pos)
                            1
                        }
                    }
                }))
        )

        builder.then(
            Commands.literal("tp-default").then(
                Commands.argument(
                    "dimension", DimensionArgument.dimension()
                ).then(Commands.argument("pos", Vec3Argument.vec3()).executes { context ->
                    val player = context.source.playerOrException
                    val level = DimensionArgument.getDimension(context, "dimension")
                    if (level == null) {
                        context.source.sendFailure(Component.literal("Dimension is not loaded."))
                        return@executes 0
                    }
                    val pos = Vec3Argument.getVec3(context, "pos")
                    player.teleportToLevel(level, pos)
                    1
                })
            )
        )

        builder.then(
            Commands.literal("give-chest")
                .then(uuidArg.then(Commands.argument("player", EntityArgument.player()).executes { context ->
                    val player = EntityArgument.getPlayer(context, "player")
                        ?: throw CommandSourceStack.ERROR_NOT_PLAYER.create()
                    when (val uuidString = StringArgumentType.getString(context, "uuid")) {
                        THIS -> {
                            val uuid = ChestLevelManager.findUUIDByLevel(context.source.level)
                            if (uuid == null) {
                                context.source.sendFailure(Component.literal("Can't find any UUID using this level."))
                                return@executes 0
                            }
                            player.addItem(createChestItem(uuid))
                            1
                        }

                        else -> {
                            val uuid = runCatching { UUID.fromString(uuidString) }.getOrElse {
                                Constants.LOGGER.error(it.localizedMessage, it)
                                context.source.sendFailure(Component.literal("UUID is not valid."))
                                return@executes 0
                            }
                            player.addItem(createChestItem(uuid))
                            1
                        }
                    }
                }))
        )

        val timeBuilder = Commands.literal("time")
        val setBuilder = Commands.literal("set")

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

        val addBuilder = Commands.literal("add")

        addBuilder.then(Commands.argument("time", TimeArgument.time()).executes {
            val uuidString = StringArgumentType.getString(it, "uuid")
            val time = IntegerArgumentType.getInteger(it, "time")
            addTime(uuidString, it.getSource(), time)
        })

        val queryBuilder = Commands.literal("query")

        queryBuilder.then(Commands.literal("daytime").executes {
            when (val uuidString = StringArgumentType.getString(it, "uuid")) {
                THIS -> {
                    val uuid = ChestLevelManager.findUUIDByLevel(it.source.level)
                    if (uuid == null) {
                        it.source.sendFailure(Component.literal("Can't find any UUID using this level."))
                        return@executes 0
                    }
                    val level = ChestLevelManager.getOrCreate(it.source.server, uuid)
                    queryTime(it.getSource(), getDayTime(level))
                }

                else -> {
                    val uuid = runCatching { UUID.fromString(uuidString) }.getOrElse { exception ->
                        Constants.LOGGER.error(exception.localizedMessage, exception)
                        it.source.sendFailure(Component.literal("UUID is not valid."))
                        return@executes 0
                    }
                    val level = ChestLevelManager.getOrCreate(it.source.server, uuid)
                    queryTime(it.getSource(), getDayTime(level))
                }
            }
        })

        queryBuilder.then(Commands.literal("gametime").executes {
            when (val uuidString = StringArgumentType.getString(it, "uuid")) {
                THIS -> {
                    val uuid = ChestLevelManager.findUUIDByLevel(it.source.level)
                    if (uuid == null) {
                        it.source.sendFailure(Component.literal("Can't find any UUID using this level."))
                        return@executes 0
                    }
                    val level = ChestLevelManager.getOrCreate(it.source.server, uuid)
                    queryTime(it.getSource(), (level.gameTime % 2147483647L).toInt())
                }

                else -> {
                    val uuid = runCatching { UUID.fromString(uuidString) }.getOrElse { exception ->
                        Constants.LOGGER.error(exception.localizedMessage, exception)
                        it.source.sendFailure(Component.literal("UUID is not valid."))
                        return@executes 0
                    }
                    val level = ChestLevelManager.getOrCreate(it.source.server, uuid)
                    queryTime(it.getSource(), (level.gameTime % 2147483647L).toInt())
                }
            }
        })

        queryBuilder.then(Commands.literal("day").executes {
            when (val uuidString = StringArgumentType.getString(it, "uuid")) {
                THIS -> {
                    val uuid = ChestLevelManager.findUUIDByLevel(it.source.level)
                    if (uuid == null) {
                        it.source.sendFailure(Component.literal("Can't find any UUID using this level."))
                        return@executes 0
                    }
                    val level = ChestLevelManager.getOrCreate(it.source.server, uuid)
                    queryTime(it.getSource(), (level.dayTime / 24000L % 2147483647L).toInt())
                }

                else -> {
                    val uuid = runCatching { UUID.fromString(uuidString) }.getOrElse { exception ->
                        Constants.LOGGER.error(exception.localizedMessage, exception)
                        it.source.sendFailure(Component.literal("UUID is not valid."))
                        return@executes 0
                    }
                    val level = ChestLevelManager.getOrCreate(it.source.server, uuid)
                    queryTime(it.getSource(), (level.dayTime / 24000L % 2147483647L).toInt())
                }
            }
        })

        timeBuilder.then(uuidWithAllArg.then(setBuilder))
        timeBuilder.then(uuidWithAllArg.then(addBuilder))
        timeBuilder.then(uuidArg.then(queryBuilder))
        builder.then(timeBuilder)
        dispatcher.register(builder)
    }

    private val uuidArg: RequiredArgumentBuilder<CommandSourceStack, String>
        get() = Commands.argument("uuid", StringArgumentType.word()).suggests { _, builder ->
            val tempSet = hashSetOf<String>()
            UUIDManager.getMap().keys.forEach(tempSet::add)
            UUIDManager.getInactiveList().forEach(tempSet::add)
            tempSet.forEach(builder::suggest)
            builder.suggest(THIS)
            builder.buildFuture()
        }

    private val uuidWithAllArg: RequiredArgumentBuilder<CommandSourceStack, String>
        get() = Commands.argument("uuid", StringArgumentType.word()).suggests { _, builder ->
            val tempSet = hashSetOf<String>()
            UUIDManager.getMap().keys.forEach(tempSet::add)
            UUIDManager.getInactiveList().forEach(tempSet::add)
            tempSet.forEach(builder::suggest)
            builder.suggest(THIS)
            builder.suggest(ALL)
            builder.buildFuture()
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
            THIS -> {
                val uuid = ChestLevelManager.findUUIDByLevel(source.level)
                if (uuid == null) {
                    source.sendFailure(Component.literal("Can't find any UUID using this level."))
                    return 0
                }
                val level = ChestLevelManager.getOrCreate(source.server, uuid)
                level.dayTime = time.toLong()
                source.sendSuccess({ Component.translatable("commands.time.set", time) }, true)
                return getDayTime(level)
            }

            ALL -> {
                for (serverLevel in source.server.allLevels) serverLevel.dayTime = time.toLong()

                source.sendSuccess({ Component.translatable("commands.time.set", time) }, true)
                return getDayTime(source.level)
            }

            else -> {
                val uuid = runCatching { UUID.fromString(uuidString) }.getOrElse {
                    Constants.LOGGER.error(it.localizedMessage, it)
                    source.sendFailure(Component.literal("UUID is not valid."))
                    return 0
                }
                val level = ChestLevelManager.getOrCreate(source.server, uuid)
                level.dayTime = time.toLong()
                source.sendSuccess({ Component.translatable("commands.time.set", time) }, true)
                return getDayTime(level)
            }
        }
    }

    private fun addTime(uuidString: String, source: CommandSourceStack, amount: Int): Int {
        when (uuidString) {
            THIS -> {
                val uuid = ChestLevelManager.findUUIDByLevel(source.level)
                if (uuid == null) {
                    source.sendFailure(Component.literal("Can't find any UUID using this level."))
                    return 0
                }
                val level = ChestLevelManager.getOrCreate(source.server, uuid)
                level.dayTime += amount.toLong()
                val i = getDayTime(level)
                source.sendSuccess({ Component.translatable("commands.time.set", i) }, true)
                return i
            }

            ALL -> {
                for (serverLevel in source.server.allLevels) {
                    serverLevel.dayTime += amount.toLong()
                }

                val i = getDayTime(source.level)
                source.sendSuccess({ Component.translatable("commands.time.set", i) }, true)
                return i
            }

            else -> {
                val uuid = runCatching { UUID.fromString(uuidString) }.getOrElse {
                    Constants.LOGGER.error(it.localizedMessage, it)
                    source.sendFailure(Component.literal("UUID is not valid."))
                    return 0
                }
                val level = ChestLevelManager.getOrCreate(source.server, uuid)
                level.dayTime += amount.toLong()
                val i = getDayTime(level)
                source.sendSuccess({ Component.translatable("commands.time.set", i) }, true)
                return i
            }
        }
    }
}