@file:Suppress("SameParameterValue", "unused")

package io.github.ykysnk.chestdimension.level.levelgen

import net.minecraft.core.HolderGetter
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.levelgen.*
import net.minecraft.world.level.levelgen.DensityFunctions.HolderHolder
import net.minecraft.world.level.levelgen.NoiseRouterData
import net.minecraft.world.level.levelgen.synth.NormalNoise.NoiseParameters

// Form net.minecraft.world.level.levelgen.NoiseRouterData
object NoiseRouterData {
    private const val ORE_THICKNESS = 0.08f
    private const val VEININESS_FREQUENCY = 1.5
    private const val NOODLE_SPACING_AND_STRAIGHTNESS = 1.5
    private const val SURFACE_DENSITY_THRESHOLD = 1.5625
    private const val CHEESE_NOISE_TARGET = -0.703125
    private val BLENDING_FACTOR: DensityFunction = DensityFunctions.constant(10.0)
    private val BLENDING_JAGGEDNESS: DensityFunction = DensityFunctions.zero()
    private val ZERO: ResourceKey<DensityFunction> = createKey("zero")
    private val Y: ResourceKey<DensityFunction> = createKey("y")
    private val SHIFT_X: ResourceKey<DensityFunction> = createKey("shift_x")
    private val SHIFT_Z: ResourceKey<DensityFunction> = createKey("shift_z")
    private val BASE_3D_NOISE_OVERWORLD: ResourceKey<DensityFunction> = createKey("overworld/base_3d_noise")
    private val BASE_3D_NOISE_NETHER: ResourceKey<DensityFunction> = createKey("nether/base_3d_noise")
    private val BASE_3D_NOISE_END: ResourceKey<DensityFunction> = createKey("end/base_3d_noise")
    private val SLOPED_CHEESE: ResourceKey<DensityFunction> = createKey("overworld/sloped_cheese")
    private val OFFSET_LARGE: ResourceKey<DensityFunction> = createKey("overworld_large_biomes/offset")
    private val FACTOR_LARGE: ResourceKey<DensityFunction> = createKey("overworld_large_biomes/factor")
    private val JAGGEDNESS_LARGE: ResourceKey<DensityFunction> = createKey("overworld_large_biomes/jaggedness")
    private val DEPTH_LARGE: ResourceKey<DensityFunction> = createKey("overworld_large_biomes/depth")
    private val SLOPED_CHEESE_LARGE: ResourceKey<DensityFunction> = createKey("overworld_large_biomes/sloped_cheese")
    private val OFFSET_AMPLIFIED: ResourceKey<DensityFunction> = createKey("overworld_amplified/offset")
    private val FACTOR_AMPLIFIED: ResourceKey<DensityFunction> = createKey("overworld_amplified/factor")
    private val JAGGEDNESS_AMPLIFIED: ResourceKey<DensityFunction> = createKey("overworld_amplified/jaggedness")
    private val DEPTH_AMPLIFIED: ResourceKey<DensityFunction> = createKey("overworld_amplified/depth")
    private val SLOPED_CHEESE_AMPLIFIED: ResourceKey<DensityFunction> = createKey("overworld_amplified/sloped_cheese")
    private val SLOPED_CHEESE_END: ResourceKey<DensityFunction> = createKey("end/sloped_cheese")
    private val SPAGHETTI_ROUGHNESS_FUNCTION: ResourceKey<DensityFunction> =
        createKey("overworld/caves/spaghetti_roughness_function")
    private val ENTRANCES: ResourceKey<DensityFunction> = createKey("overworld/caves/entrances")
    private val NOODLE: ResourceKey<DensityFunction> = createKey("overworld/caves/noodle")
    private val PILLARS: ResourceKey<DensityFunction> = createKey("overworld/caves/pillars")
    private val SPAGHETTI_2D_THICKNESS_MODULATOR: ResourceKey<DensityFunction> =
        createKey("overworld/caves/spaghetti_2d_thickness_modulator")
    private val SPAGHETTI_2D: ResourceKey<DensityFunction> = createKey("overworld/caves/spaghetti_2d")

