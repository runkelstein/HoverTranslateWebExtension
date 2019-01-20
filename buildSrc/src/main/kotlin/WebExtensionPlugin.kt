package com.inspiritious.HoverTranslateWebExtension

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

open class WebExtensionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create<WebExtension>("webExtension")
        val task = project.getTasks().create("webExtension", WebExtensionTask::class.java);

        task.dependsOn("runDceKotlinJs");
        task.doFirst { task.outputDir = extension.outputDir }

        val cleanTask = project.getTasks().create("cleanWebExtension", CleanWebExtensionTask::class.java);
        cleanTask.doFirst { cleanTask.outputDir = extension.outputDir }

        if (project.isRoot()) {
            project.getTasksByName("clean", false).first().dependsOn(cleanTask);
        }
    }

}