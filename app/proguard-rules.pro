# kotlinx.serialization keeps its generated serializers; R8 must not eat them.
-keepattributes *Annotation*, InnerClasses
-keepclassmembers class com.petconnect.** {
    *** Companion;
}
-keepclasseswithmembers class com.petconnect.** {
    kotlinx.serialization.KSerializer serializer(...);
}
