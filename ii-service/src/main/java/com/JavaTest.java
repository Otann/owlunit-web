package com;

import scala.collection.JavaConversions;

import java.util.List;
import java.util.Map;

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

public class JavaTest {

    public static void main(String[] args) {
        ScalaTest test = new ScalaTest();
        List<String> list = JavaConversions.asList(test.returnSeq());
        String option = test.returnOption().get();
        Map<String, Double> map = JavaConversions.asMap(test.returnMap()); // this is false negative
    }

}
