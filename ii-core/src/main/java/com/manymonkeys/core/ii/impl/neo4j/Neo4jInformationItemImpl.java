package com.manymonkeys.core.ii.impl.neo4j;

import org.neo4j.graphdb.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Rocket Science Software
 *
 * @author Anton Chebotaev
 */
public class Neo4jInformationItemImpl implements InformationItem {


    public static enum Relations implements RelationshipType {
        COMPONENT
    }

    /**
     * Key to metadata or relationship that stores weight
     */
    public static final String RELATIONSHIP_WEIGHT = "Relationship weight";

    /**
     * Key to metadata that stores class name of originally created item.
     * Set by corresponding dao, may be overridden by subclass ot that dao
     */
    public static final String ITEM_CLASS_NAME = "Item Class";

    /**
     * Key to metadata that stores class name of dao that created the item.
     * Exact value evaluated in runtime, depending of subclass.
     */
    public static final String DAO_CLASS_NAME = "Creator Class name";

    /**
     * Underlying key node that whole typesystem is built around.
     * Stores all related metadata
     */
    private final Node node;

    /**
     * Creates wrap around db node
     *
     * @param node to construct from
     */
    public Neo4jInformationItemImpl(Node node) {
        this.node = node;
    }

    /**
     * Protects from setting node.
     * Although node is final, this looks more clear
     *
     * @return node
     */
    public Node getNode() {
        return node;
    }

    /**
     * Allows to temporary store link to that item outside java ecosystem and retrieve that item later
     *
     * @return id in Meo4j DB
     */
    public long getId() {
        return this.node.getId();
    }

    /**
     * Wrapper around DB method that allows to store any kind of string content
     * Covered in string to make search little bit more simple
     *
     * @param name of the key
     * @return stored value
     */
    public String getMeta(String name) {
        if (node.hasProperty(name)) {
            return (String) node.getProperty(name);
        } else {
            return null;
        }
    }

    /**
     * Returns weight of link between this and requested item.
     *
     * @param item that current is connected to
     * @return weight of connection
     * @throws IllegalArgumentException if argument is implemented by different class
     */
    public double getComponentWeight(InformationItem item) {
        if (item instanceof Neo4jInformationItemImpl) {
            Node componentNode = ((Neo4jInformationItemImpl) item).node;

            // Iterate through connections and get
            for (Relationship relationship : node.getRelationships(Neo4jInformationItemImpl.Relations.COMPONENT, Direction.OUTGOING)) {
                if (relationship.getEndNode().equals(componentNode)) {
                    return (Double) relationship.getProperty(RELATIONSHIP_WEIGHT);
                }
            }

            return 0;
        } else {
            throw new IllegalArgumentException("Can't get weight of another implementation of item as a component");
        }
    }

    /**
     * Returns all components of that InformationItem.
     * Easily extended by depth and other kind of graph things
     *
     * @return collection of components
     */
    public LazyResults<InformationItem> getComponents() {
        Traverser nodes = node.traverse(Traverser.Order.BREADTH_FIRST,
                StopEvaluator.DEPTH_ONE,
                ReturnableEvaluator.ALL_BUT_START_NODE,
                Neo4jInformationItemImpl.Relations.COMPONENT,
                Direction.OUTGOING);
        return new LazyResultsTraversalImpl(nodes);
    }

    /**
     * Returns all components that contains this item as a component.
     *
     * @return collection of components
     */
    public LazyResults<InformationItem> getItemsContainingThis() {
        Traverser nodes = node.traverse(Traverser.Order.BREADTH_FIRST,
                StopEvaluator.DEPTH_ONE,
                ReturnableEvaluator.ALL_BUT_START_NODE,
                Neo4jInformationItemImpl.Relations.COMPONENT,
                Direction.INCOMING);
        return new LazyResultsTraversalImpl(nodes);
    }


    /**
     * Wraps connections to other nodes in DB to relations in InformationItem terms
     *
     * @return map between components and their weights
     */
    public LazyResults<Map.Entry<InformationItem, Double>> getComponentsWeights() {
        return new LazyResultsComponentsImpl(node.getRelationships(Neo4jInformationItemImpl.Relations.COMPONENT, Direction.OUTGOING));
    }

    @Override
    public Map<InformationItem, Double> getComponentsWeightsMap() {
        Map<InformationItem, Double> result = new HashMap<InformationItem, Double>();
        for (Map.Entry<InformationItem, Double> entry : this.getComponentsWeights()) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Neo4jInformationItemImpl && node.equals(((Neo4jInformationItemImpl) obj).node);
    }

    @Override
    public int hashCode() {
        return node.hashCode();
    }

}