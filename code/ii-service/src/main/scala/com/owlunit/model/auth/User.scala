package com.owlunit.model.auth

import reflect.BeanProperty
import java.util.UUID

/**
 * @author Ilya Pimenov
 * Owl Proprietary
 */

case class User(@BeanProperty var id: Long,
                @BeanProperty var login: String = "joe",
                @BeanProperty var password: String = "*******") {}