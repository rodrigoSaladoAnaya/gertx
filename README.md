gertx
=====

Is a grails-plugin that allows use Vert.x

verion 0.0.1

https://github.com/rodrigoSaladoAnaya/gertx

1. Use the PlatformManager to carry Vertx to Grails and expose a EventBus instance to use into de GrailsApp

2. The plugin adds two JavaScript libraries sockjs.min.js and vertxbus.js in web-app/js/libs/ directory, so you can use the power of EventBus from the view more comfortably.

3. Also adds a verticle serving as administrator other verticles in grails-app/vertx/ directory called VerticleManager.groovy that you can access e.g. via telnet to deploy, undeploy verticles and list installed and uninstalled verticles.

4. To use gertx inside your grails application runtime you have to:
⋅⋅* Add "gertx: 0.1" in BuildConfig.groovy as a plugin
⋅⋅* Add into BootStrap.groovy 'def gertxService'
⋅⋅* Init Vertx with 'gertxService.initVertx()' If you just want to use within the services
⋅⋅* And if you want to use external verticles help you install and uninstall add 'gertxService.runVerticleManager()'
⋅⋅* e.g.:

```groovy
class BootStrap {
    def gertxService
    def init = { servletContext ->
        gertxService.initVertx()
        gertxService.runVerticleManager()
    }
}
```

⋅⋅* If you use the VerticleManager be accessed from a terminal with the command 'telnet localhost 5436' to manage verticles.
⋅⋅* To add a new verticle must create it in the folder grails-app/vertx/
⋅⋅* e.g.: If you want to create a EventBus Bridge called WebEventBus.groovy create a file create a file with that name in the folder grails-app/vertx/ and type something like:

```groovy
def server = vertx.createHttpServer()
server.requestHandler { req ->
}
vertx.createSockJSServer(server).bridge(
    prefix: '/eventbus', [[:]], [[:]]
)
server.listen(5540)
```

⋅⋅* Restart your grails app and I enjoy it.

References:
http://vertx.io/embedding_manual.html#embedding-the-VERTX-platform
http://vertx.io/docs.html
http://vertx.io/core_manual_groovy.html#sockjs-eventbus-bridge