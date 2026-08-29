package io.github.ykysnk.chestdimension.extensions

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import io.github.ykysnk.chestdimension.level.UUIDManager
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.UuidArgument
import java.util.*

inline fun ArgumentBuilder<CommandSourceStack, *>.literal(
    name: String,
    block: LiteralArgumentBuilder<CommandSourceStack>.() -> Unit
) {
    then(Commands.literal(name).apply(block))
}

inline fun <T> ArgumentBuilder<CommandSourceStack, *>.argument(
    name: String,
    type: ArgumentType<T>,
    block: RequiredArgumentBuilder<CommandSourceStack, T>.() -> Unit
) {
    then(Commands.argument(name, type).apply(block))
}

internal inline fun ArgumentBuilder<CommandSourceStack, *>.uuidArg(block: RequiredArgumentBuilder<CommandSourceStack, UUID>.() -> Unit) =
    argument("uuid", UuidArgument.uuid()) {
        suggests { _, builder ->
            val tempSet = hashSetOf<String>()
            UUIDManager.getMap().keys.forEach(tempSet::add)
            UUIDManager.getInactiveMap().values.forEach { it.forEach(tempSet::add) }
            tempSet.forEach(builder::suggest)
            builder.buildFuture()
        }

        apply(block)
    }

internal inline fun ArgumentBuilder<CommandSourceStack, *>.uuidArgOnlyExist(block: RequiredArgumentBuilder<CommandSourceStack, UUID>.() -> Unit) =
    argument("uuid", UuidArgument.uuid()) {
        suggests { _, builder ->
            val tempSet = hashSetOf<String>()
            UUIDManager.getMap().keys.forEach(tempSet::add)
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