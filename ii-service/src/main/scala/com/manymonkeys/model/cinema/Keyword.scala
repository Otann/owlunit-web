package com.manymonkeys.model.cinema

import reflect.BeanProperty
import java.util.UUID

/**
 * @author Ilya Pimenov
 * Owl Proprietary
 */

case class Keyword(@BeanProperty var uuid: UUID,
                   @BeanProperty var name: String = "howl") { }