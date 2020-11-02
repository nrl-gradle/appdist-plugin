package nrlssc.gradle.tasks

import org.gradle.api.Project
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.tasks.bundling.Jar

class PathingJar extends Jar {

    static PathingJar createFrom(AppTar appTar, String jarName, String mainClassName, Closure config)
    {
        Project project = appTar.getProject()
        String taskName =   "$appTar.name-$jarName-PathingJar"
        PathingJar pjar = project.tasks.create(taskName, PathingJar.class)
        project.sourceSets.each{ ss ->
            ss.allSource.srcDirs.each { src ->
                if(src.exists()) pjar.inputs.dir(src.absolutePath)
            }
        }
        pjar.archiveName = jarName + ".jar"
        if(config != null)
        {
            pjar.configure(config)
        }
        pjar.doFirst{
            def cPath = ""
            project.configurations.default.files.each {
                cPath +=  "lib/" + it.name + " "
            }

            cPath += "lib/" + appTar.internalJar.outputs.files.first().name.replace("-appTar", "") + " "
            appTar.subAppDirs.keySet().each {
                appTar.subAppDirs.get(it).each { dir ->
                    if (dir.exists() && dir.isDirectory()) {
                        cPath += "$it/ "
                        dir.eachFileRecurse { fl ->
                            def name = ''
                            name = fl.absolutePath.replace("\\", "/").split(dir.absolutePath.replace("\\", "/"))[1]
                            name = "$it" + name
                            cPath += name + " "
                        }
                    }
                }
            }


            manifest {
                attributes('Implementation-Title': project.name,
                        'Implementation-Version': project.version,
                        'Main-Class': mainClassName,
                        'Class-Path': cPath)
            }
        }

        return pjar
    }

    static PathingJar createFrom(AppZip appTar, String jarName, String mainClassName, Closure config)
    {
        Project project = appTar.getProject()
        String taskName =   "$appTar.name-$jarName-PathingJar"
        PathingJar pjar = project.tasks.create(taskName, PathingJar.class)
        project.sourceSets.each{ ss ->
            ss.allSource.srcDirs.each{ src ->
                if(src.exists()) pjar.inputs.dir(src.absolutePath)
            }
        }
        pjar.archiveName = jarName + ".jar"
        if(config != null)
        {
            pjar.configure(config)
        }
        pjar.doFirst{
            version = ''
            def cPath = ""
            project.configurations.default.files.each {
                cPath +=  "lib/" + it.name + " "
            }

            cPath += "lib/" + appTar.internalJar.outputs.files.first().name.replace("-appZip", "") + " "
            appTar.subAppDirs.keySet().each {
                appTar.subAppDirs.get(it).each { dir ->
                    if (dir.exists() && dir.isDirectory()) {
                        cPath += "$it/ "
                        dir.eachFileRecurse { fl ->
                            def name = ''
                            name = fl.absolutePath.replace("\\", "/").split(dir.absolutePath.replace("\\", "/"))[1]
                            name = "$it" + name
                            cPath += name + " "
                        }
                    }
                }
            }


            manifest {
                attributes('Implementation-Title': project.name,
                        'Implementation-Version': project.version,
                        'Main-Class': mainClassName,
                        'Class-Path': cPath)
            }
        }

        return pjar
    }


    PathingJar()
    {
        super()
        description = 'Creates a PathingJar for use in AppZip/AppTar - nonfunctional on its own.'
    }
    //TODO also need to remove 'Task' from all the tasks, and update them to the AppZip style configurations.
}
