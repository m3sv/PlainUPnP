object Versions {
    const val kotlin = "1.3.50"
    const val versionCode = 48
    const val versionName = "1.5.3"
    const val compileSdk = 28
    const val buildTools = "28.0.3"
    const val minSdk = 21
    const val targetSdk = 28
    const val supportLibrary = "1.0.0"
    const val architectureComponents = "2.0.0"
    const val dagger = "2.24"
    const val glide = "4.9.0"
    const val cling = "2.1.2-SNAPSHOT"
    const val jetty = "8.2.0.v20160908"
    const val okhttp = "3.9.1"
    const val navigation = "2.1.0"
    const val test = "1.2.0"
}

object Dependencies {
    const val kotlin = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"
    const val timber = "com.jakewharton.timber:timber:4.7.1"

    val coroutines = mapOf(
        "core" to "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.0",
        "rxJava2" to "org.jetbrains.kotlinx:kotlinx-coroutines-rx2:1.3.0"
    )

    val rx = mapOf(
        "kotlin" to "com.github.ReactiveX:rxKotlin:2.3.0",
        "android" to "io.reactivex.rxjava2:rxandroid:2.0.1"
    )

    val androidx = mapOf(
        "navigation" to mapOf(
            "fragment" to "androidx.navigation:navigation-fragment-ktx:${Versions.navigation}",
            "ui" to "androidx.navigation:navigation-ui-ktx:${Versions.navigation}"
        ),
        "lifecycle" to "androidx.lifecycle:lifecycle-extensions:${Versions.architectureComponents}",
        "appCompat" to "androidx.appcompat:appcompat:${Versions.supportLibrary}",
        "v4" to "androidx.legacy:legacy-support-v4:${Versions.supportLibrary}",
        "design" to "com.google.android.material:material:${Versions.supportLibrary}",
        "recyclerView" to "androidx.recyclerview:recyclerview:${Versions.supportLibrary}",
        "cardView" to "androidx.cardview:cardview:${Versions.supportLibrary}",
        "constraintLayout" to "androidx.constraintlayout:constraintlayout:2.0.0-beta2",
        "preferences" to "androidx.preference:preference:${Versions.supportLibrary}"
    )

    val dagger = mapOf(
        "core" to "com.google.dagger:dagger:${Versions.dagger}",
        "compiler" to "com.google.dagger:dagger-compiler:${Versions.dagger}",
        "annotation" to "javax.inject:javax.inject:1",
        "android" to "com.google.dagger:dagger-android:${Versions.dagger}",
        "androidSupport" to "com.google.dagger:dagger-android-support:${Versions.dagger}",
        "androidProcessor" to "com.google.dagger:dagger-android-processor:${Versions.dagger}"
    )

    val glide = mapOf(
        "core" to "com.github.bumptech.glide:glide:${Versions.glide}",
        "compiler" to "com.github.bumptech.glide:compiler:${Versions.glide}"
    )

    val test = mapOf(
        "junit" to "junit:junit:4.12",
        "mockito" to "org.mockito:mockito-core:1.10.19"
    )
}