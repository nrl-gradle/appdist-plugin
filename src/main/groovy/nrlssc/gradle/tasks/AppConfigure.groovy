package nrlssc.gradle.tasks


import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.Task

class AppConfigure extends DefaultTask {

    private Project project
    private AppZip appZip
    private AppTar appTar
    private boolean initialized

    void init(Project project, AppZip appZip, AppTar appTar)
    {
        if(!initialized) {
            this.project = project
            this.appZip = appZip
            this.appTar = appTar
            initialized = true
        }
    }


    @Override
    Task configure(Closure closure) {
        appZip.configure(closure)
        appTar.configure(closure)
    }
}
