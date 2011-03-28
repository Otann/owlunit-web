package com.manymonkeys.core.ii.impl.neo4j;

import org.neo4j.graphdb.Node;
import org.neo4j.index.IndexHits;

import java.util.Iterator;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class LazyResultsIndexImpl implements LazyResults<InformationItem> {

    private IndexHits<Node> index;

    public LazyResultsIndexImpl(IndexHits<Node> index) {
        this.index = index;
    }

    @Override
    public Iterator<InformationItem> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        return index.hasNext();
    }

    @Override
    public InformationItem next() {
        return new Neo4jInformationItemImpl(index.next());
    }

    @Override
    public void remove() {
        index.remove();
    }

    @Override
    public int size() {
        return index.size();
    }

    @Override
    public void close() {
        index.close();
    }
}
