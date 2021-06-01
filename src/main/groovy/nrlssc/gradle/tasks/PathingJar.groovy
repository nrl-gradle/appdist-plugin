package nrlssc.gradle.tasks

import org.gradle.api.Project
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.tasks.bundling.Jar

class PathingJar extends Jar {

    static PathingJar createFrom(AppTask appTask, String jarName, String mainClassName, Closure config)
    {
        Project project = appTask.getProject()
        String taskName =   "$appTask.name-$jarName-PathingJar"
        PathingJar pjar = project.tasks.create(taskName, PathingJar.class)
        project.sourceSets.each{ ss ->
            ss.allSource.srcDirs.each { src ->
                if(src.exists()) pjar.inputs.dir(src.absolutePath)
            }
        }
        pjar.archiveFileName.set(jarName + ".jar")
        if(config != null)
        {
            pjar.configure(config)
        }
        pjar.doFirst{
            def cPath = ""
            List<String> handled = new ArrayList<>()
            project.configurations.compileClasspath.files.each {
                if(!handled.contains(it.name)) {
                    cPath += "lib/$it.name "
                    handled.add(it.name)
                }
            }
            project.configurations.runtimeClasspath.files.each {
                if(!handled.contains(it.name)) {
                    cPath += "lib/$it.name "
                    handled.add(it.name)
                }
            }

            def intJarName = appTask.internalJar.archiveFileName.get().replace("-appZip", "").replace("-appTar", "")
            cPath += "lib/$intJarName "
            appTask.subAppDirs.keySet().each {
                appTask.subAppDirs.get(it).each { dir ->
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
