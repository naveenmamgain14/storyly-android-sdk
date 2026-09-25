# kotlinx.serialization keeps generated serializers off R8's chopping block.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

-keepclassmembers class com.storyly.sdk.internal.net.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclasseswithmembers class com.storyly.sdk.internal.net.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Public API surface consumers reflect on / subclass.
-keep public class com.storyly.sdk.StorylyConfig { public *; }
-keep public class com.storyly.sdk.StorylyRailView { public *; }
-keep public interface com.storyly.sdk.StorylyListener { *; }
-keep public class com.storyly.sdk.model.** { public *; }
