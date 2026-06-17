-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

-keepclasseswithmembernames class * {
    native <methods>;
}

-keep class org.tensorflow.lite.** { *; }

-keep class com.mfhapps.trendingui.native.PretextNativeGeometry { *; }
-keep class com.mfhapps.trendingui.native.PretextNativeVision { *; }

-keep class com.google.mediapipe.** { *; }
