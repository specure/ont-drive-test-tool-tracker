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

-keepattributes Exceptions,InnerClasses,MethodParameters,*Annotation*,EnclosingMethod,Signature

-keepclassmembers class * { public <init>(...); }

-keep class kotlin.Metadata { *; }
-keep class kotlin.reflect.** { *; }

-keep class org.koin.** { *; }
-keep class org.koin.core.** { *; }
-keep class org.koin.dsl.** { *; }

-keep class com.specure.track.data.di.* {
    *;
}
# Keep SLF4J Logger
-keep class org.slf4j.** { *; }

# Keep all classes related to Apache Commons Logging
-keep class org.apache.commons.logging.** { *; }
-keep class org.apache.log4j.** { *; }

# Keep Apache Commons Logging's ServletContextCleaner
-keep class org.apache.commons.logging.impl.ServletContextCleaner { *; }

# Keep all Avalon Framework classes
-keep class org.apache.avalon.framework.** { *; }

# Keep Avalon Logger class
-keep class org.apache.avalon.framework.logger.Logger { *; }

# Keep all javax.servlet classes
-keep class javax.servlet.** { *; }

# Keep the Logback implementation if using Logback
-keep class ch.qos.logback.** { *; }

# Keep the Iperf implementation if using Logback
-keep class com.cadrikmdev.iperf.** { *; }

-keep class com.synaptictools.iperf.** { *; }

# Keep native method names for JNI
-keepclasseswithmembernames class * {
    native <methods>;
}

# Ignore missing javax.servlet classes
-dontwarn javax.servlet.**
# Suppress warnings for missing Apache Avalon Logger and LogKit classes
-dontwarn org.apache.avalon.framework.logger.**
-dontwarn org.apache.log.**
-dontwarn org.apache.log4j.**
-dontwarn ch.qos.logback.**
-dontwarn com.cadrikmdev.iperf.**
-dontwarn com.synaptictools.iperf.**
-dontwarn org.slf4j.**
