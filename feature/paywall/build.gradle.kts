plugins {
    id("circadia.android.library")
    id("circadia.hilt")
    id("circadia.compose")
}

android {
    namespace = "ch.circadia.tracker.feature.paywall"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(project(":core:designsystem"))
    
    implementation(libs.androidx.activity.compose)
    implementation(libs.hiltnavcompose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.kotlinx.serialization.json)
}
