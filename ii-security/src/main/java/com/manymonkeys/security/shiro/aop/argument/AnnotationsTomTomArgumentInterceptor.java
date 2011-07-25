package com.manymonkeys.security.shiro.aop.argument;

import org.apache.shiro.aop.MethodInvocation;
import org.apache.shiro.authz.AuthorizationException;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Owls
 *
 * @author Ilya Pimenov
 */
public abstract class AnnotationsTomTomArgumentInterceptor {

    protected final List<AnnotationArgumentInterceptor> argumentInterceptors;

    public AnnotationsTomTomArgumentInterceptor() {
        argumentInterceptors = Arrays.<AnnotationArgumentInterceptor>asList(new OwledArgumentAnnotationArgumentInterceptor());
        // ... add more here
    }

    protected void assertRules(MethodInvocation mi, Object o) throws AuthorizationException {
        //default implementation just ensures no deny votes are cast:
        Collection<AnnotationArgumentInterceptor> aamis = argumentInterceptors;
        if (aamis != null && !aamis.isEmpty()) {
            for (AnnotationArgumentInterceptor aai : aamis) {
                if (aai.supports(mi)) {
                    aai.assertRules(mi, o);
                }
            }
        }
    }

    public Object invoke(MethodInvocation methodInvocation, Object o) throws Throwable {
        assertRules(methodInvocation, o);
        return methodInvocation.proceed();
    }
}
