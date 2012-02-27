package com.manymonkeys.model.cinema

import reflect.BeanProperty
import java.util.UUID

/**
 * @author Ilya Pimenov
 * Owl Proprietary
 */

case class Genre (@BeanProperty var id: Long,
                  @BeanProperty var name: String = "") { }