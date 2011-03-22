package com.manymonkeys.core.ii.impl.neo4j;

import com.manymonkeys.core.ii.InformationItem;
import com.manymonkeys.core.ii.LazyResults;
import org.neo4j.graphdb.Relationship;

import java.util.Iterator;
import java.util.Map;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class LazyResultsComponentsImpl implements LazyResults<Map.Entry<InformationItem, Double>> {

    private Iterator<Relationship> relationships;

    public LazyResultsComponentsImpl(Iterable<Relationship> relationships) {
        this.relationships = relationships.iterator();
    }

    @Override
    public int size() {
        return -1;
    }

    @Override
    public void close() {
        // do nothing
    }

    @Override
    public Iterator<Map.Entry<InformationItem, Double>> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        return relationships.hasNext();  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map.Entry<InformationItem, Double> next() {
        Relationship relationship = relationships.next();
        return new Entry(new Neo4jInformationItemImpl(relationship.getEndNode()), (Double) relationship.getProperty(Neo4jInformationItemImpl.RELATIONSHIP_WEIGHT));
    }

    @Override
    public void remove() {
        relationships.remove();
    }

    static class Entry implements Map.Entry<InformationItem, Double> {

        private InformationItem item;
        private Double value;

        Entry(InformationItem item, Double value) {
            this.item = item;
            this.value = value;
        }

        @Override
        public InformationItem getKey() {
            return item;
        }

        @Override
        public Double getValue() {
            return value;
        }

        @Override
        public Double setValue(Double value) {
            this.value = value;
            return value;
        }
    }
}
