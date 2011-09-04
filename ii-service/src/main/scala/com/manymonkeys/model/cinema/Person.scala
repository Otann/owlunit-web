package com.manymonkeys.model.cinema

import reflect.{BooleanBeanProperty, BeanProperty}

/**
 * @author Ilya Pimenov
 * Owl Proprietary
 */

case class Person (@BeanProperty var name: String = "Joe",
                   @BeanProperty var surname: String = "Dow",
                   @BeanProperty var roles: java.util.Set[Role]) {}