package com.manymonkeys.core.ii.impl.neo4j;

import com.manymonkeys.core.ii.InformationItem;
import com.manymonkeys.core.ii.LazyResults;
import com.manymonkeys.core.ii.InformationItemDao;
import org.neo4j.graphdb.*;
import org.neo4j.index.IndexService;

/**
 * Rocket Science Software
 *
 * @author Anton Cheotaev
 */
public class Neo4jInformationItemDaoImpl implements InformationItemDao {

    /**
     * Database
     * Injected via Spring
     */
    private GraphDatabaseService graphDb;
    /**
     * Index service for DB
     * Injected via Spring
     */
    private IndexService indexService;

    /**
     * Data creator for InformationItem
     * Creates node in DB
     *
     * @return newly created InformationItem
     */
    public InformationItem createInformationItem() {
        Transaction tx = graphDb.beginTx();
        try {
            Neo4jInformationItemImpl ii = new Neo4jInformationItemImpl(graphDb.createNode());

            setMeta(ii, Neo4jInformationItemImpl.DAO_CLASS_NAME, this.getClass().getName());
            setItemClass(ii, Neo4jInformationItemImpl.class.getName());

            tx.success();
            return ii;
        } catch (Exception e) {
            tx.failure();
            return null;
        } finally {
            tx.finish();
        }
    }

    /**
     * Deletes underlying node in DB with all connections to it
     * There could be errors, as none of the InformationItem methods wont work properly
     * If link to that object is still alive in different thread, REALLY BAD THINGS may happen
     * TODO: In a good way, object should be eligible for GC after that
     * TODO: In a perfect way there should be no deletion in the system ;)
     *
     * @param item to be deleted
     */
    public void deleteInformationItem(InformationItem item) {
        Transaction tx = graphDb.beginTx();
        try {
            if (!(item instanceof Neo4jInformationItemImpl)) {
                throw new IllegalArgumentException("Can't operate with this implementation of ii");
            }

            Neo4jInformationItemImpl ii = (Neo4jInformationItemImpl) item;

            Node node = ii.getNode();

            Iterable<Relationship> relationships = node.getRelationships(Neo4jInformationItemImpl.Relations.COMPONENT, Direction.BOTH);
            for (Relationship relationship : relationships) {
                relationship.delete();
            }

            node.delete(); //TODO: reconsider, REALLY BAD things may happen after that
            tx.success();
        } catch (Exception e) {
            tx.failure();
        } finally {
            tx.finish();
        }
    }

    /**
     * Wrapper around DB method that sets metadata to nodes
     * This one also indexes any metadata we set
     *
     * @param item  to apply metadata
     * @param name  of the key
     * @param value to be set
     */
    public void setMeta(InformationItem item, String name, String value) {
        Transaction tx = graphDb.beginTx();
        try {

            if (!(item instanceof Neo4jInformationItemImpl)) {
                throw new IllegalArgumentException("Can't operate with this implementation of ii");
            }

            Node node = extractNode((Neo4jInformationItemImpl) item);
            node.setProperty(name, value);
            indexService.index(node, name, value);

            tx.success();
        } catch (Exception e) {
            tx.failure();
        } finally {
            tx.finish();
        }
    }

