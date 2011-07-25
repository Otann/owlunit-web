package com.manymonkeys.security.shiro.aop.argument;

import org.apache.shiro.aop.AnnotationResolver;
import org.apache.shiro.aop.MethodInvocation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Owls
 *
 * @author Ilya Pimenov
 */
public class ArgumentAnnotationResolver implements AnnotationResolver {

    /**
     * @param mi    the intercepted method to be invoked.
     * @param clazz the annotation class to use to find an annotation instance on the method.
     * @return the discovered annotation or {@code null} if an annotation instance could not be
     *         found.
     */
    public Annotation getAnnotation(MethodInvocation mi, Class<? extends Annotation> clazz) {
        if (mi == null) {
            throw new IllegalArgumentException("method argument cannot be null");
        }
        Method m = mi.getMethod();
        if (m == null) {
            String msg = MethodInvocation.class.getName() + " parameter incorrectly constructed.  getMethod() returned null";
            throw new IllegalArgumentException(msg);

        }

        Annotation[][] annotations = m.getParameterAnnotations();
        for (Annotation[] a : annotations) {
            for (Annotation aa : a) {
                if (aa.annotationType().equals(clazz)) {
                    return aa;
                }
            }
        }
        return null;
    }
}