import groovy.json.JsonSlurper
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("net.fabricmc.fabric-loom-remap")
    `maven-publish`
    kotlin("jvm")
    kotlin("plugin.serialization")
}

repositories {
    // Add repositories to retrieve artifacts from in here.
    // You should only use this when depending on other mods because
    // Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
    // See https://docs.gradle.org/current/userguide/declaring_repositories.html
    // for more information about repositories.
    maven("https://maven.isxander.dev/releases") {
        name = "Xander Maven"
    }
    maven("https://maven.terraformersmc.com/") {
        name = "Terraformers"
    }
    maven("https://maven.parchmentmc.org") {
        name = "Parchment"
    }
    maven("https://mvn.devos.one/snapshots/")
    maven("https://mvn.devos.one/releases/")
    maven("https://maven.createmod.net/")
    maven("https://raw.githubusercontent.com/Fuzss/modresources/main/maven")
    maven("https://maven.jamieswhiteshirt.com/libs-release/")
    exclusiveContent {
        forRepository {
            maven("https://api.modrinth.com/maven") {
                name = "Modrinth"
            }
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }
    maven("https://maven.covers1624.net/")
}

loom {
    splitEnvironmentSourceSets()

    mods {
        register(providers.gradleProperty("mod_id").get()) {
            sourceSet(sourceSets.main.get())
            sourceSet(sourceSets.getByName("client"))
        }
    }

    val aw = file("src/main/resources/${providers.gradleProperty("mod_id").get()}.aw")
    if (aw.exists())
        accessWidenerPath = aw
}

fabricApi {
    configureDataGeneration {
        client = true
    }
}

dependencies {
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:${providers.gradleProperty("minecraft_version").get()}")
    mappings(
        loom.layered {
            officialMojangMappings()
            parchment(
                "org.parchmentmc.data:parchment-${
                    providers.gradleProperty("minecraft_version").get()
                }:${providers.gradleProperty("parchment_mappings").get()}@zip"
            )
        }
    )
    modImplementation("net.fabricmc:fabric-loader:${providers.gradleProperty("loader_version").get()}")

    // Fabric API. This is technically optional, but you probably want it anyway.
    modImplementation("net.fabricmc.fabric-api:fabric-api:${providers.gradleProperty("fabric_api_version").get()}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${providers.gradleProperty("fabric_kotlin_version").get()}")
    modImplementation("com.terraformersmc:modmenu:${providers.gradleProperty("mod_menu_version").get()}")
    modImplementation("dev.isxander:yet-another-config-lib:${providers.gradleProperty("yacl_version").get()}")
    modCompileOnly("com.simibubi.create:create-fabric:${providers.gradleProperty("create_version").get()}")
//    modRuntimeOnly("com.simibubi.create:create-fabric:${providers.gradleProperty("create_version").get()}")
    modImplementation("maven.modrinth:sodium:${providers.gradleProperty("sodium_version").get()}")
    modImplementation("maven.modrinth:iris:${providers.gradleProperty("iris_version").get()}")
    modImplementation("org.antlr:antlr4-runtime:${providers.gradleProperty("antlr4_version").get()}")
    modImplementation("io.github.douira:glsl-transformer:${providers.gradleProperty("glsl_transformer_version").get()}")
    modImplementation("org.anarres:jcpp:${providers.gradleProperty("jcpp_version").get()}")
    include(implementation("net.mamoe.yamlkt:yamlkt:${providers.gradleProperty("yamlkt_version").get()}")!!)
}

