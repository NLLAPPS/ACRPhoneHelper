import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
plugins {

    `kotlin-dsl`
}
buildscript {


    dependencies {


        classpath("com.android.tools.build:gradle:7.3.0-alpha09") 

        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlinGradleVersion}") 

    }
}

allprojects {


    tasks.withType<KotlinCompile>().all {
        sourceCompatibility = JavaVersion.VERSION_11.toString()
        targetCompatibility = JavaVersion.VERSION_11.toString()
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_11.toString()
        }
    }
}
