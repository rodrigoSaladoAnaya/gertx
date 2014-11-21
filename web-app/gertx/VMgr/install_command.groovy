import groovy.io.FileType
import org.grails.gertx.annotation.GrailsBean
import org.grails.gertx.utils.VerticleStatus
import org.grails.gertx.utils.VerticlesInstalled

def eb = vertx.eventBus
def log = container.logger

@GrailsBean def grailsApplication
def verticlesMap = VerticlesInstalled.instance.map
final String filter = /^.*(groovy|js|rb|py|clj)$/
final File defaultDir = grailsApplication.mainContext.getResource(
        './gertx/'
).file

def validDir = { File f ->
    def avoidDirs = ['VMgr', 'test']
    return (f.isDirectory()
            && f.canRead()
            && !(f.name in avoidDirs))
}

def validFile = { File f ->
    return (f.isFile()
            && f.canRead()
            && f.name =~ filter)
}

def getShortPath = { File f ->
    def shortPath = f.absolutePath.substring(
            defaultDir.absolutePath.size() + 1
    )
    return shortPath
}

def findAviableFiles = {
    ArrayList<String> shortPaths = []
    defaultDir.eachFile { File f ->
        if (validDir(f)) {
            f.traverse(
                    type: FileType.FILES,
                    nameFilter: ~filter
            ) { f2 ->
                shortPaths << getShortPath(f2)
            }
        } else if (validFile(f)) {
            shortPaths << getShortPath(f)
        }
    }
    return shortPaths
}

def deployVerticle = { path ->
    container.deployVerticle(path, [:]) { asyncResult ->
        if (asyncResult.succeeded()) {
            def info = [
                    path: path,
                    date: new Date().format('yyyy-MM-dd HH:mm:sss'),
                    status: VerticleStatus.INSTALL.toString()
            ]
            verticlesMap."${asyncResult.result()}" = info.toString()
            println "[Install :: Ok] ${path} :: ${asyncResult.result()}"
        } else {
            asyncResult.cause().printStackTrace()
            println "[Install :: Err] ${path} :: ${asyncResult.cause()}"
        }
    }
}

def chooseOption = { arg ->
    def resp = [:]
    switch (arg) {
        case '*':
            findAviableFiles().each { path ->
                deployVerticle(path)
            }
            break
        default:
            println "Valida que el archivo exista y lo instala"
            break
    }
    return resp
}

eb.registerHandler('install_command') { msg ->
    def args = msg.body().split()
    args.each { arg ->
        chooseOption(arg)
    }
    msg.reply(verticlesMap)
}