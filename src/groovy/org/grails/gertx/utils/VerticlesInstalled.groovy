package org.grails.gertx.utils

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

@Singleton
class VerticlesInstalled {
    ConcurrentMap map = new ConcurrentHashMap();
}
