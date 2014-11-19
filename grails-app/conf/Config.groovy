environments {
    test {
        gertx.initVertx = false
        gertx.runVerticleManager = false
    }
}


log4j = {
    error 'org.codehaus.groovy.grails',
          'org.springframework',
          'org.hibernate',
          'net.sf.ehcache.hibernate'
}
