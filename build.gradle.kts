group = "com.inspiritious.HoverTranslateWebExtension"
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


buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
    publishAlways()
}

apply {
    plugin("kotlin-dce-js")
    plugin("kotlin2js")
    plugin("webExtension")
}

subprojects  {

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
        compile("com.github.cypressious.kotlin-webextensions-declarations", "webextensions-declarations", Versions.kotlinWebextensions)
        compile("org.jetbrains.kotlinx", "kotlinx-serialization-runtime-js", Versions.kotlinxSerialization)
        compile("org.jetbrains.kotlinx", "kotlinx-coroutines-core-js", Versions.kotlinxCoroutines)
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

    apply {
        plugin("webExtension")
    }

    repositories {
        jcenter()
    }

    dependencies {
        compile("org.jetbrains.kotlinx","kotlinx-html-js", Versions.kotlinxHtml)
        implementation(project(":core"))
    }
}

project(":content_script") {

    apply {
        plugin("webExtension")
    }

    repositories {
        jcenter()
    }

    dependencies {
        compile("org.jetbrains.kotlinx","kotlinx-html-js", Versions.kotlinxHtml)
        implementation(project(":core"))
    }
}

project(":background_script") {

    apply {
        plugin("webExtension")
    }

    repositories {
        jcenter()
    }

    dependencies {
        compile("org.jetbrains.kotlinx","kotlinx-html-js", Versions.kotlinxHtml)
        implementation(project(":core"))
    }
}