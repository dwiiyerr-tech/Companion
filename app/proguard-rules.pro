# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

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

# Kotlin coroutines and flow
-keep class kotlinx.coroutines.** { *; }
-keep class kotlinx.coroutines.flow.** { *; }

# Koin DI
-keep class org.koin.** { *; }
-keep class com.hermes.companion.** { *; }

# Room Database
-keep class androidx.room.** { *; }
-keep class com.hermes.companion.data.local.** { *; }

# OkHttp
-keep class okhttp3.** { *; }
-keep class okio.** { *; }

# Kotlinx Serialization
-keep class kotlinx.serialization.** { *; }

# Coil
-keep class coil.** { *; }

# WorkManager
-keep class androidx.work.** { *; }

# DataStore
-keep class androidx.datastore.** { *; }

# Navigation
-keep class androidx.navigation.** { *; }

# Compose
-keep class androidx.compose.** { *; }