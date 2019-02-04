package com.inspiritious.HoverTranslateWebExtension

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

open class WebExtensionTask : DefaultTask() {

    @Input
    var outputDir = "out"


    @TaskAction
    fun run()
    {
        logger.lifecycle("Copy files to $outputDir ...")

        val rootPath = project.rootProject.projectDir.path

        val projectName = if (project.isRoot()) "" else project.name

        // copy script files
        project.copy {
            from(project.buildDir.path + "/kotlin-js-min/main") {
                include("*.js")
            }

            into("$rootPath/$outputDir/$projectName")
        }

        // copy resources
        project.copy {
            from(project.projectDir.path + "/src/main/resources") {
                include("*")
                include("*/*")
            }

            into("$rootPath/$outputDir/$projectName")
        }

        logger.lifecycle("Finish build.")
    }

}