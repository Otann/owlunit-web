package com.owlunit.service.impl.util;

import com.owlunit.core.orthodoxal.ii.Ii;
import com.owlunit.core.orthodoxal.ii.IiDao;

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

public class Utils {

    public static Ii itemWithMeta(IiDao dao, Ii item) {
        if (Ii.NOT_LOADED.equals(item.getMetaMap())) {
            return dao.loadMeta(item);
        } else {
            return item;
        }
    }

}
