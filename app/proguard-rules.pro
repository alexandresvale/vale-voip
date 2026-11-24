# Regras ProGuard / R8 — Vale VoIP (Produção)

# Preserva nomes de arquivo e números de linha para stacktraces legíveis no Firebase Crashlytics
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Preserva anotações e assinaturas genéricas exigidas por DI, Serialization e JNI
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# --- Linphone SDK (JNI Callbacks & Native Structs) ---
-keep class org.linphone.core.** { *; }
-keep class org.linphone.mediastream.** { *; }
-keep class org.bctoolbox.** { *; }
-dontwarn org.linphone.**

# --- Google Tink (Keystore & Protobuf Keysets) ---
-keep class * extends com.google.crypto.tink.shaded.protobuf.GeneratedMessageLite {
    <fields>;
}
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.**

# --- Kotlinx Serialization (DataStore Prefs & Compose Navigation Type-Safe Routes) ---
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepnames class <2>$$serializer {
    *;
}
-keepclassmembers class com.valevoip.** {
    @kotlinx.serialization.Serializable <methods>;
}
