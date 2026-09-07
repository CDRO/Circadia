plugins {
    id("circadia.android.library")
    id("circadia.hilt")
    id("circadia.compose")
}

android {
    namespace = "ch.circadia.tracker.feature.timeline"
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
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit) // JUnit 4
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.robolectric)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.androidx.compose.ui.test.junit4)
}
