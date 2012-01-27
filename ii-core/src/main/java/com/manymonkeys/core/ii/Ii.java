package com.manymonkeys.core.ii;

import com.manymonkeys.core.ii.impl.utils.AlwaysEmptyMap;

import java.util.Map;

/**
 * Ii stands for InformationItem
 *
 * @author Anton Chebotaev
 *         Owls Proprietary
 */
public interface Ii {

    /**
     * This value is returned when you do not need item's data
     */
    public static final Map NOT_LOADED = new AlwaysEmptyMap();

    /**
     * Each ii has it's own identifier which is unique across implementation package
     *
     * @return unique identifier
     */
    long getId();

    /**
     * InformationItems holds simple meta information like names, dates, or urls
     *
     * @return immutable map of metadata
     */
    Map<String, String> getMetaMap();

    /**
     * Shortcut for {@link #getMetaMap() getMetaMap}
     *
     * @param key for metadata
     * @return metadata
     */
    String getMeta(String key);

    /**
     * Each Ii contains links to another items. Each link has weight
     *
     * @return map of item's components with weights (clients should not change it, use dao class instead)
     */
    Map<Ii, Double> getComponents();

    /**
     * Shortcut for {@link #getComponents() getComponents}
     *
     * @param component to get weight of
     * @return weight of a connection to that component. Null if there is no such component
     */
    Double getComponentWeight(Ii component);

    /**
     * As each item contains links it's , it contains also link to
     * items that contain this item
     *
     * @return map of items that contain this item as a component (clients should not change it, use dao class instead)
     */
    Map<Ii, Double> getParents();

    /**
     * Shortcut for {@link #getParents() getParents()}
     *
     * @param parent to get weight of
     * @return weight of connection to that parent. Null if there is no such parent
     */
    Double getParentWeight(Ii parent);

}
