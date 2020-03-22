object Versions {
    const val architectureComponents = "2.1.0"
    const val buildTools = "29.0.0"
    const val cardView = "1.0.0"
    const val cling = "2.1.1"
    const val compileSdk = 29
    const val constraintLayout = "2.0.0-beta4"
    const val coroutines = "1.3.4"
    const val dagger = "2.24"
    const val detekt = "1.0.0"
    const val glide = "4.11.0"
    const val jetty = "8.2.0.v20160908"
    const val kotlin = "1.3.70"
    const val minSdk = 21
    const val navigation = "2.3.0-alpha03"
    const val material = "1.2.0-alpha05"
    const val okHttp = "3.9.1"
    const val recyclerView = "1.1.0"
    const val supportLibrary = "1.1.0"
    const val targetSdk = 29
    const val test = "1.2.0"
    const val versionCode = 50
    const val versionName = "2.0.0"
}

object ClasspathDependencies {
    const val androidTools = "com.android.tools.build:gradle:3.6.0"
    const val kotlinGradle = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    const val navigationSafeArgs =
        "androidx.navigation:navigation-safe-args-gradle-plugin:${Versions.navigation}"
}

object Dependencies {
    const val kotlin = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"
    const val timber = "com.jakewharton.timber:timber:4.7.1"
    const val okHttp = "com.squareup.okhttp3:okhttp:4.2.2"

    val androidx = mapOf(
        "appCompat" to "androidx.appcompat:appcompat:${Versions.supportLibrary}",
        "cardView" to "androidx.cardview:cardview:${Versions.cardView}",
        "constraintLayout" to "androidx.constraintlayout:constraintlayout:${Versions.constraintLayout}",
        "material" to "com.google.android.material:material:${Versions.material}",
        "lifecycle" to mapOf(
            "extensions" to "androidx.lifecycle:lifecycle-extensions:${Versions.architectureComponents}",
            "liveDataKtx" to "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.architectureComponents}",
            "viewModelKtx" to "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.architectureComponents}",
            "lifecycleKtx" to "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.architectureComponents}"
        ),
        "navigation" to mapOf(
            "fragment" to "androidx.navigation:navigation-fragment-ktx:${Versions.navigation}",
            "ui" to "androidx.navigation:navigation-ui-ktx:${Versions.navigation}",
            "dynamicFeatures" to "androidx.navigation:navigation-dynamic-features-fragment:${Versions.navigation}"
        ),
        "preference" to "androidx.preference:preference:${Versions.supportLibrary}",
        "recyclerView" to "androidx.recyclerview:recyclerview:${Versions.recyclerView}"
    )

    val coroutines = mapOf(
        "core" to "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}",
        "rxJava2" to "org.jetbrains.kotlinx:kotlinx-coroutines-rx2:${Versions.coroutines}"
    )

    val dagger = mapOf(
        "annotation" to "javax.inject:javax.inject:1",
        "compiler" to "com.google.dagger:dagger-compiler:${Versions.dagger}",
        "core" to "com.google.dagger:dagger:${Versions.dagger}"
    )

    val glide = mapOf(
        "compiler" to "com.github.bumptech.glide:compiler:${Versions.glide}",
        "core" to "com.github.bumptech.glide:glide:${Versions.glide}",
        "recyclerView" to "com.github.bumptech.glide:recyclerview-integration:${Versions.glide}"
    )

    val rx = mapOf(
        "android" to "io.reactivex.rxjava2:rxandroid:2.0.1",
        "kotlin" to "com.github.ReactiveX:rxKotlin:2.3.0"
    )

    val test = mapOf(
        "junit" to "junit:junit:4.12",
        "mockito" to "org.mockito:mockito-core:1.10.19"
    )
}
