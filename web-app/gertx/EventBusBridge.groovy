def server = vertx.createHttpServer()
server.requestHandler { req ->
}
vertx.createSockJSServer(server).bridge(
        prefix: '/eventbus',
        [[:]],
        [[:]]
)
server.listen(5440)
