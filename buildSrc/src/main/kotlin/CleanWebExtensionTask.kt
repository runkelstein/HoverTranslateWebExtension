package com.inspiritious.HoverTranslateWebExtension


import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

open class CleanWebExtensionTask : DefaultTask() {

    @Input
    var outputDir = "out";

    @TaskAction
    fun run() {

        val rootPath = project.rootProject.projectDir.path
        val deletePath = "$rootPath/$outputDir"

        logger.lifecycle("deleting files in $deletePath ...")
        project.delete(deletePath)
    }
}
