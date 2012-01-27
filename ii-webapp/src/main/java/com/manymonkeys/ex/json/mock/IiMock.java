package com.manymonkeys.ex.json.mock;

import com.manymonkeys.core.ii.Ii;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */
public class IiMock implements Ii {

    private long id;
    private Map<String, String> meta = new HashMap<String, String>();
    private Map<Ii, Double> components = new HashMap<Ii, Double>();
    private Map<Ii, Double> parents    = new HashMap<Ii, Double>();

    public static Ii getSampleMovie(String name, long size){
        IiMock mock = generateItem("Sample", 0);

        mock.meta.put("NAME", name);

        for (int i = 0; i < size; ++i) {
            IiMock component = generateItem("Component", i);
            mock.components.put(component, (double) i);
            component.parents.put(mock, (double) i);

            IiMock parent = generateItem("Parent", i);
            mock.parents.put(component, (double) i);
            component.components.put(mock, (double) i);
        }

        return mock;
    }

    private static IiMock generateItem(String type, long id) {
        IiMock item = new IiMock();
        item.id = id + type.hashCode();
        item.meta.put("NAME", String.format("Item %d of %s", id, type));
        return item;
    }

    private static Map<String, UUID> cache = new HashMap<String, UUID>();
    private static UUID generateFromString(String any) {
        if (!cache.containsKey(any)) {
            cache.put(any, UUID.randomUUID());
        }
        return cache.get(any);
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public Map<String, String> getMetaMap() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getMeta(String key) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map<Ii, Double> getComponents() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Double getComponentWeight(Ii component) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map<Ii, Double> getParents() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Double getParentWeight(Ii parent) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
