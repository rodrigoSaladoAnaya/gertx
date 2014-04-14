ant.mkdir(dir: "${basedir}/grails-app/vertx")
new File(basedir, "grails-app/vertx/VerticleManager.groovy").text = '''\
import groovy.io.FileType

/**
 * Es el encargado de la administración de los verticles.
 */

def config = container.config
def log = container.logger
def port = 5436
def eb = vertx.eventBus
def socketLogAddress = 'write-log-to-socket'
def verticlesInstalledMap = [:]
def verticlesUninstalledMap = [:]
def verticlesNamesList = []

log.info """

======================================
# Inicio el Verticle Manager v0.1    #
# Puerto: ${port}                       #
# Fecha inicio: ${new Date().format("yyyy-MM-dd hh:mm:sss")} #
# telnet localhost ${port}              #
======================================

"""
/** Verifica si un verticle ya esta instalado, de ser así lo pinta en log in da el id de instalación. */
def findVerticlesInstalledByName = { verticle ->
    def verticlesFound = verticlesInstalledMap.findAll { k, v ->
        v == verticle
    }

    if (verticlesFound.size()) {
        verticlesFound.each { verticleIntalled ->
            def resp = "[Installed] ${verticleIntalled.value} :: ${verticleIntalled.key}"
            eb.send(socketLogAddress, resp.toString())
        }
    }
    return verticlesFound
}

def findVerticleInstalledById = { id ->
    def verticleFound = verticlesInstalledMap.findAll { k, v ->
        k.endsWith(id)
    }
    return verticleFound
}

/** Obtenemos todos los verticles que estén el el folder actual */
new File(config.verticlePath).eachFileMatch(FileType.FILES, ~/^.*groovy$/) { file ->
    if(!(file.name in ['VerticleManager.groovy'])) {
        verticlesNamesList << file.name
    }
}
log.info "[vertx] verticles disponibles: ${verticlesNamesList}"

/** Función que instala los verticles, también los guarda en 'verticlesInstalledMap' con el id con que se instalo. */
def installVerticle = { verticle ->
    findVerticlesInstalledByName(verticle)
    container.deployVerticle(verticle, config.environment) { asyncResult ->
        def resp = ''
        if (asyncResult.succeeded()) {
            verticlesInstalledMap << ["${asyncResult.result()}": "${verticle} installed at ${new Date().format("yyyy-MM-dd hh:mm:sss")}"]
            resp = "[Install :: Ok] ${verticle} :: ${asyncResult.result()}"
            log.info resp
            eb.send(socketLogAddress, resp.toString())
        } else {
            resp = "[Install :: Err] ${verticle} :: ${asyncResult.cause()}"
            log.error resp
            eb.send(socketLogAddress, resp.toString())
        }
    }
}

def unistallVerticleById = { id ->
    def verticles = findVerticleInstalledById(id)*.value
    container.undeployVerticle(id) { asyncResult ->
        def resp = ''
        if (asyncResult.succeeded()) {
            verticlesInstalledMap.remove("${id}")
            verticlesUninstalledMap << ["${id}": "${verticles.join(', ')} was uninstalled at ${new Date().format("yyyy-MM-dd hh:mm:sss")}"]
            resp = "[Uninstall :: Ok] ${verticles} :: ${id}"
            log.info resp
            eb.send(socketLogAddress, resp.toString())
        } else {
            resp = "[Uninstall :: Err] ${verticles} :: ${asyncResult.cause()}"
            log.error resp
            eb.send(socketLogAddress, resp.toString())
        }
    }
}

def installCommand = { tail ->
    def installAll = tail.find { it == '*' }
    if (installAll) {
        verticlesNamesList.sort { it }.each { v ->
            installVerticle(v)
        }
    } else {
        tail.each { String input ->
            def verticlesToInstall = verticlesNamesList.findAll { String verticle ->
                (verticle.toLowerCase().startsWith(input) && verticle.endsWith(".groovy"))
            }
            if (verticlesToInstall) {
                verticlesToInstall.each { verticle ->
                    installVerticle(verticle)
                }
            }
        }
    }
}

def uninstallCommand = { tail ->
    resp = ''
    def uninstAll = tail.find { it == '*' }
    if (uninstAll) {
        verticlesInstalledMap.each { i, v ->
            unistallVerticleById(i)
        }
    } else {
        tail.each { id ->
            findVerticleInstalledById(id).each { i, v ->
                unistallVerticleById(i.toString())
            }
        }
    }
}

def help = {
    def txt = """
[show * | s *]      - Muestra todos los verticles instalados y desintalados.
[show i | s i]      - Muestra todos los verticles instalados.
[show u | s u]      - Muestra todos los verticles desintalados.
[show f | s f]      - Muestra los archivos validos para usar como verticles en el directorio ./verticles/.
[show ? | s ?]      - Muestra esta ayuda.
[install * | i *]   - Instala todos los verticles que muestra el comando (show f).
[install # | i #]   - Instala los verticles que muestra el comando (show f) y que inicien con el argumento ingresado.
                       puede instalar varios al miso tiempo separando los argumentos por un espacio en blanco.
[uninstall * | u *] - Desinstala todos los verticles que muestra el comando (show i).
[uninstall # | u #] - Desinstala los verticles que muestra el comando (show i) usando el id como arguemtno de entrada
                       puede desinstalar varios separando los argumentos con espacios en blanco.
""".toString()
    eb.send(socketLogAddress, txt)
}

def shortHelp = {
    def txt = '[show ? | s ?]      - Muestra esta ayuda.'
    eb.send(socketLogAddress, txt)
}


def showCommand = { tail ->
    if (tail.size() > 0) {
        def first = tail.first()[0]?.toLowerCase()
        switch (first) {
            case 'i':
                verticlesInstalledMap.each { verticle ->
                    eb.send(socketLogAddress, "[${verticle.value}] >> ${verticle.key}".toString())
                }
                break
            case 'u':
                verticlesUninstalledMap.each { verticle ->
                    eb.send(socketLogAddress, "[${verticle.value}] >> ${verticle.key}".toString())
                }
                break
            case '*':
                verticlesInstalledMap.each { verticle ->
                    eb.send(socketLogAddress, "[Installed] [${verticle.value}] :: ${verticle.key}".toString())
                }
                verticlesUninstalledMap.each { verticle ->
                    eb.send(socketLogAddress, "[Uninstalled] [${verticle.value}] :: ${verticle.key}".toString())
                }
                break
            case 'f':
                verticlesNamesList.each { file ->
                    eb.send(socketLogAddress, '- ' + file)
                }
                break
            case '?':
                help()
                break
        }
    }
}

/** Maneja los comando que se pasen por algún cliente para administrar los verticles. */
def executeCommand = { input ->
    def inputList = input.trim().replaceAll(' +', ' ').toLowerCase().split()
    if (!inputList) return null

    def head = inputList.head()
    def tail = inputList.tail()

    switch (head) {
        case 's':
        case 'show':
            showCommand(tail)
            break
        case 'i':
        case 'install':
            installCommand(tail)
            break
        case 'u':
        case 'uninstall':
            uninstallCommand(tail)
            break
        default:
            shortHelp()
    }
}

/**
 * Instala todos los verticles que estén en contexto
 * en el momento que levanta la app.
 */
verticlesNamesList.sort { it }.each { v ->
    installVerticle(v)
}

/** Servidor para el administrador de verticles. */
vertx.createNetServer().connectHandler { socket ->
    eb.registerHandler(socketLogAddress) { msg ->
        socket.write '   ' + msg.body() + '\\n'
    }
    help()
    socket.dataHandler { buffer ->
        executeCommand(buffer.toString())
    }
}.listen port
'''