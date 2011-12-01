package com.manymonkeys.core.ii.impl.cassandra;

import com.manymonkeys.core.ii.Ii;
import com.manymonkeys.core.ii.IiDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class IiDaoTestCase extends AbstractDependencyInjectionSpringContextTests {

    final Logger log = LoggerFactory.getLogger(IiDaoTestCase.class);

    @Autowired
    IiDao dao;

    protected String[] getConfigLocations() {
        return new String[]{findConfigPath()};
    }

    private String findConfigPath() {
        return "beans/applicationContext.xml";
    }

    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();

        log.debug("================================");
        log.debug("------      Set up        ------");
        log.debug("================================");
    }

    public void onTearDown() throws Exception {
        super.onTearDown();
        log.debug("================================");
        log.debug("------     Tear down      ------");
        log.debug("================================");
    }

    public void testCreateLoadIi() throws Exception {
        Ii item = dao.createInformationItem();
        log.debug("Created Ii with uuid: " + item.getUUID().toString());
    }

    public IiDao getDao() {
        return dao;
    }

    public void setDao(IiDao dao) {
        this.dao = dao;
    }
}
