plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
}


gradlePlugin {
    plugins {
       
    }
}

repositories {
    google()
    mavenCentral()

}

dependencies {

 

    //Kotlin
    implementation(kotlin("gradle-plugin", "1.6.21"))// or implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.30")  https://plugins.gradle.org/plugin/org.jetbrains.kotlin.android.extensions - https://mvnrepository.com/artifact/org.jetbrains.kotlin/kotlin-gradle-plugin  https://kotlinlang.org/docs/reference/using-gradle.html

    //Also need to update root build.gradle.kts dependencies section
    implementation("com.android.tools.build:gradle:7.3.0-alpha09")

  


}