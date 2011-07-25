package com.manymonkeys.security.shiro.aspectj;

import com.manymonkeys.security.shiro.annotation.OwledArgument;
import com.manymonkeys.security.shiro.annotation.OwledMethod;

import java.sql.Timestamp;

/**
 * Owls
 *
 * @author Ilya Pimenov
 */
public class AnnotatedServiceImpl implements AnnotatedService {

    @OwledMethod()
    public void annotatedMethod() {
        log("executed annotatedMethod");
    }

    public void annotatedArgument(@OwledArgument String arg1) {
        log("executed annotatedArgument");
    }

    public void annotatedSecondArgument(String arg1, @OwledArgument String arg2) {
        log("executed annotatedSecondArgument");
    }

    public void annotatedThirdArgument(String arg1, String arg2, @OwledArgument String arg3) {
        log("executed annotatedThirdArgument");
    }

    public void annotatedFourthArgument(String arg1, String arg2, String arg3, @OwledArgument String arg4) {
        log("executed annotatedFourthArgument");
    }

    public void annotatedFifthArgument(String arg1, String arg2, String arg3, String arg4, @OwledArgument String arg5) {
        log("executed annotatedFifthArgument");
    }

    public void log(String aMessage) {
        if (aMessage != null) {
            System.out.println(new Timestamp(System.currentTimeMillis()).toString() + " [" + Thread.currentThread() + "] * LOG * " + aMessage);
        } else {
            System.out.println("\n\n");
        }
    }
}
