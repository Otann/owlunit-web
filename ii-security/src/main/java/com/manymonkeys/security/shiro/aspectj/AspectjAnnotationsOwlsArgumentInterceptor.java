package com.manymonkeys.security.shiro.aspectj;

import com.manymonkeys.security.shiro.aop.argument.AnnotationsTomTomArgumentInterceptor;
import org.apache.shiro.aspectj.BeforeAdviceMethodInvocationAdapter;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Owls
 *
 * @author Ilya Pimenov
 */
public class AspectjAnnotationsOwlsArgumentInterceptor extends AnnotationsTomTomArgumentInterceptor {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Performs the method interception of the before advice at the specified joint point.
     *
     * @param aJoinPoint The joint point to intercept.
     * @throws Throwable If an error occurs berforming the method invocation.
     */
    protected void performBeforeInterception(JoinPoint aJoinPoint, Object o) throws Throwable {
        if (log.isTraceEnabled())
            log.trace("#### Invoking a method decorated with a annotation (on argument)" +
                    "\n\tkind       : " + aJoinPoint.getKind() +
                    "\n\tjoinPoint  : " + aJoinPoint +
                    "\n\tannotations: " + Arrays.toString(((MethodSignature) aJoinPoint.getSignature()).getMethod().getAnnotations()) +
                    "\n\ttarget     : " + aJoinPoint.getTarget()
            );

        // 1. Adapt the join point into a method invocation
        BeforeAdviceMethodInvocationAdapter mi = BeforeAdviceMethodInvocationAdapter.createFrom(aJoinPoint);

        // 2. Delegate the authorization of the method call to the super class
        super.invoke(mi, o);
    }
}
