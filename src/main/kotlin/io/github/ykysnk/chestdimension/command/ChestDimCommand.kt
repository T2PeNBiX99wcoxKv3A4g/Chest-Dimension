package io.github.ykysnk.chestdimension.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import io.github.ykysnk.chestdimension.Constants
import io.github.ykysnk.chestdimension.extensions.teleportToLevel
import io.github.ykysnk.chestdimension.level.ChestLevelManager
import io.github.ykysnk.chestdimension.level.UUIDManager
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.DimensionArgument
import net.minecraft.commands.arguments.coordinates.Vec3Argument
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.phys.Vec3
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
                    it.withUnderlined(true).withClickEvent(ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, uuid.toString()))
                        .withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to copy UUID")))
                }
                val world = ChestLevelManager.getOrCreate(context.source.server, uuid)
                player.teleportToLevel(world, Vec3(0.5,1.0,0.5))
                context.source.sendSuccess({ Component.literal("Created world: ").append(uuidComponent) }, true)
                1
            }.getOrElse {
                Constants.LOGGER.error(it.localizedMessage, it)
                context.source.sendFailure(Component.literal("Command error (${it.localizedMessage})"))
                0
            }
        })

        val builder2 = Commands.literal("tp")

        for ((uuid) in UUIDManager.get()) {
            builder2.then(
                Commands.literal(uuid).then(Commands.argument("pos", Vec3Argument.vec3()).executes { context ->
                    val player = context.source.playerOrException
                    val pos = Vec3Argument.getVec3(context, "pos")
                    val uuid = runCatching { UUID.fromString(uuid) }.getOrElse {
                        Constants.LOGGER.error(it.localizedMessage, it)
                        context.source.sendFailure(Component.literal("UUID is not valid."))
                        return@executes 0
                    }
                    val level = ChestLevelManager.getOrCreate(context.source.server, uuid)
                    player.teleportToLevel(level, pos)
                    1
                })
            )
        }

        builder.then(builder2)

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
            Commands.literal("tp-raw").then(
                Commands.argument("uuid", StringArgumentType.string())
                    .then(Commands.argument("pos", Vec3Argument.vec3()).executes { context ->
                        val player = context.source.playerOrException
                        val name = StringArgumentType.getString(context, "uuid")
                        val uuid = runCatching { UUID.fromString(name) }.getOrElse {
                            Constants.LOGGER.error(it.localizedMessage, it)
                            context.source.sendFailure(Component.literal("UUID is not valid."))
                            return@executes 0
                        }
                        val level = ChestLevelManager.getOrCreate(context.source.server, uuid)
                        val pos = Vec3Argument.getVec3(context, "pos")
                        player.teleportToLevel(level, pos)
                        1
                    })
            )
        )

        dispatcher.register(builder)
    }
}