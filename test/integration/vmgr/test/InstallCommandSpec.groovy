package vmgr.test

import org.grails.gertx.test.GertxSpecification
import org.grails.gertx.utils.VerticlesInstalled
import org.junit.Ignore

/**
 *
 */
class InstallCommandSpec extends GertxSpecification {

    def setupSpec() {
        deployVerticle('VMgr/install_command.groovy')
    }

    def cleanup() {
        new File('./web-app/gertx/test0').deleteDir()
        new File('./web-app/gertx/test1').deleteDir()
        VerticlesInstalled.instance.map.clear()
    }

    @Ignore
    def create1TestFiles() {
        new File('./web-app/gertx/test0').mkdir()
        new File('./web-app/gertx/test0/a.groovy').write(
                'println "a.groovy Ok"'
        )
    }

    @Ignore
    def createTestFiles() {
        new File('./web-app/gertx/test0').mkdir()
        new File('./web-app/gertx/test1/uno').mkdirs()


        new File('./web-app/gertx/test0/a.groovy').write(
                'println "a.groovy Ok"'
        )
        new File('./web-app/gertx/test1/uno/b.groovy').write(
                'println "b.groovy Ok"'
        )
        new File('./web-app/gertx/test1/uno/c.txt').write(
                'println "c.groovy Ok"'
        )
    }

    void "Test <install *> command when there's no verticle"() {
        when:
        sendToVerticle('install_command', '*')
        then:
        0 == VerticlesInstalled.instance.map.size()
    }

    void "Test <install *> command with one verticle"() {
        given:
        create1TestFiles()
        when:
        sendToVerticle('install_command', '*')
        then:
        1 == VerticlesInstalled.instance.map.size()
    }

    void "Test <install *> command with two verticle"() {
        given:
        createTestFiles()
        when:
        sendToVerticle('install_command', '*')
        then:
        2 == VerticlesInstalled.instance.map.size()
    }

}
