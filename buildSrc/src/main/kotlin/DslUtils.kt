package com.inspiritious.HoverTranslateWebExtension

import org.gradle.api.Project

fun Project.isRoot() = this.rootProject.name == this.name