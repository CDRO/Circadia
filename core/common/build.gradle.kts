plugins {
    id("circadia.jvm.library")
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit)
}

val isResearchMode = project.findProperty("researchMode") == "true"
println("isResearchMode in build script: $isResearchMode")

val generateFeatureFlags by tasks.registering {
    val researchMode = isResearchMode
    val outputDir = layout.buildDirectory.dir("generated/sources/featureflags")
    outputs.dir(outputDir)
    doLast {
        val file = outputDir.get().file("ch/circadia/tracker/core/common/FeatureFlags.kt").asFile
        file.parentFile.mkdirs()
        file.writeText("""
            package ch.circadia.tracker.core.common
            
            object FeatureFlags {
                const val researchMode: Boolean = $researchMode
            }
        """.trimIndent())
    }
}

kotlin.sourceSets["main"].kotlin.srcDir(generateFeatureFlags)
