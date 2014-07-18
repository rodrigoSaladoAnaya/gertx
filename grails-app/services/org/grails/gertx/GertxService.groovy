package org.grails.gertx

import org.vertx.java.core.AsyncResult
import org.vertx.java.core.Handler
import org.vertx.java.core.Vertx
import org.vertx.java.core.eventbus.EventBus
import org.vertx.java.core.json.JsonObject
import org.vertx.java.platform.PlatformLocator

class GertxService {

    static transactional = false

    Vertx vertx
    EventBus eventBus
    def platformManager
    def grailsApplication

    void initVertx() {
        if (vertx) { return }
        
        platformManager = PlatformLocator.factory.createPlatformManager()
        vertx = platformManager.vertx()
        eventBus = vertx.eventBus()
    }

    void runVerticleManager() {
        if(!vertx) { 
            log.warn "[vertx] Before use the VerticleManager use initVertx()"
            return 
        }
        
        def verticleManagerFile = grailsApplication.mainContext.getResource("../grails-app/gertx/").file
        if (!verticleManagerFile.exists() || !verticleManagerFile.canRead()) {
            log.error "[vertx] Unable to access the resource '${verticleManagerFile}'"
            return
        }

        def verticleManagerUrl = verticleManagerFile.toURI().toURL()

        URL[] classpath = new URL[1]
        classpath[0] = verticleManagerUrl

        platformManager.deployVerticle(
                "VerticleManager.groovy", [
                verticlePath: verticleManagerUrl.path,
                environment: grailsApplication.config
        ] as JsonObject,
                classpath,
                1,
                null,
                new Handler<AsyncResult<String>>() {
                    void handle(AsyncResult<String> asyncResult) {
                        if (asyncResult.succeeded()) {
                            log.info "[vertx-manager] VerticleManager is charged."
                        } else {
                            log.error "[vertx-manager] Unable to charge the VerticleManager: ${asyncResult.cause()}."
                        }
                    }
                }
        )
    }
}
