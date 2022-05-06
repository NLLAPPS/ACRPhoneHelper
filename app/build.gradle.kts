plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlin-parcelize")

}

android {
    namespace = "com.nll.helper"
    compileSdk = 32
    defaultConfig {
        versionCode = 1
        versionName = "1.0"
        minSdk = 29
        targetSdk =31
        buildConfigField("Integer", "MINIMUM_CLIENT_VERSION_CODE", "${App.appHelperMinimumClientVersionCode}")
        buildConfigField("Integer", "MINIMUM_SERVER_VERSION_CODE", "${App.appHelperMinimumServerVersionCode}")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }


    buildTypes {
        release {
            isMinifyEnabled = true
        }
        debug {
            isMinifyEnabled = true
        }
    }


}

dependencies {
    implementation(Dependencies.Kotlin.kotlinCoroutinesCore)
    implementation(Dependencies.Kotlin.kotlinCoroutinesAndroid)
    implementation(Dependencies.AndroidX.recyclerView)
    implementation(Dependencies.AndroidX.coreKtx)
    implementation(Dependencies.AndroidX.appCompat)
    implementation(Dependencies.Google.materialComponents)
    implementation(Dependencies.AndroidX.constraintLayout)
    implementation(Dependencies.AndroidX.activity)
    implementation(Dependencies.AndroidX.lifecycleViewModelKtx)
    implementation(Dependencies.AndroidX.startup)
    implementation(Dependencies.kotPref)
    implementation(Dependencies.kotPrefEnumSupport)
    implementation(Dependencies.AndroidX.lifecycleProcessKtx)
    implementation(Dependencies.AndroidX.lifecycleService)
    implementation(Dependencies.AndroidX.coordinatorLayout)
    implementation(Dependencies.Google.materialComponents)
    implementation(Dependencies.Remoter.remoterAnnotations)
    kapt(Dependencies.Remoter.remoter)
    //If using kotlin coroutines, include following to make even the service connection simpler
    implementation(Dependencies.Remoter.remoterBuilder)
    /**
     *  We need this because we are using registerProcessDeath to listen to client crash so we can stop recording and passing Binder() requires
     *  Parceller.
     *
     *  Kotlin parcelize does not seem to work yet
     *  https://github.com/josesamuel/remoter/issues/6
     *
     */
    implementation(Dependencies.Remoter.remoterParcelerApiDependency)
    kapt(Dependencies.Remoter.remoterParcelerDependency)

    implementation(Dependencies.Square.okHttp)
    implementation(Dependencies.Square.okHttpLoggingInterceptor)
    implementation(Dependencies.acraCore)
    implementation(Dependencies.acraMail)
    implementation(Dependencies.acraNotification)
    implementation("io.karn:notify:1.4.0")


}