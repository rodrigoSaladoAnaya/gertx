class GertxBootStrap {
    def gertxService

    def init = { servletContext ->
    	gertxService.initVertx()
        gertxService.runVerticleManager()
    }
}
