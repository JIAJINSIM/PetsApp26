# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
# General ProGuard rules
#-keepattributes Signature, *Annotation*, EnclosingMethod, InnerClasses

#-dontoptimize
#-keepattributes SourceFile, LineNumberTable
-renamesourcefileattribute SourceFile
#-printmapping output.map
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscypt.**


# Remove all log messages
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# Keep your application's entry points from being obfuscated
#-keep public class * extends android.app.Activity
#-keep public class * extends android.app.Application
#-keep public class * extends android.app.Service
#-keep public class * extends android.content.BroadcastReceiver
#-keep public class * extends android.content.ContentProvider

# Keep names of classes and members you're accessing via reflection
-keep class com.example.petsapp26.** { *; }
-keepclassmembers class com.example.petsapp26.** { *; }

# Prevent the obfuscation of classes which are referenced in the AndroidManifest.xml
# or annotated with specific annotations that are used for reflection.
#-keepnames class * {
#    @androidx.room.* <fields>;
#    @androidx.room.* <methods>;
#}

# Prevent obfuscation of classes that use Parcelable, as obfuscation can break Parcelable
#-keep class * implements android.os.Parcelable {
#  public static final android.os.Parcelable$Creator *;
#}

# Keep GSON classes
#-keep class com.google.gson.stream.** { *; }


#-keepattributes Exceptions

# Keep models, DTOs, and POJOs used with Firebase, Gson, or any serialization library
# from being obfuscated. Adjust the package name accordingly.
#-keep class com.example.petsapp26.models.** { *; }

# Keep classes that are used in native methods to prevent issues during runtime.
#-keepclasseswithmembernames class * {
#    native <methods>;
#}

# If you're using Retrofit, Gson, or similar libraries for network calls and JSON parsing,
# keep class members' names from being obfuscated.
#-keepattributes Signature
#keepattributes *Annotation*
#-keepattributes EnclosingMethod

# Keep enum classes from being obfuscated to ensure that if they are used in serialization/deserialization, they remain intact.
#-keepclassmembers enum * {
 #   public static **[] values();
  #  public static ** valueOf(java.lang.String);
#}

# Obfuscate names of classes, methods, and fields
-repackageclasses ''
-allowaccessmodification
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
