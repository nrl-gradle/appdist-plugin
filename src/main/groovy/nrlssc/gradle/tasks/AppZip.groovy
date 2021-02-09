package nrlssc.gradle.tasks

import nrlssc.gradle.AppDistPlugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.Zip

import java.nio.file.Paths

class AppZip extends Zip {

    Jar internalJar

    Jar jar(final Closure jarConfig)
    {
        internalJar.configure(jarConfig)
        return internalJar
    }

    private List<Jar> pathJars = new ArrayList<>()
    Jar pathJar(String jarName, String mainClassName, Closure configurePathingJar = null)
    {
        PathingJar pjar = PathingJar.createFrom(this, jarName, mainClassName, configurePathingJar)
        pjar.archiveAppendix.set('zip')
        pathJars.add(pjar)
        subAppDirs.each { name, files ->
            files.each {
                pjar.inputs.dir(it.absolutePath)
            }
        }
        dependsOn(pjar)
        from(pjar){
            rename {
                rename('(.*)-zip(.*)', '$1$2')
            }
        }

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
            if(subAppDirs.get(subPath) == null)
            {
                subAppDirs.put(subPath, new ArrayList<>())
            }
            subAppDirs.get(subPath).add(dir)

            from(dir){
                into(subPath)
            }

            inputs.dir(dir.absolutePath)

            pathJars.each {
                it.inputs.dir(dir.absolutePath)
            }
        }

        return dir
    }



    AppZip() {
        super()
        Project project = getProject()

        doLast{
            internalJar.outputs.getFiles().each {it.delete()}
        }
        
        from {project.configurations.default}{
            into "lib"
        }

        archiveClassifier.set("app")
        internalJar = (Jar)project.tasks.create("$name-AppJar-zip", Jar.class)
        dependsOn(internalJar)
        from(internalJar){
            into("lib")
            rename('(.*)-appZip(.*)', '$1$2')
        }


        group = AppDistPlugin.TASK_GROUP
        description = 'Creates a zipped, distributable, set of pathing jars with a default entry-point at your "mainClassName".'

        internalJar.configure {
            archiveAppendix.set('appZip')
            from(project.sourceSets.main.output)
            description = 'Creates the project Jar that is used by appZip and appTar: you should not run this task directly.'
        }

    }




}
