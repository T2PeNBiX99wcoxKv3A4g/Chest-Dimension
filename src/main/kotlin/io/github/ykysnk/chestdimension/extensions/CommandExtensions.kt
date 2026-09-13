package io.github.ykysnk.chestdimension.extensions

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.github.ykysnk.chestdimension.Constants
import io.github.ykysnk.chestdimension.level.UUIDManager
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.UuidArgument
import java.util.*

inline fun ArgumentBuilder<CommandSourceStack, *>.literal(
    name: String,
    block: LiteralArgumentBuilder<CommandSourceStack>.() -> Unit
): ArgumentBuilder<CommandSourceStack, *> = then(Commands.literal(name).apply(block))

inline fun <T> ArgumentBuilder<CommandSourceStack, *>.argument(
    name: String,
    type: ArgumentType<T>,
    block: RequiredArgumentBuilder<CommandSourceStack, T>.() -> Unit
): ArgumentBuilder<CommandSourceStack, *> = then(Commands.argument(name, type).apply(block))

inline fun <T> ArgumentBuilder<T, *>.executesLogError(crossinline command: (CommandContext<T>) -> Int): ArgumentBuilder<T, *> =
    executes { context ->
        runCatching { command(context) }.onFailure {
            Constants.LOGGER.error("Failed to execute command: {}", context.input, it)
        }.getOrThrow()
    }

internal inline fun ArgumentBuilder<CommandSourceStack, *>.uuidArg(block: RequiredArgumentBuilder<CommandSourceStack, UUID>.() -> Unit) =
    argument("uuid", UuidArgument.uuid()) {
        suggests { _, builder ->
            val tempSet = hashSetOf<String>()
            UUIDManager.getMap().keys.forEach(tempSet::add)
            UUIDManager.getInactiveList().forEach(tempSet::add)
            tempSet.forEach(builder::suggest)
            builder.buildFuture()
        }

        apply(block)
    }

internal inline fun ArgumentBuilder<CommandSourceStack, *>.uuidArgOnlyExist(block: RequiredArgumentBuilder<CommandSourceStack, UUID>.() -> Unit) =
    argument("uuid", UuidArgument.uuid()) {
        suggests { _, builder ->
            val tempSet = hashSetOf<String>()
            val inactives = UUIDManager.getInactiveList()
            UUIDManager.getMap().keys.forEach {
                if (it !in inactives) tempSet.add(it)
            }
            tempSet.forEach(builder::suggest)
            builder.buildFuture()
        }

        apply(block)
    }

internal inline fun ArgumentBuilder<CommandSourceStack, *>.allDimsArg(block: RequiredArgumentBuilder<CommandSourceStack, String>.() -> Unit) =
    argument("dimension", StringArgumentType.string()) {
        suggests { context, builder ->
            val server = context.source.server
            server.allLevels.forEach {
                builder.suggest("\"${it.dimension().location()}\"")
            }
            builder.buildFuture()
        }

        apply(block)
    }