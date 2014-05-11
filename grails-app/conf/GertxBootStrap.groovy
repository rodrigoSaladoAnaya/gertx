class GertxBootStrap {
    def gertxService
    def grailsApplication

    def init = { servletContext ->
    	def conf = grailsApplication.config?.gertx
    	
    	if(conf?.initVertx == false) { return }
    	gertxService.initVertx()

    	if(conf?.runVerticleManager == false) { return }
        gertxService.runVerticleManager()
    }
}
