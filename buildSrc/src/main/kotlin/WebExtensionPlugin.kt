package com.inspiritious.HoverTranslateWebExtension

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Copy
import org.gradle.kotlin.dsl.extra

open class WebExtensionPlugin : Plugin<Project> {
    override fun apply(project: Project) {

            project.getTasks()
                .create("webExtension", WebExtensionTask::class.java)
                .dependsOn("runDceKotlinJs");


    }

}