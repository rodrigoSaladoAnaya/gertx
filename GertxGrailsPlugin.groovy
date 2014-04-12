import org.vertx.java.platform.PlatformLocator

class GertxGrailsPlugin {
    def version = "0.0.1"
    def grailsVersion = "2.0 > *"
    def title = "Gertx Plugin"
    def description = 'A Grails plugin that integrates Vert.x'
    def documentation = "http://grails.org/plugin/gertx"
    def license = "APACHE"
    def developers = [
        [name: 'Rodrigo Salado Anaya', email: 'rodrigo.salado.anaya@gmail.com']
    ]
    def issueManagement = [system: 'GITHUB', url: 'https://github.com/rodrigoSaladoAnaya/gertx/issues']
    def scm = [url: 'https://github.com/rodrigoSaladoAnaya/gertx']

    def doWithSpring = {
        platformManager(PlatformLocator)
    }
}
