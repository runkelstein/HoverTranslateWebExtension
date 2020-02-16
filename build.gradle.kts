import com.runkelstein.hoverTranslateWebExtension.buildsrc.*
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinJsDce

group = "com.runkelstein.hoverTranslateWebExtension"
version = "1.0-SNAPSHOT"


plugins {
    id("kotlin2js") version "1.3.61" apply true
    id("kotlinx-serialization") version "1.3.61" apply true
}

repositories {
    jcenter()
    mavenCentral()
    maven("https://www.jitpack.io")
}

apply {
    plugin("kotlin-dce-js")
    plugin("kotlin2js")
}

subprojects  {

    apply {
        plugin("kotlin2js")
        plugin("kotlin-dce-js")
        plugin("kotlinx-serialization")
    }

    repositories {
        jcenter()
        mavenCentral()
        maven("https://www.jitpack.io")
        maven("https://kotlin.bintray.com/kotlinx" )
    }

    dependencies {
        implementation(kotlin("stdlib-js", Versions.kotlin))
        implementation("com.github.cypressious.kotlin-webextensions-declarations", "webextensions-declarations", Versions.kotlinWebextensions)
        implementation("org.jetbrains.kotlinx", "kotlinx-serialization-runtime-js", Versions.kotlinxSerialization)
        implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core-js", Versions.kotlinxCoroutines)
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

allprojects {

    val modules = setOf("background_script", "content_script", "options")

    if (name in modules || isRoot()) {
        apply<WebExtensionPlugin>();
        configure<WebExtension> { outputDir = "out" }
    }

    if (name in modules) {
        repositories {
            jcenter()
        }

        dependencies {
            implementation("org.jetbrains.kotlinx","kotlinx-html-js", Versions.kotlinxHtml)
            implementation(project(":core"))
        }
    }

}