    /**
     * Checks connection between two items and creates one if needed.
     * After method ensures in connection existance, it sets connection weight
     * TODO: add relations index service. current speed depends of amount of relations linearly
     *
     * @param item      to add connection from
     * @param component item to add connection to
     * @param weight    of connection
     */
    public void setComponentWeight(InformationItem item, InformationItem component, double weight) {
        if (!(item instanceof Neo4jInformationItemImpl && component instanceof Neo4jInformationItemImpl)) {
            throw new IllegalArgumentException("Can't operate with this implementation of ii");
        }

        Node itemNode = extractNode((Neo4jInformationItemImpl) item);
        Node componentNode = extractNode((Neo4jInformationItemImpl) component);

        Transaction tx = graphDb.beginTx();
        try {

            boolean done = false;
            for (Relationship relationship : itemNode.getRelationships(Neo4jInformationItemImpl.Relations.COMPONENT, Direction.OUTGOING)) {
                if (relationship.getEndNode().equals(componentNode)) {
                    relationship.setProperty(Neo4jInformationItemImpl.RELATIONSHIP_WEIGHT, weight);
                    done = true;
                }
            }

            if (!done) {
                Relationship relationship = itemNode.createRelationshipTo(componentNode, Neo4jInformationItemImpl.Relations.COMPONENT);
                relationship.setProperty(Neo4jInformationItemImpl.RELATIONSHIP_WEIGHT, weight);
            }

            tx.success();
        } catch (Exception e) {
            tx.failure();
        } finally {
            tx.finish();
        }
    }

    /**
     * Removes connection between two items
     *
     * @param item      to remove connection from
     * @param component item to remove connection to
     */
    public void removeComponent(InformationItem item, InformationItem component) {
        Transaction tx = graphDb.beginTx();
        try {

            if (!(item instanceof Neo4jInformationItemImpl && component instanceof Neo4jInformationItemImpl)) {
                throw new IllegalArgumentException("Can't operate with this implementation of ii");
            }
            Node itemNode = extractNode((Neo4jInformationItemImpl) item);
            Node componentNode = extractNode((Neo4jInformationItemImpl) component);

            for (Relationship relationship : itemNode.getRelationships(Neo4jInformationItemImpl.Relations.COMPONENT, Direction.OUTGOING)) {
                if (relationship.getEndNode().equals(componentNode)) {
                    relationship.delete();
                }
            }

            tx.success();
        } catch (Exception e) {
            tx.failure();
        } finally {
            tx.finish();
        }
    }

    /**
     * Loads item by id
     * Make other services outside java ecosystem memorize items and load them
     *
     * @param id of the Item
     * @return InformationItem
     */
    public InformationItem getById(long id) {
        Transaction tx = graphDb.beginTx();
        try {
            Neo4jInformationItemImpl ii = new Neo4jInformationItemImpl(graphDb.getNodeById(id));
            tx.success();
            return ii;
        } catch (Exception e) {
            tx.failure();
            return null;
        } finally {
            tx.finish();
        }
    }

	/**
	 * Performs search by matching key-value metadata pair with Lucene query
	 *
	 * @param metaKey metadata key
	 * @param metaValue metadata value
	 * @return collection of matched items
	 */
	public LazyResults<InformationItem> getByMeta(String metaKey, String metaValue) {
        Transaction tx = graphDb.beginTx();
        try {
            LazyResults<InformationItem> result = new LazyResultsIndexImpl(indexService.getNodes(metaKey, metaValue));
            tx.success();
            return result;
        } catch (Exception e) {
            tx.failure();
            return null;
        } finally {
            tx.finish();
        }
    }

    public LazyResults<InformationItem> getAll() {
        Transaction tx = graphDb.beginTx();
        try {
            LazyResults<InformationItem> results = new LazyResultsNodeImpl(graphDb.getAllNodes());
            tx.success();
            return results;
        } catch (Exception e) {
            tx.failure();
            return null;
        } finally {
            tx.finish();
        }
    }

    // Inheritance helpers

    protected Transaction beginTransaction() {
        return graphDb.beginTx();
    }

    protected static Node extractNode(Neo4jInformationItemImpl item) {
        return item.getNode();
    }

    protected void setItemClass(InformationItem item, String name) {
        setMeta(item, Neo4jInformationItemImpl.ITEM_CLASS_NAME, name);
    }

    // Spring setters

    public void setGraphDb(GraphDatabaseService graphDb) {
        this.graphDb = graphDb;
    }

    public void setIndexService(IndexService indexService) {
        this.indexService = indexService;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        graphDb.shutdown();
    }
}