    private fun underground(
        densityFunctions: HolderGetter<DensityFunction>,
        noiseParameters: HolderGetter<NoiseParameters>,
        densityFunction: DensityFunction
    ): DensityFunction {
        val densityFunction2 = getFunction(densityFunctions, SPAGHETTI_2D)
        val densityFunction3 =
            getFunction(densityFunctions, SPAGHETTI_ROUGHNESS_FUNCTION)
        val densityFunction4 = DensityFunctions.noise(noiseParameters.getOrThrow(Noises.CAVE_LAYER), 8.0)
        val densityFunction5 = DensityFunctions.mul(DensityFunctions.constant(4.0), densityFunction4.square())
        val densityFunction6 =
            DensityFunctions.noise(noiseParameters.getOrThrow(Noises.CAVE_CHEESE), 0.6666666666666666)
        val densityFunction7 = DensityFunctions.add(
            DensityFunctions.add(DensityFunctions.constant(0.27), densityFunction6).clamp(-1.0, 1.0),
            DensityFunctions.add(
                DensityFunctions.constant(1.5),
                DensityFunctions.mul(DensityFunctions.constant(-0.64), densityFunction)
            ).clamp(0.0, 0.5)
        )
        val densityFunction8 = DensityFunctions.add(densityFunction5, densityFunction7)
        val densityFunction9 = DensityFunctions.min(
            DensityFunctions.min(
                densityFunction8,
                getFunction(densityFunctions, ENTRANCES)
            ), DensityFunctions.add(densityFunction2, densityFunction3)
        )
        val densityFunction10 = getFunction(densityFunctions, PILLARS)
        val densityFunction11 = DensityFunctions.rangeChoice(
            densityFunction10,
            -1000000.0,
            0.03,
            DensityFunctions.constant(-1000000.0),
            densityFunction10
        )
        return DensityFunctions.max(densityFunction9, densityFunction11)
    }

    private fun postProcess(densityFunction: DensityFunction): DensityFunction {
        val densityFunction2 = DensityFunctions.blendDensity(densityFunction)
        return DensityFunctions.mul(DensityFunctions.interpolated(densityFunction2), DensityFunctions.constant(0.64))
            .squeeze()
    }

