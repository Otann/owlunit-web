package com.manymonkeys.core.ii.impl.neo4j;

import org.neo4j.graphdb.Node;

import java.util.Iterator;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class LazyResultsNodeImpl implements LazyResults<InformationItem> {

    private Iterator<Node> iterator;

    public LazyResultsNodeImpl(Iterable<Node> nodes) {
        iterator = nodes.iterator();
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
    public Iterator<InformationItem> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public InformationItem next() {
        return new Neo4jInformationItemImpl(iterator.next());
    }

    @Override
    public void remove() {
        iterator.remove();
    }
}
