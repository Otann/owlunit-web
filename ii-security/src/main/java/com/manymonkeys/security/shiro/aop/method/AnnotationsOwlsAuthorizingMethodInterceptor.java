package com.manymonkeys.security.shiro.aop.method;

import org.apache.shiro.aop.MethodInvocation;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.aop.AuthorizingAnnotationMethodInterceptor;
import org.apache.shiro.authz.aop.AuthorizingMethodInterceptor;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Owls
 *
 * @author Ilya Pimenov
 */
public abstract class AnnotationsOwlsAuthorizingMethodInterceptor extends AuthorizingMethodInterceptor {

    protected Collection<AuthorizingAnnotationMethodInterceptor> methodInterceptors;

    public AnnotationsOwlsAuthorizingMethodInterceptor() {
        methodInterceptors = new ArrayList<AuthorizingAnnotationMethodInterceptor>(1);
        methodInterceptors.add(new OwledMethodAnnotationMethodInterceptor());
        // ... add more here
    }

    protected void assertAuthorized(MethodInvocation methodInvocation) throws AuthorizationException {
        //default implementation just ensures no deny votes are cast:
        Collection<AuthorizingAnnotationMethodInterceptor> aamis = getMethodInterceptors();
        if (aamis != null && !aamis.isEmpty()) {
            for (AuthorizingAnnotationMethodInterceptor aami : aamis) {
                if (aami.supports(methodInvocation)) {
                    aami.assertAuthorized(methodInvocation);
                }
            }
        }
    }

    public Collection<AuthorizingAnnotationMethodInterceptor> getMethodInterceptors() {
        return methodInterceptors;
    }

    public void setMethodInterceptors(Collection<AuthorizingAnnotationMethodInterceptor> methodInterceptors) {
        this.methodInterceptors = methodInterceptors;
    }
}
