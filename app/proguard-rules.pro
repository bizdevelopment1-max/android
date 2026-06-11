# Keep JavaScript bridge methods
-keepclassmembers class com.healthdash.app.DashBridge {
    @android.webkit.JavascriptInterface <methods>;
}
