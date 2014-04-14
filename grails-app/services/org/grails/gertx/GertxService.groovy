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
        if (vertx) {
            return
        }

        platformManager = PlatformLocator.factory.createPlatformManager()
        vertx = platformManager.vertx()
        eventBus = vertx.eventBus()
    }

    void runVerticleManager() {
        def verticleManagerFile = grailsApplication.mainContext.getResource("../grails-app/vertx/").file
        if (!verticleManagerFile.exists() || !verticleManagerFile.canRead()) {
            log.error "[vertx] No se pudo acceder al recurso '${verticleManagerFile}'"
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
                            log.info "[vertx-manager] Se cargo el verticleManager."
                        } else {
                            log.error "[vertx-manager] verticleManager ${asyncResult.cause()}."
                        }
                    }
                }
        )
    }
}
