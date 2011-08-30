package owls

import reflect.{BooleanBeanProperty, BeanProperty}

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */
class Person {

  @BeanProperty // this will generate POJO-style getter and setter for java
  var name = "Person name"

  @BooleanBeanProperty // this will generate POJO-style boolean getter and setter for java
  val badass = true;

}