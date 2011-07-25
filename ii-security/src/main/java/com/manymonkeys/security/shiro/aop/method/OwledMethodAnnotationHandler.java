package com.manymonkeys.security.shiro.aop.method;

import com.manymonkeys.security.shiro.OwledMethodException;
import com.manymonkeys.security.shiro.annotation.OwledMethod;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.aop.AuthorizingAnnotationHandler;
import org.apache.shiro.subject.Subject;

import java.lang.annotation.Annotation;

/**
 * Owls
 *
 * @author Ilya Pimenov
 */
public class OwledMethodAnnotationHandler extends AuthorizingAnnotationHandler {

    public void assertAuthorized(Annotation a) throws AuthorizationException {
        if (!(a instanceof OwledMethod)) return;

        OwledMethod rducAnnotation = (OwledMethod) a;
        Subject subject = getSubject();

        if (true) {
            System.out.println("OwledMethodAnnotationHandler was here !");
            /* do checks and bla-bla here */
            return;
        } else {
            throw new OwledMethodException();
        }
    }

    public OwledMethodAnnotationHandler() {
        super(OwledMethod.class);
    }
}