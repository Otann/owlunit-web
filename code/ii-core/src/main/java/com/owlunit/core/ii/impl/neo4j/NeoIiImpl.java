package com.owlunit.core.ii.impl.neo4j;

import com.owlunit.core.ii.Ii;
import org.neo4j.graphdb.Node;

import java.util.Map;

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

public class NeoIiImpl implements Ii {

    transient Node node;
    long id;
    Map<String, String> meta;
    Map<Ii, Double> components;
    Map<Ii, Double> parents;

    @SuppressWarnings("unchecked")
    NeoIiImpl(Node node) {
        assert node != null;
        this.node = node;
        this.id = node.getId();

        meta = NOT_LOADED;
        parents = NOT_LOADED;
        components = NOT_LOADED;
    }

    /**
     * Copying constructor mirrors all fields to decrease memory usage
     * IiDao is responsible for creation of new instances of the fields with new values
     *
     * @param item another item
     */
    NeoIiImpl(NeoIiImpl item) {
        this.node = item.node;
        this.id = node.getId();

        this.meta = item.meta;
        this.parents = item.parents;
        this.components = item.components;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public Map<String, String> getMetaMap() {
        return meta;
    }

    @Override
    public String getMeta(String key) {
        return meta.get(key);
    }

    @Override
    public Map<Ii, Double> getComponents() {
        return components;
    }

    @Override
    public Double getComponentWeight(Ii component) {
        return components.get(component);
    }

    @Override
    public Map<Ii, Double> getParents() {
        return parents;
    }

    @Override
    public Double getParentWeight(Ii parent) {
        return parents.get(parent);
    }

    @Override
    @SuppressWarnings("SimplifiableIfStatement")
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        return this.node.equals(((NeoIiImpl) o).node);
    }

    @Override
    public int hashCode() {
        return node.hashCode();
    }

    @Override
    public String toString() {
        return String.format("Ii-%d", node.getId());
    }

}
