Gertx
=====

Allows use Vert.x in a Grails application

Version
----
0.0.1

Repository
----
https://github.com/rodrigoSaladoAnaya/gertx

Issue tracker
----
https://github.com/rodrigoSaladoAnaya/gertx/issues

Introduction
----
  - Uses the PlatformManager to carry Vert.x to Grails and expose a EventBus instance.
  - The plugin adds two JavaScript libraries sockjs.min.js and vertxbus.js in web-app/js/libs directory, so you can use the EventBus in HTML more comfortably.
  - Also adds a verticle serving as administrator other verticles in grails-app/vertx directory called VerticleManager.groovy that you can access e.g. via telnet.

Examples
----
  - Install (temporarily only can be installed manually)
    - Clone the repo from the repository section
    - grails refresh-dependencies
    - grails maven-install
    - Ensure that the directory ~/.m2/repository/org/grails/plugins/gertx/[X.X.X] exists,  [X.X.X] means the version
    - Into the grails-app/conf/BuildConfig.groovy in the plugins  section add ```runtime ":gertx:0.0.1"```
  - Use a service as a verticle
    - Edit the file grails-app/conf/BootStrap.groovy with something like

```groovy
class BootStrap {

    def gertxService //Inject a plugin instance
    
    def init = { servletContext ->
        gertxService.initVertx() //Initialize Vertx
    }
    def destroy = {
    }
}
```
   - Create a service with ```grails create-service verticles.Test1```, and edit with something like

```groovy
package verticles

class Test1Service {
    static transactional = false

	def gertxUtilsService 
    
    def registerTest1Verticle() {
        gertxUtilsService.registerHandler('test-1', { msg ->
            log.info msg.body().toMap()
            msg.reply("Responding to greetings from test-1")
        })
    }
}
```
  - If you want to use the vertical from the beginning, edit the pepgrails-app/conf/BootStrap.groovy with something like

```groovy
class BootStrap {

    def gertxService
    def test1Service
    
    def init = { servletContext ->
        gertxService.initVertx()

        test1Service.registerTest1Verticle()
    }
    def destroy = {
    }
}
```
  - Check the log for something like 

```... INFO  grails.app.services.org.grails.gertx.GertxUtilsService$1  - [vertx] test-1 is the charge.```
  - Do the same to create a EventBusBridge, create a service with the command ```grails create-service verticles.WebEventBus```

```groovy
package verticles

import org.vertx.java.core.Handler
import org.vertx.java.core.Vertx
import org.vertx.java.core.http.HttpServer
import org.vertx.java.core.http.HttpServerRequest
import org.vertx.java.core.json.JsonArray
import org.vertx.java.core.json.JsonObject
import org.vertx.java.core.sockjs.SockJSServer

class WebEventBusService {
    static transactional = false

	def gertxService

    void start() {
        Vertx vertx = gertxService.vertx
        HttpServer server = vertx.createHttpServer();

        server.requestHandler(new Handler<HttpServerRequest>() {
            public void handle(HttpServerRequest req) {
            }
        })

        JsonArray permitted = new JsonArray();
        permitted.add(new JsonObject())

        SockJSServer sockJSServer = vertx.createSockJSServer(server);
        sockJSServer.bridge(
        	new JsonObject()
        		.putString("prefix", "/eventbus")
        		.putNumber("session_timeout", 30), 
        	permitted, 
        	permitted
    	)

        server.listen(5439);
        log.info "[vertx] The EventBus bridge is charged."
    }
}
```
  - Edit the BootStrap.groovy

```groovy
class BootStrap {

    def gertxService
    def webEventBusService
    def test1Service    

    def init = { servletContext ->
        gertxService.initVertx()

        webEventBusService.start()
        test1Service.registerTest1Verticle()
    }
    def destroy = {
    }
}
```
 - Restart your app and test in a browser the url ```http://localhost:5439/eventbus/```, a message 'Welcome to SockJS!' should be displayed.
 - Edit you Index.gsp view to something like and check the browser console and the app log

```html
<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<html lang="en">
<head>
    <title>Test vertx from view side</title>
    <script src="${resource(dir: 'js', file: 'libs/sockjs.min.js')}"></script>
    <script src="${resource(dir: 'js', file: 'libs/vertxbus.js')}"></script>
    <script src="//code.jquery.com/jquery-1.11.0.min.js"></script>
    <script>
        var eventBus = null;
        $(document).ready(function() {
            if (!eventBus) {
                eventBus = new vertx.EventBus("http://localhost:5439/eventbus");
            }
            eventBus.onopen = function () {
                console.log("EventBus conected...");

                eventBus.send('test-1', 'sending greetings', function (resp) {
                    console.log(resp);
                });
            }

            eventBus.onclose = function () {
                eventBus = null;
                console.log("EventBus offline...");
            }
        });
    </script>    
</head>

<body>
</body>
</html>
```

References
----
* http://vertx.io/embedding_manual.html#embedding-the-VERTX-platform
* http://vertx.io/docs.html
* http://vertx.io/core_manual_groovy.html#sockjs-eventbus-bridge


**Regards!**
