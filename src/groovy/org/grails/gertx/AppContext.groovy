package org.grails.gertx

import grails.util.Holders

@Singleton
class AppContext {

    private def propertyMissing(String name) {
        return grailsApplication(name)
    }

    private def grailsApplication(beanName) {
        return Holders.grailsApplication.mainContext.getBean(
                beanName
        )
    }
}