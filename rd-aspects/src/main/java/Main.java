import java.util.Arrays;

import javax.annotation.PostConstruct;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {
    public static void main(String[] args) {
        ApplicationContext ac = new ClassPathXmlApplicationContext("context.xml");
        TestBean testBean = (TestBean) ac.getBean("test");
        testBean.work();
        testBean.stop();
    }
}

class TestBean {

    public void work() {
        System.out.println("work работает блеать !");
    }

    public void stop() {
        System.out.println("stop работает блеать !");
    }

}

@Aspect
class LoggingAspectPC {
    private String beforeMessage;
    private String afterMessage;

    @Pointcut("execution(* TestBean.*(..))")
    private void testBeanExecution() {
    }

    @Around("testBeanExecution()")
    public Object log(ProceedingJoinPoint pjp) throws Throwable {
        System.out.printf(this.beforeMessage, pjp.getSignature().getName(), Arrays.toString(pjp.getArgs()));
        System.out.println();
        Object ret = pjp.proceed();
        System.out.printf(this.afterMessage, pjp.getSignature().getName(), Arrays.toString(pjp.getArgs()));
        System.out.println();
        return ret;
    }

    @After("testBeanExecution()")
    public void afterCall(JoinPoint jp) {
        System.out.println("After");
    }

    @PostConstruct
    public void initialize() {
        System.out.println("initialize:" + this.beforeMessage);
        System.out.println("initialize:" + this.afterMessage);
    }

    public void setBeforeMessage(String beforeMessage) {
        this.beforeMessage = beforeMessage;
    }

    public void setAfterMessage(String afterMessage) {
        this.afterMessage = afterMessage;
    }
}
                                                                                                                                                                                                   