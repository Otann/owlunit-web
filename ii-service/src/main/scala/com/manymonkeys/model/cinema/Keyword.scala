package com.manymonkeys.model.cinema

import reflect.BeanProperty
import java.util.UUID

/**
 * @author Ilya Pimenov
 * Owl Proprietary
 */

case class Keyword(@BeanProperty var name: String = "howl",
                   @BeanProperty var uuid: UUID) {}