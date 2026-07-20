# =============================================================================
# UITrends release R8 / ProGuard rules
# Used with proguard-android-optimize.txt (never use proguard-android.txt on AGP 9+)
# Docs: https://developer.android.com/topic/performance/app-optimization/enable-app-optimization
# =============================================================================

# --- Crash deobfuscation & debugging ---
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepattributes Signature
-keepattributes InnerClasses,EnclosingMethod
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,AnnotationDefault
-keepattributes *Annotation*

# --- Kotlin runtime ---
-dontwarn kotlin.**
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.coroutines.** { *; }

# --- Kotlin coroutines ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# --- Kotlinx Serialization (Navigation type-safe routes) ---
-dontnote kotlinx.serialization.AnnotationsKt
-keepattributes *Annotation*, InnerClasses
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}
-keepclasseswithmembers class <1> {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.mfhapps.trendingui.navigation.**$$serializer { *; }
-keepclassmembers class com.mfhapps.trendingui.navigation.** {
    *** Companion;
}
-keep @kotlinx.serialization.Serializable class com.mfhapps.trendingui.navigation.** { *; }

# --- Jetpack Compose (runtime; most rules ship with libraries) ---
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.platform.** { *; }
-dontwarn androidx.compose.**

# --- Android framework ---
-keep class * extends android.app.Application { <init>(); }
-keep class * extends android.app.Activity { <init>(...); }
-keep class * extends android.app.Service { <init>(...); }
-keep class * extends android.content.BroadcastReceiver { <init>(...); }
-keep class * extends android.content.ContentProvider { <init>(...); }
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
-keepclassmembers class **.R$* {
    public static <fields>;
}

# --- JNI / native Pretext geometry & vision ---
-keepclasseswithmembernames class * {
    native <methods>;
}
-keep class com.mfhapps.trendingui.native.** { *; }

# --- LiteRT (.tflite inference: segmentation / BlazeFace) ---
-keep class org.tensorflow.lite.** { *; }
-keep class org.tensorflow.lite.gpu.** { *; }
-dontwarn org.tensorflow.lite.**

# --- MediaPipe Tasks Vision (face landmarker) ---
-keep class com.google.mediapipe.** { *; }
-dontwarn com.google.mediapipe.**

# --- CameraX ---
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**

# --- Coil image loading ---
-keep class coil.** { *; }
-dontwarn coil.**

# --- DataStore / Preferences ---
-keep class androidx.datastore.*.** { *; }
-dontwarn androidx.datastore.**

# --- Splash screen / core AndroidX ---
-keep class androidx.core.splashscreen.** { *; }
-keep class androidx.startup.** { *; }

-keep class * extends androidx.room.RoomDatabase {
    <init>();
    public ** createInvalidationTracker();
    public void clearAllTables();
}
-keep class androidx.room.RoomDatabase$JournalMode { *; }
-keep class androidx.work.** {
    <init>(...);
}
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class * extends androidx.work.InputMerger {
    <init>();
}

-keep class com.google.android.gms.ads.** { *; }
-dontwarn com.google.android.gms.ads.**
-keep class com.google.android.play.core.** { *; }
-dontwarn com.google.android.play.core.**

-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# --- Optional / transitive dependency noise ---
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
-dontwarn javax.annotation.**
-dontwarn sun.misc.**

# --- BuildConfig (version shown in Settings) ---
-keep class com.mfhapps.trendingui.BuildConfig { *; }
