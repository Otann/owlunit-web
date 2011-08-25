package com.manymonkeys.ex.json.mock;

import com.manymonkeys.core.ii.InformationItem;
import com.manymonkeys.service.cinema.MovieService;
import com.manymonkeys.service.cinema.TagService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by IntelliJ IDEA.
 * User: anton
 * Date: 8/25/11
 * Time: 4:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class InformationItemMock implements InformationItem {

    private UUID uuid;
    private Map<String, String> meta = new HashMap<String, String>();
    private Map<InformationItem, Double> components = new HashMap<InformationItem, Double>();
    private Map<InformationItem, Double> parents    = new HashMap<InformationItem, Double>();

    public static InformationItem getSampleMovie(String name, long size){
        InformationItemMock mock = generateItem("Sample", 0);

        mock.meta.put(MovieService.NAME, name);

        for (int i = 0; i < size; ++i) {
            InformationItemMock component = generateItem("Component", i);
            mock.components.put(component, (double) i);
            component.parents.put(mock, (double) i);

            InformationItemMock parent = generateItem("Parent", i);
            mock.parents.put(component, (double) i);
            component.components.put(mock, (double) i);
        }

        return mock;
    }

    private static InformationItemMock generateItem(String type, long id) {
        InformationItemMock item = new InformationItemMock();
        item.uuid = generateFromString(String.format("%s-%d", type, id));
        item.meta.put(TagService.NAME, String.format("Item %d of %s", id, type));
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
    public UUID getUUID() {
        return uuid;
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
    public Map<InformationItem, Double> getComponents() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Double getComponentWeight(InformationItem component) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map<InformationItem, Double> getParents() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Double getParentWeight(InformationItem parent) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
