package vmgr

import org.grails.gertx.test.GertxSpecification
import org.vertx.java.core.json.JsonObject

/**
 *
 */
class ServerSpec extends GertxSpecification {

    def setupSpec() {
        deployVerticle('VMgr/server.groovy')
    }

    def cleanup() {
    }

    void "test something"() {
        expect:
        resp == sendToVerticle('vmgr_server', args as JsonObject)
        where:
        args << [
                [:]
        ]
        resp << [
                true,
        ]
    }
}
