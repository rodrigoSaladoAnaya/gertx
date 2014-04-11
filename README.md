gertx
=====

Is a grails-plugin that allows use Vert.x

verion 0.0.1

https://github.com/rodrigoSaladoAnaya/gertx

Use the PlatformManager to carry Vertx to Grails and expose a EventBus instance to use into de GrailsApp

The plugin adds two JavaScript libraries sockjs.min.js and vertxbus.js in web-app/js/libs/ directory, so you can use the power of EventBus from the view more comfortably.

Also adds a verticle serving as administrator other verticles in grails-app/vertx/ directory called VerticleManager.groovy that you can access e.g. via telnet to deploy, undeploy verticles and list installed and uninstalled verticles.

To use gertx inside your grails application runtime you have to:
1: Add "gertx: 0.1" in BuildConfig.groovy as a plugin
2: Add into BootStrap.groovy 'def gertxService'
2.1: Init Vertx with 'gertxService.initVertx()' If you just want to use within the services
2.2: And if you want to use external verticles help you install and uninstall add 'gertxService.runVerticleManager()'
e.g.:
<code>
class BootStrap {
    def gertxService
    def testVertxService
    def init = { servletContext ->
        gertxService.initVertx()
        gertxService.runVerticleManager()
        testVertxService.testVerticle()
    }
}
</code>
2.3: If you use the VerticleManager be accessed from a terminal with the command 'telnet localhost 5436' to manage verticles.
2.3.1: To add a new verticle must create it in the folder grails-app/vertx/
e.g.: If you want to create a EventBus Bridge called WebEventBus.groovy create a file create a file with that name in the folder grails-app/vertx/ and type something like:
<code>
def server = vertx.createHttpServer()
server.requestHandler { req ->
}
vertx.createSockJSServer(server).bridge(
        prefix: '/eventbus', [[:]], [[:]]
)
server.listen(5540)
</code>
2.3.2: Restart your grails app and I enjoy it.

References:
http://vertx.io/embedding_manual.html#embedding-the-VERTX-platform
http://vertx.io/docs.html
http://vertx.io/core_manual_groovy.html#sockjs-eventbus-bridge