val generateFallbackTranslations = tasks.register("generateFallbackTranslations") {
    description = "Generate fallback translations from en_us.json"

    val inputFile = file(
        "src/main/resources/assets/${providers.gradleProperty("mod_id").get()}/lang/en_us.json"
    )

    onlyIf {
        inputFile.exists()
    }

    val outputDir = layout.buildDirectory.dir("generated/sources/fallbackTranslations")

    inputs.file(inputFile)
    outputs.dir(outputDir)

    doLast {
        val outputDirectory = outputDir.get().asFile

        val translations = JsonSlurper()
            .parse(inputFile) as Map<*, *>

        fun toConstantName(key: String): String = key.replace(Regex("[^A-Za-z0-9]+"), "_").uppercase().trim('_')

        fun escapeKotlinString(value: String): String = value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\r", "\\r")
            .replace("\n", "\\n")
            .replace("\t", "\\t")

        val output = buildString {
            appendLine("// Generated file. DO NOT EDIT.")
            appendLine("// Generated from assets/${providers.gradleProperty("mod_id").get()}/lang/en_us.json")
            appendLine()
            appendLine("package $group")
            appendLine()
            appendLine("object FallbackTranslations {")

            translations.forEach { (key, value) ->
                require(key is String) {
                    "Translation key must be a string: $key"
                }

                require(value is String) {
                    "Translation value must be a string: $key"
                }

                val constantName = toConstantName(key)
                val text = escapeKotlinString(value)

                appendLine("    const val $constantName = \"$text\"")
            }

            appendLine("}")
        }

        val outputFile = outputDirectory.resolve("${group?.replace('.', '/')}/FallbackTranslations.kt")

        outputFile.parentFile.mkdirs()
        outputFile.writeText(output)
    }
}

val generateUpsideDownTranslation = tasks.register("generateUpsideDownTranslation") {
    description = "Generate the en_ud (Upside-Down English) translation from en_us."

    val inputFile =
        file("src/main/resources/assets/${providers.gradleProperty("mod_id").get()}/lang/en_us.json")

    onlyIf {
        inputFile.exists()
    }

    val outputDir = layout.buildDirectory.dir("generated/resources/upsideDownTranslations")

    inputs.file(inputFile)
    outputs.dir(outputDir)

    doLast {
        val outputDirectory = outputDir.get().asFile
        outputDirectory.mkdirs()

        val translations =
            JsonSlurper().parse(inputFile) as Map<*, *>

        /*
         * Minecraft format placeholders.
         *
         * Examples:
         * %s
         * %d
         * %1$s
         * %2$d
         * %1.2f
         */
        val placeholderRegex =
            Regex("%(?:\\d+\\$)?(?:\\d+)?(?:\\.\\d+)?[a-zA-Z]")

        val upsideDownMap = mapOf(
            // Lowercase
            'a' to 'ɐ',
            'b' to 'q',
            'c' to 'ɔ',
            'd' to 'p',
            'e' to 'ǝ',
            'f' to 'ɟ',
            'g' to 'ƃ',
            'h' to 'ɥ',
            'i' to 'ᴉ',
            'j' to 'ɾ',
            'k' to 'ʞ',
            'l' to 'ן',
            'm' to 'ɯ',
            'n' to 'u',
            'o' to 'o',
            'p' to 'd',
            'q' to 'b',
            'r' to 'ɹ',
            's' to 's',
            't' to 'ʇ',
            'u' to 'n',
            'v' to 'ʌ',
            'w' to 'ʍ',
            'x' to 'x',
            'y' to 'ʎ',
            'z' to 'z',

            // Uppercase
            'A' to '∀',
            'B' to 'B',
            'C' to 'Ɔ',
            'D' to '◖',
            'E' to 'Ǝ',
            'F' to 'Ⅎ',
            'G' to 'פ',
            'H' to 'H',
            'I' to 'I',
            'J' to 'ſ',
            'K' to 'ʞ',
            'L' to '˥',
            'M' to 'M',
            'N' to 'N',
            'O' to 'O',
            'P' to 'Ԁ',
            'Q' to 'Ό',
            'R' to 'ᴚ',
            'S' to 'S',
            'T' to '┴',
            'U' to '∩',
            'V' to 'Λ',
            'W' to 'M',
            'X' to 'X',
            'Y' to '⅄',
            'Z' to 'Z',

            // Punctuation
            '.' to '˙',
            ',' to '\'',
            '\'' to ',',
            '?' to '¿',
            '!' to '¡',
            '[' to ']',
            ']' to '[',
            '(' to ')',
            ')' to '(',
            '<' to '>',
            '>' to '<'
        )

        fun transformText(text: String): String {
            val matches = placeholderRegex.findAll(text).toList()

            // No placeholders.
            if (matches.isEmpty()) {
                return text
                    .reversed()
                    .map { upsideDownMap[it] ?: it }
                    .joinToString("")
            }

            val result = StringBuilder()

            /*
             * We process the text from right to left.
             *
             * Placeholders themselves are kept unchanged.
             */
            var end = text.length

            for (match in matches.asReversed()) {
                // Text after the placeholder.
                result.append(
                    text.substring(match.range.last + 1, end)
                        .reversed()
                        .map { upsideDownMap[it] ?: it }
                        .joinToString("")
                )

                // Placeholder itself.
                result.append(match.value)

                end = match.range.first
            }

            // Text before the first placeholder.
            result.append(
                text.substring(0, end)
                    .reversed()
                    .map { upsideDownMap[it] ?: it }
                    .joinToString("")
            )

            return result.toString()
        }

        val output = buildString {
            appendLine("{")

            translations.entries.forEachIndexed { index, (key, value) ->
                val translated =
                    transformText(value.toString())
                        .replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                        .replace("\n", "\\n")
                        .replace("\r", "\\r")
                        .replace("\t", "\\t")

                append("  ")
                append("\"")
                append(key)
                append("\": \"")
                append(translated)
                append("\"")

                if (index != translations.size - 1) {
                    append(",")
                }

                appendLine()
            }

            appendLine("}")
        }

        outputDirectory
            .resolve("assets/${providers.gradleProperty("mod_id").get()}/lang/en_ud.json")
            .apply {
                parentFile.mkdirs()
                writeText(output)
            }
    }
}

