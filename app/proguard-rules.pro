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

-keep class com.fasterxml.jackson.** { *; }
-keepnames class com.fasterxml.jackson.** { *; }
-keep class org.codehaus.** { *; }

-dontwarn org.koin.**
-dontwarn com.fasterxml.jackson.databind.**


-keep class com.fasterxml.** { *; }
-keepclassmembers class ** extends com.fasterxml.jackson.databind.ser.std.** {
   public <init>(...);
}

-keepclassmembers class ** extends com.fasterxml.jackson.databind.deser.std.** {
   public <init>(...);
}
 -keepnames class com.fasterxml.jackson.** { *; }
 -dontwarn com.fasterxml.jackson.databind.**
 -keep class org.codehaus.** { *; }
-keep public class your.class.** {
    *;
}
-dontwarn com.fasterxml.jackson.databind.*

-keepattributes com.specure.connectivity.presentation.mobile_network.di.*
-keepattributes com.specure.connectivity.presentation.network.di.*
-keepattributes com.specure.core.data.di.*
-keepattributes com.specure.core.presentation.designsystem.ThemeKt
-keepattributes com.specure.core.presentation.ui.DataFormattersKt
-keepattributes com.specure.intercom.data.di.*
-keepattributes com.specure.permissions.presentation.di.*
-keepattributes com.specure.permissions.presentation.screen.permissions.PermissionsScreenKt
-keepattributes com.specure.permissions.presentation.screen.permissions.PermissionsScreenViewModel
-keepattributes com.specure.permissions.presentation.util.PermissionsExtKt
-keepattributes com.specure.track.data.di.*
-keepattributes com.specure.track.location.di.*
-keepattributes com.specure.track.presentation.about.AboutScreenKt
-keepattributes com.specure.track.presentation.about.AboutScreenNav
-keepattributes com.specure.track.presentation.about.AboutScreenViewModel
-keepattributes com.specure.track.presentation.active_track.ActiveTrackScreenKt
-keepattributes com.specure.track.presentation.active_track.ActiveTrackViewModel
-keepattributes com.specure.track.presentation.active_track.service.*
-keepattributes com.specure.track.presentation.di.*
-keepattributes com.specure.track.presentation.settings.SettingsScreenKt
-keepattributes com.specure.track.presentation.settings.SettingsScreenViewModel
-keepattributes com.specure.track.presentation.settings.navigation.SettingsScreenNav
-keepattributes com.specure.track.presentation.track_overview.TrackOverviewScreenKt
-keepattributes com.specure.track.presentation.track_overview.TrackOverviewViewModel