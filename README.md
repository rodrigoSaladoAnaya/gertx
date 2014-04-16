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
  - **1** Install (temporarily only can be installed manually)
  - (1.1) Clone the repo from the repository section
  - (1.2) ```grails refresh-dependencies```
  - (1.3) ```grails maven-install```
  - (1.4) Ensure that the directory ~/.m2/repository/org/grails/plugins/gertx/[X.X.X] exists,  [X.X.X] means the version
  - (1.5) Into the grails-app/conf/BuildConfig.groovy in the plugins  section add ```runtime ":gertx:0.0.1"```
  - **2** Use a service as a verticle
  - (2.1) Edit the file grails-app/conf/BootStrap.groovy with something like

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
  - (2.2) Create a service with ```grails create-service verticles.Test1```, and edit with something like

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
  - (2.3) If you want to use the vertical from the beginning, edit the pepgrails-app/conf/BootStrap.groovy with something like

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
  - (2.4) Check the log for something like 

```... INFO  grails.app.services.org.grails.gertx.GertxUtilsService$1  - [vertx] test-1 is the charge.```
  - (2.5) Do the same to create a EventBusBridge, create a service with the command ```grails create-service verticles.WebEventBus```

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
  - (2.6) Edit the BootStrap.groovy

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
 - (2.7) Restart your app and test in a browser the url ```http://localhost:5439/eventbus/```, a message 'Welcome to SockJS!' should be displayed.
 - (2.8) Edit you Index.gsp view to something like and check the browser console and the app log

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

  - **3** Use the VerticleManager
  - (3.1) Edit the file grails-app/conf/BootStrap.groovy with

```groovy
class BootStrap {

    def gertxService
    def webEventBusService
    def test1Service    

    def init = { servletContext ->
        gertxService.initVertx()
        gertxService.runVerticleManager() // Start the VerticleManager

        webEventBusService.start()
        test1Service.registerTest1Verticle()
    }
    def destroy = {
    }
}
```
  - (3.1) Create a file as ```touch grails-app/vertx/Test2Verticle.groovy```
  - (3.2) Edit the new verticle with just two lines

```groovy
def log = container.logger
log.info "step 1"
```
  - (3.3) Restart the app and finds the message ```...INFO  null-Test2Verticle.groovy-...  - step 1``` into the log and

```
====================================
# Verticle Manager v0.1            #
# Port: 5436                       #
# Start date: 2014-04-15 04:33:033 #
# try: telnet localhost 5436       #
====================================
```
  - (3.4) Open a console and enter ```telnet localhost 5436```
  - (3.5) Write ```show i``` or ```s i``` to show all the verticles intalled

```
   [Test2Verticle.groovy installed at 2014-04-15 04:42:026] >> deployment-e6f17f64-4762-4f6d-a3c0-3f841846ac8d
```
  - (3.6) Uninstall a verticle writing ```uninstall 3f841846ac8d``` or ```u ac8d```the end of his id, either 1, 2, 3, 4 ... chars, e.g.

```
   [Uninstall :: Ok] [Test2Verticle.groovy installed at 2014-04-15 04:42:026] :: deployment-e6f17f64-4762-4f6d-a3c0-3f841846ac8d
```
  - (3.7) Edit the verticle Test2Verticle.groovy in the point (3.1) with

```groovy
def log = container.logger
def eb = vertx.eventBus

eb.registerHandler('test-1') { msg ->
	log.info "(2) ${msg.body()}"
    msg.reply("Responding to greetings from test-2")
}
```
  - (3.8) Install the Test2Verticle.groovy verticle with the command ```install Test2Verticle.groovy``` or ```i testverti```

```
   [Install :: Ok] Test2Verticle.groovy :: deployment-91ce0f18-6ace-451f-bc98-ef9fc676845f
```
  - (3.9) Reload the Index.gsp and check the browser console and the app log, several times to have sometimes ```Responding to greetings from test-1``` and sometimes ```Responding to greetings from test-2``` and the same behavior in the console log. This happens because there are two verticles registered with the same address 'test-1'

References
----
* http://vertx.io/embedding_manual.html#embedding-the-VERTX-platform
* http://vertx.io/docs.html
* http://vertx.io/core_manual_groovy.html#sockjs-eventbus-bridge


**Regards!**
