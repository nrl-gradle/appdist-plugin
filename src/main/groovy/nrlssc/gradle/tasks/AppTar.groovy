package nrlssc.gradle.tasks

import nrlssc.gradle.AppDistPlugin
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.Tar

class AppTar extends Tar implements AppTask {
    @Internal
    Jar internalJar
    @Internal
    AppTaskManager manager


    Jar jar(final Closure jarConfig)
    {
        internalJar.configure(jarConfig)
        return internalJar
    }

    Jar pathJar(String jarName, String mainClassName, Closure configurePathingJar = null)
    {
        return manager.pathJar(jarName, mainClassName, configurePathingJar)
    }

    File appDir(File dir, String appInto = "app")
    {
        return manager.appDir(dir, appInto)
    }

    AppTar() {
        super()
        group = AppDistPlugin.TASK_GROUP
        description = 'Creates a tarred, distributable, executable, pathing internalJar with an entry-point at your "mainClassName".'
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        archiveClassifier.set('app')

        manager = new AppTaskManager(this)
        internalJar = manager.createInternalJarTask()

        into('lib'){
            from project.configurations.appClasspath
        }
    }

    @Override
    Jar getInternalJar() {
        return internalJar
    }

    @Override
    Map<String, List<File>> getSubAppDirs() {
        return manager.subAppDirs
    }
}
