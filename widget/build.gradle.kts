plugins {
    id("circadia.android.library")
    id("circadia.hilt")
    id("circadia.compose")
    alias(libs.plugins.roborazzi)
}

android {
    namespace = "ch.circadia.tracker.widget"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(project(":core:designsystem"))
    implementation(project(":feature:persons"))
    
    implementation(libs.androidx.activity.compose)
    implementation(libs.hiltnavcompose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
    implementation(libs.androidx.core.ktx)
    
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit) // JUnit 4
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.roborazzi)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.glance.testing)
    testImplementation(libs.androidx.glance.appwidget.testing)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
