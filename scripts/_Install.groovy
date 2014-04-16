ant.mkdir(dir: "${basedir}/grails-app/vertx")
new File(basedir, "grails-app/vertx/VerticleManager.groovy").text = '''\
import groovy.io.FileType

/**
 * Manage verticles
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

====================================
# Verticle Manager v0.1            #
# Port: ${port}                       #
# Start date: ${new Date().format("yyyy-MM-dd hh:mm:sss")} #
# try: telnet localhost ${port}       #
====================================

"""

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

def loadAllVerticles = {
   verticlesNamesList = []
    new File(config.verticlePath).eachFileMatch(FileType.FILES, ~/^.*groovy$/) { file ->
        if(!(file.name in ['VerticleManager.groovy'])) {
            verticlesNamesList << file.name
        }
    } 
}

def showAllVertilces = {
    verticlesNamesList.each { file ->
        eb.send(socketLogAddress, '- ' + file)
    }
}

def loadCommand = { tail ->
    if (tail.size() > 0) {
        def first = tail.first()[0]?.toLowerCase()
        switch (first) {
            case '*':
                loadAllVerticles()
                showAllVertilces()
                log.info "[vertx] verticles available: ${verticlesNamesList}"
                break
            case '?':
                help()
                break
        }
    }
}

//TODO: Seek help to better explain the commands
def help = {
    def txt = """
[show * | s *]      - Displays all installed and uninstalled verticles.
[show i | s i]      - Displays all installed verticles.
[show u | s u]      - Displays all uninstalled verticles.
[show f | s f]      - Display the files available to install.
[show p | s p]      - Display the verticle's path
[show ? | s ?]      - Show this help.
[load * | s *]      - Load all verticles.
[install * | i *]   - Installs all verticles shown by the "show f" command.
[install # | i #]   - Installs all verticles shown by the "show f" command passing an argument.
[uninstall * | u *] - Uninstalls all verticles shown by the "show i" command.
[uninstall # | u #] - Uninstalls all verticles shown by the "show i" command passing an argument.
""".toString()
    eb.send(socketLogAddress, txt)
}

def shortHelp = {
    def txt = '[show ? | s ?]      - Show help.'
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
                showAllVertilces()
                break
            case 'p':
                def currentPath = new File(getClass().protectionDomain.codeSource.location.path).parent
                eb.send(socketLogAddress, "[${currentPath}]".toString())
                break
            case '?':
                help()
                break
        }
    }
}

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
        case 'l':
        case 'load':
            loadCommand(tail)
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

executeCommand 'load    *'
executeCommand 'install *'

/** The server */
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