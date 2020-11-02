package nrlssc.gradle.tasks

import nrlssc.gradle.AppDistPlugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.Tar

class AppTar extends Tar {

    public Jar internalJar

    Jar jar(final Closure jarConfig)
    {
        internalJar.configure(jarConfig)
        return internalJar
    }

    private List<Jar> pathJars = new ArrayList<>()
    Jar pathJar(String jarName, String mainClassName, Closure configurePathingJar = null)
    {
        PathingJar pjar = PathingJar.createFrom(this, jarName, mainClassName, configurePathingJar)
        pathJars.add(pjar)
        subAppDirs.each { name, files ->
            files.each {
                pjar.inputs.dir(it.absolutePath)
            }
        }
        dependsOn(pjar)
        from(pjar)
        outputs.file("${pjar.archiveFile.get().asFile.getAbsolutePath()}")

        doLast{
            pjar.outputs.getFiles().each {it.delete()}
        }
        
        return pjar
    }

    Map<String, List<File>> subAppDirs = new HashMap<>()
    File appDir(File dir, String appInto = "app")
    {
        if(dir.exists()) {
            String subPath = appInto
            if (subAppDirs.get(subPath) == null) {
                subAppDirs.put(subPath, new ArrayList<>())
            }
            subAppDirs.get(subPath).add(dir)

            from(dir) {
                into(subPath)
            }
            inputs.dir(dir.absolutePath)
            pathJars.each {
                it.inputs.dir(dir.absolutePath)
            }
        }
        return dir
    }

    AppTar() {
        super()
        Project project = getProject()

        doLast{
            internalJar.outputs.getFiles().each {it.delete()}
        }

        from {project.configurations.default}{
            into "lib"
        }

        classifier = "app"
        internalJar = (Jar)project.tasks.create("$name-AppJar", Jar.class)
        dependsOn(internalJar)
        from(internalJar){
            into("lib")
            rename('(.*)-appTar(.*)', '$1$2')
        }


        group = AppDistPlugin.TASK_GROUP
        description = 'Creates a tarred, distributable, executable, pathing internalJar with an entry-point at your "mainClassName".'

        internalJar.configure {
            appendix = 'appTar'
            from(project.sourceSets.main.output)
            description = 'Creates the project Jar that is used by appZip and appTar: you should not run this task directly.'
        }

        outputs.file("${internalJar.archiveFile.get().asFile.getAbsolutePath()}")
    }

}
