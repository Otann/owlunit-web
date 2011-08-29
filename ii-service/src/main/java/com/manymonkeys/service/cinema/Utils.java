package com.manymonkeys.service.cinema;

import com.manymonkeys.core.ii.Ii;
import com.manymonkeys.core.ii.IiDao;

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

public class Utils {

    public static Ii itemWithMeta(IiDao dao, Ii item) {
        if (item.getMetaMap() == null) {
            return dao.loadMetadata(item);
        } else {
            return item;
        }
    }

}
