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

# 1. Keep ALL classes in your model/data package (replace with your actual package name)
-keep class com.example.medialert.data.** { *; }
-keep class com.example.medialert.model.** { *; }

# 2. Keep standard serialization & annotations (Required for Gson/Firebase/Room)
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses

# 3. Keep Gson annotations & fields across the app
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# 4. Keep Firebase Firestore annotations & fields across the app
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.IgnoreExtraProperties <fields>;
}

# 5. Keep Room database generated classes
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Preserve generic signatures required by Gson's TypeToken
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Keep Gson's TypeToken class untouched
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# Keep all Room TypeConverters
-keepclassmembers class com.example.medialert.data.Converters {
    public *;
}

# Keep all data models inside your data package
-keep class com.example.medialert.data.** { *; }