    fun undefined(
        densityFunctions: HolderGetter<DensityFunction>,
        noiseParameters: HolderGetter<NoiseParameters>
    ): NoiseRouter {
        val densityFunction = DensityFunctions.noise(noiseParameters.getOrThrow(Noises.AQUIFER_BARRIER), 0.5)
        val densityFunction2 =
            DensityFunctions.noise(noiseParameters.getOrThrow(Noises.AQUIFER_FLUID_LEVEL_FLOODEDNESS), 0.67)
        val densityFunction3 =
            DensityFunctions.noise(noiseParameters.getOrThrow(Noises.AQUIFER_FLUID_LEVEL_SPREAD), 0.7142857142857143)
        val densityFunction4 = DensityFunctions.noise(noiseParameters.getOrThrow(Noises.AQUIFER_LAVA))
        val densityFunction5 = getFunction(densityFunctions, SHIFT_X)
        val densityFunction6 = getFunction(densityFunctions, SHIFT_Z)
        val densityFunction7 = DensityFunctions.shiftedNoise2d(
            densityFunction5,
            densityFunction6,
            0.25,
            noiseParameters.getOrThrow(Noises.TEMPERATURE)
        )
        val densityFunction8 = DensityFunctions.shiftedNoise2d(
            densityFunction5,
            densityFunction6,
            0.25,
            noiseParameters.getOrThrow(Noises.VEGETATION)
        )
        val densityFunction9 = getFunction(densityFunctions, NoiseRouterData.FACTOR)
        val densityFunction10 = getFunction(densityFunctions, NoiseRouterData.DEPTH)
        val densityFunction11 = noiseGradientDensity(DensityFunctions.cache2d(densityFunction9), densityFunction10)
        val densityFunction12 = getFunction(densityFunctions, SLOPED_CHEESE)
        val densityFunction13 = DensityFunctions.min(
            densityFunction12,
            DensityFunctions.mul(
                DensityFunctions.constant(5.0),
                getFunction(densityFunctions, ENTRANCES)
            )
        )
        val densityFunction14 = DensityFunctions.rangeChoice(
            densityFunction12,
            -1000000.0,
            SURFACE_DENSITY_THRESHOLD,
            densityFunction13,
            underground(densityFunctions, noiseParameters, densityFunction12)
        )
        val densityFunction15 =
            DensityFunctions.min(postProcess(slideUndefined(densityFunction14)), getFunction(densityFunctions, NOODLE))
        return NoiseRouter(
            densityFunction,
            densityFunction2,
            densityFunction3,
            densityFunction4,
            densityFunction7,
            densityFunction8,
            getFunction(densityFunctions, NoiseRouterData.CONTINENTS),
            getFunction(densityFunctions, NoiseRouterData.EROSION),
            densityFunction10,
            getFunction(densityFunctions, NoiseRouterData.RIDGES),
            slideUndefined(
                DensityFunctions.add(densityFunction11, DensityFunctions.constant(CHEESE_NOISE_TARGET))
                    .clamp(-64.0, 64.0)
            ),
            densityFunction15,
            DensityFunctions.constant(-1.0),
            DensityFunctions.constant(0.0),
            DensityFunctions.constant(0.0)
        )
    }

    private fun slideUndefined(densityFunction: DensityFunction): DensityFunction {
        return slide(
            densityFunction,
            -128,
            320,
            200,
            100,
            -0.078125,
            0,
            24,
            0.1171875
        )
    }

    private fun yLimitedInterpolatable(
        densityFunction: DensityFunction,
        densityFunction2: DensityFunction,
        i: Int,
        j: Int,
        k: Int
    ): DensityFunction {
        return DensityFunctions.interpolated(
            DensityFunctions.rangeChoice(
                densityFunction,
                i.toDouble(),
                (j + 1).toDouble(),
                densityFunction2,
                DensityFunctions.constant(k.toDouble())
            )
        )
    }

    private fun slide(
        densityFunction: DensityFunction,
        minY: Int,
        maxY: Int,
        topSlideStartOffset: Int,
        topSlideEndOffset: Int,
        topTargetDensity: Double,
        bottomSlideStartOffset: Int,
        bottomSlideEndOffset: Int,
        bottomTargetDensity: Double
    ): DensityFunction {
        val topGradient = DensityFunctions.yClampedGradient(
            minY + maxY - topSlideStartOffset,
            minY + maxY - topSlideEndOffset,
            1.0,
            0.0
        )
        var result = DensityFunctions.lerp(topGradient, topTargetDensity, densityFunction)
        val bottomGradient =
            DensityFunctions.yClampedGradient(minY + bottomSlideStartOffset, minY + bottomSlideEndOffset, 0.0, 1.0)
        result = DensityFunctions.lerp(bottomGradient, bottomTargetDensity, result)
        return result
    }

    private fun createKey(location: String): ResourceKey<DensityFunction> {
        return ResourceKey.create(Registries.DENSITY_FUNCTION, ResourceLocation(location))
    }

    private fun getFunction(
        densityFunctions: HolderGetter<DensityFunction>,
        key: ResourceKey<DensityFunction>
    ): DensityFunction = HolderHolder(densityFunctions.getOrThrow(key))

    private fun noiseGradientDensity(minFunction: DensityFunction, maxFunction: DensityFunction): DensityFunction {
        val densityFunction = DensityFunctions.mul(maxFunction, minFunction)
        return DensityFunctions.mul(DensityFunctions.constant(4.0), densityFunction.quarterNegative())
    }
}