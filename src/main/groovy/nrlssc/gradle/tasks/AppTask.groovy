package nrlssc.gradle.tasks

import org.gradle.api.Task
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.bundling.Jar

interface AppTask extends Task {
    @Input
    Jar getInternalJar()
    Map<String, List<File>> getSubAppDirs()
}