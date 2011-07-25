package com.manymonkeys.security.shiro.aspectj;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

/**
 * Owles
 *
 * @author Ilya Pimenov
 */
@Aspect()
public class OwlsAnnotationAuthorizingAspect {

    private static final String pointCupExpression =
            "execution(@com.manymonkeys.security.shiro.annotation.OwledMethod * *(..))";
    /*---------------------------------------------------------------------------------------------------------------
    | Notta Bene: Add new pointCups in the same string, they will go like:                                          |
    | private static final String pointCupExpression =                                                              |
    |       "execution(@com.manymonkeys.security.shiro.annotation.OwledMethod * *(..)) || " +               |
    |           "execution(@com.manymonkeys.security.shiro.annotation.SecondNiceAnnotation * *(..)) || " +;              |
    |           "execution(@com.manymonkeys.security.shiro.annotation.ThirdNiceAnnotation * *(..))";                     |
    | Sadly, it will be a bit more tricky for argument matching&nbsp;&#151; {@link OwlsAnnotationArgumentsAspect} |
    |                                                                                                               |
    | ps. hope this comment is big enough                                                                           |
    ===============================================================================================================*/

    @Pointcut(pointCupExpression)
    public void anyOwlsAnnotatedMethod() {
    }

    @Pointcut(pointCupExpression)
    void anyOwlsAnnotatedMethodCall(JoinPoint thisJoinPoint) {
    }

    private AspectjAnnotationsOwlsAuthorizingMethodInterceptor interceptor = new AspectjAnnotationsOwlsAuthorizingMethodInterceptor();

    @Before("anyOwlsAnnotatedMethodCall(thisJoinPoint)")
    public void executeAnnotatedMethod(JoinPoint thisJoinPoint) throws Throwable {
        interceptor.performBeforeInterception(thisJoinPoint);
    }
}
