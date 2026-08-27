package io.github.ykysnk.chestdimension.extensions

import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

inline fun ArgumentBuilder<CommandSourceStack, *>.literal(
    name: String,
    vararg parents: ArgumentBuilder<CommandSourceStack, *>,
    block: LiteralArgumentBuilder<CommandSourceStack>.() -> Unit
) {
    var builder: ArgumentBuilder<CommandSourceStack, *> = Commands.literal(name).apply(block)
    for (parent in parents) builder = parent.then(builder)
    then(builder)
}