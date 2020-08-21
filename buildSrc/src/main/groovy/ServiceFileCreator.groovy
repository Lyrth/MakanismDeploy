import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Jar

class ServiceFileCreator extends DefaultTask {

    @Input
    int cacheHash = -1

    @OutputDirectory
    File generatedFilesDir = project.file("$project.buildDir/generated/services")

    private String provides
    protected Map<String, String> serviceProviders = [:]

    Jar target

    @TaskAction
    def execute(){
        this.target.metaInf {
            serviceProviders.each { service, content ->
                File file = new File(this.generatedFilesDir, service)
                file.text = content
                from(file) {
                    into "services"
                }
            }
        }
    }

    /**
     * The class path of the service interface that is implemented
     * @param clazz Class path of the service interface that is implemented
     * @return this
     */
    @Input
    ServiceFileCreator provides(String clazz){
        Objects.requireNonNull(clazz)
        this.provides = clazz
        cacheHash = Objects.hash(cacheHash, clazz)
        return this
    }

    /**
     * The class path of the service provider
     * @param clazz Class path of the service provider
     */
    @Input
    void with(String clazz){
        if (this.provides){
            cacheHash = Objects.hash(cacheHash, clazz)
            if (!this.serviceProviders[this.provides])
                this.serviceProviders[this.provides] = clazz + '\n'
            else
                this.serviceProviders[this.provides] = this.serviceProviders[this.provides] + clazz + '\n'
            this.provides = null
        }
    }
}
