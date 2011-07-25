package com.manymonkeys.security.shiro.aop.argument;

/**
 * Owls
 *
 * @author Ilya Pimenov
 */
public class OwledArgumentAnnotationArgumentInterceptor extends AnnotationArgumentInterceptor {

    public OwledArgumentAnnotationArgumentInterceptor() {
        super(new OwledArgumentAnnotationHandler());
    }
}
