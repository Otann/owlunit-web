package com.manymonkeys.core.ii.impl.cassandra;

import com.manymonkeys.core.ii.Ii;
import com.manymonkeys.core.ii.IiDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class CassandraIiDaoTestCase extends AbstractDependencyInjectionSpringContextTests {

    @SuppressWarnings("UnusedDeclaration")
    final Logger log = LoggerFactory.getLogger(CassandraIiDaoTestCase.class);

    @Autowired
    CassandraIiDaoHectorImpl dao;

    protected String[] getConfigLocations() {
        return new String[]{"beans/applicationContext.xml"};
    }

    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
    }

    public void onTearDown() throws Exception {
        super.onTearDown();
    }

    public CassandraIiDaoHectorImpl getDao() {
        return dao;
    }

    public void setDao(CassandraIiDaoHectorImpl dao) {
        this.dao = dao;
    }

    public void testCreateDeleteLoadIi() throws Exception {
        Ii item = dao.createInformationItem();
        assertNotNull("Can not create Ii", item);

        long id = item.getId();
        dao.deleteInformationItem(item);
        assertNull("Can not delete Ii", dao.load(id));
    }

    public void testSetLoadMeta() throws Exception {
        Ii item = dao.createInformationItem();
        String metaKey = "key";
        String metaValue = "value";

        dao.setMeta(item, metaKey, metaValue);
        Ii loadedItem = dao.loadMeta(item);

        assertEquals("Meta values via set and load are different", metaValue, loadedItem.getMeta(metaKey));

        dao.deleteInformationItem(item);
    }

    public void testLoadByMeta() throws Exception {

        Ii item = dao.createInformationItem();
        String randomString = UUID.randomUUID().toString();
        String metaKey = "key-" + randomString;
        String metaValue = "value-" + randomString;

        dao.setMeta(item, metaKey, metaValue);

        Collection<Ii> loadedItems = dao.load(metaKey, metaValue);

        assertTrue("Can not load Ii by metadata pair", loadedItems.size() == 1);

        assertEquals("Loaded by meta item differs from original item", loadedItems.iterator().next(), item);
    }

    public void testAddRemoveComponent() throws Exception {
        Ii parent = dao.createInformationItem();
        Ii child = dao.createInformationItem();

        dao.setComponentWeight(parent, child, 1.0);
        parent = dao.loadComponents(parent);

        assertEquals(1.0, parent.getComponentWeight(child));

        dao.removeComponent(parent, child);
        parent = dao.loadComponents(parent);

        assertNull(parent.getComponentWeight(child));

        dao.deleteInformationItem(parent);
        dao.deleteInformationItem(child);
    }

    public void testIndirectWeights() throws Exception {
        Ii parent = dao.createInformationItem();
        Ii child = dao.createInformationItem();
        Ii grandchild = dao.createInformationItem();
        Ii grand2child = dao.createInformationItem();

        dao.setComponentWeight(parent, child, 1.0);
        dao.setComponentWeight(child, grandchild, 1.0);
        dao.setComponentWeight(grandchild, grand2child, 1.0);

        Map<Ii, Double> indirect = dao.getIndirectComponents(parent);
        
        assertEquals("Wrong indirect weight to grandchild", indirect.get(grandchild), 0.5);
        assertEquals("Wrong indirect weight to grandgrandchild", indirect.get(grand2child), 0.375);

        dao.deleteInformationItem(parent);
        dao.deleteInformationItem(child);
        dao.deleteInformationItem(grandchild);
    }
}