tasks.processResources {
    val version = version
    inputs.property("version", version)
    inputs.property("minecraft_version", providers.gradleProperty("minecraft_version").get())
    inputs.property("loader_version", providers.gradleProperty("loader_version").get())
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand(
            "version" to version,
            "mod_id" to providers.gradleProperty("mod_id").get(),
            "mod_name" to providers.gradleProperty("mod_name").get(),
            "minecraft_version" to providers.gradleProperty("minecraft_version").get(),
            "loader_version" to providers.gradleProperty("loader_version").get(),
            "fabric_kotlin_version" to providers.gradleProperty("fabric_kotlin_version").get(),
            "fabric_api_version" to providers.gradleProperty("fabric_api_version").get(),
            "mod_menu_version" to providers.gradleProperty("mod_menu_version").get(),
            "yacl_version" to providers.gradleProperty("yacl_version").get(),
        )
    }

    dependsOn(generateUpsideDownTranslation)
    from(
        layout.buildDirectory.dir(
            "generated/resources/upsideDownTranslations"
        )
    )
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

java {
    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.jar {
    val projectName = project.name
    inputs.property("projectName", projectName)

    from("LICENSE") {
        rename { "${it}_$projectName" }
    }
}

// configure the maven publication
publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }

    // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
    repositories {
        // Add repositories to publish to here.
        // Notice: This block does NOT have the same function as the block in the top level.
        // The repositories here will be used for publishing your artifact, not for
        // retrieving dependencies.
    }
}

kotlin {
    sourceSets {
        main {
            kotlin.srcDir(
                layout.buildDirectory.dir(
                    "generated/sources/fallbackTranslations"
                )
            )
        }
    }
}

tasks.named<KotlinCompile>("compileKotlin") {
    dependsOn(generateFallbackTranslations)
}

tasks.named<Jar>("sourcesJar") {
    dependsOn(generateFallbackTranslations)
}

loom {
    runs {
        configureEach {
            vmArg("-Dsodium.checks.issue2561=false")
        }
    }
}