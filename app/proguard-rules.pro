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
