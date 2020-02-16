package com.runkelstein.hoverTranslateWebExtension.buildsrc

import org.gradle.api.Project

fun Project.isRoot() = this.rootProject.name == this.name