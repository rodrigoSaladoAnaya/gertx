package org.grails.gertx.test

import org.junit.Ignore
import org.vertx.java.core.AsyncResult
import org.vertx.java.core.Handler
import org.vertx.java.core.eventbus.EventBus
import org.vertx.java.core.eventbus.Message
import org.vertx.java.core.json.JsonObject
import org.vertx.java.platform.PlatformLocator
import spock.lang.Shared
import spock.lang.Specification

class GertxSpecification extends Specification {
    @Shared def platformManager
    @Shared def vertx
    @Shared EventBus eventBus
    @Shared URL[] classpath = new URL[1]
    @Shared WAIT_TIME = 300

    @Override
    def setupSpec() {
        platformManager = PlatformLocator.factory.createPlatformManager()
        vertx = platformManager.vertx()
        eventBus = vertx.eventBus()
        def classPathUrl = new File('./web-app/gertx').toURI().toURL()
        classpath[0] = classPathUrl
    }

    @Ignore
    void deployVerticle(vertilceName, config = [:]) {
        def wait = true
        platformManager.deployVerticle(
                vertilceName,
                config as JsonObject,
                classpath,
                1,
                null,
                new Handler<AsyncResult<String>>() {
                    void handle(AsyncResult<String> asyncResult) {
                        if (asyncResult.succeeded()) {
                            println "[gertx] '${vertilceName}' is charged."
                        } else {
                            println "[gertx] Unable to charge '${vertilceName}': ${asyncResult.cause()}."
                        }
                        wait = false
                    }
                }
        )
        while (wait) {
            sleep(WAIT_TIME)
        }
    }

    @Ignore
    def sendToVerticle(address, agrs=null, timeout=5000) {
        def wait = true
        def body = null
        eventBus.sendWithTimeout(address, agrs, timeout, new Handler<AsyncResult<Message<JsonObject>>>() {
            void handle(AsyncResult<Message<JsonObject>> result) {
                if (result.succeeded()) {
                    body = result.result().body()
                } else {
                    println "Error '${address}': ${result.cause()}"
                }
                wait = false
            }
        })

        while (wait) {
            sleep(WAIT_TIME)
        }
        return body
    }
}