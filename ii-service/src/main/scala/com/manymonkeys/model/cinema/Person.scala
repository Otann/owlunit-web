package com.manymonkeys.model.cinema

import reflect.{BooleanBeanProperty, BeanProperty}
import java.util.UUID

/**
 * @author Ilya Pimenov
 * Owl Proprietary
 */

case class Person (@BeanProperty var uuid: UUID,
                   @BeanProperty var name: String = "Joe",
                   @BeanProperty var surname: String = "Dow",
                   @BeanProperty var roles: java.util.Set[Role]) {}