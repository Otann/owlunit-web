package com.manymonkeys.model.cinema

import reflect.BeanProperty
import java.util.UUID

/**
 * @author Ilya Pimenov
 *         Owl Proprietary
 */

case class Movie(@BeanProperty var name: String = "Big Lebowski",
                 @BeanProperty var year: Long = 1987,
                 @BeanProperty var description: String,
                 @BeanProperty var uuid: UUID) {}