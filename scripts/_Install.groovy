ant.mkdir(dir: "${basedir}/web-app/js/libs")
ant.copy(
        file: "${pluginBasedir}/web-app/js/libs/sockjs.min.js",
        todir: "${basedir}/web-app/js/libs"
)
ant.copy(
        file: "${pluginBasedir}/web-app/js/libs/vertxbus.js",
        todir: "${basedir}/web-app/js/libs"
)

ant.mkdir(dir: "${basedir}/grails-app/gertx")
ant.copy(
        file: "${pluginBasedir}/grails-app/gertx/VerticleManager.groovy",
        todir: "${basedir}/grails-app/gertx",
        overwrite: true
)
ant.copy(
        file: "${pluginBasedir}/grails-app/gertx/EventBusBridge.groovy",
        todir: "${basedir}/grails-app/gertx"
)

ant.mkdir(dir: "${basedir}/src/groovy/org/grails/gertx")
ant.copy(
        file: "${pluginBasedir}/src/groovy/org/grails/gertx/GertxSpecification.groovy",
        todir: "${basedir}/src/groovy/org/grails/gertx",
        overwrite: true
)