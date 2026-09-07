plugins {
    id("org.jetbrains.kotlin.plugin.compose")
}

project.dependencies.apply {
    val bom = "androidx.compose:compose-bom:2025.02.00"
    add("implementation", platform(bom))
    add("androidTestImplementation", platform(bom))
    add("implementation", "androidx.compose.ui:ui")
    add("implementation", "androidx.compose.ui:ui-tooling-preview")
    add("implementation", "androidx.compose.material3:material3")
    add("implementation", "androidx.compose.foundation:foundation")
    add("implementation", "androidx.compose.foundation:foundation-layout")
}
