package org.grails.gertx.annotation

import grails.util.Holders

@Singleton
class AppContext {

    private def propertyMissing(String name) {
        return getGrailsBean(name)
    }

    private def getGrailsBean(beanName) {
        return Holders.grailsApplication.mainContext.getBean(
                beanName
        )
    }
}