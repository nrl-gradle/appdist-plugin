package nrlssc.gradle.tasks

import org.gradle.api.Task
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.bundling.Jar

interface AppTask extends Task {
    @Internal
    Jar getInternalJar()
    @Internal
    Map<String, List<File>> getSubAppDirs()
}