object Dependencies {

    const val acraCore = "ch.acra:acra-core:${Versions.acraVer}"
    const val acraMail = "ch.acra:acra-mail:${Versions.acraVer}"
    const val acraNotification = "ch.acra:acra-notification:${Versions.acraVer}"
    const val kotPref = "com.chibatching.kotpref:kotpref:${Versions.kotPrefVer}"
    const val kotPrefEnumSupport = "com.chibatching.kotpref:enum-support:${Versions.kotPrefVer}"


    object AndroidX {

        const val activity = "androidx.activity:activity-ktx:${Versions.AndroidX.activityVer}"
        const val collection = "androidx.collection:collection-ktx:${Versions.AndroidX.collectionVer}"
        const val coordinatorLayout = "androidx.coordinatorlayout:coordinatorlayout:${Versions.AndroidX.coordinatorLayoutVer}"
        const val coreKtx = "androidx.core:core-ktx:${Versions.AndroidX.supportCoreVer}"
        const val startup = "androidx.startup:startup-runtime:${Versions.AndroidX.startupVer}"
        const val appCompat = "androidx.appcompat:appcompat:${Versions.AndroidX.appCompatVer}"
        const val constraintLayout = "androidx.constraintlayout:constraintlayout:${Versions.AndroidX.constraintLayoutVer}"
        const val lifecycleService = "androidx.lifecycle:lifecycle-service:${Versions.AndroidX.lifeCycleVer}"
        const val lifecycleRuntimeKtx = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.AndroidX.lifeCycleVer}"
        const val lifecycleViewModelKtx = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.AndroidX.lifeCycleVer}"
        const val lifecycleProcessKtx = "androidx.lifecycle:lifecycle-process:${Versions.AndroidX.lifeCycleVer}"

        const val recyclerView = "androidx.recyclerview:recyclerview:${Versions.AndroidX.recyclerViewVer}"



    }

    object Google {

        const val materialComponents = "com.google.android.material:material:${Versions.Google.materialComponentsVer}"
    }



    object Kotlin {
        const val kotlinCoroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.Kotlin.coroutinesVer}"
        const val kotlinCoroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.Kotlin.coroutinesVer}"
    }

    object Square {
        const val moshi = "com.squareup.moshi:moshi-kotlin:${Versions.Square.moshiVer}"
        const val okHttp = "com.squareup.okhttp3:okhttp:${Versions.Square.okHttpVer}"
        const val okHttpLoggingInterceptor = "com.squareup.okhttp3:logging-interceptor:${Versions.Square.okHttpVer}"
    }



    object Remoter{
        const val remoter ="com.josesamuel:remoter:${Versions.Remoter.remoterVer}"
        const val remoterBuilder ="com.josesamuel:remoter-builder:${Versions.Remoter.remoterVer}"
        const val remoterAnnotations = "com.josesamuel:remoter-annotations:${Versions.Remoter.remoterVer}"
        const val remoterParcelerApiDependency = "org.parceler:parceler-api:${Versions.Remoter.remoterParcelerDependencyVer}"
        const val remoterParcelerDependency = "org.parceler:parceler:${Versions.Remoter.remoterParcelerDependencyVer}"
    }

}
