plugins {
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

project.dependencies.apply {
    add("implementation", "com.google.dagger:hilt-android:2.55")
    add("ksp", "com.google.dagger:hilt-compiler:2.55")
}
