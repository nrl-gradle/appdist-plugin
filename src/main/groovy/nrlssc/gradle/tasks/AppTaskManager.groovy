package nrlssc.gradle.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.AbstractCopyTask
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class AppTaskManager {
    private static Logger logger = LoggerFactory.getLogger(AppTaskManager.class)

    AppTask task

    AppTaskManager(AppTask task){
        this.task = task
    }

    protected List<Jar> pathJars = new ArrayList<>()
    Jar pathJar(String jarName, String mainClassName, Closure configurePathingJar = null)
    {

        PathingJar pjar = PathingJar.createFrom(task, jarName, mainClassName, configurePathingJar)
        pathJars.add(pjar)
        subAppDirs.each { name, files ->
            files.each {
                pjar.inputs.dir(it.absolutePath)
            }
        }
        task.dependsOn(pjar)
        task.from(pjar) {
            rename {
                "${jarName}.jar"
            }
        }

        task.project.gradle.buildFinished {
            pjar.outputs.getFiles().each {if(it.exists()) it.delete()}
        }


        return pjar
    }

    protected Map<String, List<File>> subAppDirs = new HashMap<>()
    File appDir(File dir, String appInto = "app")
    {
        if(dir.exists()) {
            String subPath = appInto
            if(subAppDirs.get(subPath) == null)
            {
                subAppDirs.put(subPath, new ArrayList<>())
            }
            subAppDirs.get(subPath).add(dir)

            task.from(dir){
                into(subPath)
            }

            task.inputs.dir(dir.absolutePath)

            pathJars.each {
                it.inputs.dir(dir.absolutePath)
            }
        }

        return dir
    }

    Jar createInternalJarTask(){
        String mainName = task.name
        String type = task instanceof AppZip ? 'zip' : 'tar'

        TaskProvider<Jar> internalJar = task.project.tasks.register("$mainName-AppJar-$type", Jar.class, {
            it.configure {
                archiveAppendix.set("$mainName")
                from(project.sourceSets.main.output)
                description = 'Creates the project Jar that is used by an app task: you should not run this task directly.'
                outputs.upToDateWhen { false }
            }
        })
        task.dependsOn(internalJar)

        task.from(internalJar){
            into 'lib'
            rename("(.*)-$mainName(.*)", '$1$2')
        }

        task.project.gradle.buildFinished {
            internalJar.get().outputs.getFiles().each {if(it.exists()) it.delete()}
        }
        return internalJar.get()
    }
}
