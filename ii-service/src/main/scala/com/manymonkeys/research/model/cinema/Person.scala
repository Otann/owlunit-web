package com.manymonkeys.research.model.cinema

import reflect.BeanProperty
import java.util.UUID

/**
 * @author Ilya Pimenov
 *         Owl Proprietary
 */
case class Person (@BeanProperty var uuid: UUID,
                   @BeanProperty var name: String = "Joe",
                   @BeanProperty var surname: String = "Dow",
                   @BeanProperty var roles: Set[Role]) {}