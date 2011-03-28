package com.manymonkeys.local;

import java.util.UUID;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class InformationItem implements Comparable<InformationItem> {
    String s;

    public InformationItem() {
        this.s = UUID.randomUUID().toString();
    }

    public InformationItem(String s) {
        this.s = s;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof InformationItem && s.equals(((InformationItem) o).s);
    }

    @Override
    public int hashCode() {
        return s.hashCode();
    }

    @Override
    public String toString() {
        return s;
    }

    @Override
    public int compareTo(InformationItem o) {
        return s.compareTo(o.s);
    }
}
