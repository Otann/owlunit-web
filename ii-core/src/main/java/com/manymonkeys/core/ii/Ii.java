package com.manymonkeys.core.ii;

import java.util.Map;
import java.util.UUID;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public interface Ii {

    /**
     * Each ii has it's own identifier which is unique across implementation package
     *
     * @return unique string
     */
    UUID getUUID();

    /**
     * InformationItems holds simple meta information like names, dates, or urls
     *
     * @return immutable map of metadata
     */
    Map<String, String> getMetaMap();

    /**
     * Shortcut for {@see getMetaMap}
     *
     * @param key for metadata
     * @return metadata
     */
    String getMeta(String key);

    /**
     * Each Ii contains links to another items. Each link has weight
     *
     * @return immutable map of item's components with weights
     */
    Map<Ii, Double> getComponents();

    /**
     * Shortcut for {@see getComponents}
     *
     * @param component to get weight of
     * @return weight of a connection to that component. Null if there is no such component
     */
    Double getComponentWeight(Ii component);

    /**
     * As each item contains links it's , it contains also link to
     * items that contain this item
     *
     * @return immutable map of items that contain this item as a component
     */
    Map<Ii, Double> getParents();

    /**
     * Shortcut for {@see getParents()}
     *
     * @param parent to get weight of
     * @return weight of connection to that parent. Null if there is no such parent
     */
    Double getParentWeight(Ii parent);
}
