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
                    log.info "[vertx] ${address} is the charge."
                } else {
                    log.error "[vertx] Unable to charge ${address}: ${asyncResult.cause()}."
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

    void sendWithTimeout(String address, args, int timeout, Closure bodyHandler) {
        def messageHandler = new Handler<AsyncResult<Message<JsonObject>>>() {
            void handle(AsyncResult<Message<JsonObject>> result) {
                if (result.succeeded()) {
                    bodyHandler(result.result())
                } else {
                    log.error "The verticle '${address}' did not respond: ${result.cause()}"
                }
            }
        }
        gertxService.eventBus.sendWithTimeout(address, args, timeout, messageHandler)
    }
}
