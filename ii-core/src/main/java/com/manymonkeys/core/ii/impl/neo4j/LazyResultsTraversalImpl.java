package com.manymonkeys.core.ii.impl.neo4j;

import com.manymonkeys.core.ii.InformationItem;
import com.manymonkeys.core.ii.LazyResults;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Traverser;

import java.util.Iterator;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class LazyResultsTraversalImpl implements LazyResults<InformationItem> {

    private Iterator<Node> iterator;

    public LazyResultsTraversalImpl(Traverser traverser) {
        iterator = traverser.iterator();
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
        // do nothing
    }
}
