# Keep Compose runtime
-dontwarn androidx.compose.**

# Keep DataStore
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite {
    <fields>;
}
