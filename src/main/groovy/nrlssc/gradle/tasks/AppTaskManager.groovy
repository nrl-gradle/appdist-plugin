package nrlssc.gradle.tasks

import org.gradle.api.DefaultTask
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
}
