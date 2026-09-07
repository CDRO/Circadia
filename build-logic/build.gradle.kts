plugins {
    `kotlin-dsl`
}

group = "ch.circadia.buildlogic"

dependencies {
    implementation(libs.android.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
}
