# Hilt / Dagger
-keep class ch.circadia.tracker.** { *; }
-keep interface ch.circadia.tracker.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Kotlin Serialization
-keepattributes *Annotation*, EnclosingMethod, Signature
-keep class kotlinx.serialization.json.** { *; }
-keep class * implements kotlinx.serialization.KSerializer
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}

# Glance
-keep class androidx.glance.** { *; }
