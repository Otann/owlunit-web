package com.manymonkeys.security.shiro.aop.method;

import org.apache.shiro.aop.AnnotationResolver;
import org.apache.shiro.authz.aop.AuthorizingAnnotationMethodInterceptor;

/**
 * Owls
 *
 * @author Ilya Pimenov
 */
public class OwledMethodAnnotationMethodInterceptor extends AuthorizingAnnotationMethodInterceptor {

    public OwledMethodAnnotationMethodInterceptor() {
        super(new OwledMethodAnnotationHandler());
    }

    public OwledMethodAnnotationMethodInterceptor(AnnotationResolver resolver) {
        super(new OwledMethodAnnotationHandler(), resolver);
    }
}
