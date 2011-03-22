package com.manymonkeys.core.ii;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public interface InformationItemDao {

    InformationItem createInformationItem();
    void deleteInformationItem(InformationItem item);

    void setMeta(InformationItem item, String key, String value);
    void setComponentWeight(InformationItem item, InformationItem component, double value);
    void removeComponent(InformationItem item, InformationItem component);

    InformationItem getById(long id);
	LazyResults<InformationItem> getByMeta(String metaKey, String metaValue);
    LazyResults<InformationItem> getAll();

}
