package com.owlunit.web.model.common

import net.liftweb.mongodb.record.MongoRecord
import com.owlunit.web.lib.ui.IiTag
import net.liftweb.common.{Full, Empty, Box, Loggable}
import net.liftweb.record.field.LongField

/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 */
trait TmdbRecord[OwnerType <: TmdbRecord[OwnerType]] extends IiTagRecord[OwnerType] with Loggable {
    self: OwnerType =>

  object tmdbId extends LongField(this)

}
