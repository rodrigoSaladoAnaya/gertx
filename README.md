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

References
----
* http://vertx.io/embedding_manual.html#embedding-the-VERTX-platform
* http://vertx.io/docs.html
* http://vertx.io/core_manual_groovy.html#sockjs-eventbus-bridge


**Regards!**
