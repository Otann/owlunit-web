package com.manymonkeys;

import com.manymonkeys.ScalaTest;
import scala.Option;
import scala.collection.JavaConversions;

import java.util.List;
import java.util.Map;

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

public class JavaTest {

    public static <T> T unpack(Option<T> in) {
        if (in.isEmpty()) {
            return null;
        } else {
            return in.get();
        }
    }

    public static void main(String[] args) {
        ScalaTest test = new ScalaTest();
        List<String> list = JavaConversions.asList(test.returnSeq());
        String option = unpack(test.returnOption());
        boolean bool = test.returnBoolean();
//        Map<String, Double> map1 = test.returnMap();
        Map<String, Double> map2 = JavaConversions.asMap(test.returnMap()); // this is false negative
    }

}
