package nrlssc.gradle.tasks

import org.gradle.api.Task
import org.gradle.api.tasks.bundling.Jar

interface AppTask extends Task {
    Jar getInternalJar()
    Map<String, List<File>> getSubAppDirs()
}