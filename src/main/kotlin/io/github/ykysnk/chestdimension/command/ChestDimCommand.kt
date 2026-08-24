package io.github.ykysnk.chestdimension.command

import com.mojang.brigadier.CommandDispatcher
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
import net.minecraft.commands.arguments.coordinates.Vec3Argument
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack
import java.util.*

object ChestDimCommand {
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        val builder = Commands.literal("chestdim").requires { it.hasPermission(2) }

        builder.then(Commands.literal("create").executes { context ->
            runCatching {
                val player = context.source.playerOrException
                val uuid = runCatching { UUIDManager.randomUUID() }.getOrElse {
                    Constants.LOGGER.error(it.localizedMessage, it)
                    context.source.sendFailure(Component.literal("Invalid UUID"))
                    return@executes 0
                }

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
                    val uuidString = StringArgumentType.getString(context, "uuid")
                    val uuid = runCatching { UUID.fromString(uuidString) }.getOrElse {
                        Constants.LOGGER.error(it.localizedMessage, it)
                        context.source.sendFailure(Component.literal("UUID is not valid."))
                        return@executes 0
                    }
                    val level = ChestLevelManager.getOrCreate(context.source.server, uuid)
                    player.teleportToLevel(level, pos)
                    1
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
                    val uuidString = StringArgumentType.getString(context, "uuid")
                    val uuid = runCatching { UUID.fromString(uuidString) }.getOrElse {
                        Constants.LOGGER.error(it.localizedMessage, it)
                        context.source.sendFailure(Component.literal("UUID is not valid."))
                        return@executes 0
                    }
                    player.addItem(createChestItem(uuid))
                    1
                }))
        )

        dispatcher.register(builder)
    }

    private val uuidArg: RequiredArgumentBuilder<CommandSourceStack, String> by lazy {
        Commands.argument("uuid", StringArgumentType.word()).suggests { _, builder ->
            UUIDManager.getMap().keys.forEach(builder::suggest)
            builder.buildFuture()
        }
    }

    private fun createChestItem(uuid: UUID): ItemStack {
        val stack = ItemStack(Items.CHEST_DIMENSION)
        val tag = CompoundTag()
        tag.putUUID("UUID", uuid)
        BlockItem.setBlockEntityData(stack, BlockEntityTypes.CHEST_DIMENSION, tag)
        return stack
    }
}