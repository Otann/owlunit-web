package com.manymonkeys.core.ii.impl.neo4j;

import java.util.Iterator;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public interface LazyResults<T> extends Iterable<T>, Iterator<T> {

    int size();

    void close();

}
