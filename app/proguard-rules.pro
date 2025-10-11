# Add project specific ProGuard rules here.
# Aggressive optimization for minimal APK size

# Maximum optimization
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose
-allowaccessmodification
-repackageclasses ''

# Keep line numbers for debugging stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Remove ALL logging in release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
    public static *** wtf(...);
}

# Remove System.out/err
-assumenosideeffects class java.io.PrintStream {
    public void println(...);
    public void print(...);
}

# Keep minimal LibSU classes (only what we use)
-keep class com.topjohnwu.superuser.Shell { *; }
-keep class com.topjohnwu.superuser.Shell$* { *; }
-keep class com.topjohnwu.superuser.io.SuFile { *; }
-keep class com.topjohnwu.superuser.io.SuFileInputStream { *; }
-keep class com.topjohnwu.superuser.io.SuFileOutputStream { *; }

# Minimal Material Components (don't keep everything)
-dontwarn com.google.android.material.**

# Minimal AndroidX 
-dontwarn androidx.**

# Keep only application classes that are actually used
-keep class xzr.konabess.MainActivity { *; }
-keep class xzr.konabess.SettingsActivity { *; }
-keep class xzr.konabess.GpuTableEditor { *; }
-keep class xzr.konabess.GpuVoltEditor { *; }
-keep class xzr.konabess.KonaBessCore { *; }

# Keep adapters (used by RecyclerView)
-keep class xzr.konabess.adapters.** { *; }

# Keep utils
-keep class xzr.konabess.utils.** { *; }

# Keep native methods
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}

# Keep custom views (minimal)
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

# Keep Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Keep Serializable
-keepnames class * implements java.io.Serializable

# Remove debugging attributes
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static void check*(...);
    public static void throw*(...);
}

