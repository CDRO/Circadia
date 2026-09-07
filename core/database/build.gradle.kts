plugins {
    id("circadia.android.library")
    id("circadia.hilt")
}

android {
    namespace = "ch.circadia.tracker.core.database"
    
    defaultConfig {
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }
}

dependencies {
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.kotlinx.coroutines.core)
}
