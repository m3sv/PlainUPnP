object Versions {
    const val architectureComponents = "2.2.0"
    const val buildTools = "29.0.3"
    const val cardView = "1.0.0"
    const val cling = "3.0.0"
    const val compileSdk = 29
    const val constraintLayout = "2.0.0-beta4"
    const val coroutines = "1.3.5"
    const val coreTesting = "2.1.0"
    const val dagger = "2.27"
    const val detekt = "1.0.0"
    const val jetty = "8.2.0.v20160908"
    const val kotlin = "1.3.71"
    const val minSdk = 21
    const val nanohttpd = "2.3.1"
    const val navigation = "2.3.0-rc01"
    const val material = "1.2.0-beta01"
    const val recyclerView = "1.1.0"
    const val supportLibrary = "1.1.0"
    const val targetSdk = 29
    const val test = "1.2.0"
    const val versionCode = 58
    const val versionName = "2.4.0"
}

object ClasspathDependencies {
    const val androidTools = "com.android.tools.build:gradle:4.0.0"
    const val kotlinGradle = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    const val navigationSafeArgs =
        "androidx.navigation:navigation-safe-args-gradle-plugin:${Versions.navigation}"
}

object Dependencies {
    const val kotlin = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"
    const val timber = "com.jakewharton.timber:timber:4.7.1"
    const val glide = "com.github.bumptech.glide:glide:4.11.0"
    const val javaxCdi = "javax.enterprise:cdi-api:2.0.SP1"
    const val fastScroll = "me.zhanghai.android.fastscroll:library:1.1.3"

    val androidx = mapOf(
        "appCompat" to "androidx.appcompat:appcompat:${Versions.supportLibrary}",
        "cardView" to "androidx.cardview:cardview:${Versions.cardView}",
        "constraintLayout" to "androidx.constraintlayout:constraintlayout:${Versions.constraintLayout}",
        "coreKtx" to "androidx.core:core-ktx:1.3.0",
        "coreTesting" to "androidx.arch.core:core-testing:${Versions.coreTesting}",
        "material" to "com.google.android.material:material:${Versions.material}",
        "lifecycle" to mapOf(
            "extensions" to "androidx.lifecycle:lifecycle-extensions:${Versions.architectureComponents}",
            "liveDataKtx" to "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.architectureComponents}",
            "viewModelKtx" to "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.architectureComponents}",
            "lifecycleKtx" to "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.architectureComponents}"
        ),
        "navigation" to mapOf(
            "fragment" to "androidx.navigation:navigation-fragment-ktx:${Versions.navigation}",
            "ui" to "androidx.navigation:navigation-ui-ktx:${Versions.navigation}"
        ),
        "preference" to "androidx.preference:preference:${Versions.supportLibrary}",
        "recyclerView" to "androidx.recyclerview:recyclerview:${Versions.recyclerView}"
    )

    val coroutines = mapOf(
        "core" to "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}",
        "test" to "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.coroutines}"
    )

    val dagger = mapOf(
        "annotation" to "javax.inject:javax.inject:1",
        "compiler" to "com.google.dagger:dagger-compiler:${Versions.dagger}",
        "core" to "com.google.dagger:dagger:${Versions.dagger}"
    )

    val nanohttpd = mapOf(
        "core" to "org.nanohttpd:nanohttpd:${Versions.nanohttpd}",
        "webserver" to "org.nanohttpd:nanohttpd-webserver:${Versions.nanohttpd}"
    )

    val test = mapOf(
        "junit" to "org.junit.jupiter:junit-jupiter:5.6.2",
        "mockito" to "com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0"
    )

}
