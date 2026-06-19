# 南半球历法 ProGuard Rules

# Keep JNI native methods (sxtwl_cpp bridge)
-keep class com.nanbanqiu.wannianli.engine.SxtwlBridge { *; }

# Keep Gson serialization classes
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.nanbanqiu.wannianli.data.model.** { *; }

# Keep Compose
-dontwarn androidx.compose.**

# Keep Kotlin metadata
-keepattributes *Annotation*
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**

# Keep Android components
-keep class * extends android.app.Activity { *; }
-keep class * extends android.app.Service { *; }
-keep class * extends android.content.BroadcastReceiver { *; }

# General
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose
