package com.manymonkeys.security.shiro.aspectj;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;
import org.junit.*;

/**
 * Owls
 *
 * @author Ilya Pimenov
 */
public class TryMeServiceTest {

    private static AnnotatedService service;

    @BeforeClass
    public static void setUpClass() throws Exception {
        Logger log = Logger.getLogger(AspectjAnnotationsOwlsAuthorizingMethodInterceptor.class);
        log.addAppender(new ConsoleAppender(new SimpleLayout(), ConsoleAppender.SYSTEM_OUT));
        log.setLevel(Level.TRACE);

        Factory<SecurityManager> factory = new IniSecurityManagerFactory("classpath:shiroAnnotatedServiceTest.ini");
        SecurityManager securityManager = factory.getInstance();
        SecurityUtils.setSecurityManager(securityManager);

        service = new AnnotatedServiceImpl();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        //don't corrupt other test cases since this is static memory:
        SecurityUtils.setSecurityManager(null);
    }

    private Subject subject;

    @Before
    public void setUp() throws Exception {
        subject = SecurityUtils.getSubject();
    }

    @After
    public void tearDown() throws Exception {
        subject.logout();
    }

    private void loginAsUser() {
        subject.login(new UsernamePasswordToken("joe", "bob"));
    }

    private void loginAsAdmin() {
        subject.login(new UsernamePasswordToken("root", "secret"));
    }

    // TEST YEAH
    @Test
    public void testAnnotatedMethod_asUser() throws Exception {
        loginAsUser();
        service.annotatedMethod();
    }

    @Test
    public void testYeah_asAdmin() throws Exception {
        loginAsAdmin();
        service.annotatedMethod();
    }

    @Test
    public void testAnnotatedArgument_asAdmin() throws Exception {
        loginAsAdmin();
        service.annotatedArgument("arg1");
    }

    @Test
    public void testAnnotatedSecondArgument_asAdmin() throws Exception {
        loginAsAdmin();
        service.annotatedSecondArgument("arg1", "arg2");
    }
}