package com.owlunit.model.cinema

import reflect.BeanProperty
import java.util.UUID

/**
 * @author Ilya Pimenov
 * Owl Proprietary
 */

case class Person (@BeanProperty var id: Long,
                   @BeanProperty var name: String = "Joe",
                   @BeanProperty var surname: String = "Dow",
                   @BeanProperty var roles: java.util.Set[Role]) {}