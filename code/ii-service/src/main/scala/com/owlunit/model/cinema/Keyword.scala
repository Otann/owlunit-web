package com.owlunit.model.cinema

import reflect.BeanProperty
import java.util.UUID

/**
 * @author Ilya Pimenov
 *         Owl Proprietary
 */

case class Keyword(@BeanProperty var id: Long,
                   @BeanProperty var name: String = "") { }
