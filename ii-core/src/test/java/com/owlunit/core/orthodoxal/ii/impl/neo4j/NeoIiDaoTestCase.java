package com.owlunit.core.orthodoxal.ii.impl.neo4j;

import com.owlunit.core.orthodoxal.ii.Ii;
import com.owlunit.core.orthodoxal.ii.exception.NotFoundException;
import org.neo4j.graphdb.GraphDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

public class NeoIiDaoTestCase extends AbstractDependencyInjectionSpringContextTests {

    @SuppressWarnings("UnusedDeclaration")
    final Logger log = LoggerFactory.getLogger(NeoIiDaoTestCase.class);

    @Autowired
    NeoIiDaoImpl dao;

    GraphDatabaseService graphDb;

    protected String[] getConfigLocations() {
        return new String[]{"beans/applicationContext.xml"};
    }

    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
    }

    @Override
    public void onTearDown() throws Exception {
        super.onTearDown();
    }

    public NeoIiDaoImpl getDao() {
        return dao;
    }

    public void setDao(NeoIiDaoImpl dao) {
        this.dao = dao;
    }

    public void testCreateLoadDeleteIi() throws Exception {
        Ii createdItem = dao.createInformationItem();
        assertNotNull("Can not create Ii", createdItem);
        long id = createdItem.getId();
        
        Ii loadedItem = dao.load(id);
        assertEquals(createdItem, loadedItem);

        dao.deleteInformationItem(loadedItem);
        try {
            dao.load(id);
        } catch (NotFoundException e) {
            return;
        }
        fail("Exception should be thrown");
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
    }}
