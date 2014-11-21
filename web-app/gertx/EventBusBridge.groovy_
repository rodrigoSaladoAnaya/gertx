def server = vertx.createHttpServer()
def config = container.config
server.requestHandler { req ->
}
vertx.createSockJSServer(server).bridge(
        prefix: "/${config.eventBusName?:'eventbus'}".toString(),
        [[:]],
        [[:]]
)
server.listen(config.eventBusPort?:5440)
