package com.manymonkeys.ex.integration;

import com.manymonkeys.ex.json.controllers.OwlsFlatApi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static junit.framework.Assert.assertNotNull;

/**
 * @author Ilya Pimenov
 *         Owl Proprietary
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {
                "/applicationContext.xml"
        }
)
public class ScenarioTests {

    @Autowired
    OwlsFlatApi service;

    @Before
    public void before() {
        //
    }

    @Test
    public void testVersionMethod() {
        System.out.println(service.version());
        assertNotNull(service.version());
    }

    @Test
    public void scenario1() {
        assertNotNull("notnull");
    }
}
