# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.nutritiontracker.data.model.** { *; }
-keep class com.nutritiontracker.ai.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Room
-keep class * extends androidx.room.RoomDatabase

# WorkManager
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker

# Security Crypto
-dontwarn com.google.crypto.tink.**
