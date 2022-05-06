object Versions {

    const val kotlinGradleVersion = "1.6.21" //Also update buildSrc/build.gradle.kts manually
    const val androidGradleVersion = "7.3.0-alpha09" //Also update buildSrc/build.gradle.kts manually

    /**
     *
     *
     * Acra 5.9.0-rc2 has below issue and prevents crash notification
     * java.lang.NoSuchMethodException: org.acra.config.MailSenderConfiguration.<init> [boolean, class java.lang.String, boolean, class java.lang.String, class java.lang.String, class java.lang.String, int, class kotlin.jvm.internal.DefaultConstructorMarker]
     * at org.acra.config.MailSenderConfigurationBuilder.build(MailSenderConfigurationDsl.kt:105)
     * at org.acra.config.MailSenderConfigurationKt.mailSender(MailSenderConfiguration.kt:78)
     *
     *
     */
    const val acraVer = "5.8.4" //https://github.com/ACRA/acra
    const val kotPrefVer = "2.13.2" //https://github.com/chibatching/Kotpref


    object AndroidX {
        const val activityVer = "1.5.0-beta01" //https://developer.android.com/jetpack/androidx/releases/activity
        const val annotationVer = "1.3.0"
        const val appCompatVer = "1.5.0-alpha01" // https://developer.android.com/jetpack/androidx/releases/appcompat
        const val collectionVer = "1.2.0" //https://developer.android.com/jetpack/androidx/releases/collection
        const val constraintLayoutVer = "2.1.3" //https://developer.android.com/jetpack/androidx/releases/constraintlayout
        const val coordinatorLayoutVer = "1.2.0" //https://developer.android.com/jetpack/androidx/releases/coordinatorlayout
        const val fragmentVer = "1.5.0-beta01" //https://developer.android.com/jetpack/androidx/releases/fragment
        const val exifInterfaceVer = "1.3.3"
        const val startupVer = "1.2.0-alpha01" // https://developer.android.com/jetpack/androidx/releases/startup
        const val transitionVer = "1.4.1"
        const val lifeCycleVer = "2.5.0-beta01" // https://developer.android.com/jetpack/androidx/releases/lifecycle
        const val preferenceVer = "1.2.0" //https://developer.android.com/jetpack/androidx/releases/preference
        const val recyclerViewVer = "1.3.0-alpha02" //https://developer.android.com/jetpack/androidx/releases/recyclerview
        const val recyclerViewSelectionVer = "1.2.0-alpha01"
        const val roleManagerVer = "1.1.0-rc01" //https://developer.android.com/jetpack/androidx/releases/core
        const val roomVer = "2.4.2" //https://developer.android.com/jetpack/androidx/releases/room
        const val supportCoreVer = "1.8.0-beta01" //https://developer.android.com/jetpack/androidx/releases/core
        const val viewPager2Ver = "1.1.0-beta01" //https://developer.android.com/jetpack/androidx/releases/viewpager2
        const val workManagerVer = "2.7.1" // https://developer.android.com/jetpack/androidx/releases/work
        const val windowVer ="1.1.0-alpha01" // https://developer.android.com/jetpack/androidx/releases/window
    }

    object Google {
        const val fireBasePlatformVer = "29.3.1" //https://firebase.google.com/support/release-notes/android
        const val playServicesAuthVer = "20.1.0" //https://developers.google.com/android/guides/releases
        const val playServicesBaseVer = "18.0.1" //https://developers.google.com/android/guides/releases
        const val playServicesTasksVer = "18.0.1" //https://developers.google.com/android/guides/releases
        const val playCoreVerKtx = "1.8.1" //https://developer.android.com/guide/playcore  and https://developer.android.com/reference/com/google/android/play/core/release-notes
        const val libPhoneNumberVer = "8.12.47" // https://mvnrepository.com/artifact/com.googlecode.libphonenumber/libphonenumber
        const val libPhoneNumberGeoCoderVer = "2.183" // https://mvnrepository.com/artifact/com.googlecode.libphonenumber/geocoder
        const val materialComponentsVer = "1.6.0" // https://github.com/material-components/material-components-android/releases
        const val playBillingVer ="4.1.0" //https://developer.android.com/google/play/billing/release-notes
    }



    object Kotlin {
        const val coroutinesVer = "1.6.1" //https://github.com/Kotlin/kotlinx.coroutines
        const val coroutinesPlayServicesVer = "1.6.1" //https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-play-services and https://github.com/Kotlin/kotlinx.coroutines/tree/master/integration/kotlinx-coroutines-play-services
        const val serializationJsonVersion = "1.6.10" //https://github.com/Kotlin/kotlinx.serialization
    }

    object Square {
        const val moshiVer = "1.13.0" //https://github.com/square/moshi
        const val okHttpVer = "4.9.1" //https://square.github.io/okhttp/
    }


    object Remoter {
        const val remoterVer = "2.0.4"
        const val remoterParcelerDependencyVer ="1.1.12"
    }
}