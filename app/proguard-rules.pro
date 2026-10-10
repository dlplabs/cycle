# Firestore models are mapped by reflection.
-keep class br.com.dlpsystems.cycle.data.model.** { *; }
-keepclassmembers class br.com.dlpsystems.cycle.data.model.** {
    <init>(...);
    get*();
    set*(...);
}

# Domain models for state serialization
-keep class br.com.dlpsystems.cycle.domain.model.** { *; }

# Google Play Billing 8.0.0
-keep class com.android.billingclient.api.** { *; }

# Glance AppWidget
-keep class androidx.glance.** { *; }
-keep class br.com.dlpsystems.cycle.presentation.widget.** { *; }
