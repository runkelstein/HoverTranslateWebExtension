import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

group = "com.inspiritious"
version = "1.0-SNAPSHOT"

plugins {
    id("kotlin2js") version Versions.kotlin apply true
    `build-scan`
}

repositories {
    jcenter()
    mavenCentral()
    maven("https://www.jitpack.io")
}

dependencies {
    compile(kotlin("stdlib-js", Versions.kotlin) )
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
    }

    repositories {
        jcenter()
        mavenCentral()
        maven("https://www.jitpack.io")
    }

    dependencies {
        compile(kotlin("stdlib-js", Versions.kotlin))
        compile("com.github.cypressious.kotlin-webextensions-declarations", "webextensions-declarations", Versions.kotlinWebextensions);
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
    dependencies {
        implementation(project(":dictionaryLib"))
    }
}