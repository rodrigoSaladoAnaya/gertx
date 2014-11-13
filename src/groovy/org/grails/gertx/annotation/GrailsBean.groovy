package org.grails.gertx.annotation

import org.codehaus.groovy.transform.GroovyASTTransformationClass

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@Retention(RetentionPolicy.SOURCE)
@Target([ElementType.LOCAL_VARIABLE])
@GroovyASTTransformationClass('org.grails.gertx.annotation.GrailsBeanTransformation')
public @interface GrailsBean {
}
