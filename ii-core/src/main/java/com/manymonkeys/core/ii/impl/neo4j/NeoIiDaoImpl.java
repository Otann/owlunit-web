package com.manymonkeys.core.ii.impl.neo4j;

import com.manymonkeys.core.ii.Ii;
import com.manymonkeys.core.ii.IiDao;
import com.manymonkeys.core.ii.exception.NotFoundException;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.*;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.kernel.Traversal;
import org.neo4j.kernel.Uniqueness;

import java.util.*;

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

public class NeoIiDaoImpl implements IiDao {

    private GraphDatabaseService db;
    private int depth;

    private static String INDEX_NAME = "ITEMS";
    private static String PROPERTY_NAME_WEIGHT = "weight";

    private Evaluator depthEvaluator = new Evaluator() {
        @Override
        public Evaluation evaluate(Path path) {
            if (path.length() == depth) {
                return Evaluation.INCLUDE_AND_PRUNE;
            } else if (path.length() < depth) {
                return Evaluation.INCLUDE_AND_CONTINUE;
            } else {
                return Evaluation.EXCLUDE_AND_PRUNE;
            }
        }
    };

    public NeoIiDaoImpl(GraphDatabaseService db) {
        this.db = db;
    }

    public NeoIiDaoImpl(GraphDatabaseService db, int depth) {
        this.db = db;
        this.depth = depth;
    }

    @Override
    public Ii createInformationItem() {
        Transaction tx = db.beginTx();
        try {
            Node node = db.createNode();
            Ii result = new NeoIiImpl(node);
            tx.success();
            return result;
        } finally {
            tx.finish();
        }
    }

    @Override
    public void deleteInformationItem(Ii ii) {
        Transaction tx = db.beginTx();
        try {

            NeoIiImpl item = checkImpl(ii);
            item.node.delete();
            tx.success();

        } catch (org.neo4j.graphdb.NotFoundException e) {
            throw new NotFoundException(ii, e);
        } finally {
            tx.finish();
        }
    }

    @Override
    public Ii load(long id) {
        try {
            Node node = db.getNodeById(id);
            return new NeoIiImpl(node);
        } catch (org.neo4j.graphdb.NotFoundException e) {
            throw new NotFoundException(e);
        }
    }

    @Override
    public Collection<Ii> load(Collection<Long> longs) {
        List<Ii> result = new LinkedList<Ii>();
        for (long id : longs) {
            result.add(load(id));
        }
        return result;
    }

    @Override
    public Ii setMeta(Ii ii, String key, String value) {
        return setMetaExtended(ii, key, value, true);
    }

    @Override
    public Ii setMetaUnindexed(Ii ii, String key, String value) {
        return setMetaExtended(ii, key, value, false);
    }

    public Ii setMetaExtended(Ii ii, String key, String value, boolean isIndexed) {
        Transaction tx = db.beginTx();
        try {

            NeoIiImpl item = checkImpl(ii);

            try {
                db.index().forNodes(INDEX_NAME).remove(item.node, key);
            } catch (org.neo4j.graphdb.NotFoundException e){
                // so there was no meta, that's fine
            }

            item.node.setProperty(key, value);
            if (isIndexed) {
                db.index().forNodes(INDEX_NAME).add(item.node, key, value);
            }
            
            tx.success();

            if (NeoIiImpl.NOT_LOADED.equals(item.meta)) {
                return item;
            } else {
                NeoIiImpl newItem = new NeoIiImpl(item);
                newItem.meta = new HashMap<String, String>(item.meta);
                newItem.meta.put(key, value);
                return newItem;
            }

        } catch (org.neo4j.graphdb.NotFoundException e) {
            throw new NotFoundException(ii, e);
        } finally {
            tx.finish();
        }
    }

