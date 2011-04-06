//package com.manymonkeys.core.ii;
//
//import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
//
//import java.util.ArrayList;
//import java.util.Collection;
//
///**
// * Many Monkeys
// *
// * @author Anton Chebotaev
// */
//public class CoreBaseTestCase extends AbstractDependencyInjectionSpringContextTests {
//
//    protected String[] getConfigLocations() {
//        return new String[]{findConfigPath()};
//    }
//
//    private String findConfigPath() {
//        return "beans/applicationContext.xml";
//    }
//
//    @Override
//    protected void onSetUp() throws Exception {
//        super.onSetUp();
//        logger.debug("Set up");
//    }
//
//    public void onTearDown() {
//    }
//
//}
