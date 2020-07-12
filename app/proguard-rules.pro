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
-dontwarn org.fourthline.cling.**
-dontwarn org.seamless.**
-dontwarn org.eclipse.jetty.**

-keep class javax.** { *; }
-keep class org.fourthline.cling.** { *;}
-keep class org.seamless.** { *;}
-keep class org.eclipse.jetty.** { *;}
-keep class javax.servlet.** { *; }

-keepattributes Annotation, InnerClasses, Signature

-keepnames class com.m3sv.plainupnp.presentation.main.ControlsFragment
