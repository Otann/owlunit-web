package com.manymonkeys.security.shiro.aop.argument;

import org.apache.shiro.ShiroException;
import org.apache.shiro.aop.AnnotationHandler;

import java.lang.annotation.Annotation;

/**
 * Owls
 *
 * @author Ilya Pimenov
 */
public abstract class ArgumentAnnotationHandler extends AnnotationHandler {

    public ArgumentAnnotationHandler(Class<? extends Annotation> annotationClass) {
        super(annotationClass);
    }

    public abstract void assertRules(Annotation a, Object arg) throws ShiroException;
}
