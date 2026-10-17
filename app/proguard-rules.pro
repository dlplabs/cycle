# ====================================================================
# ProGuard & R8 Optimization Rules - Cycle
# ====================================================================

# --------------------------------------------------------------------
# 1. R8 Global Optimizations & Memory Tuning
# --------------------------------------------------------------------
-allowaccessmodification
-repackageclasses 'br.com.dlpsystems.cycle.r8'

-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# --------------------------------------------------------------------
# 2. Android 15+ (SDK 35+) Edge-to-Edge Compliance
# --------------------------------------------------------------------
# Preserve EdgeToEdge entry point so Play Console detects enableEdgeToEdge()
-keep,allowoptimization class androidx.activity.EdgeToEdge {
    public static void enable(androidx.activity.ComponentActivity);
    public static void enable(androidx.activity.ComponentActivity, androidx.activity.SystemBarStyle);
    public static void enable(androidx.activity.ComponentActivity, androidx.activity.SystemBarStyle, androidx.activity.SystemBarStyle);
    public static void enable$default(androidx.activity.ComponentActivity, androidx.activity.SystemBarStyle, androidx.activity.SystemBarStyle, int, java.lang.Object);
}
-keep,allowshrinking,allowoptimization class androidx.activity.EdgeToEdge* {
    public void setUp(...);
}

# Strip deprecated Android 15 Window color and SHORT_EDGES cutout APIs
-assumenosideeffects class android.view.Window {
    public void setStatusBarColor(int);
    public void setNavigationBarColor(int);
}
-assumenosideeffects interface androidx.activity.EdgeToEdgeImpl {
    public void adjustLayoutInDisplayCutoutMode(android.view.Window);
}
-assumenosideeffects class androidx.activity.EdgeToEdge* {
    public void adjustLayoutInDisplayCutoutMode(android.view.Window);
}

# --------------------------------------------------------------------
# 3. Annotated Entities & Glance AppWidget Receiver
# --------------------------------------------------------------------
-keep,allowobfuscation @interface androidx.annotation.Keep
-keep @androidx.annotation.Keep class * { *; }
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <fields>;
}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <init>(...);
}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <methods>;
}
-keepclassmembers,allowobfuscation,allowshrinking class * {
    @androidx.annotation.DoNotInline <methods>;
}

-keep class br.com.dlpsystems.cycle.presentation.widget.CycleGlanceWidgetReceiver { *; }

# --------------------------------------------------------------------
# 4. Scoped Firebase Auth & Play Recaptcha Rules
# --------------------------------------------------------------------
-keepclasseswithmembernames,allowobfuscation,allowoptimization interface * {
    @retrofit.http.* <methods>;
}
-keepclassmembers,allowobfuscation,allowoptimization class com.google.android.gms.internal.firebase-auth-api.** {
    public <init>();
}
-keepclassmembers class * extends com.google.android.gms.internal.firebase-auth-api.zzalf {
    <fields>;
}
-keepclassmembers class * extends com.google.android.recaptcha.internal.zznd {
    <fields>;
}
-dontwarn java.lang.ClassValue
-dontwarn kotlinx.coroutines.flow.**
-dontwarn com.squareup.okhttp3.**
-dontwarn okhttp3.**
