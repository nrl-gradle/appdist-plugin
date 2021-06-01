package nrlssc.gradle.tasks

import nrlssc.gradle.AppDistPlugin
import org.gradle.api.Project
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.Tar

class AppTar extends Tar implements AppTask {

    public Jar internalJar
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
        Project project = getProject()
        manager = new AppTaskManager(this)

        doLast{
            internalJar.outputs.getFiles().each {it.delete()}
        }

        from {project.configurations.default}{
            into "lib"
        }

        
        archiveClassifier.set("app")
        internalJar = (Jar)project.tasks.create("$name-AppJar-tar", Jar.class)
        dependsOn(internalJar)
        from(internalJar){
            into("lib")
            rename('(.*)-appTar(.*)', '$1$2')
        }


        group = AppDistPlugin.TASK_GROUP
        description = 'Creates a tarred, distributable, executable, pathing internalJar with an entry-point at your "mainClassName".'

        internalJar.configure {
            archiveAppendix.set('appTar')
            from(project.sourceSets.main.output)
            description = 'Creates the project Jar that is used by appZip and appTar: you should not run this task directly.'
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
