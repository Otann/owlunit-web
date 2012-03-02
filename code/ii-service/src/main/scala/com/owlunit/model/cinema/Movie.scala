package com.owlunit.model.cinema

import reflect.BeanProperty

/**
 * @author Ilya Pimenov
 *         Owl Proprietary
 */

case class Movie(@BeanProperty var id: Long,
                 @BeanProperty var name: String = "",
                 @BeanProperty var year: Long = 0,
                 @BeanProperty var description: String) { }