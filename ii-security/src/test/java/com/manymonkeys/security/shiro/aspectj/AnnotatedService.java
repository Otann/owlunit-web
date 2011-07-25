package com.manymonkeys.security.shiro.aspectj;

/**
 * Owls
 *
 * @author Ilya Pimenov
 */
public interface AnnotatedService {

    public void annotatedMethod();

    public void annotatedArgument(String arg1);

    public void annotatedSecondArgument(String arg1, String arg2);

    public void annotatedThirdArgument(String arg1, String arg2, String arg3);

    public void annotatedFourthArgument(String arg1, String arg2, String arg3, String arg4);

    public void annotatedFifthArgument(String arg1, String arg2, String arg3, String arg4, String arg5);
}