package com.manymonkeys.security.shiro.aspectj;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

/**
 * Owls
 *
 * @author Ilya Pimenov
 */
@Aspect()
public class OwlsAnnotationArgumentsAspect {

    /*------------------------
    |   F I R S T   A R G    |
    ========================*/

    @Pointcut("execution(* *(@com.manymonkeys.security.shiro.annotation.OwledArgument (*), ..)) && args(o, ..)")
    void anyOwlAnnotatedArgumentMethodCall(JoinPoint thisJoinPoint, Object o) {
    }

    @Before("anyOwlAnnotatedArgumentMethodCall(thisJoinPoint, o)")
    public void executeAnnotatedMethod(JoinPoint thisJoinPoint, Object o) throws Throwable {
        interceptor.performBeforeInterception(thisJoinPoint, o);
    }

    /*--------------------------
    |   S E C O N D   A R G    |
    ==========================*/

    @Pointcut("execution(* *(*, @com.manymonkeys.security.shiro.annotation.OwledArgument (*), ..)) && args(*, o, ..)")
    void anyOwlAnnotatedArgumentMethodCallSecond(JoinPoint thisJoinPoint, Object o) {
    }

    @Before("anyOwlAnnotatedArgumentMethodCallSecond(thisJoinPoint, o)")
    public void executeAnnotatedMethodSecond(JoinPoint thisJoinPoint, Object o) throws Throwable {
        interceptor.performBeforeInterception(thisJoinPoint, o);
    }

    /*------------------------
    |   T H I R D   A R G    |
    ========================*/

    @Pointcut("execution(* *(*, *, @com.manymonkeys.security.shiro.annotation.OwledArgument (*), ..)) && args(*, *, o, ..)")
    void anyOwlAnnotatedArgumentMethodCallThird(JoinPoint thisJoinPoint, Object o) {
    }

    @Before("anyOwlAnnotatedArgumentMethodCallThird(thisJoinPoint, o)")
    public void executeAnnotatedMethodThird(JoinPoint thisJoinPoint, Object o) throws Throwable {
        interceptor.performBeforeInterception(thisJoinPoint, o);
    }

    /*--------------------------
    |   F O U R T H   A R G    |
    ==========================*/

    @Pointcut("execution(* *(*, *, *, @com.manymonkeys.security.shiro.annotation.OwledArgument (*), ..)) && args(*, *, *, o, ..)")
    void anyOwlAnnotatedArgumentMethodCallForth(JoinPoint thisJoinPoint, Object o) {
    }

    @Before("anyOwlAnnotatedArgumentMethodCallForth(thisJoinPoint, o)")
    public void executeAnnotatedMethodForth(JoinPoint thisJoinPoint, Object o) throws Throwable {
        interceptor.performBeforeInterception(thisJoinPoint, o);
    }

    /*------------------------
    |   F I F T H   A R G    |
    ========================*/

    @Pointcut("execution(* *(*, *, *, *, @com.manymonkeys.security.shiro.annotation.OwledArgument (*), ..)) && args(*, *, *, *, o, ..)")
    void anyOwlAnnotatedArgumentMethodCallFifth(JoinPoint thisJoinPoint, Object o) {
    }

    @Before("anyOwlAnnotatedArgumentMethodCallFifth(thisJoinPoint, o)")
    public void executeAnnotatedMethodFifth(JoinPoint thisJoinPoint, Object o) throws Throwable {
        interceptor.performBeforeInterception(thisJoinPoint, o);
    }

    private AspectjAnnotationsOwlsArgumentInterceptor interceptor = new AspectjAnnotationsOwlsArgumentInterceptor();
}