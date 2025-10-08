package nrlssc.gradle

import nrlssc.gradle.tasks.AppConfigure
import nrlssc.gradle.tasks.AppTar
import nrlssc.gradle.tasks.AppTask
import nrlssc.gradle.tasks.AppZip
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.distribution.plugins.DistributionPlugin
import org.gradle.api.tasks.JavaExec
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class AppDistPlugin implements Plugin<Project>{
    private static Logger logger = LoggerFactory.getLogger(AppDistPlugin.class)

    public final static String TASK_GROUP = 'appdist'
    
    private Project project
    @Override
    void apply(Project target) {
        this.project = target

        project.pluginManager.withPlugin('java') {
            project.configurations{
                appClasspath{
                    extendsFrom project.configurations.default
                }
            }
            AppZip azTask = project.tasks.register("appZip", AppZip.class).get()
            AppTar atTask = project.tasks.register("appTar", AppTar.class).get()

            project.pluginManager.withPlugin('nrlssc.hgit') {
                Task rvf = project.tasks.getByName('rootVersionFile')
                azTask.from(rvf)
                azTask.dependsOn(rvf)
                atTask.from(rvf)
                atTask.dependsOn(rvf)
            }




            azTask.appDir(project.file("app"))
            atTask.appDir(project.file("app"))
            AppConfigure app = project.tasks.register("app", AppConfigure.class).get()

            app.init(project, azTask, atTask)

            JavaExec runTask = project.tasks.register("run", JavaExec.class).get()
            runTask.configure {
                group = TASK_GROUP
                classpath = project.sourceSets.main.runtimeClasspath + project.sourceSets.test.runtimeClasspath
            }

            project.gradle.projectsEvaluated {
                if (!app.getMainClassName().equalsIgnoreCase('unspecified')) {
                    azTask.pathJar("$project.name-RunMain")
                    atTask.pathJar("$project.name-RunMain")
                    runTask.mainClass.set(app.getMainClassName())
                }
            }
        }

        project.plugins.withType(DistributionPlugin){
            ConfigureDistributionAndHgit()
        }
        project.pluginManager.withPlugin('nrlssc.hgit'){
            ConfigureDistributionAndHgit()
        }
    }
    
    private void ConfigureDistributionAndHgit()
    {
        if(project.pluginManager.findPlugin('nrlssc.hgit') && project.plugins.findPlugin(DistributionPlugin.class)) {
            Task rvf = project.tasks.getByName('rootVersionFile')
            project.tasks.distTar {
                dependsOn(rvf)
                from(rvf)
            }
            project.tasks.distZip {
                dependsOn(rvf)
                from(rvf)
            }
            project.distributions {
                it.main {
                    it.contents {
                        it.from { rvf }
                    }
                }
            }
        }
    }
}
