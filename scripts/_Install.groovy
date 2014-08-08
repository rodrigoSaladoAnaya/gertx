ant.mkdir(dir: "${basedir}/web-app/js/libs")
ant.copy(
        file: "${pluginBasedir}/web-app/js/libs/sockjs.min.js",
        todir: "${basedir}/web-app/js/libs"
)
ant.copy(
        file: "${pluginBasedir}/web-app/js/libs/vertxbus.js",
        todir: "${basedir}/web-app/js/libs"
)

ant.mkdir(dir: "${basedir}/web-app/gertx")
ant.copy(
        file: "${pluginBasedir}/web-app/gertx/VerticleManager.groovy",
        todir: "${basedir}/web-app/gertx",
        overwrite: true
)
ant.copy(
        file: "${pluginBasedir}/web-app/gertx/EventBusBridge.groovy",
        todir: "${basedir}/web-app/gertx"
)