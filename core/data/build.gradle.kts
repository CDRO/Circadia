plugins {
    id("circadia.android.library")
    id("circadia.hilt")
}

android {
    namespace = "ch.circadia.tracker.core.data"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(libs.kotlinx.coroutines.core)
}
