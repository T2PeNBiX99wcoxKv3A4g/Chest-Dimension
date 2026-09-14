@file:Suppress("unused")

package io.github.ykysnk.chestdimension.level.levelgen

import net.minecraft.world.level.levelgen.SurfaceRules

// Credits: ChatGPT
@DslMarker
annotation class SurfaceRuleDsl

@SurfaceRuleDsl
class SurfaceRuleBuilder {
    private val rules = mutableListOf<SurfaceRules.RuleSource>()

    fun sequence(block: SurfaceRuleBuilder.() -> Unit) {
        val builder = SurfaceRuleBuilder()
        builder.block()

        rules += SurfaceRules.sequence(*builder.rules.toTypedArray())
    }

    fun ifTrue(condition: SurfaceRules.ConditionSource, block: SurfaceRuleBuilder.() -> Unit) {
        val builder = SurfaceRuleBuilder()
        builder.block()

        rules += SurfaceRules.ifTrue(condition, builder.build())
    }

    fun not(condition: SurfaceRules.ConditionSource): SurfaceRules.ConditionSource = SurfaceRules.not(condition)

    /*
     * Allows:
     *
     *     COARSE_DIRT
     *     DIRT
     *     GRAVEL
     *
     * directly inside the DSL.
     */
    operator fun SurfaceRules.RuleSource.unaryPlus() {
        rules += this
    }

    internal fun build(): SurfaceRules.RuleSource = SurfaceRules.sequence(*rules.toTypedArray())
}

fun surfaceRules(block: SurfaceRuleBuilder.() -> Unit): SurfaceRules.RuleSource {
    val builder = SurfaceRuleBuilder()
    builder.block()
    return builder.build()
}