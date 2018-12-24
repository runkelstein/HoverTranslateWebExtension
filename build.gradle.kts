group = "com.inspiritious"
version = "1.0-SNAPSHOT"

plugins {
    id("kotlin2js") version Versions.kotlin apply true
    id("kotlinx-serialization") version Versions.kotlin apply true
    `build-scan`
}

repositories {
    jcenter()
    mavenCentral()
    maven("https://www.jitpack.io")
}

dependencies {
    compile(kotlin("stdlib-js", Versions.kotlin) )
    implementation(project(":core"))
}

buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
    publishAlways()
}

allprojects  {

    apply {
        plugin("kotlin-dce-js")
        plugin("kotlin2js")
        plugin("kotlinx-serialization")
    }

    repositories {
        jcenter()
        mavenCentral()
        maven("https://www.jitpack.io")
        maven("https://kotlin.bintray.com/kotlinx" )
    }

    dependencies {
        compile(kotlin("stdlib-js", Versions.kotlin))
        compile("com.github.cypressious.kotlin-webextensions-declarations", "webextensions-declarations", Versions.kotlinWebextensions);
        compile("org.jetbrains.kotlinx", "kotlinx-serialization-runtime-js", Versions.kotlinSerialization)
    }


    tasks {
        compileKotlin2Js {
            kotlinOptions {
                sourceMap = true
                kotlinOptions.sourceMapEmbedSources = "always"
            }
        }
    }


}

project(":options") {

    repositories {
        jcenter()
    }

    dependencies {
        compile("org.jetbrains.kotlinx","kotlinx-html-js", Versions.kotlinHtml)
        implementation(project(":core"))
    }
}

project(":content_script") {

    repositories {
        jcenter()
    }

    dependencies {
        compile("org.jetbrains.kotlinx","kotlinx-html-js", Versions.kotlinHtml)
        implementation(project(":core"))
    }
}