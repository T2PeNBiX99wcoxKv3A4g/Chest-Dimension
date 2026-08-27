package io.github.ykysnk.chestdimension.extensions

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import io.github.ykysnk.chestdimension.command.ChestDimCommand.ALL
import io.github.ykysnk.chestdimension.command.ChestDimCommand.THIS
import io.github.ykysnk.chestdimension.level.ChestLevelManager
import io.github.ykysnk.chestdimension.level.UUIDManager
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

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

internal inline fun ArgumentBuilder<CommandSourceStack, *>.uuidArg(block: RequiredArgumentBuilder<CommandSourceStack, String>.() -> Unit) =
    argument("uuid", StringArgumentType.string()) {
        suggests { _, builder ->
            val tempSet = hashSetOf<String>()
            UUIDManager.getMap().keys.forEach(tempSet::add)
            UUIDManager.getInactiveList().forEach(tempSet::add)

            tempSet.forEach(builder::suggest)
            builder.suggest(THIS)
            builder.buildFuture()
        }

        apply(block)
    }

internal inline fun ArgumentBuilder<CommandSourceStack, *>.allDimsArg(block: RequiredArgumentBuilder<CommandSourceStack, String>.() -> Unit) =
    argument("uuid", StringArgumentType.string()) {
        suggests { context, builder ->
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

        apply(block)
    }

internal inline fun ArgumentBuilder<CommandSourceStack, *>.allDimsWithAllArg(block: RequiredArgumentBuilder<CommandSourceStack, String>.() -> Unit) =
    argument("uuid", StringArgumentType.string()) {
        suggests { context, builder ->
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

        apply(block)
    }