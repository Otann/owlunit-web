package com.manymonkeys.model.user

import reflect.BeanProperty

/**
 * @author Ilya Pimenov
 * Owl Proprietary
 */

case class User(@BeanProperty var login: String = "joe",
                @BeanProperty var password: String = "qwerty") {}