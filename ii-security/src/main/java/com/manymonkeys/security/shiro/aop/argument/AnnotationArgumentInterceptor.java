package com.manymonkeys.security.shiro.aop.argument;

import org.apache.shiro.ShiroException;
import org.apache.shiro.aop.AnnotationResolver;
import org.apache.shiro.aop.MethodInvocation;

import java.lang.annotation.Annotation;

/**
 * Owls
 *
 * @author Ilya Pimenov
 */
public class AnnotationArgumentInterceptor {

    private ArgumentAnnotationHandler handler;

    private final AnnotationResolver resolver = new ArgumentAnnotationResolver();

    public AnnotationArgumentInterceptor(ArgumentAnnotationHandler argumentAnnotationHandler) {
        if (argumentAnnotationHandler == null) {
            throw new IllegalArgumentException("AnnotationHandler argument cannot be null.");
        }
        setHandler(argumentAnnotationHandler);
    }

    public void assertRules(MethodInvocation mi, Object arg) throws ShiroException {
        try {
            getHandler().assertRules(getAnnotation(mi), arg);
        } catch (ShiroException ae) {
            if (ae.getCause() == null)
                ae.initCause(new ShiroException("хнык(("));
            throw ae;
        }
    }

    protected Annotation getAnnotation(MethodInvocation mi) {
        return resolver.getAnnotation(mi, getHandler().getAnnotationClass());
    }

    public boolean supports(MethodInvocation mi) {
        return getAnnotation(mi) != null;
    }

    public ArgumentAnnotationHandler getHandler() {
        return handler;
    }

    public void setHandler(ArgumentAnnotationHandler handler) {
        this.handler = handler;
    }
}