    @Override
    public Collection<Ii> load(String key, String value) {
        List<Ii> result = new ArrayList<Ii>();
        for (Node node : db.index().forNodes(INDEX_NAME).get(key, value)) {
            result.add(new NeoIiImpl(node));
        }
        return result;
    }

    @Override
    public Ii loadMeta(Ii ii) {
        NeoIiImpl item = checkImpl(ii);
        
        Map<String, String> meta = new HashMap<String, String>();
        for (String key : item.node.getPropertyKeys()) {
            meta.put(key, (String) item.node.getProperty(key));
        }
        
        NeoIiImpl newItem = new NeoIiImpl(item);
        newItem.meta = meta;
        return newItem;
    }

    @Override
    public Collection<Ii> loadMeta(Collection<Ii> items) {
        List<Ii> result = new ArrayList<Ii>();
        for (Ii item : items) {
            result.add(loadMeta(item));
        }
        return result;
    }

    @Override
    public Ii removeMeta(Ii ii, String key) {
        Transaction tx = db.beginTx();
        try {

            NeoIiImpl item = checkImpl(ii);
            item.node.removeProperty(key);
            tx.success();

            if (NeoIiImpl.NOT_LOADED.equals(item.meta)) {
                return item;
            } else {
                NeoIiImpl newItem = new NeoIiImpl(item);
                newItem.meta = new HashMap<String, String>(item.meta);
                newItem.meta.remove(key);
                return newItem;
            }

        } catch (org.neo4j.graphdb.NotFoundException e) {
            throw new NotFoundException(ii, e);
        } finally {
            tx.finish();
        }
    }

    @Override
    public Ii loadComponents(Ii ii) {
        NeoIiImpl item = checkImpl(ii);
        
        Map<Ii, Double> components = new HashMap<Ii, Double>();
        for (Relationship relationship : item.node.getRelationships(RelTypes.CONNECTED, Direction.OUTGOING)) {
            Node node = relationship.getEndNode();
            Double weight = (Double) relationship.getProperty(PROPERTY_NAME_WEIGHT);
            components.put(new NeoIiImpl(node), weight);
        }
        
        NeoIiImpl newItem = new NeoIiImpl(item);
        newItem.components = components;
        return newItem;
    }

    @Override
    public Collection<Ii> loadComponents(Collection<Ii> items) {
        List<Ii> result = new ArrayList<Ii>();
        for (Ii item : items) {
            result.add(loadComponents(item));
        }
        return result;
    }

    @Override
    public Ii loadParents(Ii ii) {
        NeoIiImpl item = checkImpl(ii);

        Map<Ii, Double> components = new HashMap<Ii, Double>();
        for (Relationship relationship : item.node.getRelationships(RelTypes.CONNECTED, Direction.INCOMING)) {
            Node node = relationship.getStartNode();
            Double weight = (Double) relationship.getProperty(PROPERTY_NAME_WEIGHT);
            components.put(new NeoIiImpl(node), weight);
        }

        NeoIiImpl newItem = new NeoIiImpl(item);
        newItem.components = components;
        return newItem;
    }

    @Override
    public Collection<Ii> loadParents(Collection<Ii> items) {
        List<Ii> result = new ArrayList<Ii>();
        for (Ii item : items) {
            result.add(loadParents(item));
        }
        return result;
    }

    @Override
    public Collection<Ii> search(String key, String prefix) {
        List<Ii> result = new ArrayList<Ii>();
        for (Node node : db.index().forNodes(INDEX_NAME).query(key, String.format("%s*", prefix))) {
            result.add(new NeoIiImpl(node));
        }
        return result;
    }

