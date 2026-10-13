# Keep annotated classes and members (e.g. @Keep on Firestore entities and Glance widget components)
-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @androidx.annotation.Keep <fields>;
    @androidx.annotation.Keep <init>(...);
    @androidx.annotation.Keep <methods>;
}

# Keep Glance AppWidget Receiver referenced in AndroidManifest.xml
-keep class br.com.dlpsystems.cycle.presentation.widget.CycleGlanceWidgetReceiver { *; }

# Google Play Billing
-keep class com.android.billingclient.api.** { *; }

# Reempacotamento de classes para otimizacao DEX no R8
-repackageclasses ''
-allowaccessmodification
