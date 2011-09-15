package com.manymonkeys.research.model.cinema

import reflect.BeanProperty
import java.util.UUID

/**
 * @author Ilya Pimenov
 *         Owl Proprietary
 */
case class Movie(@BeanProperty var uuid: UUID,
                 @BeanProperty var name: String = "Big Lebowski",
                 @BeanProperty var year: Long = 1987,
                 @BeanProperty var description: String) { }