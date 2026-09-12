@file:Suppress("unused")

package io.github.ykysnk.chestdimension.level.levelgen

import com.google.common.collect.ImmutableList
import io.github.ykysnk.chestdimension.level.biome.Biomes
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.Noises
import net.minecraft.world.level.levelgen.SurfaceRules
import net.minecraft.world.level.levelgen.SurfaceRules.ConditionSource
import net.minecraft.world.level.levelgen.VerticalAnchor

object SurfaceRuleData {
    private val AIR = makeStateRule(Blocks.AIR)
    private val BEDROCK = makeStateRule(Blocks.BEDROCK)
    private val WHITE_TERRACOTTA = makeStateRule(Blocks.WHITE_TERRACOTTA)
    private val ORANGE_TERRACOTTA = makeStateRule(Blocks.ORANGE_TERRACOTTA)
    private val TERRACOTTA = makeStateRule(Blocks.TERRACOTTA)
    private val RED_SAND = makeStateRule(Blocks.RED_SAND)
    private val RED_SANDSTONE = makeStateRule(Blocks.RED_SANDSTONE)
    private val STONE = makeStateRule(Blocks.STONE)
    private val DEEPSLATE = makeStateRule(Blocks.DEEPSLATE)
    private val COBBLED_DEEPSLATE = makeStateRule(Blocks.COBBLED_DEEPSLATE)
    private val DIRT = makeStateRule(Blocks.DIRT)
    private val PODZOL = makeStateRule(Blocks.PODZOL)
    private val COARSE_DIRT = makeStateRule(Blocks.COARSE_DIRT)
    private val MYCELIUM = makeStateRule(Blocks.MYCELIUM)
    private val GRASS_BLOCK = makeStateRule(Blocks.GRASS_BLOCK)
    private val CALCITE = makeStateRule(Blocks.CALCITE)
    private val GRAVEL = makeStateRule(Blocks.GRAVEL)
    private val SAND = makeStateRule(Blocks.SAND)
    private val SANDSTONE = makeStateRule(Blocks.SANDSTONE)
    private val PACKED_ICE = makeStateRule(Blocks.PACKED_ICE)
    private val SNOW = makeStateRule(Blocks.SNOW)
    private val SNOW_BLOCK = makeStateRule(Blocks.SNOW_BLOCK)
    private val MUD = makeStateRule(Blocks.MUD)
    private val POWDER_SNOW = makeStateRule(Blocks.POWDER_SNOW)
    private val ICE = makeStateRule(Blocks.ICE)
    private val WATER = makeStateRule(Blocks.WATER)
    private val LAVA = makeStateRule(Blocks.LAVA)
    private val NETHERRACK = makeStateRule(Blocks.NETHERRACK)
    private val SOUL_SAND = makeStateRule(Blocks.SOUL_SAND)
    private val SOUL_SOIL = makeStateRule(Blocks.SOUL_SOIL)
    private val BASALT = makeStateRule(Blocks.BASALT)
    private val BLACKSTONE = makeStateRule(Blocks.BLACKSTONE)
    private val WARPED_WART_BLOCK = makeStateRule(Blocks.WARPED_WART_BLOCK)
    private val WARPED_NYLIUM = makeStateRule(Blocks.WARPED_NYLIUM)
    private val NETHER_WART_BLOCK = makeStateRule(Blocks.NETHER_WART_BLOCK)
    private val CRIMSON_NYLIUM = makeStateRule(Blocks.CRIMSON_NYLIUM)
    private val ENDSTONE = makeStateRule(Blocks.END_STONE)

    private fun makeStateRule(block: Block): SurfaceRules.RuleSource {
        return SurfaceRules.state(block.defaultBlockState())
    }

    private fun surfaceNoiseAbove(value: Double): ConditionSource {
        return SurfaceRules.noiseCondition(Noises.SURFACE, value / 8.25, Double.MAX_VALUE)
    }

    fun undefined(bl: Boolean, bedrockRoof: Boolean, bedrockFloor: Boolean): SurfaceRules.RuleSource {
        val conditionSource8 = SurfaceRules.waterBlockCheck(-1, 0)
        val conditionSource9 = SurfaceRules.waterBlockCheck(0, 0)
        val conditionSource10 = SurfaceRules.waterStartCheck(-6, -1)
        // ground rule
        val ruleSource = SurfaceRules.sequence(
            SurfaceRules.ifTrue(
                SurfaceRules.isBiome(Biomes.CHEST_UNDEFINED_BIOME), SurfaceRules.ifTrue(conditionSource9, COARSE_DIRT)
            ), DIRT
        )
        val ruleSource3 = SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.ON_CEILING, STONE), GRAVEL)
        val ruleSource7 = SurfaceRules.sequence(DIRT)
        val ruleSource8 = SurfaceRules.sequence(ruleSource)
        val ruleSource9 = SurfaceRules.sequence(
            SurfaceRules.ifTrue(
                SurfaceRules.ON_FLOOR,
                SurfaceRules.ifTrue(conditionSource8, SurfaceRules.sequence(ruleSource8))
            ),
            SurfaceRules.ifTrue(
                conditionSource10,
                SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, ruleSource7))
            ),
            SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, SurfaceRules.sequence(ruleSource3))
        )
        val builder = ImmutableList.builder<SurfaceRules.RuleSource>()
        if (bedrockRoof)
            builder.add(
                SurfaceRules.ifTrue(
                    SurfaceRules.not(
                        SurfaceRules.verticalGradient(
                            "bedrock_roof",
                            VerticalAnchor.belowTop(5),
                            VerticalAnchor.top()
                        )
                    ), BEDROCK
                )
            )

        if (bedrockFloor)
            builder.add(
                SurfaceRules.ifTrue(
                    SurfaceRules.verticalGradient(
                        "bedrock_floor",
                        VerticalAnchor.bottom(),
                        VerticalAnchor.aboveBottom(5)
                    ), BEDROCK
                )
            )

        val ruleSource10 = SurfaceRules.ifTrue(SurfaceRules.abovePreliminarySurface(), ruleSource9)
        builder.add(if (bl) ruleSource10 else ruleSource9)
        builder.add(
            SurfaceRules.ifTrue(
                SurfaceRules.verticalGradient(
                    "deepslate",
                    VerticalAnchor.absolute(-64),
                    VerticalAnchor.absolute(-56)
                ), COBBLED_DEEPSLATE
            )
        )

        return SurfaceRules.sequence(*builder.build().toTypedArray())
    }
}