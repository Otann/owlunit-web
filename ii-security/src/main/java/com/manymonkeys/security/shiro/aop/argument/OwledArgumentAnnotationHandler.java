package com.manymonkeys.security.shiro.aop.argument;

import com.manymonkeys.security.shiro.OwledArgumentException;
import com.manymonkeys.security.shiro.annotation.OwledArgument;
import org.apache.shiro.ShiroException;
import org.apache.shiro.subject.Subject;

import java.lang.annotation.Annotation;

/**
 * Owls
 *
 * @author Ilya Pimenov
 */
public class OwledArgumentAnnotationHandler extends ArgumentAnnotationHandler {

    public OwledArgumentAnnotationHandler() {
        super(OwledArgument.class);
    }

    @Override
    public void assertRules(Annotation a, Object arg) throws ShiroException {
        if (!(a instanceof OwledArgument)) {
            return;
        }

        OwledArgument mcAnnotation = (OwledArgument) a;
        Subject subject = getSubject();

        if (true) {
            System.out.println("OwledArgumentAnnotationHandler was here !");
            System.out.println("check - " + arg.toString());
            /* do checks and bla-bla here */
            return;
        } else {
            throw new OwledArgumentException();
        }

    }
}