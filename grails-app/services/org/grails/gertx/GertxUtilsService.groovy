package org.grails.gertx

import org.vertx.java.core.AsyncResult
import org.vertx.java.core.AsyncResultHandler
import org.vertx.java.core.Handler
import org.vertx.java.core.eventbus.Message
import org.vertx.java.core.json.JsonObject

class GertxUtilsService {

    static transactional = false

    def gertxService

    void registerHandler(String address, Closure bodyHandler) {
        def resultHandler = new AsyncResultHandler<Void>() {
            void handle(AsyncResult<Void> asyncResult) {
                if (asyncResult.succeeded()) {
                    log.info "[vertx] Se cargo el verticle ${address}."
                } else {
                    log.error "[vertx] ${asyncResult.cause()}."
                }
            }
        }
        def messageHandler = new Handler<Message<JsonObject>>() {
            void handle(Message<JsonObject> msg) {
                bodyHandler(msg)
            }
        }

        gertxService.eventBus.registerHandler(address, messageHandler, resultHandler)
    }

    void send(String address, args, Closure bodyHandler = {}) {
        def messageHandler = new Handler<Message<JsonObject>>() {
            void handle(Message<JsonObject> msg) {
                bodyHandler(msg)
            }
        }
        gertxService.eventBus.send(address, args, messageHandler)
    }

    void publish(String address, args) {
        gertxService.eventBus.publish(address, args)
    }

    void sendWithTimeout(String address, args, Closure bodyHandler, int timeout) {
        def messageHandler = new Handler<AsyncResult<Message<JsonObject>>>() {
            void handle(AsyncResult<Message<JsonObject>> result) {
                if (result.succeeded()) {
                    bodyHandler(result.result())
                } else {
                    log.error "El verticle '${address}' no respondio: ${result.cause()}"
                }
            }
        }
        gertxService.eventBus.sendWithTimeout(address, args, timeout, messageHandler)
    }
}
