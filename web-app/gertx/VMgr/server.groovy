def eb = vertx.eventBus
eb.registerHandler('vmgr_server') { msg ->
    msg.reply(true)
}