    @Override
    public Ii setComponentWeight(Ii itemIi, Ii componentIi, Double weight) {
        Transaction tx = db.beginTx();
        try {

            NeoIiImpl item = checkImpl(itemIi);
            NeoIiImpl component = checkImpl(componentIi);
            Relationship connection = getConnection(item.node, component.node);

            if (connection == null) {
                connection = item.node.createRelationshipTo(component.node, RelTypes.CONNECTED);
            }
            connection.setProperty(PROPERTY_NAME_WEIGHT, weight);
            tx.success();

            if (NeoIiImpl.NOT_LOADED.equals(item.components)) {
                return item;
            } else {
                NeoIiImpl newItem = new NeoIiImpl(item);
                newItem.components = new HashMap<Ii, Double>(item.components);
                newItem.components.put(component, weight);
                return newItem;
            }

        } catch (org.neo4j.graphdb.NotFoundException e) {
            throw new NotFoundException(e);
        } finally {
            tx.finish();
        }
    }

    @Override
    public Ii removeComponent(Ii itemIi, Ii componentIi) {
        Transaction tx = db.beginTx();
        try {

            NeoIiImpl item = checkImpl(itemIi);
            NeoIiImpl component = checkImpl(componentIi);
            Relationship connection = getConnection(item.node, component.node);

            if (connection == null) {
                return item;
            } else {
                connection.delete();
            }
            tx.success();

            if (NeoIiImpl.NOT_LOADED.equals(item.components)) {
                return item;
            } else {
                NeoIiImpl newItem = new NeoIiImpl(item);
                newItem.components = new HashMap<Ii, Double>(item.components);
                newItem.components.remove(component);
                return newItem;
            }

        } catch (org.neo4j.graphdb.NotFoundException e) {
            throw new NotFoundException(e);
        } finally {
            tx.finish();
        }
    }

    @Override
    public Map<Ii, Double> getIndirectComponents(Ii ii) {
        
        NeoIiImpl item = checkImpl(ii);
        Map<Node, Double> nodes = new HashMap<Node, Double>();

        // Create traverser
        Traverser traverser = Traversal.description()
                .breadthFirst()
                .relationships(RelTypes.CONNECTED, Direction.OUTGOING)
                .uniqueness(Uniqueness.NODE_PATH)
                .evaluator(Evaluators.excludeStartPosition())
                .evaluator(depthEvaluator)
                .traverse(item.node);

        // Process each path
        for (Path path : traverser) {
            double weight = 0;
            int qualifier = 1;
            for (Relationship rel : path.relationships()) {
                double w = (Double) rel.getProperty(PROPERTY_NAME_WEIGHT);
                weight += w / qualifier;
                qualifier <<= 1;
            }
            incrementMapValue(nodes, path.endNode(), weight);
        }

        // Convert nodes in results to Iis
        Map<Ii, Double> result = new HashMap<Ii, Double>();
        for (Node node : nodes.keySet()) {
            result.put(new NeoIiImpl(node), nodes.get(node));
        }
        return result;
    }

    /////////////////////////////////////////////
    ////// Private
    /////////////////////////////////////////////

    private void incrementMapValue(Map<Node, Double> map, Node item, double increment) {
        if (map.containsKey(item)) {
            Double oldValue = map.get(item);
            map.put(item, oldValue + increment);
        } else {
            map.put(item, increment);
        }
    }
    

    /////////////////////////////////////////////
    ////// Static
    /////////////////////////////////////////////

    private static NeoIiImpl checkImpl(Ii item) {
        if (item instanceof NeoIiImpl) {
            return (NeoIiImpl) item;
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    private static Relationship getConnection(Node a, Node b) {
        Iterator<Relationship> iteratorA = a.getRelationships(Direction.OUTGOING, RelTypes.CONNECTED).iterator();
        Iterator<Relationship> iteratorB = a.getRelationships(Direction.INCOMING, RelTypes.CONNECTED).iterator();

        while (iteratorA.hasNext() && iteratorB.hasNext()) {
            Relationship relA = iteratorA.next();
            Relationship relB = iteratorB.next();

            if (relA.getEndNode().equals(b)) {
                return relA;
            }
            if (relB.getStartNode().equals(a)) {
                return relB;
            }
        }

        return null;
    }

}
