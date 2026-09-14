@file:Suppress("unused")

package io.github.ykysnk.chestdimension.level.levelgen

import com.google.common.collect.ImmutableList
import io.github.ykysnk.chestdimension.block.Blocks
import io.github.ykysnk.chestdimension.level.biome.Biomes
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.levelgen.Noises
import net.minecraft.world.level.levelgen.SurfaceRules
import net.minecraft.world.level.levelgen.SurfaceRules.ConditionSource
import net.minecraft.world.level.levelgen.VerticalAnchor
import net.minecraft.world.level.block.Blocks as MCBlocks

object SurfaceRuleData {
    private val AIR = makeStateRule(MCBlocks.AIR)
    private val BEDROCK = makeStateRule(MCBlocks.BEDROCK)
    private val WHITE_TERRACOTTA = makeStateRule(MCBlocks.WHITE_TERRACOTTA)
    private val ORANGE_TERRACOTTA = makeStateRule(MCBlocks.ORANGE_TERRACOTTA)
    private val TERRACOTTA = makeStateRule(MCBlocks.TERRACOTTA)
    private val RED_SAND = makeStateRule(MCBlocks.RED_SAND)
    private val RED_SANDSTONE = makeStateRule(MCBlocks.RED_SANDSTONE)
    private val STONE = makeStateRule(MCBlocks.STONE)
    private val DEEPSLATE = makeStateRule(MCBlocks.DEEPSLATE)
    private val COBBLED_DEEPSLATE = makeStateRule(MCBlocks.COBBLED_DEEPSLATE)
    private val DIRT = makeStateRule(MCBlocks.DIRT)
    private val PODZOL = makeStateRule(MCBlocks.PODZOL)
    private val COARSE_DIRT = makeStateRule(MCBlocks.COARSE_DIRT)
    private val MYCELIUM = makeStateRule(MCBlocks.MYCELIUM)
    private val GRASS_BLOCK = makeStateRule(MCBlocks.GRASS_BLOCK)
    private val CALCITE = makeStateRule(MCBlocks.CALCITE)
    private val GRAVEL = makeStateRule(MCBlocks.GRAVEL)
    private val SAND = makeStateRule(MCBlocks.SAND)
    private val SANDSTONE = makeStateRule(MCBlocks.SANDSTONE)
    private val PACKED_ICE = makeStateRule(MCBlocks.PACKED_ICE)
    private val SNOW = makeStateRule(MCBlocks.SNOW)
    private val SNOW_BLOCK = makeStateRule(MCBlocks.SNOW_BLOCK)
    private val MUD = makeStateRule(MCBlocks.MUD)
    private val POWDER_SNOW = makeStateRule(MCBlocks.POWDER_SNOW)
    private val ICE = makeStateRule(MCBlocks.ICE)
    private val WATER = makeStateRule(MCBlocks.WATER)
    private val LAVA = makeStateRule(MCBlocks.LAVA)
    private val NETHERRACK = makeStateRule(MCBlocks.NETHERRACK)
    private val SOUL_SAND = makeStateRule(MCBlocks.SOUL_SAND)
    private val SOUL_SOIL = makeStateRule(MCBlocks.SOUL_SOIL)
    private val BASALT = makeStateRule(MCBlocks.BASALT)
    private val BLACKSTONE = makeStateRule(MCBlocks.BLACKSTONE)
    private val WARPED_WART_BLOCK = makeStateRule(MCBlocks.WARPED_WART_BLOCK)
    private val WARPED_NYLIUM = makeStateRule(MCBlocks.WARPED_NYLIUM)
    private val NETHER_WART_BLOCK = makeStateRule(MCBlocks.NETHER_WART_BLOCK)
    private val CRIMSON_NYLIUM = makeStateRule(MCBlocks.CRIMSON_NYLIUM)
    private val ENDSTONE = makeStateRule(MCBlocks.END_STONE)

    private val NULL = makeStateRule(Blocks.NULL)

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
                conditionSource9, SurfaceRules.sequence(
                    SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.GRAVEYARD), COARSE_DIRT),
                    SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.NULL), NULL)
                )
            ),
            DIRT
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